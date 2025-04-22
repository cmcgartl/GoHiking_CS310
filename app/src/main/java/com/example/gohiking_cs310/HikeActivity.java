package com.example.gohiking_cs310;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * HikeActivity displays detailed information about a specific hike and allows users
 * to interact with that hike, including adding it to a custom list and submitting/viewing reviews.
 * This activity is launched with a Hike object passed through an intent.
 */
public class HikeActivity extends AppCompatActivity {

    // Firebase Firestore database instance
    FirebaseFirestore db;

    // Firebase Authentication instance
    public FirebaseAuth auth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hikepage); // Set UI layout for the hike page
        db = FirebaseFirestore.getInstance();

        // Receive hike data passed from the previous activity
        Hike hike = (Hike) getIntent().getSerializableExtra("hikeObject");
        TextView hikeTitleTextView = findViewById(R.id.textView4);

        // Update hike title text
        if (hike != null) {
            Log.d("HikeActivity", "Hike details loaded: " + hike.getName());
            hikeTitleTextView.setText("Welcome To The " + hike.getName() + " Info Page!");
        } else {
            Log.e("HikeActivity", "Hike object is null!");
        }

        // Button: View detailed hike info
        findViewById(R.id.buttonShowDetails).setOnClickListener(v -> showHikeInfo(hike));

        // Button: Add hike to a custom list
        findViewById(R.id.buttonAddHike).setOnClickListener(v -> {
            String listToUpdate = ((EditText) findViewById(R.id.editTextAddHike)).getText().toString().trim();
            addCustomHike(hike, listToUpdate);
        });

        // Button: Navigate to profile
        findViewById(R.id.buttonBackToProfile).setOnClickListener(v -> {
            startActivity(new Intent(HikeActivity.this, UserActivity.class));
            finish();
        });

        // Button: Navigate to map/home page
        findViewById(R.id.buttonBackHome).setOnClickListener(v -> {
            startActivity(new Intent(HikeActivity.this, MapsActivity.class));
            finish();
        });

        // Button: Open review page
        findViewById(R.id.buttonReview).setOnClickListener(v -> {
            if (hike != null) {
                Intent intent = new Intent(HikeActivity.this, ReviewActivity.class);
                intent.putExtra("hikeId", hike.getId());
                startActivity(intent);
            }
        });

        // Button: Logout user
        findViewById(R.id.buttonLogout).setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(this, "Logged out successfully.", Toast.LENGTH_SHORT).show();
            Intent logoutIntent = new Intent(this, MapsActivity.class);
            logoutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(logoutIntent);
            finish();
        });
    }

    /**
     * Displays detailed information about the selected hike including amenities and reviews.
     * Data is retrieved asynchronously from Firestore.
     */
    public void showHikeInfo(Hike hike) {
        if (hike == null) {
            Log.e("HikeActivity", "Hike object is null!");
            return;
        }

        List<Review> hikeReviews = new ArrayList<>();

        // Fetch hike document and reviews from Firestore
        db.collection("Hikes").document(hike.getId()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    List<Map<String, Object>> reviewMaps = (List<Map<String, Object>>) documentSnapshot.get("Reviews");

                    if (reviewMaps != null) {
                        for (Map<String, Object> reviewMap : reviewMaps) {
                            String reviewText = (String) reviewMap.get("reviewText");
                            Number ratingNumber = (Number) reviewMap.get("rating");
                            long rating = ratingNumber != null ? ratingNumber.longValue() : 0;
                            String userId = (String) reviewMap.get("userId");
                            hikeReviews.add(new Review(reviewText, rating, userId));
                        }
                    }

                    // Build display string for hike details
                    StringBuilder hikeDetails = new StringBuilder("Difficulty: " + hike.getDifficulty() + "\nTrail Conditions: ");
                    hikeDetails.append(!hike.getTrailConditions().isEmpty() ? hike.getTrailConditions() + "\n" : "Trail Conditions not available.\n");

                    if (!hikeReviews.isEmpty()) {
                        double totalRating = 0;
                        for (Review review : hikeReviews) totalRating += review.getRating();
                        double averageRating = totalRating / hikeReviews.size();
                        hikeDetails.append("Average Rating: ").append(averageRating).append("\nReviewed by: ").append(hikeReviews.size()).append(" users\n");
                    } else {
                        hikeDetails.append("No reviews for this hike yet.\n");
                    }

                    hikeDetails.append("Amenities: \n")
                            .append("Bathrooms: ").append(hike.isBathrooms() ? "Yes" : "No").append("\n")
                            .append("Parking: ").append(hike.isParking() ? "Yes" : "No").append("\n")
                            .append("Trail Markers: ").append(hike.isTrailMarkers() ? "Yes" : "No").append("\n")
                            .append("Trash Cans: ").append(hike.isTrashCans() ? "Yes" : "No").append("\n")
                            .append("Water Fountains: ").append(hike.isWaterFountains() ? "Yes" : "No").append("\n")
                            .append("WiFi: ").append(hike.isWifi() ? "Yes" : "No").append("\n");

                    new AlertDialog.Builder(HikeActivity.this)
                            .setTitle(hike.getName())
                            .setMessage(hikeDetails.toString())
                            .setPositiveButton("Close", (dialog, which) -> dialog.dismiss())
                            .show();
                });
    }

    /**
     * Adds the selected hike to a custom hike list associated with the current user.
     * Prevents duplicate entries and ensures list existence before update.
     */
    public void addCustomHike(Hike hike, String listName) {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        if (hike == null || listName == null) {
            Toast.makeText(this, "Hike or list information is missing.", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("Users").document(currentUserId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Map<String, List<String>> customList = (Map<String, List<String>>) documentSnapshot.get("customList");

                        if (customList == null || !customList.containsKey(listName)) {
                            Toast.makeText(this, "Hike list '" + listName + "' does not exist.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        List<String> hikeList = customList.get(listName);

                        if (!hikeList.contains(hike.getName())) {
                            hikeList.add(hike.getName());

                            db.collection("Users").document(currentUserId)
                                    .update("customList", customList)
                                    .addOnSuccessListener(aVoid ->
                                            Toast.makeText(this, hike.getName() + " added to " + listName, Toast.LENGTH_SHORT).show())
                                    .addOnFailureListener(e ->
                                            Toast.makeText(this, "Failed to update hike list.", Toast.LENGTH_SHORT).show());
                        } else {
                            Toast.makeText(this, hike.getName() + " is already in " + listName, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Failed to find user information.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to fetch user information.", Toast.LENGTH_SHORT).show());
    }
}