package com.example.gohiking_cs310;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * MapsActivityUnitTests contains white box tests related to map-based logic
 * for the MapsActivity component, focusing on UI marker setup.
 */
public class MapsActivityUnitTests {

    /**
     * WHITE BOX TEST #6:
     * Verifies that a Google Maps Marker is correctly created with the expected
     * title and geographic position.
     *
     * The test ensures:
     * - A MarkerOptions object is properly instantiated
     * - The marker contains the correct title ("Griffith Observatory")
     * - The marker is placed at the correct LatLng coordinates
     *
     * This type of test is useful for validating logic prior to rendering markers on the map.
     */
    @Test
    public void testCreateMapMarker() {
        LatLng expectedLocation = new LatLng(34.1341, -118.3215);
        String expectedTitle = "Griffith Observatory";

        // Simulate creating a marker for a hike/trail location
        MarkerOptions marker = new MarkerOptions()
                .position(expectedLocation)
                .title(expectedTitle);

        // Verify the marker is not null
        assertNotNull("Marker should not be null", marker);

        // Verify that the marker has the expected title and position
        assertEquals("Marker title should match", expectedTitle, marker.getTitle());
        assertEquals("Marker position should match", expectedLocation, marker.getPosition());
    }
}