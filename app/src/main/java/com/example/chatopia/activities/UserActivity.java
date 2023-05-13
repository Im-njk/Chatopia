package com.example.chatopia.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.chatopia.adapters.UserAdapter;
import com.example.chatopia.databinding.ActivityUserBinding;
import com.example.chatopia.firebase.listners.UserListner;
import com.example.chatopia.models.User;
import com.example.chatopia.utilities.Constants;
import com.example.chatopia.utilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class UserActivity extends BaseActivity implements UserListner {

    private ActivityUserBinding binding;
    PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        setListners();
        getUsers();
    }

    private void setListners() {
        binding.imageback.setOnClickListener(v ->onBackPressed());
    }

    private void showerror(){
        binding.texterrormessage.setText(String.format("%s" , "No User available"));
        binding.texterrormessage.setVisibility(View.VISIBLE);
    }

    private void getUsers(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(Constants.KEY_COLLECTION_USERS)
                .get()
                .addOnCompleteListener(task ->{
                    loading(false);
                    String currentuserid = preferenceManager.getString(Constants.KEY_USER_ID);
                    if(task.isSuccessful() && task.getResult() != null){
                        List<User> users = new ArrayList<>();
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                            if(currentuserid.equals(queryDocumentSnapshot.getId())){
                                continue;
                            }
                            User user = new User();
                            user.name = queryDocumentSnapshot.getString(Constants.KEY_NAME);
                            user.email = queryDocumentSnapshot.getString(Constants.KEY_EMAIL);
                            user.token = queryDocumentSnapshot.getString(Constants.KEY_FCM_TOKEN);
                            user.image = queryDocumentSnapshot.getString(Constants.KEY_IMAGE);
                            user.id = queryDocumentSnapshot.getId();
                            users.add(user);
                        }
                        if(users.size()>0){
                            UserAdapter userAdapter = new UserAdapter(users , this);
                            binding.recycleruserlist.setAdapter(userAdapter);
                            binding.recycleruserlist.setVisibility(View.VISIBLE);
                        }else{
                            showerror();
                        }
                    }else {
                        showerror();
                    }
                });
    }

    private void loading (Boolean isLoading){
        if(isLoading){
            binding.progressbar.setVisibility(View.VISIBLE);
        }else{
            binding.progressbar.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onUserClicked(User user) {
        Intent intent = new Intent(getApplicationContext() , ChatActivity.class);
        intent.putExtra(Constants.KEY_USER , user);
        startActivity(intent);
    }
}