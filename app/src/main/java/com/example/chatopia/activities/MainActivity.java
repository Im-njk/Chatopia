package com.example.chatopia.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;

import com.example.chatopia.adapters.RecentConversationAdapter;
import com.example.chatopia.databinding.ActivityMainBinding;
import com.example.chatopia.firebase.listners.ConversionListner;
import com.example.chatopia.models.ChatMessage;
import com.example.chatopia.models.User;
import com.example.chatopia.utilities.Constants;
import com.example.chatopia.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends BaseActivity implements ConversionListner {

    private ActivityMainBinding binding;
    private PreferenceManager preferenceManager;
    private List<ChatMessage> conversations;
    private RecentConversationAdapter recentConversationAdapter;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        loadUserDetails();
        getToken();
        setListner();
        init();
        listenConversation();
    }

    void init(){
        conversations = new ArrayList<>();
        recentConversationAdapter = new RecentConversationAdapter(conversations , this);
        binding.recyclerRecentConversion.setAdapter(recentConversationAdapter);
        db = FirebaseFirestore.getInstance();
    }

    private void setListner() {
        binding.imagesignout.setOnClickListener(v->{
            signout();
        });

        binding.fabnewchat.setOnClickListener(v->{
            startActivity(new Intent(getApplicationContext(),UserActivity.class));
        });
    }

    private void loadUserDetails() {
        binding.textName.setText(preferenceManager.getString(Constants.KEY_NAME));
        byte[] bytes = Base64.decode(preferenceManager.getString(Constants.KEY_IMAGE), Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
        binding.imageprofile.setImageBitmap(bitmap);
    }

    private void listenConversation(){
        db.collection(Constants.KEY_COLLECTIONS_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_SENDER_ID , preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
        db.collection(Constants.KEY_COLLECTIONS_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_RECIVER_ID , preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
    }

    private final EventListener<QuerySnapshot> eventListener = (value, error) ->{
        if(error != null)
            return;
        if(value != null){
            for(DocumentChange documentChange : value.getDocumentChanges()){
                if(documentChange.getType() == DocumentChange.Type.ADDED){
                    String senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    String ReciverId = documentChange.getDocument().getString(Constants.KEY_RECIVER_ID);
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.reciverId = ReciverId;
                    chatMessage.senderId = senderId;
                    if(preferenceManager.getString(Constants.KEY_USER_ID).equals(senderId)){
                        chatMessage.conversionId = documentChange.getDocument().getString(Constants.KEY_RECIVER_ID);
                        chatMessage.conversionName = documentChange.getDocument().getString(Constants.KEY_RECEIVER_NAME);
                        chatMessage.conversionImage = documentChange.getDocument().getString(Constants.KEY_RECEIVER_IMAGE);
                    }else{
                        chatMessage.conversionId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                        chatMessage.conversionName = documentChange.getDocument().getString(Constants.KEY_SENDER_NAME);
                        chatMessage.conversionImage = documentChange.getDocument().getString(Constants.KEY_SENDER_IMAGE);
                    }
                    chatMessage.message = documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE);
                    chatMessage.dateObject = documentChange.getDocument().getDate(Constants.KEY_TIME);
                    conversations.add(chatMessage);
                } else if (documentChange.getType() == DocumentChange.Type.MODIFIED) {
                    for (int i = 0; i < conversations.size(); i++) {
                        String senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                        String receiverId = documentChange.getDocument().getString(Constants.KEY_RECIVER_ID);
                        if (conversations.get(i).senderId.equals(senderId) && conversations.get(i).reciverId.equals(receiverId)) {
                            conversations.get(i).message = documentChange.getDocument ().getString(Constants.KEY_LAST_MESSAGE);
                            conversations.get(i).dateObject = documentChange.getDocument().getDate(Constants.KEY_TIME);
                            break;
                        }
                    }
                }
            }
            Collections.sort(conversations , (obj1 , obj2) -> obj2.dateObject.compareTo(obj1.dateObject));
            recentConversationAdapter.notifyDataSetChanged();
            binding.recyclerRecentConversion.smoothScrollToPosition(0);
            binding.recyclerRecentConversion.setVisibility(View.VISIBLE);
            binding.progressbar.setVisibility(View.GONE);
        }
    };

    private void showToast(String message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void getToken(){
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this::updateToken);
    }

    private void updateToken (String token) {
        preferenceManager.putString(Constants.KEY_FCM_TOKEN,token);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference reference = db.collection(Constants.KEY_COLLECTION_USERS).document(preferenceManager.getString(Constants.KEY_USER_ID));
        reference.update(Constants.KEY_FCM_TOKEN , token)
                .addOnFailureListener(e -> showToast("Unable To update Token"));
    }

    private void signout(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference reference = db.collection(Constants.KEY_COLLECTION_USERS).document(preferenceManager.getString(Constants.KEY_USER_ID));
        HashMap<String , Object> updates = new HashMap<>();
        updates.put(Constants.KEY_FCM_TOKEN , FieldValue.delete());
        reference.update(updates)
                .addOnSuccessListener(unused -> {
                    preferenceManager.clear();
                    startActivity(new Intent(getApplicationContext() , SignInActivity.class));
                    finish();
                })
                .addOnFailureListener(e->showToast("Unable To Sign Out"));
    }

    @Override
    public void onConversationClicked(User user) {
        Intent i = new Intent(MainActivity.this , ChatActivity.class);
        i.putExtra(Constants.KEY_USER , user);
        startActivity(i);
    }
}