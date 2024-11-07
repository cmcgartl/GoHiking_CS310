package com.example.gohiking_cs310;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class SignUp extends AppCompatActivity {
    private EditText editTextUsername, editTextPassword;
    private Button buttonSignUp;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mAuth = FirebaseAuth.getInstance();
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonSignUp = findViewById(R.id.buttonSignUp);

        // Go to home button
        Button goHomeButton = findViewById(R.id.button_go_home);
        goHomeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignUp.this, MapsActivity.class);
                startActivity(intent);
            }
        });

        // Set up Sign-Up button click listener
        buttonSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = editTextUsername.getText().toString();
                EditText editfullname = findViewById(R.id.username);
                String username = editfullname.getText().toString();
                String password = editTextPassword.getText().toString();

                if (!email.isEmpty() && !password.isEmpty() && !username.isEmpty()) {
                    registerUser(email, password, username);

                } else {
                    Toast.makeText(SignUp.this, "Please enter email and password", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void registerUser(String email, String password, String username) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign-up successful, navigate to MapsActivity
                            FirebaseUser user = mAuth.getCurrentUser();
                            String userID = mAuth.getCurrentUser().getUid();
                            User newUser = new User(userID, email, username);
                            //newUser.username = username;
                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            db.collection("Users").document(userID).set(newUser);
                            Toast.makeText(SignUp.this, "Registration successful!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(SignUp.this, MapsActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(SignUp.this, "Authentication failed. " + task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void reload() {
        // Reload or update UI for the logged-in user if needed
    }
}