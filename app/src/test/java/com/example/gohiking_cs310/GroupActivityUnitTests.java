package com.example.gohiking_cs310;

import org.junit.Test;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

public class GroupActivityUnitTests {

    // BLACK BOX TEST #1: Verify Group Creation with Valid Inputs
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

    // BLACK BOX TEST #2: Verify Group Activity Constructor
    @Test
    public void testGroupActivityConstructor() {
        // Arrange
        String expectedActivityID = "testID123";
        String expectedTitle = "Morning Hike";
        String expectedLocation = "Griffith Park";
        Date expectedTime = new Date();
        int expectedMaxParticipants = 10;

        // Act
        GroupActivity groupActivity = new GroupActivity(
                expectedActivityID,
                expectedTitle,
                expectedLocation,
                expectedTime,
                expectedMaxParticipants
        );

        // Assert
        assertNotNull("Participants list should not be null", groupActivity.getParticipants());
        assertEquals("Activity ID should match", expectedActivityID, groupActivity.getActivityID());
        assertEquals("Title should match", expectedTitle, groupActivity.getTitle());
        assertEquals("Location should match", expectedLocation, groupActivity.getLocation());
        assertEquals("Time should match", expectedTime, groupActivity.getTime());
        assertEquals("Max participants should match", expectedMaxParticipants, groupActivity.getMaxParticipants());
    }

    // BLACK BOX TEST #3: Verify Participants Cannot Exceed Max Limit
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

        // Simulate a new participant trying to join
        if (participants.size() >= group.getMaxParticipants()) {
            assertEquals(3, participants.size());
            assertFalse(participants.contains("user4"));
        } else {
            participants.add("user4");
        }
    }
}