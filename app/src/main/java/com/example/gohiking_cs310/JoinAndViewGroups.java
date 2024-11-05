package com.example.gohiking_cs310;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.Transaction;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class JoinAndViewGroups extends AppCompatActivity implements GroupActivityAdapter.OnGroupActivityClickListener {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private RecyclerView recyclerViewGroups;
    private GroupActivityAdapter groupActivityAdapter;
    private List<GroupActivity> groupActivities;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        setContentView(R.layout.activity_groups);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();


        Button createGroup = findViewById(R.id.creategroup);
        Button gohome = findViewById(R.id.go_home);

        recyclerViewGroups = findViewById(R.id.recyclerViewGroups);
        recyclerViewGroups.setLayoutManager(new LinearLayoutManager(this));
        groupActivities = new ArrayList<>();

        groupActivityAdapter = new GroupActivityAdapter(groupActivities, this);
        recyclerViewGroups.setAdapter(groupActivityAdapter);

        // Fetch and display group activities from Firestore
        fetchGroupActivities();

        gohome.setOnClickListener(v -> {
            Intent intent = new Intent(JoinAndViewGroups.this, MapsActivity.class);
            startActivity(intent);
        });

        createGroup.setOnClickListener(v -> showCreateGroupDialog());
    }

    @Override
    public void onSeeMembersClick(GroupActivity groupActivity) {
        String group_id = groupActivity.getActivityID();

        // Retrieve the participants array from Firestore
        db.collection("group-activities").document(group_id).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> participants = (List<String>) documentSnapshot.get("participants");

                        if (participants != null && !participants.isEmpty()) {
                            // Initialize a StringBuilder to collect participant emails
                            StringBuilder participantsText = new StringBuilder();

                            // Counter to track when all participant emails have been retrieved
                            final int[] remainingParticipants = {participants.size()};

                            for (String participant : participants) {
                                // Fetch each participant's email
                                db.collection("Users").document(participant).get()
                                        .addOnSuccessListener(userSnapshot -> {
                                            String email = userSnapshot.getString("email");
                                            if (email != null) {
                                                participantsText.append(email).append("\n");
                                            } else {
                                                participantsText.append("No email found\n");
                                            }

                                            // When all emails are fetched, show the AlertDialog
                                            remainingParticipants[0]--;
                                            if (remainingParticipants[0] == 0) {
                                                new AlertDialog.Builder(this)
                                                        .setTitle("Group Members")
                                                        .setMessage(participantsText.toString())
                                                        .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                                                        .show();
                                            }
                                        })
                                        .addOnFailureListener(e -> {
                                            remainingParticipants[0]--;
                                            Toast.makeText(this, "Failed to fetch email for user: " + participant, Toast.LENGTH_SHORT).show();
                                        });
                            }
                        } else {
                            // No participants found
                            Toast.makeText(this, "No members in this group.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Group does not exist.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error fetching group data: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    @Override
    public void onJoinClick(GroupActivity groupActivity) {
        String user_id = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        String group_id = groupActivity.getActivityID();

        // Fetch the latest state of the group to ensure participants list is up-to-date
        db.collection("group-activities").document(group_id).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> participants = (List<String>) documentSnapshot.get("participants");
                        if (participants.contains(user_id)) {
                            Toast.makeText(this, "ERROR: YOU'RE ALREADY A MEMBER OF THIS GROUP.", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (participants.size() >= groupActivity.getMaxParticipants()) {
                            Toast.makeText(this, "ERROR: GROUP IS FULL.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // If no errors, proceed with joining
                        db.runTransaction((Transaction.Function<Void>) transaction -> {
                            DocumentReference groupRef = db.collection("group-activities").document(group_id);
                            DocumentReference userRef = db.collection("Users").document(user_id);
                            transaction.update(groupRef, "participants", FieldValue.arrayUnion(user_id));
                            transaction.update(userRef, "groupActivities", FieldValue.arrayUnion(group_id));
                            return null;
                        }).addOnSuccessListener(aVoid ->
                                Toast.makeText(this, "SUCCESSFULLY JOINED GROUP", Toast.LENGTH_SHORT).show()
                        ).addOnFailureListener(e ->
                                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show()
                        );
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error fetching group data: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

   /* @Override
    public void onJoinClick(GroupActivity groupActivity) {
        String currentUserId = mAuth.getCurrentUser().getUid();
        String groupId = groupActivity.getActivityID();
        //
        //Toast.makeText(this, "steo 1.", Toast.LENGTH_SHORT).show();
        // Check if the user is already in the group
        db.collection("GroupActivities").document(groupId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    //Toast.makeText(this, "steo 2222.", Toast.LENGTH_SHORT).show();
                    List<String> participants = (List<String>) documentSnapshot.get("participants");
                    if (participants == null) {
                        //Toast.makeText(this, "NULL 2222.", Toast.LENGTH_SHORT).show();
                    }
                    if (participants != null && participants.contains(currentUserId)) {
                        Toast.makeText(this, "ERROR: YOU'RE ALREADY A MEMBER OF THIS GROUP.", Toast.LENGTH_SHORT).show();
                    } else {
                        // Add user to group's participants array
                        if (participants == null) {
                           // Log.d("GroupActivity", "Participants is null");
                        }
                        db.collection("GroupActivities").document(groupId)
                                .update("participants", FieldValue.arrayUnion(currentUserId))
                                .addOnSuccessListener(aVoid -> {
                                    //Toast.makeText(this, "steo 3.", Toast.LENGTH_SHORT).show();
                                })

                                .addOnSuccessListener(aVoid -> {
                                    // Add group to user's groupActivities array
                                    db.collection("Users").document(currentUserId)
                                            .update("groupActivities", FieldValue.arrayUnion(groupId))
                                            .addOnSuccessListener(aVoid2 -> {
                                                Toast.makeText(this, "SUCCESSFULLY JOINED GROUP", Toast.LENGTH_SHORT).show();
                                            })
                                            .addOnFailureListener(e -> {
                                                // Toast.makeText(this, "Failed to add group to user's activities.", Toast.LENGTH_SHORT).show();
                                            });
                                })
                                .addOnFailureListener(e -> {
                                    //Toast.makeText(this, "Failed to add user to group.", Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    //Toast.makeText(this, "Error checking group membership.", Toast.LENGTH_SHORT).show();
                });
    }*/

    @Override
    public void onLeaveClick(GroupActivity groupActivity) {
        String user_id = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        String group_id = groupActivity.getActivityID();

        // Fetch the latest state of the group to ensure participants list is up-to-date
        db.collection("group-activities").document(group_id).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> participants = (List<String>) documentSnapshot.get("participants");
                        if (!participants.contains(user_id)) {
                            Toast.makeText(this, "ERROR: YOU'RE NOT A MEMBER OF THIS GROUP.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // If no errors, proceed with joining
                        db.runTransaction((Transaction.Function<Void>) transaction -> {
                            DocumentReference groupRef = db.collection("group-activities").document(group_id);
                            DocumentReference userRef = db.collection("Users").document(user_id);
                            transaction.update(groupRef, "participants", FieldValue.arrayRemove(user_id));
                            transaction.update(userRef, "groupActivities", FieldValue.arrayRemove(group_id));
                            return null;
                        }).addOnSuccessListener(aVoid ->
                                Toast.makeText(this, "SUCCESSFULLY LEFT GROUP", Toast.LENGTH_SHORT).show()
                        ).addOnFailureListener(e ->
                                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show()
                        );
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error fetching group data: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }


    @Override
    public void onAddMemberClick(GroupActivity groupActivity) {
        String user_id = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        db.collection("Users").document(user_id).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> friends = (List<String>) documentSnapshot.get("friends");
                        if (friends != null && !friends.isEmpty()) {
                            // Create a list to store friend emails or IDs for display
                            List<String> friendNames = new ArrayList<>();

                            // Fetch emails or names of friends to display in the dialog
                            AtomicInteger badFriends = new AtomicInteger();
                            for (String friend : friends) {
                                //Toast.makeText(this, friend, Toast.LENGTH_SHORT).show();
                                db.collection("Users").document(friend).get()
                                        .addOnSuccessListener(friendSnapshot -> {
                                            String friendEmail = friendSnapshot.getString("email");

                                            if (friendEmail != null) {
                                                friendNames.add(friendEmail);
                                                //Toast.makeText(this, friendEmail, Toast.LENGTH_SHORT).show();
                                                // Once all friends are added to the list, show the dialog
                                                if (friendNames.size() == friends.size() - badFriends.get()) {
                                                    showFriendsDialog(friendNames, groupActivity);
                                                }
                                            }
                                            else{
                                                badFriends.addAndGet(1);
                                            }
                                        })
                                        .addOnFailureListener(e ->
                                                Toast.makeText(this, "Failed to fetch friend data.", Toast.LENGTH_SHORT).show()
                                        );
                            }
                        } else {
                            Toast.makeText(this, "No friends found.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "User does not exist.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showFriendsDialog(List<String> friendNames, GroupActivity groupActivity) {
        String[] friendArray = friendNames.toArray(new String[0]);
        new AlertDialog.Builder(this)
                .setTitle("Select a Friend to Add to Group")
                .setItems(friendArray, (dialog, which) -> {
                    String selectedFriendEmail = friendNames.get(which);

                    // Query Firestore to find the user document with the selected email
                    db.collection("Users").whereEqualTo("email", selectedFriendEmail).get()
                            .addOnSuccessListener(querySnapshot -> {
                                if (!querySnapshot.isEmpty()) {
                                    String friendID = querySnapshot.getDocuments().get(0).getId();

                                    DocumentReference groupRef = db.collection("group-activities").document(groupActivity.getActivityID());
                                    DocumentReference friendRef = db.collection("Users").document(friendID);

                                    // Check if friend is already in the group and if there's capacity
                                    groupRef.get().addOnSuccessListener(groupSnapshot -> {
                                        if (groupSnapshot.exists()) {
                                            List<String> participants = (List<String>) groupSnapshot.get("participants");
                                            Long maxParticipants = groupSnapshot.getLong("maxParticipants");

                                            if (participants != null && participants.contains(friendID)) {
                                                Toast.makeText(this, "Friend is already a member of this group.", Toast.LENGTH_SHORT).show();
                                            } else if (participants != null && maxParticipants != null && participants.size() >= maxParticipants) {
                                                Toast.makeText(this, "Group is at maximum capacity.", Toast.LENGTH_SHORT).show();
                                            } else {
                                                // Check if the friend already has this group activity in their list
                                                friendRef.get().addOnSuccessListener(friendSnapshot -> {
                                                    List<String> friendGroupActivities = (List<String>) friendSnapshot.get("groupActivities");

                                                    if (friendGroupActivities != null && friendGroupActivities.contains(groupActivity.getActivityID())) {
                                                        Toast.makeText(this, "Friend is already participating in this activity.", Toast.LENGTH_SHORT).show();
                                                    } else {
                                                        // Add friend to group and group to friend's groupActivities list in a transaction
                                                        db.runTransaction((Transaction.Function<Void>) transaction -> {
                                                            transaction.update(groupRef, "participants", FieldValue.arrayUnion(friendID));
                                                            transaction.update(friendRef, "groupActivities", FieldValue.arrayUnion(groupActivity.getActivityID()));
                                                            return null;
                                                        }).addOnSuccessListener(aVoid ->
                                                                Toast.makeText(this, "Friend added to group!", Toast.LENGTH_SHORT).show()
                                                        ).addOnFailureListener(e ->
                                                                Log.d("GroupActivity", "Error adding friend to group: " + e.getMessage())
                                                        );
                                                    }
                                                }).addOnFailureListener(e ->
                                                        Toast.makeText(this, "Failed to check friend's group activities: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                                                );
                                            }
                                        } else {
                                            Toast.makeText(this, "Group does not exist.", Toast.LENGTH_SHORT).show();
                                        }
                                    }).addOnFailureListener(e ->
                                            Toast.makeText(this, "Failed to fetch group data: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                                    );
                                } else {
                                    Toast.makeText(this, "No user found with this email.", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Failed to retrieve friend ID: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                            );
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void fetchGroupActivities() {
        db.collection("group-activities").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        groupActivities.clear(); // Clear the list before adding items
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            GroupActivity groupActivity = document.toObject(GroupActivity.class);
                            groupActivities.add(groupActivity);
                        }
                        groupActivityAdapter.notifyDataSetChanged(); // Refresh the RecyclerView
                    } else {
                        Toast.makeText(this, "Failed to fetch group activities.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showCreateGroupDialog() {
        // Inflate the custom dialog layout
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_create_group, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);

        // Find input fields in the dialog
        EditText titleInput = dialogView.findViewById(R.id.editTextTitle);
        EditText locationInput = dialogView.findViewById(R.id.editTextLocation);
        NumberPicker maxParticipantsPicker = dialogView.findViewById(R.id.numberPickerMaxParticipants);
        Button buttonPickDate = dialogView.findViewById(R.id.buttonPickDate);
        Button buttonPickTime = dialogView.findViewById(R.id.buttonPickTime);

        // Configure the NumberPicker for max participants
        maxParticipantsPicker.setMinValue(1);
        maxParticipantsPicker.setMaxValue(50);
        maxParticipantsPicker.setWrapSelectorWheel(true);

        // Initialize date and time to the current date/time
        final Calendar calendar = Calendar.getInstance();

        // Date picker dialog
        buttonPickDate.setOnClickListener(v -> {
            new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
        });

        // Time picker dialog
        buttonPickTime.setOnClickListener(v -> {
            new TimePickerDialog(this, (view, hourOfDay, minute) -> {
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
        });

        // Set up dialog buttons
        builder.setPositiveButton("Create", (dialog, which) -> {
            String title = titleInput.getText().toString().trim();
            String location = locationInput.getText().toString().trim();
            int maxParticipants = maxParticipantsPicker.getValue(); // Get the selected value from NumberPicker

            if (!title.isEmpty() && !location.isEmpty()) {
                Date dateTime = calendar.getTime(); // Get selected date and time
                createGroupActivityInFirestore(title, location, maxParticipants, dateTime);
            } else {
                Toast.makeText(JoinAndViewGroups.this, "Please fill in all fields.", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void createGroupActivityInFirestore(String title, String location, int maxParticipants, Date dateTime) {
        // Generate a unique ID for the activity
        String activityID = UUID.randomUUID().toString();

        // Create a GroupActivity instance
        GroupActivity groupActivity = new GroupActivity(activityID, title, location, dateTime, maxParticipants);

        // Create a Map to represent the fields in Firestore
        Map<String, Object> activityMap = new HashMap<>();
        activityMap.put("activityID", groupActivity.getActivityID());
        activityMap.put("title", groupActivity.getTitle());
        activityMap.put("location", groupActivity.getLocation());
        activityMap.put("time", groupActivity.getTime());
        activityMap.put("maxParticipants", groupActivity.getMaxParticipants());
        activityMap.put("participants", groupActivity.getParticipants());

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // Add the document to Firestore
        db.collection("group-activities")
                .document(activityID)
                .set(activityMap)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Group activity created successfully!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to create group activity.", Toast.LENGTH_SHORT).show();
                });
    }
}
