package com.trackigandchatting.chat_adapters;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;
import com.trackigandchatting.R;
import com.trackigandchatting.main_chat_activities.SpecificChatActivity;
import com.trackigandchatting.models.ChatsModel;

import java.text.SimpleDateFormat;

public class SpecificMessageAdapter extends FirestoreRecyclerAdapter<ChatsModel, RecyclerView.ViewHolder> {

    Activity context;
    FirestoreRecyclerOptions<ChatsModel> fireStoreRecyclerOptions;

    int ITEM_SEND=1;
    int ITEM_RECIEVE=2;


    public SpecificMessageAdapter(@NonNull Activity context, FirestoreRecyclerOptions<ChatsModel> fireStoreRecyclerOptions) {
        super(fireStoreRecyclerOptions);
        this.context = context;
        this.fireStoreRecyclerOptions = fireStoreRecyclerOptions;
    }

    @Override
    protected void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull ChatsModel model) {

        ChatsModel messages=fireStoreRecyclerOptions.getSnapshots().get(position);
        if(holder.getClass()==SenderViewHolder.class)
        {
            SenderViewHolder viewHolder=(SenderViewHolder)holder;
            viewHolder.textViewmessaage.setText(messages.getMessage());
            viewHolder.timeofmessage.setText(new SimpleDateFormat("HH:mm").format(messages.getLastUpdateTime()));
        }
        else
        {
            RecieverViewHolder viewHolder=(RecieverViewHolder)holder;
            viewHolder.textViewmessaage.setText(messages.getMessage());
            viewHolder.timeofmessage.setText(new SimpleDateFormat("HH:mm").format(messages.getLastUpdateTime()));
        }
    }

    @Override
    public int getItemViewType(int position) {
        //get who is sender or receiver
        ChatsModel chatsModel=fireStoreRecyclerOptions.getSnapshots().get(position);
        if(FirebaseAuth.getInstance().getCurrentUser().getUid().equals(chatsModel.getUid()))
        {
            return  ITEM_SEND;
        }
        else
        {
            return ITEM_RECIEVE;
        }
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {


        if(viewType==ITEM_SEND)
        {
            View view= LayoutInflater.from(context).inflate(R.layout.senderchatlayout,parent,false);
            return new SenderViewHolder(view);
        }
        else
        {
            View view= LayoutInflater.from(context).inflate(R.layout.recieverchatlayout,parent,false);
            return new RecieverViewHolder(view);
        }
    }

    public class SenderViewHolder extends RecyclerView.ViewHolder {

        TextView textViewmessaage;
        TextView timeofmessage;

        public SenderViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewmessaage=itemView.findViewById(R.id.sendermessage);
            timeofmessage=itemView.findViewById(R.id.timeofmessage);
        }
    }

    public class RecieverViewHolder extends RecyclerView.ViewHolder {

        TextView textViewmessaage;
        TextView timeofmessage;

        public RecieverViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewmessaage=itemView.findViewById(R.id.sendermessage);
            timeofmessage=itemView.findViewById(R.id.timeofmessage);

        }
    }
}
