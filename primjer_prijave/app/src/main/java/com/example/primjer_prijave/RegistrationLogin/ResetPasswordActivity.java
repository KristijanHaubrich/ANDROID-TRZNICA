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
import android.widget.Toast;

import com.example.primjer_prijave.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ResetPasswordActivity extends AppCompatActivity {

    private Button back;
    private FirebaseAuth auth;
    private EditText et_email;
    private Button btn_reset;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        InitializeUI();

    }

    private void InitializeUI() {
        back = findViewById(R.id.btn_back);
        et_email = findViewById(R.id.et_email);
        btn_reset = findViewById(R.id.btn_reset);
        progressBar = findViewById(R.id.progressBar);
        auth = FirebaseAuth.getInstance();

        btn_reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetPassword();
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(ResetPasswordActivity.this , MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                finish();
                startActivity(intent);
            }
        });
    }

    private void resetPassword() {
        String email = et_email.getText().toString().trim();

        if(email.isEmpty()){
                et_email.setError("E-mail adresa je nužna");
                et_email.requestFocus();
                return;
        }

        else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            et_email.setError("Nepravilan unos e-mail adrese");
            et_email.requestFocus();
            return;
        }

        else{
            progressBar.setVisibility(View.VISIBLE);
            auth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                    if(task.isSuccessful()){
                        Toast.makeText(ResetPasswordActivity.this,"Provjerite email za promjenu lozinke",Toast.LENGTH_LONG).show();
                        progressBar.setVisibility(View.GONE);
                        Intent intent = new Intent(ResetPasswordActivity.this ,MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);

                    }
                    else{
                        Toast.makeText(ResetPasswordActivity.this,"Nešto ne valja",Toast.LENGTH_LONG).show();
                    }

                }
            });
        }

    }


}