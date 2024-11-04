package com.example.gohiking_cs310;
import com.google.type.DateTime;

import java.util.ArrayList;
import java.util.Date;

public class GroupActivity {
    private String activityID;
    private String title;
    private String location;
    private Date time;
    private int maxParticipants;
    private ArrayList<UserActivity> participants;

    public GroupActivity(String activityID, String title, String location, Date time, int maxParticipants) {
        this.activityID = activityID;
        this.title = title;
        this.location = location;
        this.time = time;
        this.maxParticipants = maxParticipants;
        this.participants = new ArrayList<>();
    }

    public void joinActivity(UserActivity user) {
        if (participants.size() < maxParticipants) {
            participants.add(user);
        }
    }

    public void leaveActivity(UserActivity user) {
        participants.remove(user);
    }

    // Getters and setters (if needed)
    public String getActivityID() { return activityID; }
    public String getTitle() { return title; }
    public String getLocation() { return location; }
    public Date getTime() { return time; }
    public int getMaxParticipants() { return maxParticipants; }
    public ArrayList<UserActivity> getParticipants() { return participants; }
}
