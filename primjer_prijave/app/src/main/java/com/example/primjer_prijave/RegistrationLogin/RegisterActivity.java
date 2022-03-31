package com.example.primjer_prijave.RegistrationLogin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.primjer_prijave.R;
import com.example.primjer_prijave.SearchUsers.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.List;

public class RegisterActivity extends AppCompatActivity {

    private List<String> users = new ArrayList<String>();
    private DatabaseReference dataref;
    private FirebaseAuth auth;
    private EditText et_email;
    private EditText et_password;
    private EditText et_username;
    private Button btn_register, btn_back;
    private ProgressBar progressBar;
    private ValueEventListener dataListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_register);
        dataref = FirebaseDatabase.getInstance().getReference();
        InitializeUI();
    }


    private void InitializeUI() {

        dataref = FirebaseDatabase.getInstance().getReference().child("Users");

        auth = FirebaseAuth.getInstance();

        btn_register = findViewById(R.id.btn_register);
        btn_back = findViewById(R.id.btn_back);
        progressBar = findViewById(R.id.progressBar);

        et_email = findViewById(R.id.et_mail);
        et_password = findViewById(R.id.et_password);
        et_username = findViewById(R.id.et_username);



        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this ,MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                et_email.setText("");
                et_password.setText("");
                et_username.setText("");
            }
        });



        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    registerUser();
            }
        });


    }

    private void register(String username, String email, String password) {
        if(username.isEmpty()){
            et_username.setError("Korisničko ime već postoji");
            et_username.requestFocus();
            return;
        }
        else if(username.contains(".") || username.contains("#") || username.contains("$") || username.contains("[") || username.contains("]")){
            et_username.setError("Korisničko ime ne smije sadržavati '.', '#', '$', '[', ili ']'");
            et_username.requestFocus();
            return;
        }
        else{



            progressBar.setVisibility(View.VISIBLE);
            auth.createUserWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if(task.isSuccessful()){
                                dataref.child(auth.getCurrentUser().getUid()).setValue(new User(username,email)).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        if(task.isSuccessful()){
                                            Toast.makeText(RegisterActivity.this, "Korisnik je uspješno registriran", Toast.LENGTH_LONG).show();
                                            progressBar.setVisibility(View.GONE);
                                            et_email.setText("");
                                            et_password.setText("");
                                            et_username.setText("");

                                            FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
                                                @Override
                                                public void onComplete(@NonNull Task<String> task) {
                                                    if(task.isSuccessful()){
                                                        String token = task.getResult();
                                                        FirebaseDatabase.getInstance().getReference("Tokens").child(username).setValue(token);
                                                    }


                                                }
                                            });

                                            Intent intent = new Intent(RegisterActivity.this ,MainActivity.class);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                            startActivity(intent);
                                        }
                                        else{
                                            Toast.makeText(RegisterActivity.this, "Korisnik je neuspješno registriran", Toast.LENGTH_LONG).show();
                                            progressBar.setVisibility(View.GONE);
                                            et_email.setText("");
                                            et_password.setText("");
                                            et_username.setText("");
                                        }
                                    }
                                });
                            }
                            else{
                                Toast.makeText(RegisterActivity.this, "Korisnik je neuspješno registriran", Toast.LENGTH_LONG).show();
                                progressBar.setVisibility(View.GONE);
                                et_email.setText("");
                                et_password.setText("");
                                et_username.setText("");
                            }



                        }
                    });
        }

    }



    private void registerUser(){
        final String username = et_username.getText().toString().trim();
        final String password = et_password.getText().toString().trim();
        final String email = et_email.getText().toString().trim();

        if(username.isEmpty()){
            et_username.setError("Korisničko ime je obavezno");
            et_username.requestFocus();
            return;
        }

        else if(password.isEmpty()){
            et_password.setError("Lozinka je obavezna");
            et_password.requestFocus();
            return;
        }

        else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            et_email.setError("Nepravilan unos email-a");
            et_email.requestFocus();
            return;
        }

        else if(username.isEmpty()){
            et_username.setError("Korisničko ime je obavezno");
            et_username.requestFocus();
            return;
        }

        else if(password.length() < 6){
            et_password.setError("Lozinka treba imati barem 6 znakova");
            et_password.requestFocus();
            return;
        }

        else {

            dataListener = new ValueEventListener() {
                String new_name = username;
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for(DataSnapshot current:snapshot.getChildren()){
                        if(current.exists()){
                            if(current.getValue(User.class).getUsername().equals(new_name))
                                new_name = "";
                        }

                    }

                    register(new_name,email,password);
                    dataref.removeEventListener(dataListener);

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(RegisterActivity.this, "Nešto ne valja", Toast.LENGTH_LONG).show();
                }
            };
            dataref.addListenerForSingleValueEvent(dataListener);

        }

    }



}