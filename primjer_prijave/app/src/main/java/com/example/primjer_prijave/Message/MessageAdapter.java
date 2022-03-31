package com.example.primjer_prijave.Message;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.primjer_prijave.R;
import com.example.primjer_prijave.SearchUsers.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;


public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    private Context context;
    private List<Message> messages;
    private String uri;

    public MessageAdapter(Context context, List<Message> messages){
        this.context = context;
        this.messages = messages;
    }

    @NonNull
    @Override
    public MessageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.message_item,parent,false);
        MessageAdapter.ViewHolder holder = new MessageAdapter.ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull MessageAdapter.ViewHolder holder, int position) {
        Message current = messages.get(position);
        holder.cardViewLeft.setVisibility(INVISIBLE);
        holder.cardViewRight.setVisibility(INVISIBLE);
        if(current.getMessageType().equals("receive")){
            loadReceiverMessage(holder,current);
        }else{
            loadSenderMessage(holder,current);
        }


    }

    private void loadSenderMessage(MessageAdapter.ViewHolder holder, Message current){
        String sender = current.getSender();
        holder.cardViewRight.setVisibility(VISIBLE);
        holder.message_right.setText(current.getBody());
        Glide.with(holder.profilePicRight.getContext()).clear(holder.profilePicRight);
        FirebaseDatabase.getInstance().getReference("profilePics").child(sender).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    uri = snapshot.getValue().toString();
                    Glide.with(holder.profilePicRight.getContext().getApplicationContext()).load(uri).into(holder.profilePicRight);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadReceiverMessage(MessageAdapter.ViewHolder holder, Message current){
        String sender = current.getSender();
        holder.cardViewLeft.setVisibility(VISIBLE);
        holder.message_left.setText(current.getBody());
        Glide.with(holder.profilePicLeft.getContext()).clear(holder.profilePicLeft);
        FirebaseDatabase.getInstance().getReference("profilePics").child(sender).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    uri = snapshot.getValue().toString();
                    Glide.with(holder.profilePicLeft.getContext().getApplicationContext()).load(uri).into(holder.profilePicLeft);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private CardView cardViewLeft,cardViewRight;
        private TextView message_left, message_right;
        private CircleImageView profilePicLeft,profilePicRight;

        public ViewHolder(View itemView){
            super(itemView);
            cardViewLeft = itemView.findViewById(R.id.left);
            cardViewRight = itemView.findViewById(R.id.right);

            message_left = itemView.findViewById(R.id.tv_message_left);
            message_right = itemView.findViewById(R.id.tv_message_right);

            profilePicLeft = itemView.findViewById(R.id.profile_image_left);
            profilePicRight = itemView.findViewById(R.id.profile_image_right);
        }

    }
}
