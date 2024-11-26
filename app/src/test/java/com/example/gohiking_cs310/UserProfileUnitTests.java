package com.example.gohiking_cs310;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserProfileUnitTests {

    private User userProfile;

    @Before
    public void setUp() {
        userProfile = new User("TestUser", "testuser@example.com", "TestUsername");
    }

    // WHITE BOX TEST #7: Add a friend to the user's friends list
    @Test
    public void testAddFriend() {
        String friendEmail = "friend@example.com";
        userProfile.addFriend(friendEmail);

        assertTrue("Friend should be added to the friends list", userProfile.getFriends().contains(friendEmail));
        assertEquals("Friends list should contain one friend", 1, userProfile.getFriends().size());
    }

    // WHITE BOX TEST #12: Add a custom hike to the user's custom hikes
    @Test
    public void testAddCustomHike() {
        String hikeId = "hike123";
        userProfile.addCustomHike(hikeId);

        assertTrue("Hike should be added to custom hikes", userProfile.getCustomHikes().contains(hikeId));
        assertEquals("Custom hikes list should contain one hike", 1, userProfile.getCustomHikes().size());
    }

    // WHITE BOX TEST #13: Add a group activity to the user's activities
    @Test
    public void testAddGroupActivity() {
        String activityId = "group123";
        userProfile.addGroupActivity(activityId);

        assertTrue("Group activity should be added", userProfile.getGroupActivities().contains(activityId));
        assertEquals("Group activities list should contain one activity", 1, userProfile.getGroupActivities().size());
    }

    // WHITE BOX TEST #14: Add a custom list and set its privacy
    @Test
    public void testCustomListAndPrivacy() {
        String listName = "MyFavoriteHikes";
        List<String> hikes = Arrays.asList("Hike1", "Hike2", "Hike3");
        userProfile.customList.put(listName, hikes);
        userProfile.listPrivacy.put(listName, true);

        assertTrue("Custom list should exist", userProfile.customList.containsKey(listName));
        assertEquals("Custom list should contain the correct hikes", hikes, userProfile.customList.get(listName));
        assertTrue("Custom list should be public", userProfile.listPrivacy.get(listName));
    }
}