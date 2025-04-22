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

/**
 * TogglePrivacyTest verifies the functionality of toggling the privacy status
 * for a user's custom hike list in Firestore.
 *
 * This test ensures:
 * - Privacy toggling correctly updates Firestore.
 * - Invalid and null list names are handled gracefully.
 */
@RunWith(AndroidJUnit4.class)
public class TogglePrivacyTest {
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String testUserId;

    /**
     * Sets up a new test user and initializes Firestore document fields for testing.
     * Uses CountDownLatch to ensure async operations are complete before test methods run.
     */
    @Before
    public void setUp() throws InterruptedException {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Create unique email for the test user
        String randomEmail = "testuser_" + System.currentTimeMillis() + "@example.com";
        String randomPassword = "password123";

        // Register Espresso idling resource hook for Firebase async sync
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

        // Create test Firebase user and initialize empty custom list data
        CountDownLatch userCreatedLatch = new CountDownLatch(1);
        CountDownLatch firestoreInitLatch = new CountDownLatch(1);

        auth.createUserWithEmailAndPassword(randomEmail, randomPassword)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        testUserId = user.getUid();

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
     * Tests the privacy toggle behavior for:
     * - A valid list
     * - An invalid list
     * - A null list
     */
    @Test
    public void testTogglePrivacy() throws InterruptedException {
        // Initialize a list with privacy set to true
        Map<String, List<String>> tempList = new HashMap<>();
        Map<String, Boolean> tempPrivacy = new HashMap<>();
        List<String> tempStrings = new ArrayList<>();
        tempList.put("HikeList1", tempStrings);
        tempPrivacy.put("HikeList1", true);

        // Set initial values in Firestore
        CountDownLatch latch = new CountDownLatch(1);
        db.collection("Users").document(testUserId).set(new HashMap<String, Object>() {{
            put("customList", tempList);
            put("listPrivacy", tempPrivacy);
        }}).addOnSuccessListener(aVoid -> latch.countDown());
        latch.await();

        // === Toggle privacy for a valid list ===
        CountDownLatch Latch1 = new CountDownLatch(1);
        try (ActivityScenario<CustomListActivity> scenario = ActivityScenario.launch(CustomListActivity.class)) {
            scenario.onActivity(activity -> {
                activity.db = db;
                activity.togglePrivacy("HikeList1"); // should flip from true -> false
                Latch1.countDown();
            });
        }
        Latch1.await();

        // Verify that privacy was successfully toggled to false
        CountDownLatch latch2 = new CountDownLatch(1);
        db.collection("Users").document(testUserId).get().addOnSuccessListener(documentSnapshot -> {
            Map<String, Object> data = documentSnapshot.getData();
            Map<String, Boolean> listPrivacy = (Map<String, Boolean>) data.get("listPrivacy");
            assertNotNull("listPrivacy should not be null", listPrivacy);
            assertTrue("Privacy map should contain the list", listPrivacy.containsKey("HikeList1"));
            assertFalse("Privacy for HikeList1 should now be false", listPrivacy.get("HikeList1"));
            latch2.countDown();
        });
        latch2.await();

        // === Toggle privacy with an invalid list name ===
        CountDownLatch Latch3 = new CountDownLatch(1);
        try (ActivityScenario<CustomListActivity> scenario = ActivityScenario.launch(CustomListActivity.class)) {
            scenario.onActivity(activity -> {
                activity.db = db;
                activity.togglePrivacy("InvalidList"); // should be ignored
                Latch3.countDown();
            });
        }
        Latch3.await();

        // Confirm privacy remains unchanged for existing list
        CountDownLatch Latch4 = new CountDownLatch(1);
        db.collection("Users").document(testUserId).get().addOnSuccessListener(documentSnapshot -> {
            Map<String, Object> data = documentSnapshot.getData();
            Map<String, Boolean> listPrivacy = (Map<String, Boolean>) data.get("listPrivacy");
            assertNotNull("listPrivacy should not be null", listPrivacy);
            assertFalse("Privacy for HikeList1 should remain false", listPrivacy.get("HikeList1"));
            Latch4.countDown();
        });
        Latch4.await();

        // === Toggle privacy with a null list name ===
        CountDownLatch Latch5 = new CountDownLatch(1);
        try (ActivityScenario<CustomListActivity> scenario = ActivityScenario.launch(CustomListActivity.class)) {
            scenario.onActivity(activity -> {
                activity.db = db;
                activity.togglePrivacy(null); // should be ignored
                Latch5.countDown();
            });
        }
        Latch5.await();

        // Confirm privacy is still unchanged
        CountDownLatch Latch6 = new CountDownLatch(1);
        db.collection("Users").document(testUserId).get().addOnSuccessListener(documentSnapshot -> {
            Map<String, Object> data = documentSnapshot.getData();
            Map<String, Boolean> listPrivacy = (Map<String, Boolean>) data.get("listPrivacy");
            assertNotNull("listPrivacy should not be null", listPrivacy);
            assertFalse("Privacy for HikeList1 should still be false", listPrivacy.get("HikeList1"));
            Latch6.countDown();
        });
        Latch6.await();
    }

    /**
     * Cleans up by signing out and deleting the test user document.
     */
    @After
    public void tearDown() throws InterruptedException {
        FirebaseAuth.getInstance().signOut();
        TestEnvironment.testHooks = null;
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.getIdlingResource());

        if (testUserId != null) {
            db.collection("Users").document(testUserId).delete();
        }
    }
}