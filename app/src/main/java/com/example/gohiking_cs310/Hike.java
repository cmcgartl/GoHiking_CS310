package com.example.gohiking_cs310;

import com.google.firebase.firestore.PropertyName;

import java.io.Serializable;
import java.util.ArrayList;

public class Hike implements Serializable {
    private String id;
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
    private ArrayList<Double> ratings;
    private ArrayList<String> reviews;
    private String trailConditions;

    public Hike() {}

    public Hike(String Name){
        this.name = Name;
    }
    public Hike(String id, String name, int difficulty, double lat, double lng, boolean bathrooms,
                boolean parking, ArrayList<Double> ratings, ArrayList<String> reviews,
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
    public ArrayList<String> getReviews() { return reviews; }
    @PropertyName("Reviews")
    public void setReviews(ArrayList<String> reviews) { this.reviews = reviews; }

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