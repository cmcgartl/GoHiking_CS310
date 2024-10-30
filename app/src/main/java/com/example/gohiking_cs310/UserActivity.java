package com.example.gohiking_cs310;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;


    public class UserActivity extends AppCompatActivity {
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_user_profile);
        }
    }

    /*public void addFriend(UserActivity user) {
        friendsList.add(user);
    }

    public void removeFriend(UserActivity user) {
        friendsList.remove(user);
    }

    public ArrayList<UserActivity> getFriends() {
        return friendsList;
    }
}*/
