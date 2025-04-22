package com.example.gohiking_cs310;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * UserActivity serves as the main profile and social hub for a logged-in user.
 * Features include:
 * - Viewing and editing personal hike lists
 * - Searching for hikes
 * - Viewing other users’ hikes and reviews
 * - Adding friends by email
 * - Logging out and navigating between activities
 */
public class UserActivity extends AppCompatActivity {

    // Firebase Firestore instance for accessing user and hike data
    private FirebaseFirestore db;

    // Data structures for hike search autocomplete
    private ArrayList<String> hikesList = new ArrayList<>();
    private ArrayAdapter<String> hikeAdapter;

    // Autocomplete search fields for hikes and friends
    private AutoCompleteTextView autoCompleteHikeSearch;
    private AutoCompleteTextView autoCompleteFriendSearch;

    // Stores list of friends as pairs of (userId, email)
    private List<Pair<String, String>> friendsList = new ArrayList<>();

    private Boolean pub; // (May be used for profile/list privacy, context-dependent)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        setTheme(R.style.Theme_GoHiking_CS310);

        // Initialize Firestore instance
        db = FirebaseFirestore.getInstance();

        // Setup autocomplete text fields and adapter for hike search
        autoCompleteHikeSearch = findViewById(R.id.editTextSearchHike);
        autoCompleteFriendSearch = findViewById(R.id.editTextAddFriend);
        hikeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, hikesList);
        autoCompleteHikeSearch.setAdapter(hikeAdapter);

        // Show dropdown and clear hint when hike field is focused
        autoCompleteHikeSearch.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                autoCompleteHikeSearch.setHint("");
                autoCompleteHikeSearch.showDropDown();
            } else if (autoCompleteHikeSearch.getText().toString().isEmpty()) {
                autoCompleteHikeSearch.setHint("Add Friend");
            }
        });

        // Clear or restore hint for friend search field
        autoCompleteFriendSearch.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                autoCompleteFriendSearch.setHint("");
            } else if (autoCompleteFriendSearch.getText().toString().isEmpty()) {
                autoCompleteHikeSearch.setHint("Add Friend");
            }
        });

        // Load suggestions dynamically as the user types
        autoCompleteHikeSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                loadHikeSuggestions(s.toString());
            }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(android.text.Editable s) {}
        });

        // When a hike is selected from the dropdown, display it and launch query
        autoCompleteHikeSearch.setOnItemClickListener((parent, view, position, id) -> {
            String selectedHike = hikeAdapter.getItem(position);
            Toast.makeText(UserActivity.this, "Selected Hike: " + selectedHike, Toast.LENGTH_SHORT).show();
            hikeQuery(selectedHike);
        });

        // Handle user logout and return to home screen
        Button logoutButton = findViewById(R.id.buttonLogOut);
        logoutButton.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut(); // Invalidate Firebase session
            Toast.makeText(UserActivity.this, "Logged out successfully.", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(UserActivity.this, MapsActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish(); // Prevent back navigation
        });

        Button buttonBackToHome = findViewById(R.id.buttonBackHome);
        buttonBackToHome.setOnClickListener(v -> {
            Intent intent = new Intent(UserActivity.this, MapsActivity.class);
            startActivity(intent);
            finish();
        });

        // Navigate to Custom List screen
        Button buttonCustom = findViewById(R.id.buttonCustomList);
        buttonCustom.setOnClickListener(v -> {
            Intent intent = new Intent(UserActivity.this, CustomListActivity.class);
            startActivity(intent);
            finish();
        });

        // Add friend based on email input
        Button addFriend = findViewById(R.id.buttonAddFriend);
        addFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText emailInput = findViewById(R.id.editTextAddFriend);
                String email = emailInput.getText().toString().trim();
                if (!email.isEmpty()) {
                    queryUserByEmail(email); // Search and add friend
                } else {
                    Toast.makeText(UserActivity.this, "Please enter an email.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Set up current user info display (email + username)
        TextView tv = findViewById(R.id.username);
        TextView name = findViewById(R.id.textView2);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (user != null) {
            String email = user.getEmail();

            // Fetch username from Firestore using current user ID
            db.collection("Users").document(user.getUid()).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String username = documentSnapshot.getString("username");
                            tv.setText(email);
                            name.setText(username);
                        } else {
                            tv.setText("No username found for this user");
                        }
                    })
                    .addOnFailureListener(e -> {
                        tv.setText("Failed to retrieve user information");
                    });
        } else {
            tv.setText("No user is currently logged in");
        }

        // Load and display friend list
        Button myFriendsButton = findViewById(R.id.buttonMyFriends);
        myFriendsButton.setOnClickListener(v -> fetchAndShowFriends());

        // Manually search for hike by name
        Button searchButton = findViewById(R.id.search_button);
        searchButton.setOnClickListener(v -> {
            EditText searchInput = findViewById(R.id.editTextSearchHike);
            String searchQuery = searchInput.getText().toString().trim();
            if (!searchQuery.isEmpty()) {
                hikeQuery(searchQuery);
            } else {
                Toast.makeText(UserActivity.this, "Please enter a search term.", Toast.LENGTH_SHORT).show();
            }
        });

        Button backToHomeButton = findViewById(R.id.buttonBackHome);
        backToHomeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserActivity.this, MapsActivity.class);
                startActivity(intent);
            }
        });

        // Display user’s own hike lists
        Button myHikes = findViewById(R.id.buttonMyHikes);
        myHikes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                showFriendsLists(currentUserId); // Reuses the list viewer for self
            }
        });
    }

    /**
     * Loads hardcoded hike suggestions to support local testing and dropdown search.
     * This can later be replaced by Firestore-based dynamic loading.
     */
    private void loadHikeSuggestions(String query) {
        hikesList.clear();
        hikesList.add("Griffith Observatory");
        hikesList.add("Hollywood Sign");
        hikesList.add("Palos Verdes Estates Shoreline Preserve");
        hikesList.add("Sycamore Canyon Trailhead");
        hikesList.add("Temescal Canyon Falls");
        hikesList.add("Trail Canyon Falls");
        hikeAdapter.notifyDataSetChanged(); // Notify UI to update dropdown
    }

    /**
     * Queries the Firestore Users collection to find a user by email.
     * If found, the user is added as a mutual friend with the current user.
     */
    private void queryUserByEmail(String email) {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db.collection("Users")
                .whereEqualTo("email", email)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (!querySnapshot.isEmpty()) {
                            for (QueryDocumentSnapshot document : querySnapshot) {
                                String friendUserId = document.getId();
                                String foundEmail = document.getString("email");
                                String username = document.getString("username");

                                // Add friend to current user's list
                                db.collection("Users").document(currentUserId)
                                        .update("friends", FieldValue.arrayUnion(friendUserId))
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(UserActivity.this, email + " added as a friend", Toast.LENGTH_SHORT).show();
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(UserActivity.this, "Failed to add friend.", Toast.LENGTH_SHORT).show();
                                        });

                                // Also add current user to the friend's list
                                db.collection("Users").document(friendUserId)
                                        .update("friends", FieldValue.arrayUnion(currentUserId))
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(UserActivity.this, "Failed to add current user as friend.", Toast.LENGTH_SHORT).show();
                                        });
                            }
                        } else {
                            Toast.makeText(UserActivity.this, "No user found with this email.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(UserActivity.this, "Failed to fetch user data.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Retrieves the current user's list of friends and displays a dialog
     * once all friend emails are fetched via Firestore lookups.
     */
    private void fetchAndShowFriends() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db.collection("Users").document(currentUserId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        ArrayList<String> friends = (ArrayList<String>) documentSnapshot.get("friends");
                        if (friends != null && !friends.isEmpty()) {
                            fetchFriendDetails(friends);
                        } else {
                            Toast.makeText(UserActivity.this, "You have no friends added.", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(UserActivity.this, "Failed to fetch friends.", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Fetches detailed friend information for a list of friend IDs.
     * Once all friends are retrieved, displays them in a dialog.
     */
    private void fetchFriendDetails(ArrayList<String> friendIds) {
        friendsList.clear();
        for (String friendId : friendIds) {
            db.collection("Users").document(friendId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String friendEmail = documentSnapshot.getString("email");
                            friendsList.add(new Pair<>(friendId, friendEmail));

                            // Once all friend documents are retrieved, display them
                            if (friendsList.size() == friendIds.size()) {
                                showFriendsDialog(friendIds);
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(UserActivity.this, "Failed to fetch friend details.", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    /**
     * Builds and displays a dialog with a list of the user's friends.
     * Allows interaction with each friend (view their reviews/lists).
     */
    private void showFriendsDialog(ArrayList<String> friendIds) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Your Friends");

        String[] friendsArray = new String[friendsList.size()];
        for (int i = 0; i < friendsList.size(); i++) {
            friendsArray[i] = friendsList.get(i).second; // friend email
        }

        builder.setItems(friendsArray, (dialog, which) -> {
            String selectedFriendId = friendsList.get(which).first;
            String selectedFriendEmail = friendsList.get(which).second;
            showFriendOptions(selectedFriendEmail, selectedFriendId);
        });

        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    /**
     * Queries hike data by name and navigates to the hike details page if found.
     * Constructs a Hike object from Firestore document fields.
     */
    private void hikeQuery(String hike) {
        if (TestEnvironment.testHooks != null) TestEnvironment.testHooks.increment();

        db.collection("Hikes")
                .whereEqualTo("Name", hike)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot snapshot = task.getResult();

                        if (!snapshot.isEmpty()) {
                            for (QueryDocumentSnapshot document : snapshot) {
                                // Retrieve hike details from Firestore
                                String hikeID = document.getId();
                                Boolean bathrooms = document.getBoolean("Bathrooms") != null ? document.getBoolean("Bathrooms") : false;
                                Long difficulty = document.getLong("Difficulty") != null ? document.getLong("Difficulty") : 0;
                                Long lat = document.getLong("Lat") != null ? document.getLong("Lat") : 0;
                                Long hikelong = document.getLong("Long") != null ? document.getLong("Long") : 0;
                                String name = document.getString("Name") != null ? document.getString("Name") : "Unknown";
                                Boolean parking = document.getBoolean("Parking") != null ? document.getBoolean("Parking") : false;
                                ArrayList<Double> ratings = document.get("Ratings") != null ? (ArrayList<Double>) document.get("Ratings") : new ArrayList<>();
                                List<Review> reviews = document.get("Reviews") != null ? (List<Review>) document.get("Reviews") : new ArrayList<>();
                                String conditions = document.getString("Trail Conditions") != null ? document.getString("Trail Conditions") : "Not available";
                                Boolean markers = document.getBoolean("Trail Markers") != null ? document.getBoolean("Trail Markers") : false;
                                Boolean trash = document.getBoolean("Trash Cans") != null ? document.getBoolean("Trash Cans") : false;
                                Boolean water = document.getBoolean("Water Fountains") != null ? document.getBoolean("Water Fountains") : false;
                                Boolean wifi = document.getBoolean("WiFi") != null ? document.getBoolean("WiFi") : false;

                                // Create and pass Hike object to next activity
                                Hike queriedHike = new Hike(
                                        hikeID,
                                        name,
                                        difficulty.intValue(),
                                        lat.doubleValue(),
                                        hikelong.doubleValue(),
                                        bathrooms,
                                        parking,
                                        ratings,
                                        reviews,
                                        conditions,
                                        markers,
                                        trash,
                                        water,
                                        wifi
                                );

                                if (queriedHike != null) {
                                    Log.d("HikeActivity", "Hike details loaded: " + queriedHike.getName());
                                } else {
                                    Log.e("HikeActivity", "Hike object is null!");
                                }

                                Intent intent = new Intent(UserActivity.this, HikeActivity.class);
                                intent.putExtra("hikeObject", queriedHike);
                                startActivity(intent);
                            }
                        } else {
                            ToastUtil.showToast(UserActivity.this, "No hike found");
                        }
                    }
                    if (TestEnvironment.testHooks != null) TestEnvironment.testHooks.decrement();
                });
    }

    /**
     * Presents options for interacting with a selected friend (view reviews or lists).
     */
    private void showFriendOptions(String friendEmail, String selectedFriendId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Please select an option");

        String[] options = new String[2];
        options[0] = "View " + friendEmail + "'s reviews";
        options[1] = "View " + friendEmail + "'s custom lists";

        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                showFriendsReviews(friendEmail, selectedFriendId);
            } else {
                showFriendsLists(selectedFriendId);
            }
        });

        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    /**
     * Loads a friend’s (or current user’s) custom hike lists from Firestore.
     * Applies privacy filtering based on ownership and visibility flags.
     */
    private void showFriendsLists(String userId) {
        db.collection("Users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        Map<String, List<String>> customList = (Map<String, List<String>>) documentSnapshot.get("customList");

                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle(userId.equals(currentUserId) ? "Your List" : "Your friend's Lists");

                        if (customList == null || customList.isEmpty()) {
                            builder.setMessage(userId.equals(currentUserId) ? "You have no custom lists!" : "This user has no lists!");
                            builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
                            builder.create().show();
                            return;
                        }

                        String[] lists = new String[customList.size()];
                        int i = 0;
                        for (String hike : customList.keySet()) {
                            lists[i++] = hike;
                        }

                        builder.setItems(lists, (dialog, which) -> {
                            String selectedList = lists[which];
                            showCustomHikes(selectedList, userId);
                        });

                        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
                        builder.create().show();
                    }
                });
    }

    /**
     * Displays all reviews associated with a given user ID in a dialog.
     */
    private void showFriendsReviews(String friendEmail, String userId) {
        db.collection("Users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<Map<String, Object>> reviewList = (List<Map<String, Object>>) documentSnapshot.get("userReviews");
                        StringBuilder userReviews = new StringBuilder();

                        if (reviewList == null || reviewList.isEmpty()) {
                            userReviews.append("No reviews!");
                        } else {
                            for (Map<String, Object> reviewMap : reviewList) {
                                String hikeName = (String) reviewMap.get("hikeName");
                                String reviewText = (String) reviewMap.get("reviewText");
                                Number ratingNumber = (Number) reviewMap.get("rating");
                                Double rating = ratingNumber != null ? ratingNumber.doubleValue() : null;

                                if (hikeName != null && reviewText != null && rating != null) {
                                    userReviews.append("Hike Name: ").append(hikeName)
                                            .append("\nReview: ").append(reviewText)
                                            .append("\nRating: ").append(rating)
                                            .append("\n\n");
                                }
                            }
                        }

                        new android.app.AlertDialog.Builder(UserActivity.this)
                                .setTitle(friendEmail + "'s reviews")
                                .setMessage(userReviews.toString())
                                .setPositiveButton("Close", (dialog, which) -> dialog.dismiss())
                                .show();
                    } else {
                        Toast.makeText(UserActivity.this, "No custom hikes added.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(UserActivity.this, "Failed to fetch custom hikes.", Toast.LENGTH_SHORT).show()
                );
    }

    /**
     * Displays the hikes stored in a user's selected custom list.
     * Respects list privacy based on owner and public flag.
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

                        // Display logic based on privacy flag and user ownership
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
                                hikeList.append("This list is Private!");
                            }
                        }

                        new android.app.AlertDialog.Builder(UserActivity.this)
                                .setTitle("Custom Hikes")
                                .setMessage(hikeList.toString())
                                .setPositiveButton("Close", (dialog, which) -> dialog.dismiss())
                                .show();
                    } else {
                        Toast.makeText(UserActivity.this, "No custom hikes added.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(UserActivity.this, "Failed to fetch custom hikes.", Toast.LENGTH_SHORT).show()
                );
    }
}