package com.example.gohiking_cs310;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

public class HikeUnitTests {
    private Hike hike;

    @Before
    public void setUp() {
        ArrayList<Double> ratings = new ArrayList<>();
        ratings.add(4.5);
        ratings.add(3.0);

        ArrayList<String> reviews = new ArrayList<>();
        reviews.add("Beautiful trail!");
        reviews.add("Could use better maintenance.");

        hike = new Hike("123", "Griffith Park Trail", 3, 34.1341, -118.3215, true,
                true, ratings, reviews, "Dry", true, true, true, false);
    }

    //WHITE BOX TEST #8: Hike getters and setters
    @Test
    public void testGettersAndSetters() {
        assertEquals("123", hike.getId());
        hike.setId("456");
        assertEquals("456", hike.getId());
        assertEquals("Griffith Park Trail", hike.getName());
        hike.setName("Runyon Canyon");
        assertEquals("Runyon Canyon", hike.getName());

        assertEquals(3, hike.getDifficulty());
        hike.setDifficulty(5);
        assertEquals(5, hike.getDifficulty());


        assertEquals(34.1341, hike.getLat(), 0.0001);
        hike.setLat(35.0000);
        assertEquals(35.0000, hike.getLat(), 0.0001);

        assertEquals(-118.3215, hike.getLng(), 0.0001);
        hike.setLng(-117.0000);
        assertEquals(-117.0000, hike.getLng(), 0.0001);
    }

    //WHITE BOX TEST #9: hike ammenities
    @Test
    public void testAmenities() {
        assertTrue(hike.isBathrooms());
        assertTrue(hike.isParking());
        assertTrue(hike.isTrailMarkers());
        assertTrue(hike.isTrashCans());
        assertTrue(hike.isWaterFountains());
        assertFalse(hike.isWifi());
        hike.setBathrooms(false);
        assertFalse(hike.isBathrooms());
        hike.setWifi(true);
        assertTrue(hike.isWifi());
    }

    //WHITE BOX TEST #10: hike ratings and reviews
    @Test
    public void testRatingsAndReviews() {
        assertEquals(2, hike.getRatings().size());
        assertEquals(4.5, hike.getRatings().get(0), 0.0001);
        hike.getRatings().add(5.0);
        assertEquals(3, hike.getRatings().size());
        assertEquals(5.0, hike.getRatings().get(2), 0.0001);
        assertEquals(2, hike.getReviews().size());
        assertTrue(hike.getReviews().contains("Beautiful trail!"));
        hike.getReviews().add("Amazing hike!");
        assertEquals(3, hike.getReviews().size());
        assertTrue(hike.getReviews().contains("Amazing hike!"));
    }

    //WHITE BOX TEST #11: hike trail conditions
    @Test
    public void testTrailConditions() {
        assertEquals("Dry", hike.getTrailConditions());
        hike.setTrailConditions("Wet");
        assertEquals("Wet", hike.getTrailConditions());
    }
}