package com.example.gohiking_cs310;

import android.content.Intent;
import android.os.Bundle;
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


// Login.java
// Activity to authenticate a returning user using Firebase Authentication.
public class Login extends AppCompatActivity {
    private EditText editTextUsername, editTextPassword;
    private Button buttonLogin;
    private FirebaseAuth mAuth; // Firebase Authentication instance

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Reference UI elements
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        Button goHomeButton = findViewById(R.id.button_go_home);

        // Allow users to navigate to the homepage (MapsActivity) without logging in
        goHomeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Login.this, MapsActivity.class);
                startActivity(intent);
            }
        });

        // Attempt login when the "Login" button is pressed
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = editTextUsername.getText().toString();
                String password = editTextPassword.getText().toString();

                // Validate that both fields are filled
                if (!email.isEmpty() && !password.isEmpty()) {
                    loginUser(email, password); // Proceed to login
                } else {
                    Toast.makeText(Login.this, "Please enter email and password", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Logs in the user via Firebase Authentication.
     * Navigates to the UserActivity on success.
     * Displays a toast message on failure.
     */
    private void loginUser(String email, String password) {
        // Test hook increment: useful for instrumentation test synchronization
        if (TestEnvironment.testHooks != null) TestEnvironment.testHooks.increment();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        // Test hook decrement: marks async Firebase call as complete
                        if (TestEnvironment.testHooks != null) TestEnvironment.testHooks.decrement();

                        if (task.isSuccessful()) {
                            // Login success: navigate to user profile page
                            FirebaseUser user = mAuth.getCurrentUser();
                            ToastUtil.showToast(Login.this, "Login successful!");
                            Intent intent = new Intent(Login.this, UserActivity.class);
                            startActivity(intent);
                            finish(); // Prevent user from going back to login screen
                        } else {
                            // Login failed: show detailed error message
                            ToastUtil.showToast(Login.this, "Login failed: " + task.getException().getMessage());
                        }
                    }
                });
    }
}