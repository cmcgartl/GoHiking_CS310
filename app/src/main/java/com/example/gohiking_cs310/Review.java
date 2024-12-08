package com.example.gohiking_cs310;
public class Review {
    private String reviewText;
    private long rating;
    private String userId;

    // Default constructor required for Firestore
    public Review() {}

    public Review(String reviewText, long rating, String userId) {
        this.reviewText = reviewText;
        this.rating = rating;
        this.userId = userId;
    }

    // Getters and setters
    public String getReviewText() {
        return reviewText;
    }

    public void setReviewText(String reviewText) {
        this.reviewText = reviewText;
    }

    public long getRating() {
        return rating;
    }

    public void setRating(long rating) {
        this.rating = rating;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}