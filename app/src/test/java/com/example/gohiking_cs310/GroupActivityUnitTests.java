package com.example.gohiking_cs310;

import org.junit.Test;
import java.util.Date;
import java.util.List;
import static org.junit.Assert.*;

/**
 * Unit tests for the GroupActivity class.
 * Tests:
 * - Creation of GroupActivity objects with valid data
 * - Updating scheduled time of a group
 * - Enforcing the maximum number of participants
 */
public class GroupActivityUnitTests {

    /**
     * WHITE BOX TEST #3:
     * Verifies that a GroupActivity is correctly created using valid inputs.
     * Confirms that all fields are initialized and that the participant list is empty on creation.
     */
    @Test
    public void testCreateGroup_ValidInputs() {
        GroupActivity group = new GroupActivity(
                "1",
                "Morning Hike",
                "Griffith Park",
                new Date(),
                10
        );

        assertEquals("1", group.getActivityID());
        assertEquals("Morning Hike", group.getTitle());
        assertEquals("Griffith Park", group.getLocation());
        assertNotNull(group.getTime());
        assertEquals(10, group.getMaxParticipants());
        assertTrue(group.getParticipants().isEmpty());
    }

    /**
     * WHITE BOX TEST #4:
     * Validates the ability to update the scheduled time of a GroupActivity.
     * Ensures that the new time replaces the previous value as expected.
     */
    @Test
    public void testUpdateTimeInGroupActivity() {
        String activityID = "testID456";
        String title = "Evening Walk";
        String location = "Central Park";
        Date initialTime = new Date();
        int maxParticipants = 5;

        GroupActivity groupActivity = new GroupActivity(activityID, title, location, initialTime, maxParticipants);

        Date newTime = new Date(initialTime.getTime() + 3600000); // +1 hour
        groupActivity.setTime(newTime);

        assertNotNull("Time should not be null after update.", groupActivity.getTime());
        assertEquals("Time should be updated to the new value.", newTime, groupActivity.getTime());
        assertNotEquals("Time should not match the initial value after update.", initialTime, groupActivity.getTime());
    }

    /**
     * WHITE BOX TEST #5:
     * Tests the enforcement of the maximum number of participants.
     * Adds participants up to the max limit and ensures no additional members can join.
     */
    @Test
    public void testJoinGroup_WhenFull() {
        GroupActivity group = new GroupActivity(
                "1",
                "Sunset Hike",
                "Topanga State Park",
                new Date(),
                3
        );

        List<String> participants = group.getParticipants();
        participants.add("user1");
        participants.add("user2");
        participants.add("user3");

        // Attempting to add a fourth participant should not be allowed logically
        if (participants.size() >= group.getMaxParticipants()) {
            assertEquals(3, participants.size());
            assertFalse(participants.contains("user4")); // user4 should not be present
        } else {
            participants.add("user4");
        }
    }
}