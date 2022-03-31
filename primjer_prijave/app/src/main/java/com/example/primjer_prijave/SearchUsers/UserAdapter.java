package com.example.primjer_prijave.SearchUsers;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.primjer_prijave.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private Context mcontext;
    private List<User> mUsers;
    private ValueEventListener profilePicListener;
    private Intent intent;
    private int subscribers_count;

    public UserAdapter(Context mcontext, List<User> Users){
        this.mcontext = mcontext;
        this.mUsers = Users;
    }

    @NonNull
    @Override
    public UserAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mcontext).inflate(R.layout.activity_user_item,parent,false);
        intent = new Intent(view.getContext() , ShowUserActivity.class);
        UserAdapter.ViewHolder holder = new UserAdapter.ViewHolder(view);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToUserProfile(view,holder);
            }
        });
        return holder;
    }


    @Override
    public void onBindViewHolder(@NonNull UserAdapter.ViewHolder holder, int position) {
        holder.delete.setVisibility(View.INVISIBLE);
        holder.username.setText(mUsers.get(position).getUsername());
        getSubscribersCount(holder);
        setProfilePic(holder);
    }

    private void goToUserProfile(View view, UserAdapter.ViewHolder holder){

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        int position = holder.getLayoutPosition();
        User user = mUsers.get(position);
        intent.putExtra("username", user.getUsername());
        view.getContext().startActivity(intent);
    }

    private void getSubscribersCount(UserAdapter.ViewHolder holder){
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

    private void setProfilePic(UserAdapter.ViewHolder holder){
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

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView username;
        private String uri;
        private ImageView delete;
        private TextView subs;
        private de.hdodenhof.circleimageview.CircleImageView profilePic;

        public ViewHolder(View itemView){
            super(itemView);
            delete = itemView.findViewById(R.id.iv_delete);
            username = itemView.findViewById(R.id.tv_username);
            subs = itemView.findViewById(R.id.tv_subs);
            profilePic = itemView.findViewById(R.id.profile_image);
        }
    }

    public void addNewUser(User user){
        mUsers.add(user);
        notifyItemInserted(mUsers.size()-1);
    }
}
