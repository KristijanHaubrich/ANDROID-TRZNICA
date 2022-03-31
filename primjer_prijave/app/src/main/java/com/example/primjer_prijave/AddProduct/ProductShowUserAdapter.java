package com.example.primjer_prijave.AddProduct;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.primjer_prijave.R;
import com.example.primjer_prijave.SearchUsers.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class ProductShowUserAdapter extends RecyclerView.Adapter<ProductShowUserAdapter.ProductViewHolder>{
    private Context mContext;
    private List<Product> mProducts;
    private String username,profilePicUri;
    private ValueEventListener userListener;

    public ProductShowUserAdapter(Context mContext, List<Product> products, String username) {
        this.mContext = mContext;
        this.mProducts = products;
        this.username = username;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.product_show_user_item,parent,false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product current = mProducts.get(position);

        Glide.with(holder.iv_product_image.getContext()).clear(holder.iv_product_image);
        holder.iv_product_image.setImageDrawable(null);
        Glide.with(holder.iv_product_image.getContext().getApplicationContext()).load(current.getImageUri()).placeholder(R.drawable.ic_notify).into(holder.iv_product_image);

        holder.tv_name.setText(current.getName());
        holder.tv_description.setText(current.getDescription());
        holder.tv_price.setText("Cijena: " + current.getPrice());

       getProfilePic(holder);

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
        Glide.with(holder.iv_profile_pic.getContext()).clear(holder.iv_profile_pic);
        FirebaseDatabase.getInstance().getReference("profilePics").child(username).addValueEventListener(new ValueEventListener() {
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
        });
    }

    @Override
    public int getItemCount()  {
        return mProducts.size();
    }

    public class ProductViewHolder extends RecyclerView.ViewHolder {

        public TextView tv_name, tv_description, tv_price;
        public ImageView iv_product_image, iv_profile_pic;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_name = itemView.findViewById(R.id.tv_name);
            tv_price = itemView.findViewById(R.id.tv_price);
            tv_description = itemView.findViewById(R.id.tv_description);
            iv_product_image = itemView.findViewById(R.id.iv_product_image);
            iv_profile_pic = itemView.findViewById(R.id.profile_image);
        }
    }
}
