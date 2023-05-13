package com.example.chatopia.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatopia.databinding.ItemContainerUserBinding;
import com.example.chatopia.firebase.listners.UserListner;
import com.example.chatopia.models.User;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder>{

    private final List<User> users;
    private final UserListner userListner;

    public UserAdapter(List<User> users, UserListner userListner) {
        this.users = users;
        this.userListner = userListner;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContainerUserBinding itemContainerUserBinding = ItemContainerUserBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
        return new ViewHolder(itemContainerUserBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.setUser(users.get(position));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    private Bitmap getbitmapimg(String encodedImage){
        byte[] bytes = Base64.decode(encodedImage , Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes , 0 , bytes.length);
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        ItemContainerUserBinding binding;

        public ViewHolder(@NonNull ItemContainerUserBinding itemContainerUserbinding) {
            super(itemContainerUserbinding.getRoot());
            binding = itemContainerUserbinding;
        }

        void setUser(User user){
            binding.textemail.setText(user.email);
            binding.textname.setText(user.name);
            binding.imageprofile.setImageBitmap(getbitmapimg(user.image));
            binding.getRoot().setOnClickListener(v -> userListner.onUserClicked(user));
        }
    }
}
