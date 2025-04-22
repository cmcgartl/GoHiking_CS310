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

// This activity allows users to browse, join, leave, and manage hiking group activities.
// Features include: viewing group members, creating new group events, and inviting friends.
public class JoinAndViewGroups extends AppCompatActivity implements GroupActivityAdapter.OnGroupActivityClickListener {

    private FirebaseAuth mAuth; // Firebase Authentication to manage user sessions
    private FirebaseFirestore db; // Firestore instance to interact with backend
    private RecyclerView recyclerViewGroups; // RecyclerView to display group activities
    private GroupActivityAdapter groupActivityAdapter; // Adapter for displaying group cards
    private List<GroupActivity> groupActivities; // Local list of group activities

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this); // Initialize Firebase context if not already done
        setContentView(R.layout.activity_groups);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        Button createGroup = findViewById(R.id.creategroup); // Button to open dialog to create group
        Button gohome = findViewById(R.id.go_home); // Button to return to home screen

        // Set up RecyclerView and adapter for displaying groups
        recyclerViewGroups = findViewById(R.id.recyclerViewGroups);
        recyclerViewGroups.setLayoutManager(new LinearLayoutManager(this));
        groupActivities = new ArrayList<>();
        groupActivityAdapter = new GroupActivityAdapter(groupActivities, this);
        recyclerViewGroups.setAdapter(groupActivityAdapter);

        // Load all group activities from Firestore
        fetchGroupActivities();

        // Navigation handlers
        gohome.setOnClickListener(v -> {
            Intent intent = new Intent(JoinAndViewGroups.this, MapsActivity.class);
            startActivity(intent);
        });

        createGroup.setOnClickListener(v -> showCreateGroupDialog());
    }

    // Handler for viewing members of a selected group
    @Override
    public void onSeeMembersClick(GroupActivity groupActivity) {
        String group_id = groupActivity.getActivityID();

        db.collection("group-activities").document(group_id).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> participants = (List<String>) documentSnapshot.get("participants");

                        if (participants != null && !participants.isEmpty()) {
                            StringBuilder participantsText = new StringBuilder();
                            final int[] remainingParticipants = {participants.size()};

                            // Fetch and display each participant's username
                            for (String participant : participants) {
                                db.collection("Users").document(participant).get()
                                        .addOnSuccessListener(userSnapshot -> {
                                            String email = userSnapshot.getString("username");
                                            participantsText.append(email != null ? email : "No email found").append("\n");

                                            if (--remainingParticipants[0] == 0) {
                                                new AlertDialog.Builder(this)
                                                        .setTitle("Group Members")
                                                        .setMessage(participantsText.toString())
                                                        .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                                                        .show();
                                            }
                                        })
                                        .addOnFailureListener(e -> {
                                            remainingParticipants[0]--;
                                            Toast.makeText(this, "Failed to fetch email for user.", Toast.LENGTH_SHORT).show();
                                        });
                            }
                        } else {
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

    // Handler for joining a group
    @Override
    public void onJoinClick(GroupActivity groupActivity) {
        String user_id = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        String group_id = groupActivity.getActivityID();

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

                        // Add user to group and update user's group list atomically
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

    // Handler for leaving a group
    @Override
    public void onLeaveClick(GroupActivity groupActivity) {
        String user_id = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        String group_id = groupActivity.getActivityID();

        db.collection("group-activities").document(group_id).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> participants = (List<String>) documentSnapshot.get("participants");

                        if (!participants.contains(user_id)) {
                            Toast.makeText(this, "ERROR: YOU'RE NOT A MEMBER OF THIS GROUP.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Remove user from group and their group list atomically
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

    // Handler for adding a friend to a group
    @Override
    public void onAddMemberClick(GroupActivity groupActivity) {
        String user_id = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

        db.collection("Users").document(user_id).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> friends = (List<String>) documentSnapshot.get("friends");

                        if (friends != null && !friends.isEmpty()) {
                            List<String> friendNames = new ArrayList<>();
                            AtomicInteger badFriends = new AtomicInteger();

                            for (String friend : friends) {
                                db.collection("Users").document(friend).get()
                                        .addOnSuccessListener(friendSnapshot -> {
                                            String friendEmail = friendSnapshot.getString("username");
                                            if (friendEmail != null) {
                                                friendNames.add(friendEmail);
                                                if (friendNames.size() == friends.size() - badFriends.get()) {
                                                    showFriendsDialog(friendNames, groupActivity);
                                                }
                                            } else {
                                                badFriends.incrementAndGet();
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

    // Dialog to select a friend to invite to a group
    private void showFriendsDialog(List<String> friendNames, GroupActivity groupActivity) {
        String[] friendArray = friendNames.toArray(new String[0]);
        new AlertDialog.Builder(this)
                .setTitle("Select a Friend to Add to Group")
                .setItems(friendArray, (dialog, which) -> {
                    String selectedFriendEmail = friendNames.get(which);

                    db.collection("Users").whereEqualTo("username", selectedFriendEmail).get()
                            .addOnSuccessListener(querySnapshot -> {
                                if (!querySnapshot.isEmpty()) {
                                    String friendID = querySnapshot.getDocuments().get(0).getId();

                                    DocumentReference groupRef = db.collection("group-activities").document(groupActivity.getActivityID());
                                    DocumentReference friendRef = db.collection("Users").document(friendID);

                                    groupRef.get().addOnSuccessListener(groupSnapshot -> {
                                        if (groupSnapshot.exists()) {
                                            List<String> participants = (List<String>) groupSnapshot.get("participants");
                                            Long maxParticipants = groupSnapshot.getLong("maxParticipants");

                                            if (participants != null && participants.contains(friendID)) {
                                                Toast.makeText(this, "Friend is already a member of this group.", Toast.LENGTH_SHORT).show();
                                            } else if (participants != null && maxParticipants != null && participants.size() >= maxParticipants) {
                                                Toast.makeText(this, "Group is at maximum capacity.", Toast.LENGTH_SHORT).show();
                                            } else {
                                                friendRef.get().addOnSuccessListener(friendSnapshot -> {
                                                    List<String> friendGroups = (List<String>) friendSnapshot.get("groupActivities");

                                                    if (friendGroups != null && friendGroups.contains(groupActivity.getActivityID())) {
                                                        Toast.makeText(this, "Friend is already participating in this activity.", Toast.LENGTH_SHORT).show();
                                                    } else {
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
                                                });
                                            }
                                        } else {
                                            Toast.makeText(this, "Group does not exist.", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            });
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    // Fetch all group activities from Firestore and update RecyclerView
    private void fetchGroupActivities() {
        db.collection("group-activities").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        groupActivities.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            GroupActivity groupActivity = document.toObject(GroupActivity.class);
                            groupActivities.add(groupActivity);
                        }
                        groupActivityAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(this, "Failed to fetch group activities.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Open dialog to allow user to input group details and create a new group
    private void showCreateGroupDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_create_group, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);

        EditText titleInput = dialogView.findViewById(R.id.editTextTitle);
        EditText locationInput = dialogView.findViewById(R.id.editTextLocation);
        NumberPicker maxParticipantsPicker = dialogView.findViewById(R.id.numberPickerMaxParticipants);
        Button buttonPickDate = dialogView.findViewById(R.id.buttonPickDate);
        Button buttonPickTime = dialogView.findViewById(R.id.buttonPickTime);

        maxParticipantsPicker.setMinValue(1);
        maxParticipantsPicker.setMaxValue(50);

        final Calendar calendar = Calendar.getInstance();

        buttonPickDate.setOnClickListener(v -> new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            calendar.set(year, month, dayOfMonth);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show());

        buttonPickTime.setOnClickListener(v -> new TimePickerDialog(this, (view, hour, minute) -> {
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show());

        builder.setPositiveButton("Create", (dialog, which) -> {
            String title = titleInput.getText().toString().trim();
            String location = locationInput.getText().toString().trim();
            int maxParticipants = maxParticipantsPicker.getValue();
            Date dateTime = calendar.getTime();

            if (!title.isEmpty() && !location.isEmpty()) {
                createGroupActivityInFirestore(title, location, maxParticipants, dateTime);
            } else {
                Toast.makeText(this, "Please fill in all fields.", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    // Create and store a new group activity in Firestore
    private void createGroupActivityInFirestore(String title, String location, int maxParticipants, Date dateTime) {
        String activityID = UUID.randomUUID().toString();
        GroupActivity groupActivity = new GroupActivity(activityID, title, location, dateTime, maxParticipants);

        Map<String, Object> activityMap = new HashMap<>();
        activityMap.put("activityID", activityID);
        activityMap.put("title", title);
        activityMap.put("location", location);
        activityMap.put("time", dateTime);
        activityMap.put("maxParticipants", maxParticipants);
        activityMap.put("participants", groupActivity.getParticipants());

        db.collection("group-activities").document(activityID).set(activityMap)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Group activity created successfully!", Toast.LENGTH_SHORT).show();
                    fetchGroupActivities();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to create group activity.", Toast.LENGTH_SHORT).show()
                );
    }
}