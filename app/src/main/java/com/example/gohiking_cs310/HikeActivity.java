package com.example.gohiking_cs310;



import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class HikeActivity extends AppCompatActivity {
    private FirebaseFirestore db;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hikeinfo);
        db = FirebaseFirestore.getInstance();
        Intent intent = getIntent();
        Hike hike = (Hike)getIntent().getSerializableExtra("hikeObject");
        TextView hikeTitleTextView = findViewById(R.id.text_hike_name);
        if (hike != null) {
            Log.d("HikeActivity", "Hike details loaded: " + hike.getName());
            hikeTitleTextView.setText(hike.getName());
        } else {
            Log.e("HikeActivity", "Hike object is null!");
        }

        Button showDetails = findViewById(R.id.buttonShowDetails);
        showDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showHikeInfo(hike);

            }
        });

        Button addHike = findViewById(R.id.buttonAddHike);
        addHike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addCustomHike(hike);
            }
        });

        Button backToProfile = findViewById(R.id.buttonBackToProfile);
        backToProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HikeActivity.this, UserActivity.class);
                startActivity(intent);
            }
        });

        Button reviewButton = findViewById(R.id.buttonReview);
        reviewButton.setOnClickListener(v -> {
            Intent intent2 = new Intent(HikeActivity.this, ReviewActivity.class);
            assert hike != null;
            intent2.putExtra("hikeId", hike.getId()); // Ensure hike.getId() is not null
            startActivity(intent2);
        });

    }

    public void showHikeInfo(Hike hike) {
        if (hike == null) {
            Log.e("HikeActivity", "Hike object is null!");
            return;
        }
        StringBuilder hikeDetails = new StringBuilder("Difficulty: " + hike.getDifficulty() + "\n" + "Trail Conditions: ");
        if (hike.getTrailConditions() != "") hikeDetails.append(hike.getTrailConditions()).append("\n");
        else hikeDetails.append("Trail Conditions not available.\n");
        if (hike.getRatings() != null && !hike.getRatings().isEmpty()) {
            int numRatings = hike.getRatings().size();
            double totalRating = 0;
            for (Number rating : hike.getRatings()) {
                if (rating instanceof Long) {
                    totalRating += rating.longValue();
                } else if (rating instanceof Double) {
                    totalRating += rating.doubleValue();
                }
            }
            double averageRating = totalRating / numRatings;
            hikeDetails.append("Average Rating: ").append(averageRating).append("\n");
        }
        else hikeDetails.append("No ratings yet.\n");
        hikeDetails.append("Reviews: ");
        if (hike.getReviews() != null && !hike.getReviews().isEmpty()) {
            hikeDetails.append("\n");
            for (String review : hike.getReviews()) {
                hikeDetails.append(review).append("\n");
            }
        }
        else hikeDetails.append("No reviews yet.\n");
        hikeDetails.append("Amenities: \n" +
                "Bathrooms: " + (hike.isBathrooms() ? "Yes" : "No") + "\n" +
                "Parking: " + (hike.isParking() ? "Yes" : "No") + "\n" +
                "Trail Markers: " + (hike.isTrailMarkers() ? "Yes" : "No") + "\n" +
                "Trash Cans: " + (hike.isTrashCans() ? "Yes" : "No") + "\n" +
                "Water Fountains: " + (hike.isWaterFountains() ? "Yes" : "No") + "\n" +
                "WiFi: " + (hike.isWifi() ? "Yes" : "No") + "\n");


        new AlertDialog.Builder(HikeActivity.this)
                .setTitle(hike.getName())
                .setMessage(hikeDetails.toString())
                .setPositiveButton("Close", (dialog, which) -> dialog.dismiss())
                .show();
    }

    public void addCustomHike(Hike hike) {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Log.d("HikeActivity", "Current User ID: " + currentUserId);
        if (hike == null) {
            Log.d("HikeActivity", "Hike object is null, cannot add to custom hikes.");
            Toast.makeText(HikeActivity.this, "Hike information is missing.", Toast.LENGTH_SHORT).show();
            return;
        }
        db.collection("Users").document(currentUserId)
                .update("customHikes", FieldValue.arrayUnion(hike.getName()))
                .addOnSuccessListener(aVoid -> {
                    Log.d("HikeActivity", hike.getName() + " successfully added to custom hikes list.");
                    Toast.makeText(HikeActivity.this, hike.getName() + " added to custom hikes list", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e("HikeActivity", "Failed to add hike to custom hikes list: " + e.getMessage());
                    Toast.makeText(HikeActivity.this, "Failed to add hike.", Toast.LENGTH_SHORT).show();
                });
    }
}
