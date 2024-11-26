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

public class CustomListActivity extends AppCompatActivity {
    FirebaseFirestore db;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.custom_list);
        db = FirebaseFirestore.getInstance();

        Button buttonBackToHome = findViewById(R.id.buttonBackHome);
        buttonBackToHome.setOnClickListener(v -> {
            Intent intent = new Intent(CustomListActivity.this, MapsActivity.class);
            startActivity(intent);
            finish();
        });
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
        Button myLists = findViewById(R.id.buttonMyLists);
        myLists.setOnClickListener(v -> {
            String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            showMyLists(currentUserId);
        });
    }

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

    private void updatePrivacy(String list, String currUser){
        db.collection("Users").document(currUser).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Map<String, Boolean> privacy = (Map<String, Boolean>) documentSnapshot.get("listPrivacy");
                        privacy.put(list, true);
                        db.collection("Users").document(currUser)
                                .update("listPrivacy", privacy);
                    }
                });
    }

    void togglePrivacy(String list){
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (list == null) {
            Toast.makeText(CustomListActivity.this, "List name is missing.", Toast.LENGTH_SHORT).show();
            return;
        }
        db.collection("Users").document(currentUserId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Map<String, Boolean> privacy = (Map<String, Boolean>) documentSnapshot.get("listPrivacy");
                        if(!privacy.containsKey(list)){
                            Toast.makeText(CustomListActivity.this, "please enter a valid list", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        privacy.compute(list, (k, pub) -> !pub);
                        db.collection("Users").document(currentUserId)
                                .update("listPrivacy", privacy)
                                .addOnSuccessListener(aVoid -> {
                                    if(privacy.get(list)) {
                                        Toast.makeText(CustomListActivity.this, list + " privacy updated to public", Toast.LENGTH_SHORT).show();
                                    }
                                    else{
                                        Toast.makeText(CustomListActivity.this, list + " privacy updated to private", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                });
    }

    private void showMyLists(String userId){
        db.collection("Users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Map<String, List<String>> customList = (Map<String, List<String>>) documentSnapshot.get("customList");
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle("Your Lists");
                        String[] lists = new String[customList.size()];
                        int i = 0;
                        for(String hike : customList.keySet()){
                            lists[i] = hike;
                            i++;
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
