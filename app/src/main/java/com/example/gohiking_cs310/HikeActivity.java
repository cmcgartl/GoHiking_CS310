package com.example.gohiking_cs310;



import android.app.AlertDialog;
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
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class HikeActivity extends AppCompatActivity {
    FirebaseFirestore db;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hikeinfo);
        db = FirebaseFirestore.getInstance();
        Intent intent = getIntent();
        Hike hike = (Hike) getIntent().getSerializableExtra("hikeObject");
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
                EditText AddHike = findViewById(R.id.editTextAddHike);
                String listToUpdate = AddHike.getText().toString().trim();
                addCustomHike(hike,listToUpdate);
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
            intent2.putExtra("hikeId", hike.getId());
            intent2.putExtra("Hike", hike);// Ensure hike.getId() is not null
            startActivity(intent2);
        });

    }

    public void showHikeInfo(Hike hike) {
        if (hike == null) {
            Log.e("HikeActivity", "Hike object is null!");
            return;
        }
        List<Review> hikeReviews = new ArrayList<>();
        db.collection("Hikes").document(hike.getId()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    List<Map<String, Object>> hikeReviewsMap = ( List<Map<String, Object>>) documentSnapshot.get("Reviews");
                    if (hikeReviewsMap != null) {
                        for (Map<String, Object> reviewMap : hikeReviewsMap) {
                            String reviewText = (String) reviewMap.get("reviewText");
                            Number ratingNumber = (Number) reviewMap.get("rating");
                            long rating = ratingNumber != null ? ratingNumber.longValue() : 0;
                            String userId = (String) reviewMap.get("userId");
                            hikeReviews.add(new Review(reviewText, rating, userId));
                        }
                    }
                    StringBuilder hikeDetails = new StringBuilder("Difficulty: " + hike.getDifficulty() + "\n" + "Trail Conditions: ");
                    if (hike.getTrailConditions() != "") {
                        hikeDetails.append(hike.getTrailConditions()).append("\n");
                    }
                    else {hikeDetails.append("Trail Conditions not available.\n");}
                    if (!hikeReviews.isEmpty()) {
                        int numRatings = hikeReviews.size();
                        double totalRating = 0;
                        for (Review review : hikeReviews) {
                            totalRating += review.getRating();
                        }
                        double averageRating = totalRating / numRatings;
                        hikeDetails.append("Average Rating: ").append(averageRating).append("\n");
                        hikeDetails.append("Reviewed by: " + numRatings + " users").append("\n");
                    } else {
                        hikeDetails.append("No reviews for this hike yet.\n");
                    }
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
                });
    }
    public void addCustomHike(Hike hike, String listName) {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (hike == null || listName == null) {
            Toast.makeText(HikeActivity.this, "Hike or list information is missing.", Toast.LENGTH_SHORT).show();
            return;
        }
        db.collection("Users").document(currentUserId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Map<String, List<String>> customList = (Map<String, List<String>>) documentSnapshot.get("customList");
                        if (customList == null || !customList.containsKey(listName)) {
                            Log.d("HikeActivity", "Hike list '" + listName + "' does not exist in user's custom lists.");
                            Toast.makeText(HikeActivity.this, "Hike list '" + listName + "' does not exist.", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        List<String> hikeList = customList.get(listName);
                        if (!hikeList.contains(hike.getName())) {
                            hikeList.add(hike.getName());
                            db.collection("Users").document(currentUserId)
                                    .update("customList", customList)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(HikeActivity.this, hike.getName() + " added to " + listName, Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(HikeActivity.this, "Failed to update hike list.", Toast.LENGTH_SHORT).show();
                                    });
                        } else {
                            Toast.makeText(HikeActivity.this, hike.getName() + " is already in " + listName, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(HikeActivity.this, "Failed to find user information.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(HikeActivity.this, "Failed to fetch user information.", Toast.LENGTH_SHORT).show();
                });
    }
}
