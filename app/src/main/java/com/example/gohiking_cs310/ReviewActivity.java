package com.example.gohiking_cs310;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.gohiking_cs310.Review;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReviewActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private String hikeId;
    private TextView reviewListTextView;
    RatingBar ratingBar;
    EditText reviewEditText;
    ArrayList<String> reviews = new ArrayList<>();
    ArrayList<Long> ratings = new ArrayList<>();
    private ListView reviewListView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        reviewListView = findViewById(R.id.review_list_view);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, reviews);
        reviewListView.setAdapter(adapter);

        reviewListView.setOnItemClickListener((parent, view, position, id) -> showReviewDetails(position));
        db = FirebaseFirestore.getInstance();
        hikeId = getIntent().getStringExtra("hikeId");
        reviewListView = findViewById(R.id.review_list_view);
        ratingBar = findViewById(R.id.rating_bar);
        reviewEditText = findViewById(R.id.review_edit_text);
        Button submitButton = findViewById(R.id.submit_review_button);
        TextView title = findViewById(R.id.textView3);
        db.collection ("Hikes").document(hikeId).get()
                        .addOnSuccessListener(documentSnapshot -> {
                            String hikeName = (String) documentSnapshot.get("Name");
                            title.setText("Welcome To The " + hikeName + " Review Page!");
                        });
        Button backToProfile = findViewById(R.id.buttonBackToProfile);
        backToProfile.setOnClickListener(v -> {
            Intent intent = new Intent(ReviewActivity.this, UserActivity.class);
            startActivity(intent);
        });

        Button backHome = findViewById(R.id.buttonBackToHome);
        backHome.setOnClickListener(v -> {
            Intent intent = new Intent(ReviewActivity.this, MapsActivity.class);
            startActivity(intent);
        });

        Button logoutButton = findViewById(R.id.buttonLogout);
        logoutButton.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut(); // Log out the current user
            Toast.makeText(ReviewActivity.this, "Logged out successfully.", Toast.LENGTH_SHORT).show();

            // go back to MapsActivity
            Intent intent = new Intent(ReviewActivity.this, MapsActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        loadReviews();

        submitButton.setOnClickListener(v -> submitReview());
    }

   /* void loadReviews() {
        db.collection("Hikes").document(hikeId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                reviews = (ArrayList<String>) documentSnapshot.get("Reviews");
                ratings = (ArrayList<Long>) documentSnapshot.get("Ratings");

                String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                for (int i = 0; i < reviews.size(); i++) {
                    String[] parts = reviews.get(i).split(":", 2);
                    if (parts.length > 1 && parts[0].equals(currentUserId)) {
                        reviewEditText.setText(parts[1].trim());
                        ratingBar.setRating(ratings.get(i));
                        userReviewIndex = i;
                        break;
                    }
                }
                displayReviews();
                displayAverageRating();
            }
        });
    }*/

    void loadReviews() {
        db.collection("Hikes").document(hikeId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                List<Map<String, Object>> reviewMaps = (List<Map<String, Object>>) documentSnapshot.get("Reviews");
                reviews.clear();
                ratings.clear();

                if (reviewMaps != null) {
                    for (Map<String, Object> reviewMap : reviewMaps) {
                        String reviewText = (String) reviewMap.get("reviewText");
                        long rating = (long) reviewMap.get("rating");
                        String userId = (String) reviewMap.get("userId");

                        reviews.add(reviewText + " (Rating: " + rating + "/5)");
                        ratings.add(rating);
                    }
                }

                // Notify the adapter about the updated data
                ArrayAdapter<String> adapter = (ArrayAdapter<String>) reviewListView.getAdapter();
                adapter.notifyDataSetChanged();

                // Update the average rating
                displayAverageRating();
            }
        });
    }

    void showReviewDetails(int position) {
        // Retrieve the clicked review's details
        db.collection("Hikes").document(hikeId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    List<Map<String, Object>> reviewMaps = (List<Map<String, Object>>) documentSnapshot.get("Reviews");
                    if (reviewMaps != null && position < reviewMaps.size()) {
                        Map<String, Object> reviewMap = reviewMaps.get(position);
                        String userId = (String) reviewMap.get("userId");

                        if (userId != null) {
                            // Fetch the user's details from Firestore
                            db.collection("Users").document(userId).get()
                                    .addOnSuccessListener(userSnapshot -> {
                                        String username = (String) userSnapshot.get("username");

                                        // Build AlertDialog
                                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                                        builder.setTitle("Review Details");

                                        // Options to display
                                        String[] options = {
                                                "Review submitted by " + (username != null ? username : "Unknown User"),
                                                "View " + (username != null ? username : "User") + "'s reviews"
                                        };

                                        builder.setItems(options, (dialog, which) -> {
                                            if (which == 1) { // Second option selected
                                                showUserRatings(username != null ? username : "Unknown User", userId);
                                            }
                                        });

                                        builder.setPositiveButton("Close", (dialog, which) -> dialog.dismiss());
                                        builder.show();
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("ReviewActivity", "Failed to fetch user details", e);
                                        Toast.makeText(this, "Failed to fetch user details.", Toast.LENGTH_SHORT).show();
                                    });
                        } else {
                            Log.e("ReviewActivity", "User ID not found for review at position: " + position);
                            Toast.makeText(this, "User ID not found for this review.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e("ReviewActivity", "No reviews found or invalid position.");
                        Toast.makeText(this, "Review details unavailable.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("ReviewActivity", "Failed to fetch hike document", e);
                    Toast.makeText(this, "Failed to fetch hike details.", Toast.LENGTH_SHORT).show();
                });
    }

    void displayReviews() {
        StringBuilder reviewsText = new StringBuilder();
        reviewsText.append("User Reviews and Ratings: \n\n");
        int i = 0;
        if (reviews != null && !reviews.isEmpty()) {
            for (String review : reviews) {
                String displayReview = review.contains(":") ? review.substring(review.indexOf(":") + 1).trim() : review;

                if (i < ratings.size()) {
                    reviewsText.append("- ").append("\"").append(displayReview).append("\"").append(" (").append(ratings.get(i)).append("/5)").append("\n");
                } else {
                    reviewsText.append("- ").append("\"").append(displayReview).append("\"").append("\n");
                }
                i++;
            }
        } else {
            reviewsText.append("No reviews yet UPDATED.");
        }
        reviewListTextView.setText(reviewsText.toString());
    }


    void displayAverageRating() {
        if (ratings != null && !ratings.isEmpty()) {
            Long total = 0L;
            for (Long rating : ratings) {
                total += rating;
            }
            long averageRating = total / ratings.size();
            String averageRatingText = "Average Rating: " + averageRating + "/5";
            ((TextView) findViewById(R.id.average_rating_text_view)).setText(averageRatingText);
        } else {
            ((TextView) findViewById(R.id.average_rating_text_view)).setText("No ratings yet.");
        }
    }

    void submitReview() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        String reviewText = reviewEditText.getText().toString().trim();
        float rating = ratingBar.getRating();
        String currentUserId = user.getUid();

        if (!reviewText.isEmpty() && rating > 0) {
            db.collection("Hikes").document(hikeId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        String hikeName = documentSnapshot.getString("Name"); // Assuming "name" field exists

                        Review review = new Review(reviewText, (long) rating, currentUserId);
                        Map<String, Object> reviewMap = new HashMap<>();
                        reviewMap.put("reviewText", review.getReviewText());
                        reviewMap.put("rating", review.getRating());
                        reviewMap.put("userId", review.getUserId());
                        reviewMap.put("hikeId", hikeId);
                        reviewMap.put("hikeName", hikeName); // Add hike name here

                        // Update hike reviews
                        db.collection("Hikes").document(hikeId)
                                .update("Reviews", FieldValue.arrayUnion(review))
                                .addOnSuccessListener(aVoid -> {
                                    // Update user reviews
                                    db.collection("Users").document(currentUserId)
                                            .update("userReviews", FieldValue.arrayUnion(reviewMap))
                                            .addOnSuccessListener(innerVoid -> {
                                                Toast.makeText(this, "Review submitted!", Toast.LENGTH_SHORT).show();
                                                reviewEditText.setText("");
                                                ratingBar.setRating(0);
                                                loadReviews();
                                            })
                                            .addOnFailureListener(e -> Log.e("ReviewActivity", "Failed to update user reviews", e));
                                })
                                .addOnFailureListener(e -> Log.e("ReviewActivity", "Failed to update hike reviews", e));
                    })
                    .addOnFailureListener(e -> Log.e("ReviewActivity", "Failed to fetch hike details", e));
        } else {
            Toast.makeText(this, "Please provide both a review and rating", Toast.LENGTH_SHORT).show();
        }
    }

    private void showUserRatings(String friendEmail, String userId) {
        db.collection("Users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    List<Map<String, Object>> reviewList = (List<Map<String, Object>>) documentSnapshot.get("userReviews");
                    StringBuilder userReviews = new StringBuilder();

                    if (reviewList == null || reviewList.isEmpty()) {
                        userReviews.append("No reviews available for this user.");
                    } else {
                        for (Map<String, Object> reviewMap : reviewList) {
                            String hikeName = (String) reviewMap.get("hikeName");
                            String reviewText = (String) reviewMap.get("reviewText");
                            Number ratingNumber = (Number) reviewMap.get("rating");
                            Double rating = ratingNumber != null ? ratingNumber.doubleValue() : null;

                            if (hikeName != null && reviewText != null && rating != null) {
                                userReviews.append("Hike Name: ").append(hikeName)
                                        .append("\nReview: ").append(reviewText)
                                        .append("\nRating: ").append(rating)
                                        .append("\n\n");
                            }
                        }
                    }

                    new AlertDialog.Builder(ReviewActivity.this)
                            .setTitle(friendEmail + "'s reviews")
                            .setMessage(userReviews.toString())
                            .setPositiveButton("Close", (dialog, which) -> dialog.dismiss())
                            .show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ReviewActivity.this, "Failed to fetch user reviews.", Toast.LENGTH_SHORT).show();
                    Log.e("ReviewActivity", "Error fetching user reviews", e);
                });
    }
}
