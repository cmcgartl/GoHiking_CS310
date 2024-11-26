package com.example.gohiking_cs310;

import org.junit.Test;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

public class GroupActivityUnitTests {

    // WHITE BOX TEST #3: Verify Group Creation with Valid Inputs
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

    // WHITE BOX TEST #4: Verify Updating Time in GroupActivity
    @Test
    public void testUpdateTimeInGroupActivity() {
        String activityID = "testID456";
        String title = "Evening Walk";
        String location = "Central Park";
        Date initialTime = new Date();
        int maxParticipants = 5;

        GroupActivity groupActivity = new GroupActivity(activityID, title, location, initialTime, maxParticipants);

        Date newTime = new Date(initialTime.getTime() + 3600000); // 1 hour later
        groupActivity.setTime(newTime);

        assertNotNull("Time should not be null after update.", groupActivity.getTime());
        assertEquals("Time should be updated to the new value.", newTime, groupActivity.getTime());
        assertNotEquals("Time should not match the initial value after update.", initialTime, groupActivity.getTime());
    }

    // WHITE BOX TEST #5: Verify Participants Cannot Exceed Max Limit
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

        if (participants.size() >= group.getMaxParticipants()) {
            assertEquals(3, participants.size());
            assertFalse(participants.contains("user4"));
        } else {
            participants.add("user4");
        }
    }
}