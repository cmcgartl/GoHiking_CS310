package com.example.gohiking_cs310;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.type.DateTime;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class JoinAndViewGroups extends AppCompatActivity {
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groups);
        mAuth = FirebaseAuth.getInstance();
        Button createGroup = findViewById(R.id.creategroup);
        Button gohome = findViewById(R.id.go_home);

        gohome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(JoinAndViewGroups.this, MapsActivity.class);
                startActivity(intent);
            }
        });

        createGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCreateGroupDialog();
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
        db.collection("GroupActivities")
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
