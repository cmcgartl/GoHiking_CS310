package com.example.gohiking_cs310;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User class stores user-specific metadata including account info, social connections,
 * custom hikes, group participation, and review activity.
 */
public class User {

    // Public fields used for Firebase serialization/deserialization
    public String userId;
    public String username;
    public Boolean Public; // Indicates whether the user’s profile or custom lists are public
    public List<Review> userReviews; // Reviews submitted by the user

    // Private user data
    private String email;

    // Social and activity-related lists
    private List<String> friends; // List of user IDs who are friends with this user
    private List<String> customHikes; // List of hike IDs created or saved by the user
    public List<String> groupActivities; // Group hikes or activities user is part of

    // custom hike lists and their privacy settings
    public Map<String, List<String>> customList; // e.g., "Favorites" → list of hike IDs
    public Map<String, Boolean> listPrivacy;     // e.g., "Favorites" → true (public) or false (private)

    // Constructor for Firebase or minimal account setup
    public User(String userId, String email) {
        this.userId = userId;
        this.email = email;
        this.friends = new ArrayList<>();
        this.customHikes = new ArrayList<>();
        this.groupActivities = new ArrayList<>();
        this.customList = new HashMap<>();
        this.listPrivacy = new HashMap<>();
        this.userReviews = new ArrayList<>();
        this.Public = true;
    }

    // Overloaded constructor for account setup with username
    public User(String userId, String email, String username) {
        this.userId = userId;
        this.email = email;
        this.username = username;
        this.friends = new ArrayList<>();
        this.customHikes = new ArrayList<>();
        this.groupActivities = new ArrayList<>();
        this.customList = new HashMap<>();
        this.listPrivacy = new HashMap<>();
        this.userReviews = new ArrayList<>();
        this.Public = true;
    }

    // ---------- GETTERS AND SETTERS ----------

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

    public List<String> getFriends() {
        return friends;
    }

    // Add a friend by user ID
    public void addFriend(String friendId) {
        friends.add(friendId);
    }

    public List<String> getCustomHikes() {
        return customHikes;
    }

    // Add a custom hike ID to the user's list
    public void addCustomHike(String hikeId) {
        customHikes.add(hikeId);
    }

    public List<String> getGroupActivities() {
        return groupActivities;
    }

    // Register user as part of a group hike
    public void addGroupActivity(String hikeId) {
        groupActivities.add(hikeId);
    }
}
