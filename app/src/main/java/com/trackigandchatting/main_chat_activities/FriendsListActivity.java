package com.trackigandchatting.main_chat_activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.WindowManager;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.trackigandchatting.R;
import com.trackigandchatting.chat_adapters.AllFriendsAdapter;
import com.trackigandchatting.models.ChatsModel;

public class FriendsListActivity extends AppCompatActivity {

    FirebaseAuth firebaseAuth;
    FirebaseFirestore firebaseFirestore;
    RecyclerView recyclerViewChat;
    AllFriendsAdapter allChatsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_list);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        recyclerViewChat = findViewById(R.id.recyclerViewFriendList);

        syncChatPeopleFromFirestore();
    }

    private void syncChatPeopleFromFirestore() {

        Query query = firebaseFirestore.collection("Users").whereNotEqualTo("uid",firebaseAuth.getUid());
        FirestoreRecyclerOptions<ChatsModel> allChats = new FirestoreRecyclerOptions.Builder<ChatsModel>().setQuery(query, ChatsModel.class).build();

        allChatsAdapter = new AllFriendsAdapter(this,allChats);
        recyclerViewChat.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager =new LinearLayoutManager(this);
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