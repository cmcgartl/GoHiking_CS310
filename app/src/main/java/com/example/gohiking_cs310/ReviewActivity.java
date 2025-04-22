package com.example.gohiking_cs310;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ReviewActivity allows users to view and submit reviews for a specific hike.
 * It fetches hike info from Firebase Firestore, allows authenticated users to submit a review,
 * displays the list of all reviews and the average rating, and allows exploring other users' reviews.
 */
public class ReviewActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private String hikeId;

    private TextView reviewListTextView;
    private RatingBar ratingBar;
    private EditText reviewEditText;

    private ArrayList<String> reviews = new ArrayList<>();
    private ArrayList<Long> ratings = new ArrayList<>();

    private ListView reviewListView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        // Initialize UI components
        reviewListView = findViewById(R.id.review_list_view);
        ratingBar = findViewById(R.id.rating_bar);
        reviewEditText = findViewById(R.id.review_edit_text);
        Button submitButton = findViewById(R.id.submit_review_button);
        TextView title = findViewById(R.id.textView3);

        // Setup adapter to show review strings in the ListView
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, reviews);
        reviewListView.setAdapter(adapter);

        // Get Firestore instance and hikeId passed from previous activity
        db = FirebaseFirestore.getInstance();
        hikeId = getIntent().getStringExtra("hikeId");

        // Set dynamic title with hike name
        db.collection("Hikes").document(hikeId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    String hikeName = (String) documentSnapshot.get("Name");
                    title.setText("Welcome To The " + hikeName + " Review Page!");
                });

        // Handle review item click to show reviewer info
        reviewListView.setOnItemClickListener((parent, view, position, id) -> showReviewDetails(position));

        // Navigation buttons
        Button backToProfile = findViewById(R.id.buttonBackToProfile);
        backToProfile.setOnClickListener(v -> startActivity(new Intent(this, UserActivity.class)));

        Button backHome = findViewById(R.id.buttonBackToHome);
        backHome.setOnClickListener(v -> startActivity(new Intent(this, MapsActivity.class)));

        Button logoutButton = findViewById(R.id.buttonLogout);
        logoutButton.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(this, "Logged out successfully.", Toast.LENGTH_SHORT).show();

            // Redirect to MapsActivity, clearing back stack
            Intent intent = new Intent(this, MapsActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        // Load all reviews for the hike
        loadReviews(false);

        // Submit new review
        submitButton.setOnClickListener(v -> submitReview());
    }

    /**
     * Loads all reviews for the current hike.
     * Optionally interacts with test environment hook if triggered by a submit.
     */
    void loadReviews(boolean called_by_submit) {
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

                        // Format for display
                        reviews.add(reviewText + " (Rating: " + rating + "/5)");
                        ratings.add(rating);
                    }
                }

                // Notify adapter to refresh UI
                ArrayAdapter<String> adapter = (ArrayAdapter<String>) reviewListView.getAdapter();
                adapter.notifyDataSetChanged();

                // Update the average rating
                displayAverageRating(called_by_submit);
            }
        });
    }

    /**
     * When a review is clicked, show the user who submitted it
     * and allow exploring more reviews by that user.
     */
    void showReviewDetails(int position) {
        db.collection("Hikes").document(hikeId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    List<Map<String, Object>> reviewMaps = (List<Map<String, Object>>) documentSnapshot.get("Reviews");

                    if (reviewMaps != null && position < reviewMaps.size()) {
                        Map<String, Object> reviewMap = reviewMaps.get(position);
                        String userId = (String) reviewMap.get("userId");

                        if (userId != null) {
                            // Fetch user data to show their name
                            db.collection("Users").document(userId).get()
                                    .addOnSuccessListener(userSnapshot -> {
                                        String username = (String) userSnapshot.get("username");

                                        // Build a simple dialog with options
                                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                                        builder.setTitle("Review Details");
                                        String[] options = {
                                                "Review submitted by " + (username != null ? username : "Unknown User"),
                                                "View " + (username != null ? username : "User") + "'s reviews"
                                        };

                                        builder.setItems(options, (dialog, which) -> {
                                            if (which == 1) {
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
                            Toast.makeText(this, "User ID not found for this review.", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("ReviewActivity", "Failed to fetch hike document", e);
                    Toast.makeText(this, "Failed to fetch hike details.", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Calculates and displays the average rating across all reviews.
     * @param called_by_submit Used for decrementing test hook if triggered by test
     */
    void displayAverageRating(boolean called_by_submit) {
        if (ratings != null && !ratings.isEmpty()) {
            long total = 0L;
            for (Long rating : ratings) {
                total += rating;
            }

            long averageRating = total / ratings.size();
            String averageRatingText = "Average Rating: " + averageRating + "/5";
            ((TextView) findViewById(R.id.average_rating_text_view)).setText(averageRatingText);
        } else {
            ((TextView) findViewById(R.id.average_rating_text_view)).setText("No ratings yet.");
        }

        // Allow testing synchronization if in test mode
        if (TestEnvironment.testHooks != null && called_by_submit) {
            TestEnvironment.testHooks.decrement();
        }
    }

    /**
     * Submits the current user's review and updates both hike and user documents in Firestore.
     */
    void submitReview() {
        if (TestEnvironment.testHooks != null) TestEnvironment.testHooks.increment();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        String reviewText = reviewEditText.getText().toString().trim();
        float rating = ratingBar.getRating();
        String currentUserId = user.getUid();

        // Input validation
        if (!reviewText.isEmpty() && rating > 0) {
            db.collection("Hikes").document(hikeId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        String hikeName = documentSnapshot.getString("Name");

                        // Create and structure review object
                        Review review = new Review(reviewText, (long) rating, currentUserId);
                        Map<String, Object> reviewMap = new HashMap<>();
                        reviewMap.put("reviewText", review.getReviewText());
                        reviewMap.put("rating", review.getRating());
                        reviewMap.put("userId", review.getUserId());
                        reviewMap.put("hikeId", hikeId);
                        reviewMap.put("hikeName", hikeName);

                        // Append review to hike and user documents
                        db.collection("Hikes").document(hikeId)
                                .update("Reviews", FieldValue.arrayUnion(review))
                                .addOnSuccessListener(aVoid -> {
                                    db.collection("Users").document(currentUserId)
                                            .update("userReviews", FieldValue.arrayUnion(reviewMap))
                                            .addOnSuccessListener(innerVoid -> {
                                                ToastUtil.showToast(this, "Submitted Review: " + reviewText + " " + rating);
                                                reviewEditText.setText("");
                                                ratingBar.setRating(0);
                                                loadReviews(true);
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

    /**
     * Opens a dialog showing all reviews by a specific user.
     */
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

                    new AlertDialog.Builder(this)
                            .setTitle(friendEmail + "'s reviews")
                            .setMessage(userReviews.toString())
                            .setPositiveButton("Close", (dialog, which) -> dialog.dismiss())
                            .show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to fetch user reviews.", Toast.LENGTH_SHORT).show();
                    Log.e("ReviewActivity", "Error fetching user reviews", e);
                });
    }
}