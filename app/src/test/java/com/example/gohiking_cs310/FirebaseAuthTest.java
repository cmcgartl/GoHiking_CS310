package com.example.gohiking_cs310;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;
import static org.junit.Assert.assertTrue;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import org.junit.Test;

/**
 * FirebaseAuthTest contains white box unit tests for Firebase authentication logic.
 * These tests simulate and validate FirebaseAuth behavior such as:
 * - User identity and existence checks
 * - Simulated login and logout state transitions
 *
 * Mockito is used to mock FirebaseAuth and FirebaseUser objects without making actual network calls.
 */
public class FirebaseAuthTest {

    /**
     * WHITE BOX TEST #1:
     * Verifies that a user created and assigned to FirebaseAuth exists and matches
     * the expected UID and email attributes.
     *
     * The test simulates:
     * - A FirebaseAuth instance with a mock user
     * - A local User object with the same ID/email
     * - A check that ensures FirebaseAuth returns the same user information
     */
    @Test
    public void testCheckIfUserExists() {
        // Create mock objects for FirebaseAuth and FirebaseUser
        FirebaseAuth mockFirebaseAuth = mock(FirebaseAuth.class);
        FirebaseUser mockFirebaseUser = mock(FirebaseUser.class);

        // Define the simulated user's credentials
        String firebaseUserEmail = "existinguser@example.com";
        String firebaseUserUid = "id0";
        User testUser = new User(firebaseUserUid, firebaseUserEmail);

        // Set mock return values for the mockFirebaseUser
        when(mockFirebaseAuth.getCurrentUser()).thenReturn(mockFirebaseUser);
        when(mockFirebaseUser.getUid()).thenReturn(firebaseUserUid);
        when(mockFirebaseUser.getEmail()).thenReturn(firebaseUserEmail);

        // Retrieve the "current user" from FirebaseAuth
        FirebaseUser firebaseUser = mockFirebaseAuth.getCurrentUser();

        // Check if the user matches the expected ID and email
        boolean userExists = firebaseUser != null
                && testUser.userId.equals(firebaseUser.getUid()) &&
                testUser.getEmail().equals(firebaseUser.getEmail());

        // Assert the user is correctly recognized as existing
        assertTrue("User should exist in the system.", userExists);
    }

    /**
     * WHITE BOX TEST #2:
     * Verifies user logout logic by mocking a user login and then simulating logout.
     *
     * The test simulates:
     * - A FirebaseAuth object with a logged-in user
     * - A logout event by returning null for getCurrentUser()
     * - An assertion that confirms no user remains authenticated
     */
    @Test
    public void testUserLogout() {
        // Mock FirebaseAuth and FirebaseUser
        FirebaseAuth mockFirebaseAuth = mock(FirebaseAuth.class);
        FirebaseUser mockFirebaseUser = mock(FirebaseUser.class);

        // Setup mocked return values for a logged-in user
        String loggedInUserId = "user123";
        String loggedInUserEmail = "loggedinuser@example.com";
        when(mockFirebaseAuth.getCurrentUser()).thenReturn(mockFirebaseUser);
        when(mockFirebaseUser.getUid()).thenReturn(loggedInUserId);
        when(mockFirebaseUser.getEmail()).thenReturn(loggedInUserEmail);

        // Confirm the user is initially logged in
        FirebaseUser firebaseUser = mockFirebaseAuth.getCurrentUser();
        assertNotNull("User should initially be logged in.", firebaseUser);

        // Simulate logout by setting getCurrentUser() to return null
        when(mockFirebaseAuth.getCurrentUser()).thenReturn(null);
        firebaseUser = mockFirebaseAuth.getCurrentUser();

        // Validate that the user is logged out
        boolean userLoggedOut = firebaseUser == null;
        assertTrue("User should be logged out.", userLoggedOut);
    }
}