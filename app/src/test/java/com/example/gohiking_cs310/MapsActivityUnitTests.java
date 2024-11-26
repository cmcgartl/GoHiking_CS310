package com.example.gohiking_cs310;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class MapsActivityUnitTests {


    // WHITE BOX TEST #6: Verify Map Marker Creation
    @Test
    public void testCreateMapMarker() {
        // Arrange
        LatLng expectedLocation = new LatLng(34.1341, -118.3215); // Example coordinates for Griffith Observatory
        String expectedTitle = "Griffith Observatory";

        // Act
        MarkerOptions marker = new MarkerOptions()
                .position(expectedLocation)
                .title(expectedTitle);

        // Assert
        assertNotNull("Marker should not be null", marker);
        assertEquals("Marker title should match", expectedTitle, marker.getTitle());
        assertEquals("Marker position should match", expectedLocation, marker.getPosition());
    }
}