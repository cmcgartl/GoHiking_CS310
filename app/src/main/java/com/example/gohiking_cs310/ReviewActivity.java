package com.example.gohiking_cs310;

public class ReviewActivity {
    private String reviewID;
    private String userID;
    private String hikeID;
    private double rating;
    private String comments;

    public ReviewActivity(String reviewID, String userID, String hikeID, double rating, String comments) {
        this.reviewID = reviewID;
        this.userID = userID;
        this.hikeID = hikeID;
        this.rating = rating;
        this.comments = comments;
    }

    public void submitReview() {
    }

    public String getReview() {
        return comments;
    }
}
