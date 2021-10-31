package com.trackigandchatting.main_chat_activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.squareup.picasso.Picasso;
import com.trackigandchatting.R;
import com.trackigandchatting.SessionManagement;
import com.trackigandchatting.chat_adapters.AllFriendsAdapter;
import com.trackigandchatting.chat_adapters.SpecificMessageAdapter;
import com.trackigandchatting.models.ChatsModel;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class SpecificChatActivity extends AppCompatActivity {

    String imageuri,mrecievername,chatId,enteredmessage;
    ImageView userImage;
    TextView userName;
    ImageButton backbuttonofspecificchat,sendMessage;
    EditText getmessage;
    RecyclerView recyclerViewChatList;
    FirebaseFirestore firebaseFirestore;
    SpecificMessageAdapter specificMessageAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_specific_chat);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        userImage = findViewById(R.id.userImage);
        userName = findViewById(R.id.userName);
        backbuttonofspecificchat = findViewById(R.id.backbuttonofspecificchat);
        sendMessage = findViewById(R.id.sendMessage);
        getmessage = findViewById(R.id.getmessage);
        recyclerViewChatList = findViewById(R.id.recyclerViewChatList);

        firebaseFirestore = FirebaseFirestore.getInstance();

        chatId = getIntent().getStringExtra("chatId");
        mrecievername = getIntent().getStringExtra("name");
        imageuri = getIntent().getStringExtra("imageuri");

        userName.setText(mrecievername);

        if (imageuri.isEmpty()) {
            Toast.makeText(getApplicationContext(), "null is recieved", Toast.LENGTH_SHORT).show();
        } else {
            Picasso.get().load(imageuri).into(userImage);
        }

        backbuttonofspecificchat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                enteredmessage = getmessage.getText().toString();
                if (enteredmessage.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Enter message first", Toast.LENGTH_SHORT).show();
                } else {
                    sendMessage();
                }
            }
        });

        displayChatList();
    }

    private void sendMessage() {
        Map<String, Object> note = new HashMap<>();
        note.put("uid", FirebaseAuth.getInstance().getUid());
        note.put("message",enteredmessage);
        note.put("lastUpdateTime", new Date());

        FirebaseFirestore.getInstance().collection("ChatList").document(chatId).collection("conversations").document().set(note).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                recyclerViewChatList.post(new Runnable() {
                    @Override
                    public void run() {
                        recyclerViewChatList.smoothScrollToPosition(specificMessageAdapter.getItemCount() - 1);
                    }
                });
            }
        });
        getmessage.setText(null);

    }

    private void displayChatList(){
        Query query = firebaseFirestore.collection("ChatList").document(chatId).collection("conversations").orderBy("lastUpdateTime", Query.Direction.ASCENDING);

        FirestoreRecyclerOptions<ChatsModel> allChats = new FirestoreRecyclerOptions.Builder<ChatsModel>().setQuery(query, ChatsModel.class).build();

        specificMessageAdapter = new SpecificMessageAdapter(this,allChats);
        recyclerViewChatList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager =new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true); // start list from end
        recyclerViewChatList.setLayoutManager(linearLayoutManager);
        recyclerViewChatList.setAdapter(specificMessageAdapter);
        specificMessageAdapter.notifyDataSetChanged();

    }

    @Override
    public void onStart() {
        super.onStart();
        specificMessageAdapter.startListening();
        recyclerViewChatList.setAdapter(specificMessageAdapter);
    }

    @Override
    public void onStop() {
        super.onStop();

        if(specificMessageAdapter!=null)
        {
            specificMessageAdapter.stopListening();
        }
    }
}