package com.example.gohiking_cs310;

public class HikeActivity {
    private String hikeID;
    private String location;
    private String difficulty;
    private String amenities;
    private String trailCondition;
    private double rating;

    public HikeActivity(String hikeID, String location, String difficulty, String amenities, String trailCondition, double rating) {
        this.hikeID = hikeID;
        this.location = location;
        this.difficulty = difficulty;
        this.amenities = amenities;
        this.trailCondition = trailCondition;
        this.rating = rating;
    }

    public double getRating() {
        return rating;
    }

    public String getDetails() {
        return location + " - " + difficulty + " difficulty";
    }

    public void addReview(ReviewActivity review) {

    }
}
