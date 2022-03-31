package com.example.primjer_prijave.RegistrationLogin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.primjer_prijave.R;
import com.example.primjer_prijave.SearchUsers.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private String username, Password;
    private DatabaseReference refUser;
    private ProgressBar progressBar;
    private FirebaseAuth auth;
    private TextView tv_forgot_pass;
    private TextView tv_register;
    private EditText et_email;
    private EditText et_password;
    private Button btn_login;
    private ValueEventListener userListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        InitializeUI();
    }

    private void InitializeUI() {
        tv_forgot_pass = findViewById(R.id.tv_forgot_pass);
        tv_register = findViewById(R.id.tv_register);

        refUser = FirebaseDatabase.getInstance().getReference("Users");
        auth = FirebaseAuth.getInstance();
        btn_login = findViewById(R.id.btn_login);
        et_email = findViewById(R.id.et_mail);
        et_password = findViewById(R.id.et_password);
        progressBar = findViewById(R.id.progressBar);

        tv_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this ,RegisterActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                finish();
                startActivity(intent);
            }
        });

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userLogin();
            }
        });

        tv_forgot_pass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this ,ResetPasswordActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                finish();
                startActivity(intent);
            }
        });

    }

    private void goToUserProfile(){
        FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    FirebaseDatabase.getInstance().getReference("Passwords").child(snapshot.getValue(User.class).getUsername()).setValue(Password);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        Intent intent = new Intent(MainActivity.this , UserProfileActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("type","login");
        finish();
        startActivity(intent);

        et_email.setText("");
        et_password.setText("");
    }

    private void getToken(FirebaseUser user){
        userListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    User user = snapshot.getValue(User.class);
                    username = user.getUsername();
                    FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
                        @Override
                        public void onComplete(@NonNull Task<String> task) {
                            if(task.isSuccessful()){
                                String token = task.getResult();
                                FirebaseDatabase.getInstance().getReference("Tokens").child(username).setValue(token);
                            }
                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                            refUser.child(user.getUid()).removeEventListener(userListener);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        refUser.child(user.getUid()).addValueEventListener(userListener);
    }

    private void login(String email, String password){
        Password = password;
        progressBar.setVisibility(View.VISIBLE);
        auth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                    if(user.isEmailVerified()){
                        getToken(user);
                        goToUserProfile();
                    }
                    else{
                        user.sendEmailVerification();
                        Toast.makeText(MainActivity.this, "Niste potvrdili svoj e-mail. Provjerite svoju e-mail adresu da potvrdite svoj e-mail.", Toast.LENGTH_LONG).show();
                        et_email.setText("");
                        et_password.setText("");
                    }

                    progressBar.setVisibility(View.GONE);

                }
                else{
                    Toast.makeText(MainActivity.this, "Neuspjela prijava! Molimo pokušajte ponovo",Toast.LENGTH_LONG).show();
                    progressBar.setVisibility(View.GONE);

                }
            }
        });
    }

    private void userLogin() {
        String email = et_email.getText().toString().trim();
        String password = et_password.getText().toString().trim();

        if(email.isEmpty()){
            et_email.setError("E-mail adresa je nužna");
            et_email.requestFocus();
            return;
        }

        else if(password.isEmpty()){
            et_password.setError("Lozinka je nužna");
            et_password.requestFocus();
            return;
        }

        else if(password.length() < 6){
            et_password.setError("Lozinka treba imati barem 6 znakova");
            et_password.requestFocus();
            return;
        }


        else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            et_email.setError("Nepravilan unos email-a");
            et_email.requestFocus();
            return;
        }

        else login(email,password);

    }


}