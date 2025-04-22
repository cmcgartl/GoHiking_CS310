package com.example.gohiking_cs310;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.Arrays;
import java.util.List;

/**
 * UserProfileUnitTests performs white box testing on the User model class.
 * These tests ensure that user profile management functionality (such as
 * managing friends, custom hikes, group activities, and privacy settings)
 * behaves correctly and consistently.
 */
public class UserProfileUnitTests {

    private User userProfile;

    /**
     * Initializes a test user profile before each test case.
     * This user is used to verify the behavior of various user-related operations.
     */
    @Before
    public void setUp() {
        userProfile = new User("TestUser", "testuser@example.com", "TestUsername");
    }

    /**
     * WHITE BOX TEST #7:
     * Verifies that a friend can be successfully added to the user's friends list.
     * Confirms that the friend appears in the list and that the list size is updated.
     */
    @Test
    public void testAddFriend() {
        String friendEmail = "friend@example.com";
        userProfile.addFriend(friendEmail);

        assertTrue("Friend should be added to the friends list", userProfile.getFriends().contains(friendEmail));
        assertEquals("Friends list should contain one friend", 1, userProfile.getFriends().size());
    }

    /**
     * WHITE BOX TEST #12:
     * Verifies that a custom hike ID can be added to the user's custom hikes list.
     * Confirms proper insertion and list size update.
     */
    @Test
    public void testAddCustomHike() {
        String hikeId = "hike123";
        userProfile.addCustomHike(hikeId);

        assertTrue("Hike should be added to custom hikes", userProfile.getCustomHikes().contains(hikeId));
        assertEquals("Custom hikes list should contain one hike", 1, userProfile.getCustomHikes().size());
    }

    /**
     * WHITE BOX TEST #13:
     * Confirms that a group activity ID can be successfully added to the user's list of group activities.
     * Ensures that the value appears in the list and count is as expected.
     */
    @Test
    public void testAddGroupActivity() {
        String activityId = "group123";
        userProfile.addGroupActivity(activityId);

        assertTrue("Group activity should be added", userProfile.getGroupActivities().contains(activityId));
        assertEquals("Group activities list should contain one activity", 1, userProfile.getGroupActivities().size());
    }

    /**
     * WHITE BOX TEST #14:
     * Tests adding a new named custom list of hikes and setting its privacy status.
     * Ensures:
     * - The list is stored in the map
     * - The correct hikes are stored in the list
     * - The privacy flag is set to true
     */
    @Test
    public void testCustomListAndPrivacy() {
        String listName = "MyFavoriteHikes";
        List<String> hikes = Arrays.asList("Hike1", "Hike2", "Hike3");

        // Simulate adding a custom list and marking it public
        userProfile.customList.put(listName, hikes);
        userProfile.listPrivacy.put(listName, true);

        assertTrue("Custom list should exist", userProfile.customList.containsKey(listName));
        assertEquals("Custom list should contain the correct hikes", hikes, userProfile.customList.get(listName));
        assertTrue("Custom list should be public", userProfile.listPrivacy.get(listName));
    }
}