package com.example.gohiking_cs310;


import androidx.test.core.app.ActivityScenario;
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

    @Before
    public void setUp() throws InterruptedException {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        auth.signInAnonymously().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = auth.getCurrentUser();
                testUserId = user.getUid();
                Map<String, Object> initialData = new HashMap<>();
                initialData.put("customList", new HashMap<>());
                initialData.put("listPrivacy", new HashMap<>());
                db.collection("Users").document(testUserId).set(initialData);
            }
        });
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
            });
            Thread.sleep(3000);
            db.collection("Users").document(testUserId).get().addOnSuccessListener(documentSnapshot -> {
                assertTrue("Document exists", documentSnapshot.exists());
                Map<String, Object> data = documentSnapshot.getData();
                Map<String, Object> customList = (Map<String, Object>) data.get("customList");
                boolean containsG = false;
                List<String> hikeList = (List<String>) customList.get("testList");
                for(int i = 0; i < hikeList.size(); i++) {
                    if (hikeList.get(i).equals("Griffith Observatory")) {
                        containsG = true;
                    }
                }
                assertTrue("Testlist should contain Griffith Observatory", containsG);
            });
        }
    }
    @Test
    public void testTogglePrivacy() throws InterruptedException {
        Map<String, List<String>> tempList = new HashMap<>();
        Map<String, Boolean> tempPrivacy = new HashMap<>();
        List<String> tempStrings = new ArrayList<>();
        tempList.put("HikeList1", tempStrings);
        tempPrivacy.put("HikeList1", true);

        CountDownLatch latch = new CountDownLatch(1);
        db.collection("Users").document(testUserId).set(new HashMap<String, Object>() {{
            put("customList", tempList);
            put("listPrivacy", tempPrivacy);
        }}).addOnSuccessListener(aVoid -> latch.countDown());
        latch.await();

        CountDownLatch Latch1 = new CountDownLatch(1);
        try (ActivityScenario<CustomListActivity> scenario = ActivityScenario.launch(CustomListActivity.class)) {
            scenario.onActivity(activity -> {
                activity.db = db;
                activity.togglePrivacy("HikeList1");
                Latch1.countDown();
            });
        }
        Latch1.await();

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

        // Invalid list name
        CountDownLatch Latch3 = new CountDownLatch(1);
        try (ActivityScenario<CustomListActivity> scenario = ActivityScenario.launch(CustomListActivity.class)) {
            scenario.onActivity(activity -> {
                activity.db = db;
                activity.togglePrivacy("InvalidList");
                Latch3.countDown();
            });
        }
        Latch3.await();

        CountDownLatch Latch4 = new CountDownLatch(1);
        db.collection("Users").document(testUserId).get().addOnSuccessListener(documentSnapshot -> {
            Map<String, Object> data = documentSnapshot.getData();
            Map<String, Boolean> listPrivacy = (Map<String, Boolean>) data.get("listPrivacy");
            assertNotNull("listPrivacy should not be null", listPrivacy);
            assertFalse("Privacy for HikeList1 should remain false", listPrivacy.get("HikeList1"));
            Latch4.countDown();
        });
        Latch4.await();

        //Null list name
        CountDownLatch Latch5 = new CountDownLatch(1);
        try (ActivityScenario<CustomListActivity> scenario = ActivityScenario.launch(CustomListActivity.class)) {
            scenario.onActivity(activity -> {
                activity.db = db;
                activity.togglePrivacy(null);
                Latch5.countDown();
            });
        }
        Latch5.await();

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
    @After
    public void tearDown() throws InterruptedException {
        // Clean up test data
        if (testUserId != null) {
            db.collection("Users").document(testUserId).delete();
        }
        Thread.sleep(2000);
    }
}
