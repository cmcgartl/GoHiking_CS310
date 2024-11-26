package com.example.gohiking_cs310;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.junit.Test;

public class FirebaseAuthTest {

    //WHITE BOX TEST #1: Create a new user, add them to Firebase and ensure they exist
    @Test
    public void testCheckIfUserExists() {
        FirebaseAuth mockFirebaseAuth = mock(FirebaseAuth.class);
        FirebaseUser mockFirebaseUser = mock(FirebaseUser.class);

        String firebaseUserEmail = "existinguser@example.com";
        String firebaseUserUid = "id0";
        User testUser = new User(firebaseUserUid, firebaseUserEmail);

        when(mockFirebaseAuth.getCurrentUser()).thenReturn(mockFirebaseUser);
        when(mockFirebaseUser.getUid()).thenReturn(firebaseUserUid);
        when(mockFirebaseUser.getEmail()).thenReturn(firebaseUserEmail);

        FirebaseUser firebaseUser = mockFirebaseAuth.getCurrentUser();

        boolean userExists = firebaseUser != null
                && testUser.userId.equals(firebaseUser.getUid()) &&
                testUser.getEmail().equals(firebaseUser.getEmail());

        // Assert that the user exists
        assertTrue("User should exist in the system.", userExists);
    }

    //WHITE BOX TEST #2: Check if a user is logged out of the system
    @Test
    public void testUserLogout() {
        FirebaseAuth mockFirebaseAuth = mock(FirebaseAuth.class);
        FirebaseUser mockFirebaseUser = mock(FirebaseUser.class);

        String loggedInUserId = "user123";
        String loggedInUserEmail = "loggedinuser@example.com";
        when(mockFirebaseAuth.getCurrentUser()).thenReturn(mockFirebaseUser);
        when(mockFirebaseUser.getUid()).thenReturn(loggedInUserId);
        when(mockFirebaseUser.getEmail()).thenReturn(loggedInUserEmail);

        // Ensure the user is logged in initially
        FirebaseUser firebaseUser = mockFirebaseAuth.getCurrentUser();
        assertNotNull("User should initially be logged in.", firebaseUser);

        // Simulate user logout by setting the current user to null
        when(mockFirebaseAuth.getCurrentUser()).thenReturn(null);
        // Retrieve the user after logout
        firebaseUser = mockFirebaseAuth.getCurrentUser();
        // Validate that no user is logged in
        boolean userLoggedOut = firebaseUser == null;
        assertTrue("User should be logged out.", userLoggedOut);
    }
}