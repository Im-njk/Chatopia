package com.example.chatopia.adapters;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatopia.databinding.ItemContainerRecivedMessageBinding;
import com.example.chatopia.databinding.ItemContainerSentMessageBinding;
import com.example.chatopia.models.ChatMessage;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private  Bitmap reciverImage;
    private final String senderid;
    private final List<ChatMessage> chatMessages;

    private static final int VIEW_TYPE_SEND = 1;
    private static final int VIEW_TYPE_RECIVED = 2;

    public void setReciverImage(Bitmap bitmap){
        reciverImage = bitmap;
    }

    public ChatAdapter(Bitmap reciverImage, String senderid, List<ChatMessage> chatMessages) {
        this.reciverImage = reciverImage;
        this.senderid = senderid;
        this.chatMessages = chatMessages;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == VIEW_TYPE_SEND){
            return new SentMessageViewHolder(
                    ItemContainerSentMessageBinding.inflate(
                            LayoutInflater.from(parent.getContext())
                            ,parent
                            ,false)
            );
        }else {
            return new RecivedMessageViewHolder(
                    ItemContainerRecivedMessageBinding.inflate(
                            LayoutInflater.from(parent.getContext())
                            ,parent
                            ,false)
            );
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(getItemViewType(position) == VIEW_TYPE_SEND){
            ((SentMessageViewHolder) holder).setData(chatMessages.get(position));
        }else{
            ((RecivedMessageViewHolder)holder).setData(chatMessages.get(position),reciverImage);
        }
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    @Override
    public int getItemViewType(int position) {
        if(chatMessages.get(position).senderId.equals(senderid)){
            return VIEW_TYPE_SEND;
        }else{
            return VIEW_TYPE_RECIVED;
        }
    }

    static class SentMessageViewHolder extends RecyclerView.ViewHolder{
        private final ItemContainerSentMessageBinding binding;
        SentMessageViewHolder(ItemContainerSentMessageBinding sentMessageBinding){
            super(sentMessageBinding.getRoot());
            binding = sentMessageBinding;
        }

        void setData(ChatMessage chatMessage){
            binding.textmessage.setText(chatMessage.message);
            binding.texttime.setText(chatMessage.dateTime);
        }
    }

    static class RecivedMessageViewHolder extends RecyclerView.ViewHolder{
        private final ItemContainerRecivedMessageBinding binding;
        RecivedMessageViewHolder(ItemContainerRecivedMessageBinding recivedMessageBinding){
            super(recivedMessageBinding.getRoot());
            binding = recivedMessageBinding;
        }

        void setData(ChatMessage chatMessage , Bitmap reciverbitmap){
            binding.textmessage.setText(chatMessage.message);
            binding.texttime.setText(chatMessage.dateTime);
            if(reciverbitmap !=null)
                binding.imageprofile.setImageBitmap(reciverbitmap);
        }
    }
}
