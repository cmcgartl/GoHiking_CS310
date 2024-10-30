package com.example.gohiking_cs310;
import java.util.ArrayList;

public class UserActivity {
    private String userID;
    private String name;
    private String email;
    private String password;
    private ArrayList<UserActivity> friendsList;

    public UserActivity(String userID, String name, String email, String password) {
        this.userID = userID;
        this.name = name;
        this.email = email;
        this.password = password;
        this.friendsList = new ArrayList<>();
    }

    public void addFriend(UserActivity user) {
        friendsList.add(user);
    }

    public void removeFriend(UserActivity user) {
        friendsList.remove(user);
    }

    public ArrayList<UserActivity> getFriends() {
        return friendsList;
    }
}
