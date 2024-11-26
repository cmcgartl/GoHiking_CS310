package com.example.gohiking_cs310;


import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static com.google.common.base.Verify.verify;


import androidx.appcompat.app.AlertDialog;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;


import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
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


import static org.hamcrest.CoreMatchers.any;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;




import static java.util.regex.Pattern.matches;


import android.content.DialogInterface;


@RunWith(AndroidJUnit4.class)
public class CustomListActivityFirebaseTest {
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String testUserId;

    @Before
    public void setUp() throws InterruptedException {
        // Initialize Firebase components
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();


        // Sign in as an anonymous test user
        auth.signInAnonymously().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = auth.getCurrentUser();
                assertNotNull("Test user should not be null", user);
                testUserId = user.getUid();


                // Set up initial data for the test user
                Map<String, Object> initialData = new HashMap<>();
                initialData.put("customList", new HashMap<>());
                initialData.put("listPrivacy", new HashMap<>());
                db.collection("Users").document(testUserId).set(initialData);
            } else {
                fail("Failed to sign in anonymously for testing");
            }
        });
        // Wait for the setup to complete
        Thread.sleep(3000);
    }
    @Test
    public void testCreateCustomList() throws InterruptedException {
        try (ActivityScenario<CustomListActivity> scenario = ActivityScenario.launch(CustomListActivity.class)) {
            scenario.onActivity(activity -> {
                activity.db = db;
                String listName = "TestList";
                activity.createCustomList(listName);
            });
            Thread.sleep(3000);
            db.collection("Users").document(testUserId).get().addOnSuccessListener(documentSnapshot -> {
                assertTrue("Document exists", documentSnapshot.exists());
                Map<String, Object> data = documentSnapshot.getData();
                Map<String, Object> customList = (Map<String, Object>) data.get("customList");
                Map<String, Boolean> listPrivacy = (Map<String, Boolean>) data.get("listPrivacy");
                assertTrue("Custom list should contain the new list", customList.containsKey("TestList"));
                assertTrue("Privacy map should contain the new list", listPrivacy.containsKey("TestList"));
                assertTrue("Privacy for the new list should be true", listPrivacy.get("TestList"));
            }).addOnFailureListener(e -> fail("Failed to fetch Firestore document: " + e.getMessage()));
        }
    }

    @Test
    public void addHikeToCustomList() throws InterruptedException {
        Map<String, List<String>> tempList = new HashMap<>();
        List<String> tempStrings = new ArrayList<>();
        tempList.put("testList", tempStrings);
        db.collection("Users").document( testUserId).update("customList", tempList)
                .addOnSuccessListener(aVoid -> {
                    System.out.println("Custom list updated successfully.");
                })
                .addOnFailureListener(e -> {
                    System.out.println("Failed to update custom list: " + e.getMessage());
                });
        try (ActivityScenario<HikeActivity> scenario = ActivityScenario.launch(HikeActivity.class)) {
            scenario.onActivity(activity -> {
                activity.db = db;
                Hike newHike = new Hike("Griffith Observatory");
                Hike newHike2 = new Hike("Hollywood Sign");
                Hike newHike3 = new Hike("Temescal Canyon Falls");
                activity.addCustomHike(newHike, "testList");
                //activity.addCustomHike(newHike2, "testList");
                //activity.addCustomHike(newHike3, "testList");
            });
            Thread.sleep(3000);
            db.collection("Users").document(testUserId).get().addOnSuccessListener(documentSnapshot -> {
                assertTrue("Document exists", documentSnapshot.exists());
                Map<String, Object> data = documentSnapshot.getData();
                Map<String, Object> customList = (Map<String, Object>) data.get("customList");
                boolean containsG = false;
                boolean containsInvalid = false;
                List<String> hikeList = (List<String>) customList.get("testList");
                for(int i = 0; i < hikeList.size(); i++) {
                    if (hikeList.get(i).equals("Griffith Observatory")) {
                        containsG = true;
                    }
                }
                assertTrue("Testlist should contain Griffith Observatory", containsG);
                //assertTrue("Testlist should contain Hollywood Sign", containsH);
                //assertTrue("Testlist should contain Temescal Canyon Flls", containsT);
            }).addOnFailureListener(e -> fail("Failed to fetch Firestore document: " + e.getMessage()));
        }
    }


    @Test
    public void testTogglePrivacy() throws InterruptedException {
        // Initialize Firestore data
        Map<String, List<String>> tempList = new HashMap<>();
        Map<String, Boolean> tempPrivacy = new HashMap<>();
        boolean pub = true;
        List<String> tempStrings = new ArrayList<>();
        tempList.put("HikeList1", tempStrings);
        tempPrivacy.put("HikeList1", pub);
        db.collection("Users").document( testUserId).update("customList", tempList)
                .addOnSuccessListener(aVoid -> {
                    System.out.println("Custom list updated successfully.");
                })
                .addOnFailureListener(e -> {
                    System.out.println("Failed to update custom list: " + e.getMessage());
                });
        db.collection("Users").document( testUserId).update("listPrivacy", tempPrivacy)
                .addOnSuccessListener(aVoid -> {
                    System.out.println("Custom list updated successfully.");
                })
                .addOnFailureListener(e -> {
                    System.out.println("Failed to update custom list: " + e.getMessage());
                });
        // Launch activity and toggle privacy
        CountDownLatch toggleLatch = new CountDownLatch(1);
        try (ActivityScenario<CustomListActivity> scenario = ActivityScenario.launch(CustomListActivity.class)) {
            scenario.onActivity(activity -> {
                activity.db = db;
                activity.togglePrivacy("HikeList1");
                toggleLatch.countDown();
            });
        }
        toggleLatch.await(); // Wait for togglePrivacy to complete


        // Verify the updated privacy
        CountDownLatch verifyLatch = new CountDownLatch(1);
        db.collection("Users").document(testUserId).get().addOnSuccessListener(documentSnapshot -> {
            assertTrue("Document exists", documentSnapshot.exists());
            Map<String, Object> data = documentSnapshot.getData();
            assertNotNull("Data should not be null", data);


            Map<String, Boolean> listPrivacy = (Map<String, Boolean>) data.get("listPrivacy");
            assertNotNull("listPrivacy should not be null", listPrivacy);
            assertTrue("Privacy map should contain the new list", listPrivacy.containsKey("HikeList1"));
            assertTrue("Privacy for HikeList1 should be false", !listPrivacy.get("HikeList1"));


            verifyLatch.countDown();
        }).addOnFailureListener(e -> fail("Failed to fetch Firestore document: " + e.getMessage()));
        verifyLatch.await();
    }
    @After
    public void tearDown() throws InterruptedException {
        // Clean up test data
        if (testUserId != null) {
            db.collection("Users").document(testUserId).delete();
        }


        // Wait for cleanup to complete
        Thread.sleep(2000);
    }
}
