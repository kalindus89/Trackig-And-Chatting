package com.trackigandchatting.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.trackigandchatting.R;
import com.trackigandchatting.chat_adapters.FirebaseAllChatFriendAdapter;
import com.trackigandchatting.models.ChatsModel;

public class ChatFragment extends Fragment {

    FirebaseAuth firebaseAuth;
    FirebaseFirestore firebaseFirestore;

    RecyclerView recyclerViewChat;
    FirebaseAllChatFriendAdapter allChatsAdapter;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v =inflater.inflate(R.layout.fragment_chat,null);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        recyclerViewChat = v.findViewById(R.id.recyclerViewChat);

        syncChatPeopleFromFirestore();

        return v;
    }

    private void syncChatPeopleFromFirestore() {

        Query query = firebaseFirestore.collection("Users").whereNotEqualTo("uid",firebaseAuth.getUid());
        FirestoreRecyclerOptions<ChatsModel> allChats = new FirestoreRecyclerOptions.Builder<ChatsModel>().setQuery(query, ChatsModel.class).build();

        allChatsAdapter = new FirebaseAllChatFriendAdapter(getContext(),allChats);
        recyclerViewChat.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager =new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        recyclerViewChat.setLayoutManager(linearLayoutManager);
        recyclerViewChat.setAdapter(allChatsAdapter);
        allChatsAdapter.notifyDataSetChanged();
    }
    @Override
    public void onStart() {
        super.onStart();
        allChatsAdapter.startListening();
        recyclerViewChat.setAdapter(allChatsAdapter);
    }

    @Override
    public void onStop() {
        super.onStop();

        if(allChatsAdapter!=null)
        {
            allChatsAdapter.stopListening();
            //noteAdapter.startListening();
        }
    }

}