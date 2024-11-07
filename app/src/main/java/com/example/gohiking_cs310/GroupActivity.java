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
    private ArrayList<String> participants;

    public GroupActivity() {}
    public GroupActivity(String activityID, String title, String location, Date time, int maxParticipants) {
        this.activityID = activityID;
        this.title = title;
        this.location = location;
        this.time = time;
        this.maxParticipants = maxParticipants;
        this.participants = new ArrayList<String>();
    }

    public String getActivityID() { return activityID; }
    public String getTitle() { return title; }
    public String getLocation() { return location; }
    public Date getTime() { return time; }
    public int getMaxParticipants() { return maxParticipants; }
    public ArrayList<String> getParticipants() { return new ArrayList<>(); }
}
