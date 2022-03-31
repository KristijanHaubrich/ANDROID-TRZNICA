package com.example.primjer_prijave.SearchUsers;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.primjer_prijave.AddProduct.ImageActivity;
import com.example.primjer_prijave.AddProduct.Product;
import com.example.primjer_prijave.AddProduct.ProductAdapter;
import com.example.primjer_prijave.AddProduct.ProductShowUserAdapter;
import com.example.primjer_prijave.Message.MessageActivity;
import com.example.primjer_prijave.R;
import com.example.primjer_prijave.RegistrationLogin.UserProfileActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ShowUserActivity extends AppCompatActivity {

    private ImageView iv_send;
    private String username,subToken, subscriber,sender, uri;

    private TextView tv_name,tv_subs, tv_product_description, tv_instruction;
    private CircleImageView iv_profilePic;
    private RecyclerView product_recycler;
    private SearchView searchView;
    private List<Product> products;
    private ProductShowUserAdapter adapter;
    private Button btn_back, btn_subscribe, btn_unsubscribe;
    private ValueEventListener userListener;
    private int subscribers_count;
    private Intent messageIntent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_user);
        setUserData();
        initializeUI();
    }


    private void initializeUI() {
        iv_send = findViewById(R.id.iv_send);
        tv_instruction = findViewById(R.id.tv_instruction);
        btn_unsubscribe = findViewById(R.id.btn_unsubscribe);
        btn_back = findViewById(R.id.btn_back);
        btn_subscribe = findViewById(R.id.btn_subscribe);
        checkIfSubscribed();

        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               GoToUserProfile();
            }
        });
        btn_subscribe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                subscribe();
            }
        });

        btn_unsubscribe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unsubscribe();
            }
        });

        if(!username.equals(subscriber)){
            iv_send.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openInbox();
                }
            });
        }else iv_send.setEnabled(false);

    }

    private void GoToUserProfile(){
        Intent intent = new Intent(this , UserProfileActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        finish();
        startActivity(intent);
    }

    private void openInbox() {
        messageIntent = new Intent(this, MessageActivity.class);
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    sender = snapshot.getValue(User.class).getUsername();
                    messageIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    messageIntent.putExtra("receiver", username);
                    messageIntent.putExtra("sender", sender);

                    startActivity(messageIntent);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void unsubscribe() {
            FirebaseDatabase.getInstance().getReference("Subscribers").child(username).child(subscriber).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Toast.makeText(getBaseContext(), "Uspješno otkazana pretplata na korisnika " + username,Toast.LENGTH_LONG).show();
                    btn_subscribe.setText("Pretplati se");
                    btn_subscribe.setEnabled(true);
                    btn_unsubscribe.setEnabled(false);
                }
            });
    }

    private void checkIfSubscribed() {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    User user = snapshot.getValue(User.class);
                    subscriber = user.getUsername();
                    DatabaseReference subRef = FirebaseDatabase.getInstance().getReference("Subscribers").child(username).child(subscriber);
                    subRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.exists()){
                                btn_subscribe.setEnabled(false);
                                btn_subscribe.setText("Pretplaćeni");
                                btn_unsubscribe.setEnabled(true);
                            }else {
                                btn_subscribe.setText("Pretplati se");
                                btn_subscribe.setEnabled(true);
                                btn_unsubscribe.setEnabled(false);
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
        });
    }

    private void subscribe() {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    User user = snapshot.getValue(User.class);
                    subscriber = user.getUsername();
                    DatabaseReference tokenRef = FirebaseDatabase.getInstance().getReference("Tokens").child(subscriber);
                    tokenRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.exists()){
                                subToken = snapshot.getValue().toString();
                                DatabaseReference subRef = FirebaseDatabase.getInstance().getReference("Subscribers").child(username).child(subscriber);
                                subRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if(snapshot.exists()){
                                            Toast.makeText(getBaseContext(), "Već ste pretplaćeni na korisnika",Toast.LENGTH_LONG).show();
                                        }else{
                                            subRef.setValue(subToken).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    Toast.makeText(getBaseContext(), "Uspješna pretplata na korisnika " + username,Toast.LENGTH_LONG).show();
                                                    btn_subscribe.setEnabled(false);
                                                    btn_subscribe.setText("Pretplaćeni");
                                                    btn_unsubscribe.setEnabled(true);
                                                }
                                            });

                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) { }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



    }

    private void setUserData() {
        Bundle extras = getIntent().getExtras();
        tv_name = findViewById(R.id.tv_name);
        tv_name.setText(extras.getString("username"));
        username = extras.getString("username");
        tv_subs = findViewById(R.id.tv_subs);
        FirebaseDatabase.getInstance().getReference("Subscribers").child(username).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    subscribers_count = 0;
                    for(DataSnapshot current:snapshot.getChildren()){
                        if(current.exists()) subscribers_count++;
                    }

                    tv_subs.setText("Broj pretplatnika: " + subscribers_count);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        tv_product_description = findViewById(R.id.tv_product_description);
        iv_profilePic = findViewById(R.id.profile_picture);
        tv_product_description.setText("Proizvodi");

        FirebaseDatabase.getInstance().getReference("profilePics").child(username).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    uri = snapshot.getValue().toString();
                    if(ShowUserActivity.this != null) Glide.with(getApplicationContext()).load(Uri.parse(uri)).placeholder((R.drawable.ic_profile)).into(iv_profilePic);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        product_recycler = findViewById(R.id.product_recycler);
        searchView = findViewById(R.id.searchView);
        setProductRecycler(tv_name.toString());
    }

    private void setProductRecycler(String username) {
        product_recycler.setHasFixedSize(true);

        product_recycler.setLayoutManager(new LinearLayoutManager(this));
        products = new ArrayList<>();
        fetchData();

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

    private void search(String newText) {
        List<Product> list = new ArrayList<>();
        for(Product current : products){
            if(current.getName().toLowerCase().contains(newText)){
                list.add(current);
            }
        }
        ProductShowUserAdapter adapter = new ProductShowUserAdapter(getBaseContext(),list,username);
        product_recycler.setAdapter(adapter);
    }

    private void fetchData() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Products").child(username);
        userListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    products.clear();
                    for(DataSnapshot snapshot1:snapshot.getChildren()){
                        Product product = snapshot1.getValue(Product.class);
                        products.add(product);
                    }

                    adapter =  new ProductShowUserAdapter(getBaseContext(),products,username);
                    product_recycler.setAdapter(adapter);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        ref.addValueEventListener(userListener);
    }


}