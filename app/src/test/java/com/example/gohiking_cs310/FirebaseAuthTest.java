package com.example.gohiking_cs310;

import static org.mockito.Mockito.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.junit.Test;

public class FirebaseAuthTest {

    //WHITE BOX TEST #1: Check if a user exists in the system
    @Test
    public void testCheckIfUserExists() {
        FirebaseAuth mockFirebaseAuth = mock(FirebaseAuth.class);
        FirebaseUser mockFirebaseUser = mock(FirebaseUser.class);

        when(mockFirebaseAuth.getCurrentUser()).thenReturn(mockFirebaseUser);
        when(mockFirebaseUser.getEmail()).thenReturn("existinguser@example.com");

        FirebaseUser user = mockFirebaseAuth.getCurrentUser();
        boolean userExists = user != null && "existinguser@example.com".equals(user.getEmail());

        assertTrue("User should exist in the system.", userExists);
    }

    //WHITE BOX TEST #2: Check if a user does not exist in the system
    @Test
    public void testCheckIfUserDoesNotExist() {
        FirebaseAuth mockFirebaseAuth = mock(FirebaseAuth.class);

        when(mockFirebaseAuth.getCurrentUser()).thenReturn(null);

        FirebaseUser user = mockFirebaseAuth.getCurrentUser();
        boolean userExists = user != null;

        assertFalse("User should not exist in the system.", userExists);
    }
}