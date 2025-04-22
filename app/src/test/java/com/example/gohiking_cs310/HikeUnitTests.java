package com.example.gohiking_cs310;

import org.junit.Before;
import org.junit.Test;
import java.util.ArrayList;
import static org.junit.Assert.*;

/**
 * HikeUnitTests verifies core functionality of the Hike model class,
 * including getters, setters, amenities, reviews, and trail conditions.
 *
 * All tests are white box tests to confirm internal state changes and data correctness.
 */
public class HikeUnitTests {
    private Hike hike;

    /**
     * Initializes a sample Hike object with mock data before each test runs.
     */
    @Before
    public void setUp() {
        ArrayList<Double> ratings = new ArrayList<>();
        ratings.add(4.5);
        ratings.add(3.0);

        ArrayList<Review> reviews = new ArrayList<>();
        Review newreview = new Review("great hike", 5, "12345");
        Review secondreview = new Review("bad hike", 4, "6789");
        reviews.add(newreview);
        reviews.add(secondreview);

        hike = new Hike(
                "123",                      // ID
                "Griffith Park Trail",         // Name
                3,                             // Difficulty
                34.1341,                       // Latitude
                -118.3215,                     // Longitude
                true,                          // Bathrooms
                true,                          // Parking
                ratings,                       // Ratings list
                reviews,                       // Reviews list
                "Dry",                         // Trail Conditions
                true,                          // Trail Markers
                true,                          // Trash Cans
                true,                          // Water Fountains
                false                          // WiFi
        );
    }

    /**
     * WHITE BOX TEST #8:
     * Verifies all basic getter and setter methods for Hike ID, name, difficulty, and location.
     * Confirms that values are correctly updated and retrieved.
     */
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

    /**
     * WHITE BOX TEST #9:
     * Validates the boolean amenity fields of the Hike class, including:
     * bathrooms, parking, trail markers, trash cans, water fountains, and WiFi.
     * Also verifies updates using the respective setters.
     */
    @Test
    public void testAmenities() {
        assertTrue(hike.isBathrooms());
        assertTrue(hike.isParking());
        assertTrue(hike.isTrailMarkers());
        assertTrue(hike.isTrashCans());
        assertTrue(hike.isWaterFountains());
        assertFalse(hike.isWifi());

        // Test updating amenity flags
        hike.setBathrooms(false);
        assertFalse(hike.isBathrooms());

        hike.setWifi(true);
        assertTrue(hike.isWifi());
    }

    /**
     * WHITE BOX TEST #10:
     * Verifies the functionality of rating and review lists.
     * Ensures they are correctly initialized and allow dynamic additions.
     */
    @Test
    public void testRatingsAndReviews() {
        assertEquals(2, hike.getRatings().size());
        assertEquals(4.5, hike.getRatings().get(0), 0.0001);

        // Add a new rating and verify it
        hike.getRatings().add(5.0);
        assertEquals(3, hike.getRatings().size());
        assertEquals(5.0, hike.getRatings().get(2), 0.0001);

        // Review list checks
        assertEquals(2, hike.getReviews().size());
        assertEquals("great hike", hike.getReviews().get(0).getReviewText());
        assertEquals("bad hike", hike.getReviews().get(1).getReviewText());

        // Add a third review and confirm
        Review addreview = new Review("hike was okay", 2, "1238");
        hike.getReviews().add(addreview);
        assertEquals(3, hike.getReviews().size());
        assertEquals("hike was okay", hike.getReviews().get(2).getReviewText());
    }

    /**
     * WHITE BOX TEST #11:
     * Validates the trail conditions field and its getter/setter behavior.
     */
    @Test
    public void testTrailConditions() {
        assertEquals("Dry", hike.getTrailConditions());

        // Change and confirm the new condition
        hike.setTrailConditions("Wet");
        assertEquals("Wet", hike.getTrailConditions());
    }
}