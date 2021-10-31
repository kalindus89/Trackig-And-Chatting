package com.trackigandchatting.chat_adapters;

import android.app.Activity;
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
import com.trackigandchatting.SessionManagement;
import com.trackigandchatting.main_chat_activities.SpecificChatActivity;
import com.trackigandchatting.models.ChatsModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ChattingFriendsAdapter extends FirestoreRecyclerAdapter<ChatsModel, ChattingFriendsAdapter.ChatsViewHolder> {

    Activity context;
    FirestoreRecyclerOptions<ChatsModel> fireStoreRecyclerOptions;

    public ChattingFriendsAdapter(@NonNull Activity context, FirestoreRecyclerOptions<ChatsModel> fireStoreRecyclerOptions) {
        super(fireStoreRecyclerOptions);
        this.context = context;
        this.fireStoreRecyclerOptions = fireStoreRecyclerOptions;
    }

    @Override
    protected void onBindViewHolder(@NonNull ChattingFriendsAdapter.ChatsViewHolder holder, int position, @NonNull ChatsModel model) {

        holder.particularusername.setText(model.getName());
        String uri=model.getImage();

        Picasso.get().load(uri).placeholder(R.drawable.defaultprofile).into(holder.mimageviewofuser);


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(context, SpecificChatActivity.class);
                intent.putExtra("name",model.getName());
                intent.putExtra("chatId",fireStoreRecyclerOptions.getSnapshots().getSnapshot(holder.getAbsoluteAdapterPosition()).getId());
                intent.putExtra("imageuri",model.getImage());
                context.startActivity(intent);
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
