package com.example.primjer_prijave.AddProduct;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.primjer_prijave.R;
import com.example.primjer_prijave.RegistrationLogin.RegisterActivity;
import com.example.primjer_prijave.SearchUsers.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder>  {

    private Context mContext;
    private List<Product> mProducts;
    private String username,profilePicUri;
    private ValueEventListener profilePicListener, userListener;


    public ProductAdapter(Context mContext, List<Product> products) {
        this.mContext = mContext;
        this.mProducts = products;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.product_item,parent,false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product current = mProducts.get(position);



        holder.tv_name.setText(current.getName());
        holder.tv_description.setText(current.getDescription());
        holder.tv_price.setText("Cijena: " + current.getPrice());

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        getProfilePic(holder);

        userRef.addListenerForSingleValueEvent(userListener);


        holder.iv_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteProduct(position,current, holder);
            }
        });



        holder.iv_product_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.iv_product_image.setRotation(holder.iv_product_image.getRotation()+90);
            }
        });

        holder.iv_profile_pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.iv_profile_pic.setRotation(holder.iv_profile_pic.getRotation()+90);
            }
        });
    }

    private void getProfilePic(ProductViewHolder holder){
        userListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    User user = snapshot.getValue(User.class);
                    username = user.getUsername();
                    setProfilePic(holder);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
    }

    private void setProfilePic(ProductViewHolder holder){
        Glide.with(holder.iv_profile_pic.getContext()).clear(holder.iv_profile_pic);
        profilePicListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    profilePicUri = snapshot.getValue().toString();
                }
                Glide.with(holder.iv_profile_pic.getContext().getApplicationContext()).load(profilePicUri).placeholder(R.drawable.ic_profile).into(holder.iv_profile_pic);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        FirebaseDatabase.getInstance().getReference("profilePics").child(username).addValueEventListener(profilePicListener);
    }

    private void deleteProduct(int position, Product current, ProductViewHolder view) {
        mProducts.remove(position);
        notifyDataSetChanged();
        FirebaseStorage store = FirebaseStorage.getInstance();
        DatabaseReference dataRef = FirebaseDatabase.getInstance().getReference("Products").child(username).child(current.getName());
        if(!current.getImageUri().equals("null")){
            StorageReference ref = store.getReferenceFromUrl(current.getImageUri());
            ref.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                }
            });
        }


        dataRef.removeValue();
    }

    @Override
    public int getItemCount() {
        return mProducts.size();
    }

    public class ProductViewHolder extends RecyclerView.ViewHolder {

        public TextView tv_name, tv_description, tv_price;
        public ImageView iv_product_image, iv_profile_pic, iv_delete;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_name = itemView.findViewById(R.id.tv_name);
            iv_delete = itemView.findViewById(R.id.iv_delete);
            tv_price = itemView.findViewById(R.id.tv_price);
            tv_description = itemView.findViewById(R.id.tv_description);
            iv_product_image = itemView.findViewById(R.id.iv_product_image);
            iv_profile_pic = itemView.findViewById(R.id.profile_image);
        }
    }



}

