package com.analysisgroup.streamingapp.LiveChat;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.analysisgroup.streamingapp.R;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MyViewHolder> {
    private  List<ChatList> chatLists;
    private String UID;

    public ChatAdapter(List<ChatList> chatLists, Context context) {
        this.chatLists = chatLists;
        this.context = context;
        this.UID = FirebaseAuth.getInstance().getUid();
    }

    private final Context context;

    @NonNull
    @Override
    public ChatAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_adapter_layout, null));
    }

    @Override
    public void onBindViewHolder(@NonNull ChatAdapter.MyViewHolder holder, int position) {
        ChatList chatList = chatLists.get(position);

        holder.username.setBackgroundResource(R.drawable.rounded_bubble);
        holder.username.setTextColor(Color.BLACK);
        holder.username.setText(chatList.getUsername());
        holder.message.setText(chatList.getMsg());
    }

    @Override
    public int getItemCount() {
        return chatLists.size();
    }

    public void updateChatList(List<ChatList> chatLists) {
        this.chatLists = chatLists;
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView username, message;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            username = itemView.findViewById(R.id.msgUsername);
            message = itemView.findViewById(R.id.msgContent);
        }
    }
}
