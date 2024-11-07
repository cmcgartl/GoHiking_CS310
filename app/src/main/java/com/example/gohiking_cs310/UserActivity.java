package com.example.gohiking_cs310;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.DocumentSnapshot;


import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class UserActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private ArrayList<String> friendsList = new ArrayList<>();
    private ArrayList<String> hikesList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        db = FirebaseFirestore.getInstance();

        Button buttonBackToHome = findViewById(R.id.buttonBackHome);
        buttonBackToHome.setOnClickListener(v -> {
            Intent intent = new Intent(UserActivity.this, MapsActivity.class);
            startActivity(intent);
            finish(); // Optional: Close the current activity if needed
        });

        Button addFriend = findViewById(R.id.buttonAddFriend);
        addFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText emailInput = findViewById(R.id.editTextAddFriend);
                String email = emailInput.getText().toString().trim();
                if (!email.isEmpty()) {
                    queryUserByEmail(email);
                } else {
                    Toast.makeText(UserActivity.this, "Please enter an email.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        TextView tv = findViewById(R.id.username);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (user != null) {
            String email = user.getEmail();

            // Access the Firestore document for the current user
            db.collection("Users").document(user.getUid()).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String username = documentSnapshot.getString("username"); // Retrieve the "username" field
                            tv.setText("Basic User Information: \n Full Name: " + username + "\nEmail: " + email);
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

        Button myFriendsButton = findViewById(R.id.buttonMyFriends);
        myFriendsButton.setOnClickListener(v -> fetchAndShowFriends());

        Button logout = findViewById(R.id.buttonLogOut);
        logout.setOnClickListener(v -> {
            // Log out Firebase Auth current user
            FirebaseAuth.getInstance().signOut();

            // Redirect to MapsActivity (or another activity, like LoginActivity, if you want to log them out completely)
            Intent intent = new Intent(UserActivity.this, MapsActivity.class);
            startActivity(intent);

            // Optionally, finish the current activity to remove it from the back stack
            finish();
        });

        Button searchButton = findViewById(R.id.buttonSearchHike);
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

        Button myHikes = findViewById(R.id.buttonMyHikes);
        myHikes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCustomHikes();
            }
        });
    }

    private void queryUserByEmail(String email) {
        // Get the current user's ID
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

                                // Log user info for debugging
                                Log.d("MY ACCT", "Current User ID: " + currentUserId);
                                Log.d("FRIEND ACCT", "Friend Email: " + foundEmail);

                                // Add the friend to the current user's friends array
                                db.collection("Users").document(currentUserId)
                                        .update("friends", FieldValue.arrayUnion(friendUserId))
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(UserActivity.this, email + " added as a friend", Toast.LENGTH_SHORT).show();
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(UserActivity.this, "Failed to add friend.", Toast.LENGTH_SHORT).show();
                                        });

                                // Add the current user to the friend's friends array
                                db.collection("Users").document(friendUserId)
                                        .update("friends", FieldValue.arrayUnion(currentUserId))
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(UserActivity.this, "Failed to add current user as friend.", Toast.LENGTH_SHORT).show();
                                        });
                            }
                        } else {
                            // No user found with the specified email
                            Toast.makeText(UserActivity.this, "No user found with this email.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(UserActivity.this, "Failed to fetch user data.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void fetchAndShowFriends() {
        Log.d("func", "showFriendsDialog");
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Log.d("user", currentUserId);
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

    private void fetchFriendDetails(ArrayList<String> friendIds) {
        Log.d("func", "fetchFriendDetails");
        friendsList.clear();
        // A counter to track completed fetch operations
        AtomicInteger counter = new AtomicInteger(0);

        for (String friendId : friendIds) {
            db.collection("Users").document(friendId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String friendEmail = documentSnapshot.getString("username");
                            friendsList.add(friendEmail);
                        }
                        // Increment the counter after each success
                        if (counter.incrementAndGet() == friendIds.size()) {
                            // Show dialog once all friend details are fetched
                            showFriendsDialog();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(UserActivity.this, "Failed to fetch friend details.", Toast.LENGTH_SHORT).show();
                        // Also increment the counter in case of failure
                        if (counter.incrementAndGet() == friendIds.size()) {
                            showFriendsDialog();
                        }
                    });
        }
    }

    private void showFriendsDialog() {
        Log.d("func", "showFriendsDialog");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Your Friends");

        String[] friendsArray = friendsList.toArray(new String[0]);
        builder.setItems(friendsArray, (dialog, which) -> {

        });

        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    private void searchForHikes(String searchQuery) {
        hikesList.clear();

        // Query the "Hikes" collection in Firestore
        db.collection("Hikes")
                .orderBy("name")
                .startAt(searchQuery)
                .endAt(searchQuery + "\uf8ff") // Matches hikes whose names start with the search term
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot document : task.getResult()) {
                            String hikeName = document.getString("name");
                            if (hikeName != null) {
                                hikesList.add(hikeName);
                            }
                        }
                        if (!hikesList.isEmpty()) {
                            showSearchResultsDialog();
                        } else {
                            Toast.makeText(getApplicationContext(), "No hikes found matching your search.", Toast.LENGTH_SHORT).show();
                        }

                    } else {
                        Toast.makeText(getApplicationContext(), "Error searching for hikes.", Toast.LENGTH_SHORT).show();

                    }
                });
    }

    private void hikeQuery(String hike) {
        db.collection("Hikes")
                .whereEqualTo("Name", hike)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot snapshot = task.getResult();
                        if (!snapshot.isEmpty()) {
                            for (QueryDocumentSnapshot document : snapshot) {
                                String hikeID = document.getId();
                                Boolean bathrooms = document.getBoolean("Bathrooms") != null ? document.getBoolean("Bathrooms") : false;
                                Long difficulty = document.getLong("Difficulty") != null ? document.getLong("Difficulty") : 0;
                                Long lat = document.getLong("Lat") != null ? document.getLong("Lat") : 0;
                                Long hikelong = document.getLong("Long") != null ? document.getLong("Long") : 0;
                                String name = document.getString("Name") != null ? document.getString("Name") : "Unknown";
                                Boolean parking = document.getBoolean("Parking") != null ? document.getBoolean("Parking") : false;
                                ArrayList<Double> ratings = document.get("Ratings") != null ? (ArrayList<Double>) document.get("Ratings") : new ArrayList<>();
                                ArrayList<String> reviews = document.get("Reviews") != null ? (ArrayList<String>) document.get("Reviews") : new ArrayList<>();
                                String conditions = document.getString("Trail Conditions") != null ? document.getString("Trail Conditions") : "Not available";
                                Boolean markers = document.getBoolean("Trail Markers") != null ? document.getBoolean("Trail Markers") : false;
                                Boolean trash = document.getBoolean("Trash Cans") != null ? document.getBoolean("Trash Cans") : false;
                                Boolean water = document.getBoolean("Water Fountains") != null ? document.getBoolean("Water Fountains") : false;
                                Boolean wifi = document.getBoolean("WiFi") != null ? document.getBoolean("WiFi") : false;


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
                            Toast.makeText(UserActivity.this, "No hike found", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    // Method to show the search results in a popup
    private void showSearchResultsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Search Results");

        String[] hikesArray = hikesList.toArray(new String[0]);
        builder.setItems(hikesArray, (dialog, which) -> {

        });

        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    private void showCustomHikes() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db.collection("Users").document(currentUserId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        ArrayList<String> userHikes = (ArrayList<String>) documentSnapshot.get("customHikes");
                        if (userHikes != null && !userHikes.isEmpty()) {
                            StringBuilder hikeList = new StringBuilder("My Hikes: " + "\n");
                            for (int i = 0; i < userHikes.size(); i++) {
                                hikeList.append(userHikes.get(i)).append("\n");
                            }
                            new android.app.AlertDialog.Builder(UserActivity.this)
                                    .setTitle("Custom Hikes: ")
                                    .setMessage(userHikes.toString())
                                    .setPositiveButton("Close", (dialog, which) -> dialog.dismiss())
                                    .show();
                        } else {
                            Toast.makeText(UserActivity.this, "No custom hikes added.", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(UserActivity.this, "Failed to fetch custom hikes.", Toast.LENGTH_SHORT).show()
                );
    }
}
