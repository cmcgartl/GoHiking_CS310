package com.example.gohiking_cs310;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * SignUp handles user registration via Firebase Authentication.
 * After successful sign-up, a user document is added to Firestore and the user is redirected to the main map screen.
 */
public class SignUp extends AppCompatActivity {

    private EditText editTextUsername, editTextPassword;
    private Button buttonSignUp;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Link UI components to variables
        editTextUsername = findViewById(R.id.editTextUsername);  // Email input
        editTextPassword = findViewById(R.id.editTextPassword);  // Password input
        buttonSignUp = findViewById(R.id.buttonSignUp);          // Register button

        // Go Home button: navigates to the main map without signing up
        Button goHomeButton = findViewById(R.id.button_go_home);
        goHomeButton.setOnClickListener(v -> {
            Intent intent = new Intent(SignUp.this, MapsActivity.class);
            startActivity(intent);
        });

        // Sign-Up button click listener: triggers user registration
        buttonSignUp.setOnClickListener(v -> {
            String email = editTextUsername.getText().toString();
            EditText editfullname = findViewById(R.id.username);  // User-entered display name
            String username = editfullname.getText().toString();
            String password = editTextPassword.getText().toString();

            // Basic input validation
            if (!email.isEmpty() && !password.isEmpty() && !username.isEmpty()) {
                registerUser(email, password, username);
            } else {
                Toast.makeText(SignUp.this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Registers a new user with Firebase Authentication,
     * creates a corresponding Firestore User document on success,
     * and redirects to the map screen.
     */
    private void registerUser(String email, String password, String username) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // User registration succeeded
                        FirebaseUser user = mAuth.getCurrentUser();
                        String userID = user.getUid();

                        // Create custom user object to store in Firestore
                        User newUser = new User(userID, email, username);

                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        db.collection("Users").document(userID).set(newUser); // Store user info in "Users" collection

                        Toast.makeText(SignUp.this, "Registration successful!", Toast.LENGTH_SHORT).show();

                        // Navigate to main map screen
                        Intent intent = new Intent(SignUp.this, MapsActivity.class);
                        startActivity(intent);
                        finish(); // Finish signup activity to prevent back navigation
                    } else {
                        // Handle registration failure and show error
                        Toast.makeText(SignUp.this, "Authentication failed. " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}