package com.example.chatopia.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;

import com.example.chatopia.adapters.ChatAdapter;
import com.example.chatopia.databinding.ActivityChatBinding;
import com.example.chatopia.models.ChatMessage;
import com.example.chatopia.models.User;
import com.example.chatopia.network.ApiClient;
import com.example.chatopia.network.ApiService;
import com.example.chatopia.utilities.Constants;
import com.example.chatopia.utilities.PreferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends BaseActivity {

    ActivityChatBinding binding;
    private User reciveruser;

    private List<ChatMessage> chatMessages;
    private ChatAdapter chatAdapter;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore db;
    private String conversationId;

    private Boolean isAvailable = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        loadreciverdetail();
        setlistner();
        init();
        listenMessages();
    }

    private void init() {
        preferenceManager = new PreferenceManager(getApplicationContext());
        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(
                getbitmapfromstring(reciveruser.image),
                preferenceManager.getString(Constants.KEY_USER_ID),
                chatMessages
        );
        binding.recyclerchat.setAdapter(chatAdapter);
        db = FirebaseFirestore.getInstance();
    }

    private void sendMessage() {
        if(binding.inputmessage.getText().toString().equals("") || binding.inputmessage.getText().toString() == null){
            return;
        }
        HashMap<String, Object> message = new HashMap<>();
        message.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
        message.put(Constants.KEY_RECIVER_ID, reciveruser.id);
        message.put(Constants.KEY_TIME, new Date());
        message.put(Constants.KEY_MESSAGE, binding.inputmessage.getText().toString());
        db.collection(Constants.KEY_COLLECTION_CHAT).add(message);
        if(conversationId != null){
            updateConversion(binding.inputmessage.getText().toString().trim());
        }else{
            HashMap<String , Object> conversion = new HashMap<>();
            conversion.put(Constants.KEY_SENDER_ID,preferenceManager.getString(Constants.KEY_USER_ID));
            conversion.put(Constants.KEY_SENDER_NAME , preferenceManager.getString(Constants.KEY_NAME));
            conversion.put(Constants.KEY_SENDER_IMAGE , preferenceManager.getString(Constants.KEY_IMAGE));
            conversion.put(Constants.KEY_RECIVER_ID , reciveruser.id);
            conversion.put(Constants.KEY_RECEIVER_NAME , reciveruser.name);
            conversion.put(Constants.KEY_RECEIVER_IMAGE , reciveruser.image);
            conversion.put(Constants.KEY_LAST_MESSAGE ,binding.inputmessage.getText().toString().trim());
            conversion.put(Constants.KEY_TIME ,new Date());
            addConversion(conversion);


        }
        if(!isAvailable){
            try {
                JSONArray tokens = new JSONArray();
                tokens.put(reciveruser.token);

                JSONObject data = new JSONObject();
                data.put(Constants.KEY_USER_ID , preferenceManager.getString(Constants.KEY_USER_ID));
                data.put(Constants.KEY_NAME , preferenceManager.getString(Constants.KEY_NAME));
                data.put(Constants.KEY_FCM_TOKEN , preferenceManager.getString(Constants.KEY_FCM_TOKEN));
                data.put(Constants.KEY_MESSAGE , binding.inputmessage.getText().toString());
                JSONObject body = new JSONObject();
                body.put(Constants.REMOTE_MSG_DATA , data);
                body.put(Constants.REMOTE_MSG_REGISTRATION_IDS,tokens);
                sendNotification(body.toString());
            }catch (Exception e){
                showToast(e.getMessage());
            }
        }

        binding.inputmessage.setText(null);
    }

    private void showToast(String message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void sendNotification(String mesageBody){
        ApiClient.getClient().create(ApiService.class).sendMessage(
                Constants.getRemoteMsgHeader(),
                mesageBody
        ).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if(response.isSuccessful()){
                    try {
                        if(response.body() != null){
                            JSONObject responseJson = new JSONObject(response.body());
                            JSONArray results = responseJson.getJSONArray("results");
                            if(responseJson.getInt("failure") == 1){
                                JSONObject error = (JSONObject)results.get(0);
                                showToast(error.getString("error"));
                                return;
                            }
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                        showToast(e.getMessage());
                    }
                    showToast("Notification Sent Succcessfully");
                }else {
                    showToast("Error : " + response.code());
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                showToast(t.getMessage());
            }
        });
    }

    private void loadreciverdetail() {
        reciveruser = (User) getIntent().getSerializableExtra(Constants.KEY_USER);
        binding.textname.setText(reciveruser.name);
        binding.imageprofile.setImageBitmap(getbitmapfromstring(reciveruser.image));
    }

    private void setlistner() {
        binding.imageback.setOnClickListener(v -> onBackPressed());
        binding.layoutsend.setOnClickListener(v -> sendMessage());
    }

    private Bitmap getbitmapfromstring(String encodedImage) {
        if(encodedImage == null)
            return null;
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    private String getReadableDateTime(Date date) {
        return new SimpleDateFormat("MM dd, yyyy - hh:mm a", Locale.getDefault()).format(date);
    }

    private void listenAvailabilitOfReciver(){
        db.collection(Constants.KEY_COLLECTION_USERS).document(
                reciveruser.id
        ).addSnapshotListener(ChatActivity.this , ((value, error) ->{
            if(error != null)
                return;
            if(value != null){
                if(value.getLong(Constants.KEY_AVAILABILITY)!= null){
                    int availability = Objects.requireNonNull(
                            value.getLong(Constants.KEY_AVAILABILITY)
                    ).intValue();
                    isAvailable = availability == 1;
                }
                reciveruser.token = value.getString(Constants.KEY_FCM_TOKEN);
                if(reciveruser.image == null){
                    reciveruser.image = value.getString(Constants.KEY_IMAGE);
                    chatAdapter.setReciverImage(getbitmapfromstring(reciveruser.image));
                    chatAdapter.notifyItemRangeChanged(0,chatMessages.size());
                }
            }
            if(isAvailable)
                binding.textOnline.setVisibility(View.VISIBLE);
            else
                binding.textOnline.setVisibility(View.GONE);
        }));
    }


    private void listenMessages() {
        db.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .whereEqualTo(Constants.KEY_RECIVER_ID, reciveruser.id)
                .addSnapshotListener(eventListener);
        db.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID, reciveruser.id)
                .whereEqualTo(Constants.KEY_RECIVER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
    }

    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if (error != null) {
            return;
        }
        if (value != null) {
            int count = chatMessages.size();
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    chatMessage.reciverId = documentChange.getDocument().getString(Constants.KEY_RECIVER_ID);
                    chatMessage.message = documentChange.getDocument().getString(Constants.KEY_MESSAGE);
                    chatMessage.dateTime = getReadableDateTime(documentChange.getDocument().getDate(Constants.KEY_TIME));
                    chatMessage.dateObject = documentChange.getDocument().getDate(Constants.KEY_TIME);
                    chatMessages.add(chatMessage);
                }
            }
            Collections.sort(chatMessages, (obj1, obj2) -> obj1.dateObject.compareTo(obj2.dateObject));
            if (count == 0) {
                chatAdapter.notifyDataSetChanged();
            } else {
                chatAdapter.notifyItemRangeInserted(chatMessages.size(), chatMessages.size());
                binding.recyclerchat.smoothScrollToPosition(chatMessages.size() - 1);
            }
            binding.recyclerchat.setVisibility(View.VISIBLE);
        }
        binding.progressview.setVisibility(View.GONE);
        if(conversationId == null){
            checkforconversion();
        }
    };

    private void addConversion(HashMap<String , Object> conversion){
        db.collection(Constants.KEY_COLLECTIONS_CONVERSATIONS)
                .add(conversion)
                .addOnSuccessListener(documentReference -> conversationId = documentReference.getId());
    }

    private void updateConversion(String messsage){
        db.collection(Constants.KEY_COLLECTIONS_CONVERSATIONS)
                .document(conversationId)
                .update(
                        Constants.KEY_LAST_MESSAGE , messsage,
                        Constants.KEY_TIME , new Date()
                );
    }

    private final void checkforconversion(){
        if(chatMessages.size() != 0){
            checkForConversionRemotly(
                    preferenceManager.getString(Constants.KEY_USER_ID),
                    reciveruser.id
            );
            checkForConversionRemotly(
                    reciveruser.id,
                    preferenceManager.getString(Constants.KEY_USER_ID)
            );
        }
    }

    private void checkForConversionRemotly(String senderId, String reciverId){
        db.collection(Constants.KEY_COLLECTIONS_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_SENDER_ID , senderId)
                .whereEqualTo(Constants.KEY_RECIVER_ID, reciverId)
                .get()
                .addOnCompleteListener(conversionOnCompleteListener);
    }

    private final OnCompleteListener<QuerySnapshot> conversionOnCompleteListener = task -> {
        if(task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0) {
            DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
            conversationId = documentSnapshot.getId();
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        listenAvailabilitOfReciver();
    }
}