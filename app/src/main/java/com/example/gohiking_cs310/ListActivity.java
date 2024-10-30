package com.example.gohiking_cs310;

import java.util.ArrayList;

public class ListActivity {
    private String listID;
    private String listName;
    private boolean visibility;
    private ArrayList<HikeActivity> hikes;

    public ListActivity(String listID, String listName, boolean visibility) {
        this.listID = listID;
        this.listName = listName;
        this.visibility = visibility;
        this.hikes = new ArrayList<>();
    }

    public void addHike(HikeActivity hike) {
        hikes.add(hike);
    }

    public void removeHike(HikeActivity hike) {
        hikes.remove(hike);
    }

    public void setVisibility(boolean visibility) {
        this.visibility = visibility;
    }
}
