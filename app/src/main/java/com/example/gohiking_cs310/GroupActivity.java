package com.example.gohiking_cs310;

import java.util.ArrayList;
import java.util.Date;

/**
 * Class represents a Group Activity trail
 * This class serves as a data model for Firestore documents in the "Group Activities" collection
 */

public class GroupActivity {
    private String activityID;
    private String title;
    private String location;
    private Date time;
    private int maxParticipants;
    private ArrayList<String> participants;

    // Default constructor (required for Firestore)
    public GroupActivity() {}

    /**
     * Full constructor for creating a complete Group Activity object.
     * Used when populating the UI or submitting to Firestore.
     */
    public GroupActivity(String activityID, String title, String location, Date time, int maxParticipants) {
        this.activityID = activityID;
        this.title = title;
        this.location = location;
        this.time = time;
        this.maxParticipants = maxParticipants;
        this.participants = new ArrayList<>();
    }

    // Getters and setters
    public String getActivityID() {
        return activityID;
    }

    public void setActivityID(String activityID) {
        this.activityID = activityID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public int getMaxParticipants() {
        return maxParticipants;
    }

    public void setMaxParticipants(int maxParticipants) {
        this.maxParticipants = maxParticipants;
    }

    public ArrayList<String> getParticipants() {
        return participants;
    }

    public void setParticipants(ArrayList<String> participants) {
        this.participants = participants;
    }
}