package com.example.chatopia.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.example.chatopia.R;
import com.example.chatopia.databinding.ActivitySignUpBinding;
import com.example.chatopia.utilities.Constants;
import com.example.chatopia.utilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.regex.Pattern;

public class SignUpActivity extends AppCompatActivity {

    ActivitySignUpBinding binding;
    PreferenceManager preferenceManager;
    String encodedImage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        setListener();
    }

    private void setListener() {
        binding.textsignin.setOnClickListener(v-> onBackPressed());
        binding.layoutimage.setOnClickListener(v->{
            Intent intent  = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickImage.launch(intent);
        });
        binding.buttonsignup.setOnClickListener(v->{
            if(issignupdetailsvalid()){
                signup();
            }
        });
    }

    private void showToast(String message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void signup(){
        loading(true);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        HashMap<String , Object> user = new HashMap<>();
        user.put(Constants.KEY_NAME, binding.inputName.getText().toString().trim());
        user.put(Constants.KEY_EMAIL, binding.inputEmail.getText().toString().trim());
        user.put(Constants.KEY_PASSWORD, binding.inputPassword.getText().toString().trim());
        user.put(Constants.KEY_IMAGE, encodedImage);
        db.collection(Constants.KEY_COLLECTION_USERS)
                .add(user)
                .addOnSuccessListener(documentReference -> {
                    loading(false);
                    preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN , true);
                    preferenceManager.putString(Constants.KEY_USER_ID , documentReference.getId());
                    preferenceManager.putString(Constants.KEY_NAME , binding.inputName.getText().toString().trim());
                    preferenceManager.putString(Constants.KEY_IMAGE , encodedImage);
                    Intent intent  = new Intent(getApplicationContext() , MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .addOnFailureListener(exception -> {
                    loading(false);
                    showToast(exception.getMessage());
                });
    }

    private String getEncodedImage(Bitmap bitmap){
        int previewWidth = 150;
        int previewHeight = bitmap.getHeight()  * previewWidth / bitmap.getWidth();
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap , previewWidth , previewHeight , false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG , 50 , byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes , Base64.DEFAULT);
    }

    private final ActivityResultLauncher<Intent> pickImage =  registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if(result.getResultCode() == RESULT_OK){
                    if(result.getData() !=null){
                        Uri imageUri = result.getData().getData();
                        try {
                            InputStream inputStream = getContentResolver().openInputStream(imageUri);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            binding.imageprofile.setImageBitmap(bitmap);
                            binding.textaddimage.setVisibility(View.GONE);
                            encodedImage = getEncodedImage(bitmap);
                        } catch (Exception e) {
                            showToast(e.getMessage());
                        }
                    }
                }
            }
    );

    private boolean issignupdetailsvalid(){
        if(encodedImage == null){
            showToast("Select Profile Image");
            return false;
        }else if(binding.inputName.getText().toString().trim().isEmpty()){
            showToast("Enter Name");
            return false;
        } else if (binding.inputEmail.getText().toString().trim().isEmpty()) {
            showToast("Enter Email");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.getText().toString()).matches()) {
            showToast("Enter Valid Email");
        } else if (binding.inputPassword.getText().toString().trim().isEmpty()) {
            showToast("Enter Password"); 
            return false;
        } else if (!binding.inputPassword.getText().toString().equals(binding.inputConfirmPassword.getText().toString())) {
            showToast("Password and Conform Password must be confirm");
            return false;
        }else{
            return true;
        }
        return false;
    }

    private void loading(Boolean isLoading){
        if(isLoading){
            binding.buttonsignup.setVisibility(View.INVISIBLE);
            binding.progressbar.setVisibility(View.VISIBLE);
        }else{
            binding.buttonsignup.setVisibility(View.VISIBLE);
            binding.progressbar.setVisibility(View.INVISIBLE);
        }
    }
}