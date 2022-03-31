package com.example.primjer_prijave.RegistrationLogin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.view.MenuItem;

import com.example.primjer_prijave.AddProduct.AddProductFragment;
import com.example.primjer_prijave.Message.MessageFragment;
import com.example.primjer_prijave.R;
import com.example.primjer_prijave.SearchUsers.SearchFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class UserProfileActivity extends AppCompatActivity {

    private DatabaseReference dataref;
    private FirebaseUser user;
    private String type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);



        InitializeUI();
    }

    private void InitializeUI() {
        type  = getIntent().getStringExtra("type");
        user = FirebaseAuth.getInstance().getCurrentUser();
        dataref = FirebaseDatabase.getInstance().getReference("Users");

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);

        setStartingFragment();

    }

    private void setStartingFragment(){
        if(type != null && type.equals("upload")){
            AddProductFragment fragment = new AddProductFragment();
            Bundle bundle = new Bundle();
            bundle.putString("type", type);
            bundle.putString("productName", getIntent().getStringExtra("productName"));
            bundle.putString("productDescription", getIntent().getStringExtra("productDescription"));
            bundle.putString("productPrice", getIntent().getStringExtra("productPrice"));
            bundle.putString("imageUri", getIntent().getStringExtra("imageUri"));

            fragment.setArguments(bundle);
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();
        }
        else if(type != null && type.equals("message")){
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new MessageFragment()).commit();
        }
        else getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Fragment selectedFragment = null;

                        switch(item.getItemId()){
                            case R.id.nav_home:
                                selectedFragment = new HomeFragment();
                                break;
                            case R.id.nav_search:
                                selectedFragment = new SearchFragment();
                                break;
                            case R.id.nav_add_product:
                                Bundle bundle = new Bundle();
                                bundle.putString("type", "container");
                                selectedFragment = new AddProductFragment();
                                selectedFragment.setArguments(bundle);
                                break;
                            case R.id.nav_message:
                                selectedFragment = new MessageFragment();
                                break;
                        }

                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,selectedFragment).commit();

                       return true;



                }
            };



}