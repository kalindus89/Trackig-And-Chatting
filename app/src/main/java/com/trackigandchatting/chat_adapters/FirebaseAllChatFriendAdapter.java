package com.trackigandchatting.chat_adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;
import com.trackigandchatting.R;
import com.trackigandchatting.models.ChatsModel;
import com.trackigandchatting.specific_chat.SpecificChatActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class FirebaseAllChatFriendAdapter extends FirestoreRecyclerAdapter<ChatsModel,FirebaseAllChatFriendAdapter.ChatsViewHolder> {

    Activity context;
    FirestoreRecyclerOptions<ChatsModel> fireStoreRecyclerOptions;

    public FirebaseAllChatFriendAdapter(@NonNull Activity context, FirestoreRecyclerOptions<ChatsModel> fireStoreRecyclerOptions) {
        super(fireStoreRecyclerOptions);
        this.context = context;
        this.fireStoreRecyclerOptions = fireStoreRecyclerOptions;
    }

    @Override
    protected void onBindViewHolder(@NonNull FirebaseAllChatFriendAdapter.ChatsViewHolder holder, int position, @NonNull ChatsModel model) {

        holder.particularusername.setText(model.getName());
        String uri=model.getImage();

        Picasso.get().load(uri).placeholder(R.drawable.defaultprofile).into(holder.mimageviewofuser);
        if(model.getStatus().equals("Online"))
        {
            holder.statusofuser.setText(model.getStatus());
            holder.statusofuser.setTextColor(Color.GREEN);
        }
        else
        {
            holder.statusofuser.setText(model.getStatus());
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               /* Intent intent=new Intent(context, SpecificChatActivity.class);
                intent.putExtra("name",model.getName());
                intent.putExtra("receiveruid",model.getUid());
                intent.putExtra("imageuri",model.getImage());
                context.startActivity(intent);*/
                List<String> commonChatRoomId = new ArrayList();
                commonChatRoomId.add(FirebaseAuth.getInstance().getUid().toLowerCase(Locale.ROOT));
                commonChatRoomId.add(model.getUid().toLowerCase(Locale.ROOT));
                Collections.sort(commonChatRoomId);

                String chatRoomId=commonChatRoomId.get(0)+commonChatRoomId.get(1);

                DocumentReference nycRef = FirebaseFirestore.getInstance().collection("Users")
                        .document(FirebaseAuth.getInstance().getUid()).collection("myChats").document(chatRoomId);

                nycRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                Toast.makeText(context, "Already created", Toast.LENGTH_SHORT).show();

                            } else {
                                crateChatRoom(chatRoomId,FirebaseAuth.getInstance().getUid(),model.getUid());

                            }
                        } else {
                            Toast.makeText(context, "Not ok big", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

    }

    private void crateChatRoom(String chatRoomId, String user_1, String user_2) {

        Map<String, Object> note = new HashMap<>();
        note.put("user_1", user_1);
        note.put("user_2", user_2);
        note.put("lastUpdateTime", new Date());

        FirebaseFirestore.getInstance().collection("Users").document(user_1).collection("myChats").document(chatRoomId).set(note);
        FirebaseFirestore.getInstance().collection("Users").document(user_2).collection("myChats").document(chatRoomId).set(note).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                context.finish();
            }
        });


    }

    @NonNull
    @Override
    public ChatsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.current_chat_item_layout,parent,false);
        return new ChatsViewHolder(view);
    }

    public class ChatsViewHolder extends RecyclerView.ViewHolder {

        private TextView particularusername;
        private TextView statusofuser;
        private ImageView mimageviewofuser;

        public ChatsViewHolder(@NonNull View itemView) {
            super(itemView);

            particularusername=itemView.findViewById(R.id.nameofuser);
            statusofuser=itemView.findViewById(R.id.statusofuser);
            mimageviewofuser=itemView.findViewById(R.id.imageviewofuser);
        }
    }
}
