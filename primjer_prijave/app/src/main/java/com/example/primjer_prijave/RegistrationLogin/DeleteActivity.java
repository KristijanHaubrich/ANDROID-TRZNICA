package com.example.primjer_prijave.RegistrationLogin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.primjer_prijave.AddProduct.Product;
import com.example.primjer_prijave.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class DeleteActivity extends AppCompatActivity {
    private Button delete,back;
    private String  username;
    private boolean  deleteCheck = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete);
        delete = findViewById(R.id.btn_delete);
        back = findViewById(R.id.btn_back);
        username = getIntent().getStringExtra("username");
        Log.e("username",username);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteUser();
            }
        });
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DeleteActivity.this , UserProfileActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                finish();
                startActivity(intent);
            }
        });
    }

    private void deleteUser() {
        FirebaseAuth.getInstance().getCurrentUser().delete();
        deleteProfilePic();
        deleteFromUsers();
        deleteFromMessages();
        deleteFromSubscribers();
        deleteToken();
        deleteFromProducts();
        deletePassword();
        Intent intent = new Intent(DeleteActivity.this , MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        finish();
        startActivity(intent);

    }

    private void deletePassword() {
        if( FirebaseDatabase.getInstance().getReference("Passwords").child(username) != null)FirebaseDatabase.getInstance().getReference("Passwords").child(username).removeValue();
    }

    private void deleteProfilePic() {
        deleteCheck = true;
        if(FirebaseDatabase.getInstance().getReference("profilePics").child(username) != null){
            FirebaseStorage store = FirebaseStorage.getInstance();
            FirebaseDatabase.getInstance().getReference("profilePics").child(username).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists() && deleteCheck){
                        String url = snapshot.getValue().toString();
                        StorageReference ref = store.getReferenceFromUrl(url);
                        ref.delete();
                        FirebaseDatabase.getInstance().getReference("profilePics").child(username).removeValue();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            deleteCheck = false;
        }


    }

    private void deleteToken() {
        if( FirebaseDatabase.getInstance().getReference("Tokens").child(username) != null)FirebaseDatabase.getInstance().getReference("Tokens").child(username).removeValue();
    }

    private void deleteFromSubscribers() {
        if(FirebaseDatabase.getInstance().getReference("Subscribers").child(username) != null)FirebaseDatabase.getInstance().getReference("Subscribers").child(username).removeValue();
    }

    private void deleteFromProducts() {
        deleteCheck = true;
        FirebaseStorage store = FirebaseStorage.getInstance();
        FirebaseDatabase.getInstance().getReference("Products").child(username).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists() && deleteCheck){
                    for(DataSnapshot current: snapshot.getChildren()){
                        Product product = current.getValue(Product.class);
                        if(!product.getImageUri().equals("null")){
                            StorageReference ref = store.getReferenceFromUrl(product.getImageUri());
                            ref.delete();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        FirebaseDatabase.getInstance().getReference("Products").child(username).removeValue();
        StorageReference storeRef = FirebaseStorage.getInstance().getReference("productPics").child(username);
        storeRef.delete();
        deleteCheck = false;
    }

    private void deleteFromUsers() {
        FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).removeValue();
    }

    private void deleteFromMessages() {
        deleteCheck = true;
        DatabaseReference messageRef =  FirebaseDatabase.getInstance().getReference("Messages");
        messageRef.child(username).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists() && deleteCheck){
                    messageRef.child(username).removeValue();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

       deleteCheck = false;

    }
}