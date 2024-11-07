package com.example.gohiking_cs310;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class ReviewActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private String hikeId;
    private TextView reviewListTextView;
    private RatingBar ratingBar;
    private EditText reviewEditText;
    private ArrayList<String> reviews = new ArrayList<>();
    private ArrayList<Long> ratings = new ArrayList<>();
    private int userReviewIndex = -1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        db = FirebaseFirestore.getInstance();
        hikeId = getIntent().getStringExtra("hikeId");

        reviewListTextView = findViewById(R.id.review_list_text_view);
        ratingBar = findViewById(R.id.rating_bar);
        reviewEditText = findViewById(R.id.review_edit_text);
        Button submitButton = findViewById(R.id.submit_review_button);

        Button backToProfile = findViewById(R.id.buttonBackToHike);
        backToProfile.setOnClickListener(v -> {
            Intent intent = new Intent(ReviewActivity.this, HikeActivity.class);
            startActivity(intent);
        });

        loadReviews();

        submitButton.setOnClickListener(v -> submitReview());
    }

    private void loadReviews() {
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
    }

    private void displayReviews() {
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
            reviewsText.append("No reviews yet.");
        }
        reviewListTextView.setText(reviewsText.toString());
    }


    private void displayAverageRating() {
        if (ratings != null && !ratings.isEmpty()) {
            Long total = 0L;
            for (Long rating : ratings) {
                total += rating;
            }
            long averageRating = total / ratings.size();
            String averageRatingText = "Average Rating: " + averageRating;
            ((TextView) findViewById(R.id.average_rating_text_view)).setText(averageRatingText);
        } else {
            ((TextView) findViewById(R.id.average_rating_text_view)).setText("No ratings yet.");
        }
    }

    private void submitReview() {
        String reviewText = reviewEditText.getText().toString().trim();
        float rating = ratingBar.getRating();
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String userReview = currentUserId + ": " + reviewText;

        if (!reviewText.isEmpty() && rating > 0) {
            if (userReviewIndex != -1) {
                reviews.set(userReviewIndex, userReview);
                ratings.set(userReviewIndex, (long) rating);
            } else {
                reviews.add(userReview);
                ratings.add((long) rating);
            }

            db.collection("Hikes").document(hikeId)
                    .update("Reviews", reviews, "Ratings", ratings)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Review submitted!", Toast.LENGTH_SHORT).show();
                        reviewEditText.setText("");
                        ratingBar.setRating(0);
                        displayReviews();
                        displayAverageRating();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Failed to submit review", Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(this, "Please provide both a review and rating", Toast.LENGTH_SHORT).show();
        }
    }
}



/*package com.example.gohiking_cs310;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class ReviewActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private String hikeId;
    private TextView reviewListTextView;
    private RatingBar ratingBar;
    private EditText reviewEditText;
    private ArrayList<String> reviews = new ArrayList<>();
    private ArrayList<Long> ratings = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        db = FirebaseFirestore.getInstance();
        hikeId = getIntent().getStringExtra("hikeId"); // Pass hike ID from HikeActivity

        reviewListTextView = findViewById(R.id.review_list_text_view);
        ratingBar = findViewById(R.id.rating_bar);
        reviewEditText = findViewById(R.id.review_edit_text);
        Button submitButton = findViewById(R.id.submit_review_button);

        Button backToProfile = findViewById(R.id.buttonBackToHike);
        backToProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ReviewActivity.this, HikeActivity.class);
                startActivity(intent);
            }
        });

        loadReviews();

        submitButton.setOnClickListener(v -> submitReview());
    }

    private void loadReviews() {
        db.collection("Hikes").document(hikeId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Log.d("ReviewActivity", "Document exists");
                reviews = (ArrayList<String>) documentSnapshot.get("Reviews");
                ratings = (ArrayList<Long>) documentSnapshot.get("Ratings");
                Log.d("ReviewActivity", "Reviews: " + reviews);
                Log.d("ReviewActivity", "Ratings: " + ratings);
                displayReviews();
                displayAverageRating();
            }
        });
    }

    private void displayReviews() {
        StringBuilder reviewsText = new StringBuilder();
        reviewsText.append("User Reviews and Ratings: \n\n");
        int i = 0;
        if (reviews != null && !reviews.isEmpty()) {
            for (String review : reviews) {
                if (i < ratings.size()) reviewsText.append("- ").append("\"" + review + "\"" + "\t (" + String.valueOf(ratings.get(i)) + "/5)").append("\n");
                else reviewsText.append("- ").append("\"" + review + "\"").append("\n");
                i++;
            }
        } else {
            reviewsText.append("No reviews yet.");
        }
        reviewListTextView.setText(reviewsText.toString());
    }

    private void displayAverageRating() {
        if (ratings != null && !ratings.isEmpty()) {
            Long total = 0L;
            for (Long rating : ratings) {
                total += rating;
            }
            long averageRating = total / ratings.size();
            String averageRatingText = "Average Rating: " + averageRating;
            ((TextView) findViewById(R.id.average_rating_text_view)).setText(averageRatingText);
        } else {
            ((TextView) findViewById(R.id.average_rating_text_view)).setText("No ratings yet.");
        }
    }

    private void submitReview() {
        String reviewText = reviewEditText.getText().toString().trim();
        float rating = ratingBar.getRating();

        if (!reviewText.isEmpty() && rating > 0) {
            reviews.add(reviewText);
            ratings.add((long) rating);

            db.collection("Hikes").document(hikeId)
                    .update("Reviews", reviews, "Ratings", ratings)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Review submitted!", Toast.LENGTH_SHORT).show();
                        reviewEditText.setText("");
                        ratingBar.setRating(0);
                        displayReviews();
                        displayAverageRating();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Failed to submit review", Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(this, "Please provide both a review and rating", Toast.LENGTH_SHORT).show();
        }
    }
}*/