package com.example.gohiking_cs310;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This activity allows users to manage their personal hiking lists:
 * - Create named custom hike lists
 * - Toggle visibility (public/private) of each list
 * - Display user's custom lists and associated hikes
 * - Navigate back to profile or home map activity
 * - Securely handles logged-in user session via Firebase Authentication
 */
public class CustomListActivity extends AppCompatActivity {
    FirebaseFirestore db;
    public FirebaseAuth auth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lists);
        db = FirebaseFirestore.getInstance();

        // === Navigation Buttons ===

        // Go back to home (maps) page
        Button buttonBackToHome = findViewById(R.id.buttonBackHome);
        buttonBackToHome.setOnClickListener(v -> {
            Intent intent = new Intent(CustomListActivity.this, MapsActivity.class);
            startActivity(intent);
            finish();
        });

        // Go back to user profile page
        Button profile = findViewById(R.id.buttonBackToProfile);
        profile.setOnClickListener(v -> {
            Intent intent = new Intent(CustomListActivity.this, UserActivity.class);
            startActivity(intent);
            finish();
        });

        // Log out and return to home screen
        Button logoutButton = findViewById(R.id.buttonLogout);
        logoutButton.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(CustomListActivity.this, "Logged out successfully.", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(CustomListActivity.this, MapsActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        // === Custom List Creation ===

        // Add new custom list based on input field
        Button newList = findViewById(R.id.buttonCreateList);
        newList.setOnClickListener(v -> {
            EditText addList = findViewById(R.id.editTextCreateList);
            String listToAdd = addList.getText().toString().trim();
            if (!listToAdd.isEmpty()) {
                createCustomList(listToAdd);
            } else {
                Toast.makeText(CustomListActivity.this, "Please enter list name.", Toast.LENGTH_SHORT).show();
            }
        });

        // === Privacy Toggle ===

        // Toggle visibility (public/private) for a custom list
        Button togglePrivacy = findViewById(R.id.buttonPrivacyToggle);
        togglePrivacy.setOnClickListener(v -> {
            EditText privacy = findViewById(R.id.editTextPrivacy);
            String listToUpdate = privacy.getText().toString().trim();
            if (!listToUpdate.isEmpty()) {
                togglePrivacy(listToUpdate);
            } else {
                Toast.makeText(CustomListActivity.this, "Please enter list name.", Toast.LENGTH_SHORT).show();
            }
        });

        // === View My Lists ===

        // Display the current user's custom lists
        Button myLists = findViewById(R.id.buttonMyLists);
        myLists.setOnClickListener(v -> {
            String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            showMyLists(currentUserId);
        });
    }

    /**
     * Creates a new custom list with the provided name for the current user.
     * If it doesn't exist already, adds it to Firestore with default privacy.
     */
    void createCustomList(String ListName) {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (ListName == null) {
            Toast.makeText(CustomListActivity.this, "List name is missing.", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("Users").document(currentUserId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Map<String, List<String>> customList = (Map<String, List<String>>) documentSnapshot.get("customList");
                        List<String> hikeList = new ArrayList<>();

                        if (!customList.containsKey(ListName)) {
                            customList.put(ListName, hikeList);
                            db.collection("Users").document(currentUserId)
                                    .update("customList", customList)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(CustomListActivity.this, ListName + " added to your custom lists", Toast.LENGTH_SHORT).show();
                                        updatePrivacy(ListName, currentUserId);
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(CustomListActivity.this, "Failed to update hike list.", Toast.LENGTH_SHORT).show();
                                    });
                        } else {
                            Toast.makeText(CustomListActivity.this, ListName + " is already in your custom list", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(CustomListActivity.this, "Failed to find user information.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(CustomListActivity.this, "Failed to fetch user information.", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Updates the privacy setting for a new custom list.
     * Sets default to public (true) when list is first created.
     */
    private void updatePrivacy(String list, String currUser) {
        db.collection("Users").document(currUser).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Map<String, Boolean> privacy = (Map<String, Boolean>) documentSnapshot.get("listPrivacy");
                        privacy.put(list, true); // Default is public
                        db.collection("Users").document(currUser)
                                .update("listPrivacy", privacy);
                    }
                });
    }

    /**
     * Toggles the privacy setting (public/private) for the given list.
     * Only accessible to the list owner.
     */
    void togglePrivacy(String list) {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (list == null) {
            Toast.makeText(CustomListActivity.this, "List name is missing.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TestEnvironment.testHooks != null) TestEnvironment.testHooks.increment();

        db.collection("Users").document(currentUserId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Map<String, Boolean> privacy = (Map<String, Boolean>) documentSnapshot.get("listPrivacy");
                        if (!privacy.containsKey(list)) {
                            Toast.makeText(CustomListActivity.this, "please enter a valid list", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        privacy.compute(list, (k, pub) -> !pub); // Flip value

                        db.collection("Users").document(currentUserId)
                                .update("listPrivacy", privacy)
                                .addOnSuccessListener(aVoid -> {
                                    if (privacy.get(list)) {
                                        Toast.makeText(CustomListActivity.this, list + " privacy updated to public", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(CustomListActivity.this, list + " privacy updated to private", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                });

        if (TestEnvironment.testHooks != null) TestEnvironment.testHooks.decrement();
    }

    /**
     * Fetches and displays the current user's custom hiking lists in an AlertDialog.
     */
    private void showMyLists(String userId) {
        db.collection("Users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Map<String, List<String>> customList = (Map<String, List<String>>) documentSnapshot.get("customList");

                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle("Your Lists");

                        String[] lists = new String[customList.size()];
                        int i = 0;

                        if (customList == null || customList.isEmpty()) {
                            builder.setMessage("You have no custom lists!");
                            builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
                        } else {
                            for (String hike : customList.keySet()) {
                                lists[i++] = hike;
                            }

                            builder.setItems(lists, (dialog, which) -> {
                                String selectedList = lists[which];
                                showCustomHikes(selectedList, userId);
                            });

                            builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
                        }
                        builder.create().show();
                    }
                });
    }

    /**
     * Displays the contents of a specific custom list (if public or owned by the user).
     * Shows list content or appropriate privacy message in an AlertDialog.
     */
    private void showCustomHikes(String listName, String userId) {
        db.collection("Users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Map<String, List<String>> customList = (Map<String, List<String>>) documentSnapshot.get("customList");
                        Map<String, Boolean> privacy = (Map<String, Boolean>) documentSnapshot.get("listPrivacy");
                        Boolean isPublic = privacy.get(listName);
                        StringBuilder hikeList = new StringBuilder();
                        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                        if (customList.get(listName) == null || customList.get(listName).isEmpty()) {
                            if (isPublic || userId.equals(currentUserId)) {
                                hikeList.append("No hikes in this list!");
                            } else {
                                hikeList.append("This list is Private!");
                            }
                        } else {
                            if (isPublic || userId.equals(currentUserId)) {
                                for (String hike : customList.get(listName)) {
                                    hikeList.append(hike).append("\n");
                                }
                            } else {
                                hikeList.append("This hike is Private!");
                            }
                        }

                        new android.app.AlertDialog.Builder(CustomListActivity.this)
                                .setTitle("Custom Hikes")
                                .setMessage(hikeList.toString())
                                .setPositiveButton("Close", (dialog, which) -> dialog.dismiss())
                                .show();
                    } else {
                        Toast.makeText(CustomListActivity.this, "No custom hikes added.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(CustomListActivity.this, "Failed to fetch custom hikes.", Toast.LENGTH_SHORT).show()
                );
    }
}