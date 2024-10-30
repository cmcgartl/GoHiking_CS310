package com.example.gohiking_cs310;

import androidx.fragment.app.FragmentActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.example.gohiking_cs310.databinding.ActivityMapsBinding;

import java.util.ArrayList;
import java.util.HashMap;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private FirebaseFirestore db;
    private CollectionReference hikesCollection;
    private HashMap<Marker, Hike> markerHikeMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Firestore and hikes collection
        db = FirebaseFirestore.getInstance();
        hikesCollection = db.collection("Hikes");

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Initialize buttons and set click listeners
        Button loginButton = findViewById(R.id.button_login);
        Button signUpButton = findViewById(R.id.button_signup);

        loginButton.setOnClickListener(v -> {
            Intent intent = new Intent(MapsActivity.this, Login.class);
            startActivity(intent);
        });

        signUpButton.setOnClickListener(v -> {
            Intent intent = new Intent(MapsActivity.this, SignUp.class);
            startActivity(intent);
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        // Default location (e.g., Los Angeles) and zoom level
        LatLng losAngeles = new LatLng(34.052235, -118.243683);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(losAngeles, 10));

        // Fetch hikes from Firestore and add markers
        getHikesAndAddMarkers();
    }

    private void getHikesAndAddMarkers() {
        hikesCollection.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Hike hike = document.toObject(Hike.class);

                            // Extract latitude and longitude for each hike
                            LatLng location = new LatLng(hike.getLat(), hike.getLng());

                            // Add a marker to the map and store it in the map
                            Marker marker = mMap.addMarker(new MarkerOptions()
                                    .position(location)
                                    .title(hike.getName())
                                    .snippet("Tap for details"));

                            // Map the marker to its corresponding hike
                            markerHikeMap.put(marker, hike);
                        }

                        // Set a listener for marker clicks to show detailed info
                        mMap.setOnMarkerClickListener(marker -> {
                            showHikeDetails(markerHikeMap.get(marker));
                            return false; // Return false to also show the default info window
                        });

                        Log.d("MapsActivity", "Markers added for hikes: " + task.getResult().size());
                    } else {
                        Toast.makeText(MapsActivity.this, "Failed to load hikes.", Toast.LENGTH_SHORT).show();
                        Log.e("MapsActivity", "Error loading hikes: ", task.getException());
                    }
                });
    }
    private void showHikeDetails(Hike hike) {
        if (hike == null) return;


        StringBuilder hikeDetails = new StringBuilder("Difficulty: " + hike.getDifficulty() + "\n" + "Trail Conditions: ");
        if (hike.getTrailConditions() != "") hikeDetails.append(hike.getTrailConditions()).append("\n");
        else hikeDetails.append("Trail Conditions not available.\n");
        if (hike.getRatings() != null && !hike.getRatings().isEmpty()) {
            int numRatings = hike.getRatings().size();
            double totalRating = 0;
            for (double rating : hike.getRatings()) {
                totalRating += rating;
            }
            double averageRating = totalRating / numRatings;
            hikeDetails.append("Average Rating: ").append(averageRating).append("\n");
        }
        else hikeDetails.append("No ratings yet.\n");
        hikeDetails.append("Reviews: ");
        if (hike.getReviews() != null && !hike.getReviews().isEmpty()) {
            hikeDetails.append("\n");
            for (String review : hike.getReviews()) {
                hikeDetails.append(review).append("\n");
            }
        }
        else hikeDetails.append("No reviews yet.\n");
        hikeDetails.append("Amenities: \n" +
                "Bathrooms: " + (hike.isBathrooms() ? "Yes" : "No") + "\n" +
                "Parking: " + (hike.isParking() ? "Yes" : "No") + "\n" +
                "Trail Markers: " + (hike.isTrailMarkers() ? "Yes" : "No") + "\n" +
                "Trash Cans: " + (hike.isTrashCans() ? "Yes" : "No") + "\n" +
                "Water Fountains: " + (hike.isWaterFountains() ? "Yes" : "No") + "\n" +
                "WiFi: " + (hike.isWifi() ? "Yes" : "No") + "\n");

        // Create and show an AlertDialog with the hike details
        new AlertDialog.Builder(this)
                .setTitle(hike.getName())
                .setMessage(hikeDetails.toString())
                .setPositiveButton("Close", (dialog, which) -> dialog.dismiss())
                .show();
    }
}