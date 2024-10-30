package com.example.gohiking_cs310;
import java.util.ArrayList;

public class GroupActivity {
    private String activityID;
    private String title;
    private String location;
    private String time;
    private int maxParticipants;
    private ArrayList<UserActivity> participants;

    public GroupActivity(String activityID, String title, String location, String time, int maxParticipants) {
        this.activityID = activityID;
        this.title = title;
        this.location = location;
        this.time = time;
        this.maxParticipants = maxParticipants;
        this.participants = new ArrayList<>();
    }

    public void createActivity() {

    }

    public void joinActivity(UserActivity user) {
        if (participants.size() < maxParticipants) {
            participants.add(user);
        }
    }

    public void leaveActivity(UserActivity user) {
        participants.remove(user);
    }
}
