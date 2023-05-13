package com.example.chatopia.adapters;

import android.graphics.Bitmap;

import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatopia.databinding.ItemContainerRecentConversionBinding;
import com.example.chatopia.firebase.listners.ConversionListner;
import com.example.chatopia.models.ChatMessage;
import com.example.chatopia.models.User;

import java.util.List;

public class RecentConversationAdapter extends RecyclerView.Adapter<RecentConversationAdapter.ConvesionViewHolder>{

    private List<ChatMessage> chatMessages;
    private ConversionListner conversionListner;

    public RecentConversationAdapter(List<ChatMessage> chatMessages, ConversionListner conversionListner) {
        this.chatMessages = chatMessages;
        this.conversionListner = conversionListner;
    }

    @NonNull
    @Override
    public ConvesionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ConvesionViewHolder(
                ItemContainerRecentConversionBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull ConvesionViewHolder holder, int position) {
        holder.setdata(chatMessages.get(position));
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    class ConvesionViewHolder extends RecyclerView.ViewHolder{
        ItemContainerRecentConversionBinding binding;
        ConvesionViewHolder(ItemContainerRecentConversionBinding itemContainerRecentConversionBinding){
            super(itemContainerRecentConversionBinding.getRoot());
            binding = itemContainerRecentConversionBinding;
        }

        void setdata(ChatMessage chatMessage){
            binding.imageprofile.setImageBitmap(getbitmapfromstring(chatMessage.conversionImage));
            binding.textname.setText(chatMessage.conversionName);
            binding.textrecentmsg.setText(chatMessage.message);
            binding.getRoot().setOnClickListener(v->{
                User user = new User();
                user.id = chatMessage.conversionId;
                user.name = chatMessage.conversionName;
                user.image = chatMessage.conversionImage;
                conversionListner.onConversationClicked(user);
            });
        }
    }

    private Bitmap getbitmapfromstring(String encodedImage){
        byte[] bytes = Base64.decode(encodedImage , Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes , 0 , bytes.length);
    }
}
