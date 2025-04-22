package com.example.gohiking_cs310;

/**
 * Represents a user-submitted review for a hike.
 * Includes a text description, numerical rating, and the ID of the user who submitted it.
 */
public class Review {
    private String reviewText;
    private long rating;
    private String userId;


    //Default constructor required for Firestore to deserialize review documents.
    public Review() {}

    //Constructs a Review object with all relevant details.
    public Review(String reviewText, long rating, String userId) {
        this.reviewText = reviewText;
        this.rating = rating;
        this.userId = userId;
    }

    //return The text of the review.
    public String getReviewText() {
        return reviewText;
    }

    //return The text of the rating.
    public long getRating() {
        return rating;
    }

    //return The unique ID of the user who created the review.
    public String getUserId() {
        return userId;
    }
}