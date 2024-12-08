package com.example.gohiking_cs310;

import androidx.fragment.app.FragmentActivity;

import android.app.AlertDialog;
import android.content.Context;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.example.gohiking_cs310.databinding.ActivityMapsBinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    GoogleMap mMap;
    private ActivityMapsBinding binding;
    private FirebaseFirestore db;
    private CollectionReference hikesCollection;
    HashMap<Marker, Hike> markerHikeMap = new HashMap<>();
    private HashMap<String, Hike> HikeIDMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        hikesCollection = db.collection("Hikes");

        //get SupportMapFragment, notified when map is ready.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Boolean isLoggedIn = FirebaseAuth.getInstance().getCurrentUser() != null;
        Button profileButton = findViewById(R.id.button_profile);
        Button groupButton = findViewById(R.id.button_group);
        Button loginButton = findViewById(R.id.button_login);
        Button signUpButton = findViewById(R.id.button_signup);
        Button logout = findViewById(R.id.buttonLogOut);

        if (!isLoggedIn) {
            loginButton.setVisibility(View.VISIBLE);
            signUpButton.setVisibility(View.VISIBLE);
            profileButton.setVisibility(View.GONE);
            groupButton.setVisibility(View.GONE);
            logout.setVisibility(View.GONE);

            loginButton.setOnClickListener(v -> {
                Intent intent = new Intent(MapsActivity.this, Login.class);
                startActivity(intent);
            });

            signUpButton.setOnClickListener(v -> {
                Intent intent = new Intent(MapsActivity.this, SignUp.class);
                startActivity(intent);
            });
        }
        else{
            loginButton.setVisibility(View.GONE);
            signUpButton.setVisibility(View.GONE);
            profileButton.setVisibility(View.VISIBLE);
            groupButton.setVisibility(View.VISIBLE);

            logout.setOnClickListener(v -> {
                FirebaseAuth.getInstance().signOut();

                Intent intent = new Intent(MapsActivity.this, MapsActivity.class);
                startActivity(intent);
                finish();
            });

            profileButton.setOnClickListener(v -> {
                Intent intent = new Intent(MapsActivity.this, UserActivity.class);
                startActivity(intent);
            });

            groupButton.setOnClickListener(v -> {
                Intent intent = new Intent(MapsActivity.this, JoinAndViewGroups.class);
                startActivity(intent);
            });
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        LatLng losAngeles = new LatLng(34.052235, -118.243683);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(losAngeles, 10));
        getHikesAndAddMarkers();
    }

    private void getHikesAndAddMarkers() {
        hikesCollection.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            try {
                                // Deserialize the Hike object, ignoring the reviews field
                                Hike hike = document.toObject(Hike.class);

                                // Handle the reviews field dynamically
                                List<?> rawReviews = (List<?>) document.get("reviews");
                                List<Review> reviews = new ArrayList<>();
                                if (rawReviews != null) {
                                    for (Object rawReview : rawReviews) {
                                        if (rawReview instanceof String) {
                                            // Handle legacy String reviews
                                            reviews.add(new Review((String) rawReview, 0, "unknownUser"));
                                        } else if (rawReview instanceof Map) {
                                            // Handle new Review objects
                                            Map<String, Object> reviewMap = (Map<String, Object>) rawReview;
                                            String reviewText = (String) reviewMap.get("reviewText");
                                            long rating = (long) reviewMap.get("rating");
                                            String userId = (String) reviewMap.get("userId");
                                            reviews.add(new Review(reviewText, rating, userId));
                                        }
                                    }
                                }
                                hike.setReviews(reviews); // Set the reviews in the hike object

                                // Add a marker for this hike
                                LatLng location = new LatLng(hike.getLat(), hike.getLng());
                                Marker marker = mMap.addMarker(new MarkerOptions()
                                        .position(location)
                                        .title(hike.getName())
                                        .snippet("Tap for details"));

                                markerHikeMap.put(marker, hike);
                                HikeIDMap.put(hike.getId(), hike);
                            } catch (Exception e) {
                                Log.e("MapsActivity", "Failed to deserialize hike document: " + document.getId(), e);
                            }
                        }

                        mMap.setOnMarkerClickListener(marker -> {
                            showHikeDetails(this, markerHikeMap.get(marker));
                            return false;
                        });

                        Log.d("MapsActivity", "Markers added for hikes: " + task.getResult().size());
                    } else {
                        Toast.makeText(MapsActivity.this, "Failed to load hikes.", Toast.LENGTH_SHORT).show();
                        Log.e("MapsActivity", "Error loading hikes: ", task.getException());
                    }
                });
    }
    public static void showHikeDetails(Context context, Hike hike) {
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
            for (Review review : hike.getReviews()) {
                hikeDetails.append("temp review test").append("\n");
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

        new AlertDialog.Builder(context)
                .setTitle(hike.getName())
                .setMessage(hikeDetails.toString())
                .setPositiveButton("Close", (dialog, which) -> dialog.dismiss())
                .show();
    }
}