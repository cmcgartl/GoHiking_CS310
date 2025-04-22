package com.example.gohiking_cs310;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

/**
 * Activity to handle user authentication through firebase
 * Takes user email and password and authenticates credentials
 * using Firebase's Authentication
 */
public class EmailPasswordActivity extends AppCompatActivity {

    // Firebase Authentication instance to handle login and registration
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize FirebaseAuth instance
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Optional check: can be used to determine if a user is already logged in
        FirebaseUser currentUser = mAuth.getCurrentUser();
    }

    /**
     * Logs in an existing user using Firebase Authentication.
     * If successful, navigates to the MapsActivity.
     */
    private void loginUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Successful login: get current user and navigate to app home
                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(EmailPasswordActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(EmailPasswordActivity.this, MapsActivity.class);
                            startActivity(intent);
                            finish(); // End this activity so user can't go back by pressing Back
                        } else {
                            // Handle login failure and show error message
                            Toast.makeText(EmailPasswordActivity.this, "Login failed. " + task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    /**
     * Registers a new user account using Firebase Authentication.
     * If successful, the user is created in Firebase and logged in automatically.
     *
     */
    private void registerUser(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Account creation succeeded; user is now authenticated
                            FirebaseUser user = mAuth.getCurrentUser();
                            // You may optionally navigate or show a success message here
                        } else {
                            // Handle registration failure
                            Toast.makeText(EmailPasswordActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}