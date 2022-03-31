package com.example.primjer_prijave.Message;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.view.menu.MenuView;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.primjer_prijave.R;
import com.example.primjer_prijave.SearchUsers.User;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class InboxAdapter extends RecyclerView.Adapter<InboxAdapter.ViewHolder> {

    private boolean msg = true;
    private String currentUsername;
    private Intent intent;
    private Context mcontext;
    private List<String> mUsers;
    private ValueEventListener profilePicListener;
    private int subscribers_count;

    public InboxAdapter(Context mcontext, List<String> Users){
        this.mcontext = mcontext;
        this.mUsers = Users;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mcontext).inflate(R.layout.activity_user_item,parent,false);

        ViewHolder holder = new ViewHolder(view);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               goToMessages(view,holder);
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.username.setText(mUsers.get(position));
        getSubscribersCount(holder);
        setProfilePic(holder);
    }

    private void goToMessages(View view,ViewHolder holder){
        FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists() && msg){
                    intent = new Intent(view.getContext() , MessageActivity.class);
                    currentUsername = snapshot.getValue(User.class).getUsername();
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    int position = holder.getLayoutPosition();
                    String user = mUsers.get(position);
                    intent.putExtra("receiver", user);
                    intent.putExtra("sender", currentUsername);
                    view.getContext().startActivity(intent);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getSubscribersCount(ViewHolder holder){
        FirebaseDatabase.getInstance().getReference("Subscribers").child(holder.username.getText().toString()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    subscribers_count = 0;
                    for(DataSnapshot current:snapshot.getChildren()){
                        if(current.exists()) subscribers_count++;
                    }
                    holder.subs.setText("Broj pretplatnika: " + subscribers_count);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void setProfilePic(ViewHolder holder){
        Glide.with(holder.profilePic.getContext()).clear(holder.profilePic);
        profilePicListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    holder.uri = snapshot.getValue().toString();
                    Glide.with(holder.profilePic.getContext().getApplicationContext()).load(holder.uri).placeholder(R.drawable.ic_profile).into(holder.profilePic);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        FirebaseDatabase.getInstance().getReference("profilePics").child(holder.username.getText().toString()).addValueEventListener(profilePicListener);
    }

    private void removeMessages(String receiver, View view) {
        FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    User user = snapshot.getValue(User.class);
                    FirebaseDatabase.getInstance().getReference("Messages").child(user.getUsername()).child(receiver).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(view.getContext(),"Poruke obrisane",Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView username;
        private String uri;
        private TextView subs;
        private ImageView delete;
        private de.hdodenhof.circleimageview.CircleImageView profilePic;

        public ViewHolder(View itemView){
            super(itemView);
            delete = itemView.findViewById(R.id.iv_delete);
            username = itemView.findViewById(R.id.tv_username);
            subs = itemView.findViewById(R.id.tv_subs);
            profilePic = itemView.findViewById(R.id.profile_image);

            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    removeMessages(username.getText().toString(),itemView);
                }
            });

        }
    }

    public void addNewUser(String user){
        mUsers.add(user);
        notifyItemInserted(mUsers.size()-1);
    }
}
