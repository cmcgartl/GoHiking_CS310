package com.example.gohiking_cs310;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PrivacyActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy);
        db.collection("Users").document(currentUserId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Map<String, Object> privacyMap = (Map<String, Object>) documentSnapshot.get("privacy");
                        if (privacyMap == null || !privacyMap.isEmpty()) {
                            Map<String, Object> temp = new HashMap<>();
                            temp.put("customHikesPublic", true);
                            temp.put("emailPublic", true);
                            temp.put("friendsPublic", true);
                            temp.put("groupActivitiesPublic", true);

                            db.collection("Users").document(currentUserId).update("privacy", temp)
                                    .addOnSuccessListener(aVoid -> Log.d("PrivacyMap", "Privacy map initialized with default values."))
                                    .addOnFailureListener(e -> Log.e("PrivacyMap", "Failed to initialize privacy map", e));
                        } else{
                            Log.d("PrivacyMap", "Privacy map already has values.");
                        }
                    } else{
                        Log.d("PrivacyMap", "User document does not exist.");
                    }
                });

        Button customHikes = findViewById(R.id.buttonCustomHikes);
        customHikes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

        Button email = findViewById(R.id.buttonEmail);
        email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

        Button friends = findViewById(R.id.buttonFriends);
        friends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

        Button groupActivities = findViewById(R.id.buttonGroupActivities);
        groupActivities.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
        loadSettings();
    }

    private void togglePrivacy(String privacyKey, Button button, String label){

    }

    private void setButtonText(Boolean isPublic, Button button, String label) {
        if (isPublic) {
            button.setText(label + ": Public");
        } else {
            button.setText(label + ": Private");
        }
    }

    private void loadSettings(){
        db.collection("Users").document(currentUserId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Map<String, Object> privacyMap = (Map<String, Object>) documentSnapshot.get("privacy");
                        if (privacyMap != null) {
                            setButtonText((Boolean)privacyMap.get("customHikesPublic"), findViewById(R.id.buttonCustomHikes), "Custom Hikes");
                            setButtonText((Boolean)privacyMap.get("emailPublic"), findViewById(R.id.buttonEmail), "Email");
                            setButtonText((Boolean)privacyMap.get("friendsPublic"), findViewById(R.id.buttonFriends), "Friends");
                            setButtonText((Boolean)privacyMap.get("groupActivitiesPublic"), findViewById(R.id.buttonGroupActivities), "Group Activities");
                        } else{
                            Log.d("PrivacyMap", "Privacy map does not exist.");
                        }
                    } else{
                        Log.d("PrivacyMap", "User document does not exist.");
                    }
                });
    }
}
