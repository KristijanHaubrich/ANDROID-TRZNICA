package com.example.primjer_prijave.RegistrationLogin;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.primjer_prijave.AddProduct.ImageActivity;
import com.example.primjer_prijave.AddProduct.Product;
import com.example.primjer_prijave.AddProduct.ProductAdapter;
import com.example.primjer_prijave.R;
import com.example.primjer_prijave.SearchUsers.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment  {


    private String username, profilePicUri;
    private Button btn_logout, btn_delete_acc;
    private TextView tv_username, tv_email, tv_product_description, tv_subs;
    private ImageView iv_profile_pic;
    private RecyclerView product_recycler;
    private SearchView searchView;
    private List<Product> products;
    private ProductAdapter adapter;
    private ValueEventListener userListener, fetchListener, profilePicListener;
    private int subscribers_count;
    private boolean check = false;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home,container,false);
        initializeUI(view);
        return view;
    }

    private void initializeUI(View view) {
        btn_delete_acc = view.findViewById(R.id.delete_acc);
        iv_profile_pic = view.findViewById(R.id.profile_picture);
        btn_logout = view.findViewById(R.id.btn_logout);
        tv_username = view.findViewById(R.id.tv_name);
        tv_subs = view.findViewById(R.id.tv_subs);
        tv_email = view.findViewById(R.id.tv_email);
        product_recycler = view.findViewById(R.id.product_recycler);
        searchView = view.findViewById(R.id.searchView);
        tv_product_description = view.findViewById(R.id.tv_product_description);

        loadProfilePic();
        checkSubscribersCount(view);

        btn_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(view.getContext() , MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getActivity().finish();
                startActivity(intent);
            }
        });

        iv_profile_pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImage();
            }
        });


        btn_delete_acc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToDeleteUser();
            }
        });


    }

    private void checkSubscribersCount(View view){
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        userListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    User user = snapshot.getValue(User.class);
                    username = user.getUsername();
                    setRecycler(view);
                    tv_username.setText(username);
                    tv_email.setText(FirebaseAuth.getInstance().getCurrentUser().getEmail());
                    FirebaseDatabase.getInstance().getReference("profilePics").child(username).addListenerForSingleValueEvent(profilePicListener);
                    userRef.removeEventListener(userListener);

                    FirebaseDatabase.getInstance().getReference("Subscribers").child(username).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.exists()){
                                subscribers_count = 0;
                                for(DataSnapshot current:snapshot.getChildren()){
                                    FirebaseDatabase.getInstance().getReference("Users").addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if(snapshot.exists()){
                                                for(DataSnapshot snap:snapshot.getChildren()){
                                                    User user = snap.getValue(User.class);
                                                    if(user.getUsername().equals(current.getKey()) && current.exists()) check = true;
                                                }
                                                if(check == false)  FirebaseDatabase.getInstance().getReference("Subscribers").child(username).child(current.getKey()).removeValue();
                                                check = false;
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                                    if(current.exists()) subscribers_count++;
                                }

                                tv_subs.setText("Broj pretplatnika: " + subscribers_count);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        userRef.addValueEventListener(userListener);
    }

    private void loadProfilePic(){
        profilePicListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    profilePicUri = snapshot.getValue().toString();
                    Glide.with(getContext()).clear(iv_profile_pic);
                    Glide.with(getContext()).load(Uri.parse(profilePicUri)).placeholder(R.drawable.ic_profile).into(iv_profile_pic);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
    }

    private void goToDeleteUser() {
        Intent intent = new Intent(getContext() , DeleteActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("username",tv_username.getText().toString());
        getActivity().finish();
        startActivity(intent);
    }

    private void setRecycler(View view) {

        product_recycler.setHasFixedSize(true);

        product_recycler.setLayoutManager(new LinearLayoutManager(view.getContext()));
        products = new ArrayList<>();
        fetchData(view);

        if(searchView != null){
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    search(newText.toLowerCase());
                    return false;
                }
            });
        }

    }

    private void fetchData(View view) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Products").child(username);
        fetchListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                products.clear();
                for(DataSnapshot snapshot1:snapshot.getChildren()){
                    Product product = snapshot1.getValue(Product.class);
                    products.add(product);
                }

                adapter =  new ProductAdapter(view.getContext(),products);
                product_recycler.setAdapter(adapter);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        ref.addValueEventListener(fetchListener);
    }

    private void search(String newText) {
        List<Product> list = new ArrayList<>();
        for(Product current : products){
            if(current.getName().toLowerCase().contains(newText)){
                list.add(current);
            }
        }
        ProductAdapter adapter = new ProductAdapter(getContext(),list);
        product_recycler.setAdapter(adapter);

    }

    private void uploadImage() {
        Intent intent = new Intent(getContext() , ImageActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("type", "profile");
        getContext().startActivity(intent);

    }


}
