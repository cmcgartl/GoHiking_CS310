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

// Main activity responsible for displaying hikes on a Google Map
// and managing user session-based UI controls
public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    GoogleMap mMap;
    private ActivityMapsBinding binding;

    // Firebase Firestore database reference
    private FirebaseFirestore db;

    // Reference to the "Hikes" collection in Firestore
    private CollectionReference hikesCollection;

    // Maps markers to their associated Hike objects for quick lookup
    HashMap<Marker, Hike> markerHikeMap = new HashMap<>();

    // Maps hike IDs to Hike objects for programmatic lookup
    private HashMap<String, Hike> HikeIDMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize view binding for layout
        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Firebase Firestore
        db = FirebaseFirestore.getInstance();
        hikesCollection = db.collection("Hikes");

        // Set up the Google Map fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Configure UI elements based on user login status
        Boolean isLoggedIn = FirebaseAuth.getInstance().getCurrentUser() != null;

        Button profileButton = findViewById(R.id.button_profile);
        Button groupButton = findViewById(R.id.button_group);
        Button loginButton = findViewById(R.id.button_login);
        Button signUpButton = findViewById(R.id.button_signup);
        Button logout = findViewById(R.id.buttonLogOut);

        // Update button visibility and behavior based on login state
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
        } else {
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

        // Enable zoom controls on the map
        mMap.getUiSettings().setZoomControlsEnabled(true);

        // Center map on Los Angeles
        LatLng losAngeles = new LatLng(34.052235, -118.243683);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(losAngeles, 10));

        // Fetch hikes from Firestore and add corresponding markers
        getHikesAndAddMarkers();
    }

    // Fetch hikes from Firestore and dynamically add markers to the map
    private void getHikesAndAddMarkers() {
        hikesCollection.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            try {
                                // Deserialize hike object while handling reviews manually
                                Hike hike = document.toObject(Hike.class);

                                // Handle serialized reviews separately for flexibility
                                List<?> rawReviews = (List<?>) document.get("reviews");
                                List<Review> reviews = new ArrayList<>();
                                if (rawReviews != null) {
                                    for (Object rawReview : rawReviews) {
                                        if (rawReview instanceof String) {
                                            reviews.add(new Review((String) rawReview, 0, "unknownUser"));
                                        } else if (rawReview instanceof Map) {
                                            Map<String, Object> reviewMap = (Map<String, Object>) rawReview;
                                            String reviewText = (String) reviewMap.get("reviewText");
                                            long rating = (long) reviewMap.get("rating");
                                            String userId = (String) reviewMap.get("userId");
                                            reviews.add(new Review(reviewText, rating, userId));
                                        }
                                    }
                                }

                                // Add reviews back to hike object
                                hike.setReviews(reviews);

                                // Create marker for hike location
                                LatLng location = new LatLng(hike.getLat(), hike.getLng());
                                Marker marker = mMap.addMarker(new MarkerOptions()
                                        .position(location)
                                        .title(hike.getName())
                                        .snippet("Tap for details"));

                                // Map marker and hike ID to their objects
                                markerHikeMap.put(marker, hike);
                                HikeIDMap.put(hike.getId(), hike);
                            } catch (Exception e) {
                                Log.e("MapsActivity", "Failed to deserialize hike: " + document.getId(), e);
                            }
                        }

                        // Set listener to show details when a marker is clicked
                        mMap.setOnMarkerClickListener(marker -> {
                            showHikeDetails(this, markerHikeMap.get(marker));
                            return false;
                        });

                        Log.d("MapsActivity", "Markers added for hikes: " + task.getResult().size());
                    } else {
                        Toast.makeText(MapsActivity.this, "Failed to load hikes.", Toast.LENGTH_SHORT).show();
                        Log.e("MapsActivity", "Firestore error: ", task.getException());
                    }
                });
    }

    // Show detailed information about a selected hike in an alert dialog
    public static void showHikeDetails(Context context, Hike hike) {
        if (hike == null) return;

        StringBuilder hikeDetails = new StringBuilder("Difficulty: " + hike.getDifficulty() + "\n" + "Trail Conditions: ");
        hikeDetails.append(!hike.getTrailConditions().isEmpty() ? hike.getTrailConditions() + "\n" : "Trail Conditions not available.\n");

        if (hike.getRatings() != null && !hike.getRatings().isEmpty()) {
            int numRatings = hike.getRatings().size();
            double totalRating = 0;
            for (double rating : hike.getRatings()) {
                totalRating += rating;
            }
            hikeDetails.append("Average Rating: ").append(totalRating / numRatings).append("\n");
        } else {
            hikeDetails.append("No ratings yet.\n");
        }

        hikeDetails.append("Reviews: ");
        if (hike.getReviews() != null && !hike.getReviews().isEmpty()) {
            hikeDetails.append("\n");
            for (Review review : hike.getReviews()) {
                hikeDetails.append("temp review test").append("\n"); // Placeholder for future review formatting
            }
        } else {
            hikeDetails.append("No reviews yet.\n");
        }

        // Add available amenities to the hike description
        hikeDetails.append("Amenities: \n" +
                "Bathrooms: " + (hike.isBathrooms() ? "Yes" : "No") + "\n" +
                "Parking: " + (hike.isParking() ? "Yes" : "No") + "\n" +
                "Trail Markers: " + (hike.isTrailMarkers() ? "Yes" : "No") + "\n" +
                "Trash Cans: " + (hike.isTrashCans() ? "Yes" : "No") + "\n" +
                "Water Fountains: " + (hike.isWaterFountains() ? "Yes" : "No") + "\n" +
                "WiFi: " + (hike.isWifi() ? "Yes" : "No") + "\n");

        // Display the hike details in a dialog
        new AlertDialog.Builder(context)
                .setTitle(hike.getName())
                .setMessage(hikeDetails.toString())
                .setPositiveButton("Close", (dialog, which) -> dialog.dismiss())
                .show();
    }
}