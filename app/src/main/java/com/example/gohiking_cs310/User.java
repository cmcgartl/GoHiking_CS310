package com.example.gohiking_cs310;

import android.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.example.gohiking_cs310.Review;

public class User {
    public String userId;
    private String email;
    private List<String> friends;
    private List<String> customHikes;
    public List<Review> userReviews;
    public List<String> groupActivities;
    public Map<String,List<String>> customList;
    public Map<String, Boolean> listPrivacy;
    public String username;
    public Boolean Public;


    // Constructor
    public User(String userId, String email) {
        this.userId = userId;
        this.email = email;
        this.friends = new ArrayList<>();
        this.customHikes = new ArrayList<>();
        this.groupActivities = new ArrayList<>();
        this.Public = true;
        this.customList = new HashMap<>();
        this.listPrivacy = new HashMap<>();
        this.userReviews = new ArrayList<>();
    }

    public User(String userId, String email, String username) {
        this.userId = userId;
        this.email = email;
        this.friends = new ArrayList<>();
        this.customHikes = new ArrayList<>();
        this.groupActivities = new ArrayList<>();
        this.username = username;
        this.Public = true;
        this.customList = new HashMap<>();
        this.listPrivacy = new HashMap<>();
        this.userReviews = new ArrayList<>();
    }



    // Getters and setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }


    public List<String> getFriends() {return friends;}

    public void addFriend(String friendId) {
        friends.add(friendId);
    }


    public List<String> getCustomHikes() {
        return customHikes;
    }

    public void addCustomHike(String hikeId) {
        customHikes.add(hikeId);
    }


    public List<String> getGroupActivities() {
        return groupActivities;
    }

    public void addGroupActivity(String hikeId) {
        groupActivities.add(hikeId);
    }
}
