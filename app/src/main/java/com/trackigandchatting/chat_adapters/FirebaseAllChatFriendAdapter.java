package com.trackigandchatting.chat_adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.squareup.picasso.Picasso;
import com.trackigandchatting.R;
import com.trackigandchatting.models.ChatsModel;
import com.trackigandchatting.specific_chat.SpecificChatActivity;

public class FirebaseAllChatFriendAdapter extends FirestoreRecyclerAdapter<ChatsModel,FirebaseAllChatFriendAdapter.ChatsViewHolder> {

    Context context;
    FirestoreRecyclerOptions<ChatsModel> fireStoreRecyclerOptions;

    public FirebaseAllChatFriendAdapter(@NonNull Context context, FirestoreRecyclerOptions<ChatsModel> fireStoreRecyclerOptions) {
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
                Intent intent=new Intent(context, SpecificChatActivity.class);
                intent.putExtra("name",model.getName());
                intent.putExtra("receiveruid",model.getUid());
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
