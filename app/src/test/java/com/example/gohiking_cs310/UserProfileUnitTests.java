package com.example.gohiking_cs310;

import org.junit.Test;
import static org.junit.Assert.*;

public class UserProfileUnitTests {

    // WHITE BOX TEST #7: Add a friend to the user's friends list
    @Test
    public void testAddFriend() {
        User userProfile = new User("TestUser", "testuser@example.com");
        String friendEmail = "friend@example.com";

        userProfile.addFriend(friendEmail);

        assertTrue("Friend should be added to the friends list", userProfile.getFriends().contains(friendEmail));
        assertEquals("Friends list should contain one friend", 1, userProfile.getFriends().size());
    }

}