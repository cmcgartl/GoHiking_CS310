package com.example.gohiking_cs310;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class CustomListActivityFirebaseTest {

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String testUserId;

    /**
     * Sets up a clean Firebase test environment before each test.
     * - Registers an Espresso idling resource
     * - Dynamically creates a new test user
     * - Initializes Firestore with empty custom list and privacy fields
     */
    @Before
    public void setUp() throws InterruptedException {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Generate a unique email to avoid conflicts in Firebase Auth
        String randomEmail = "testuser_" + System.currentTimeMillis() + "@example.com";
        String randomPassword = "password123";

        // Register idling resources for test synchronization
        TestEnvironment.testHooks = new TestHooks() {
            @Override
            public void increment() {
                EspressoIdlingResource.increment();
            }

            @Override
            public void decrement() {
                EspressoIdlingResource.decrement();
            }
        };
        IdlingRegistry.getInstance().register(EspressoIdlingResource.getIdlingResource());

        // Synchronize async Firebase operations
        CountDownLatch userCreatedLatch = new CountDownLatch(1);
        CountDownLatch firestoreInitLatch = new CountDownLatch(1);

        // Create new Firebase test user
        auth.createUserWithEmailAndPassword(randomEmail, randomPassword)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        testUserId = user.getUid();

                        // Set initial user data with empty lists
                        Map<String, Object> userData = new HashMap<>();
                        userData.put("customList", new HashMap<>());
                        userData.put("listPrivacy", new HashMap<>());

                        db.collection("Users").document(testUserId)
                                .set(userData)
                                .addOnSuccessListener(aVoid -> firestoreInitLatch.countDown())
                                .addOnFailureListener(e -> {
                                    fail("Failed to initialize Firestore: " + e.getMessage());
                                    firestoreInitLatch.countDown();
                                });

                        userCreatedLatch.countDown();
                    } else {
                        fail("Failed to create test user.");
                        userCreatedLatch.countDown();
                        firestoreInitLatch.countDown();
                    }
                });

        userCreatedLatch.await();
        firestoreInitLatch.await();
    }

    /**
     * Tests creation of a new custom list in Firestore.
     * - Simulates the CustomListActivity context
     * - Performs a write operation to Firestore
     * - Verifies that the list and its privacy flag were added
     */
    @Test
    public void testCreateCustomList() throws InterruptedException {
        CountDownLatch writeLatch = new CountDownLatch(1);
        CountDownLatch readLatch = new CountDownLatch(1);

        // Launch activity to simulate normal UI context
        try (ActivityScenario<CustomListActivity> scenario = ActivityScenario.launch(CustomListActivity.class)) {
            scenario.onActivity(activity -> {
                activity.db = db;

                String listName = "TestList";

                // Firestore write: add empty custom list with public access
                Map<String, Object> updateMap = new HashMap<>();
                updateMap.put("customList." + listName, new ArrayList<String>());
                updateMap.put("listPrivacy." + listName, true);

                db.collection("Users").document(testUserId)
                        .update(updateMap)
                        .addOnSuccessListener(unused -> writeLatch.countDown())
                        .addOnFailureListener(e -> {
                            fail("Failed to create custom list in Firestore: " + e.getMessage());
                            writeLatch.countDown();
                        });
            });
        }

        writeLatch.await(); // Wait for Firestore write

        // Validate the list was successfully added
        db.collection("Users").document(testUserId).get().addOnSuccessListener(documentSnapshot -> {
            assertTrue("Document exists", documentSnapshot.exists());
            Map<String, Object> data = documentSnapshot.getData();
            Map<String, Object> customList = (Map<String, Object>) data.get("customList");
            Map<String, Boolean> listPrivacy = (Map<String, Boolean>) data.get("listPrivacy");

            assertNotNull("customList should not be null", customList);
            assertNotNull("listPrivacy should not be null", listPrivacy);
            assertTrue("Custom list should contain the new list", customList.containsKey("TestList"));
            assertTrue("Privacy map should contain the new list", listPrivacy.containsKey("TestList"));
            assertTrue("Privacy for the new list should be true", listPrivacy.get("TestList"));

            readLatch.countDown();
        }).addOnFailureListener(e -> {
            fail("Failed to fetch Firestore document: " + e.getMessage());
            readLatch.countDown();
        });

        readLatch.await(); // Wait for Firestore read assertions
    }

    /**
     * Tests adding a hike to an existing custom list.
     * - Adds an empty test list first
     * - Uses HikeActivity to call addCustomHike()
     * - Verifies hike was written to Firestore
     */
    @Test
    public void addHikeToCustomList() throws InterruptedException {
        // Step 1: Initialize custom list with an empty array
        CountDownLatch initLatch = new CountDownLatch(1);
        Map<String, List<String>> tempList = new HashMap<>();
        tempList.put("testList", new ArrayList<>());

        db.collection("Users").document(testUserId).update("customList", tempList)
                .addOnSuccessListener(aVoid -> initLatch.countDown())
                .addOnFailureListener(e -> {
                    fail("Failed to update custom list: " + e.getMessage());
                    initLatch.countDown();
                });
        initLatch.await();

        // Step 2: Launch HikeActivity and call addCustomHike()
        CountDownLatch writeLatch = new CountDownLatch(1);
        try (ActivityScenario<HikeActivity> scenario = ActivityScenario.launch(HikeActivity.class)) {
            scenario.onActivity(activity -> {
                activity.db = db;

                // Simulate adding "Griffith Observatory" to "testList"
                Hike newHike = new Hike("Griffith Observatory");
                activity.addCustomHike(newHike, "testList");

                // Wait for Firestore update to finish
                db.collection("Users").document(testUserId).get().addOnSuccessListener(doc -> writeLatch.countDown());
            });
        }

        writeLatch.await(); // Wait for hike addition

        // Step 3: Verify hike is in the list
        CountDownLatch readLatch = new CountDownLatch(1);
        db.collection("Users").document(testUserId).get().addOnSuccessListener(documentSnapshot -> {
            assertTrue("Document exists", documentSnapshot.exists());
            Map<String, Object> data = documentSnapshot.getData();
            Map<String, Object> customList = (Map<String, Object>) data.get("customList");

            assertNotNull("Custom list should not be null", customList);

            List<String> hikeList = (List<String>) customList.get("testList");
            assertNotNull("testList should exist in customList", hikeList);
            assertTrue("testList should contain Griffith Observatory", hikeList.contains("Griffith Observatory"));

            readLatch.countDown();
        }).addOnFailureListener(e -> {
            fail("Failed to fetch custom list: " + e.getMessage());
            readLatch.countDown();
        });

        readLatch.await(); // Wait for read verification
    }

    /**
     * Cleans up after each test by deleting the test user and resetting hooks/resources.
     */
    @After
    public void tearDown() throws InterruptedException {
        if (testUserId != null) {
            db.collection("Users").document(testUserId).delete();
        }
        FirebaseAuth.getInstance().signOut();
        TestEnvironment.testHooks = null;
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.getIdlingResource());
    }
}