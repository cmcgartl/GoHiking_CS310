package com.example.gohiking_cs310;

import com.google.firebase.firestore.PropertyName;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Class represents a hiking trail
 * This class serves as a data model for Firestore documents in the "Hikes" collection
 * and is also used for transferring hike data between activities via serialization.
 */
public class Hike implements Serializable {

    // Unique Firestore document ID
    private String id;

    // Trail attributes stored in Firestore and shown in the app
    private String name;
    private int difficulty;
    private double lat;
    private double lng;
    private boolean bathrooms;
    private boolean parking;
    private boolean trailMarkers;
    private boolean trashCans;
    private boolean waterFountains;
    private boolean wifi;

    // Aggregated user ratings and reviews
    private ArrayList<Double> ratings;
    private String trailConditions;
    private List<Review> reviews;

    /**
     * Default constructor required for Firestore deserialization.
     */
    public Hike() {}

    /**
     * Minimal constructor used when only the name is needed (e.g., for search or temporary usage).
     */
    public Hike(String Name) {
        this.name = Name;
    }

    /**
     * Full constructor for creating a complete Hike object.
     * Used when populating the UI or submitting to Firestore.
     */
    public Hike(String id, String name, int difficulty, double lat, double lng, boolean bathrooms,
                boolean parking, ArrayList<Double> ratings, List<Review> reviews,
                String trailConditions, boolean trailMarkers, boolean trashCans,
                boolean waterFountains, boolean wifi) {
        this.id = id;
        this.name = name;
        this.difficulty = difficulty;
        this.lat = lat;
        this.lng = lng;
        this.bathrooms = bathrooms;
        this.parking = parking;
        this.ratings = ratings;
        this.reviews = reviews;
        this.trailConditions = trailConditions;
        this.trailMarkers = trailMarkers;
        this.trashCans = trashCans;
        this.waterFountains = waterFountains;
        this.wifi = wifi;
    }

    // Getters and setters (annotated for Firestore field mapping)
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    @PropertyName("Name")
    public String getName() { return name; }
    @PropertyName("Name")
    public void setName(String name) { this.name = name; }

    @PropertyName("Difficulty")
    public int getDifficulty() { return difficulty; }
    @PropertyName("Difficulty")
    public void setDifficulty(int difficulty) { this.difficulty = difficulty; }

    @PropertyName("Lat")
    public double getLat() { return lat; }
    @PropertyName("Lat")
    public void setLat(double lat) { this.lat = lat; }

    @PropertyName("Long")
    public double getLng() { return lng; }
    @PropertyName("Long")
    public void setLng(double lng) { this.lng = lng; }

    @PropertyName("Bathrooms")
    public boolean isBathrooms() { return bathrooms; }
    @PropertyName("Bathrooms")
    public void setBathrooms(boolean bathrooms) { this.bathrooms = bathrooms; }

    @PropertyName("Parking")
    public boolean isParking() { return parking; }
    @PropertyName("Parking")
    public void setParking(boolean parking) { this.parking = parking; }

    @PropertyName("Ratings")
    public ArrayList<Double> getRatings() { return ratings; }
    @PropertyName("Ratings")
    public void setRatings(ArrayList<Double> ratings) { this.ratings = ratings; }

    @PropertyName("Reviews")
    public List<Review> getReviews() { return reviews; }
    @PropertyName("Reviews")
    public void setReviews(List<Review> reviews) { this.reviews = reviews; }

    @PropertyName("Trail Conditions")
    public String getTrailConditions() { return trailConditions; }
    @PropertyName("Trail Conditions")
    public void setTrailConditions(String trailConditions) { this.trailConditions = trailConditions; }

    @PropertyName("Trail Markers")
    public boolean isTrailMarkers() { return trailMarkers; }
    @PropertyName("Trail Markers")
    public void setTrailMarkers(boolean trailMarkers) { this.trailMarkers = trailMarkers; }

    @PropertyName("Trash Cans")
    public boolean isTrashCans() { return trashCans; }
    @PropertyName("Trash Cans")
    public void setTrashCans(boolean trashCans) { this.trashCans = trashCans; }

    @PropertyName("Water Fountains")
    public boolean isWaterFountains() { return waterFountains; }
    @PropertyName("Water Fountains")
    public void setWaterFountains(boolean waterFountains) { this.waterFountains = waterFountains; }

    @PropertyName("WiFi")
    public boolean isWifi() { return wifi; }
    @PropertyName("WiFi")
    public void setWifi(boolean wifi) { this.wifi = wifi; }
}