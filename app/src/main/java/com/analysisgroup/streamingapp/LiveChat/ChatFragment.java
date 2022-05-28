package com.analysisgroup.streamingapp.LiveChat;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.analysisgroup.streamingapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ChatFragment extends Fragment {

    private EditText messageEditText;
    private ImageButton sendButton;
    private RecyclerView chattingRecyclerView;
    private ChatAdapter chatAdapter;

    private final List<ChatList> chatLists = new ArrayList<>();

    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    DatabaseReference DATABASE_USERS;
    DataSnapshot CHAT;
    String SECRETKEY;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        messageEditText = view.findViewById(R.id.messageTxt);
        sendButton = view.findViewById(R.id.sendButton);
        chattingRecyclerView = view.findViewById(R.id.chattingRecyclerView);

        Bundle bundle = getArguments();
        assert bundle != null;
        SECRETKEY = bundle.getString("secrectKey");

        chattingRecyclerView.setHasFixedSize(true);
        chattingRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        chatAdapter = new ChatAdapter(chatLists, requireActivity());
        chattingRecyclerView.setAdapter(chatAdapter);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        DATABASE_USERS = FirebaseDatabase.getInstance().getReference("DATABASE USERS");

        DATABASE_USERS.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot user : snapshot.getChildren()) {
                    if (user.hasChild("chat")) {
                        String key = ""+user.child("SecretKey").getValue();
                        if (key.equals(SECRETKEY)) {
                            CHAT = user;

                            if (user.child("chat").hasChild("messages")) {
                                chatLists.clear();

                                for (DataSnapshot msgSnapshot : user.child("chat").child("messages").getChildren()) {
                                    if (msgSnapshot.hasChild("msg") && msgSnapshot.hasChild("UID")) {
                                        final String message = msgSnapshot.child("msg").getValue(String.class);
                                        final String UID = msgSnapshot.child("UID").getValue(String.class);
                                        assert UID != null;
                                        final String username = snapshot.child(UID).child("Username").getValue(String.class);

                                        ChatList chatList = new ChatList(UID,username,message);
                                        chatLists.add(chatList);
                                        chatAdapter.updateChatList(chatLists);
                                        chattingRecyclerView.scrollToPosition(chatLists.size() - 1);
                                    }
                                }
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        sendButton.setOnClickListener(click -> {
            final String currentTimeStamp = String.valueOf(System.currentTimeMillis()).substring(0,10);
            final String message = messageEditText.getText().toString();
            final String uidUser = Objects.requireNonNull(CHAT.child("UID").getValue()).toString();

            DATABASE_USERS.child(uidUser).child("chat").child("messages").child(currentTimeStamp).child("msg").setValue(message);
            DATABASE_USERS.child(uidUser).child("chat").child("messages").child(currentTimeStamp).child("UID").setValue(firebaseUser.getUid());
            messageEditText.setText("");
        });

        return view;
    }
}