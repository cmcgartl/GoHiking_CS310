package com.example.gohiking_cs310;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.firebase.auth.FirebaseAuth;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class SearchReviewTests {

    @Rule
    public ActivityScenarioRule<UserActivity> activityRule =
            new ActivityScenarioRule<>(UserActivity.class);

    // BLACK BOX TEST 10: Verify Search Functionality with Valid Input
    // AUTHOR: DOUG PETERSON
    @Test
    public void testSearchWithValidInput() {
        // Log in the user
        activityRule.getScenario().onActivity(activity -> {
            FirebaseAuth.getInstance().signInWithEmailAndPassword("martinestrin2@yahoo.com", "WHITEBOXTEST2")
                    .addOnCompleteListener(task -> {
                        if (!task.isSuccessful()) {
                            throw new AssertionError("Failed to log in user for testing.");
                        }
                    });
        });
        activityRule.getScenario().recreate();

        // Navigate to the hike activity via the user profile
        onView(withId(R.id.search_button))
                .check(matches(isDisplayed()))
                .perform(click());

        // Step 1: Enter a valid hike name in the search field
        onView(withId(R.id.editTextSearchHike))
                .perform(typeText("Griffith Observatory"), closeSoftKeyboard());

        // Step 2: Click the search button
        onView(withId(R.id.search_button))
                .check(matches(isDisplayed()))
                .perform(click());

        // Step 3: Verify the hike details page is displayed
        onView(withText("Griffith Observatory"))
                .check(matches(isDisplayed()));
    }

    // BLACK BOX TEST 11: Verify Search Functionality with Invalid Input
    // AUTHOR: DOUG PETERSON
    @Test
    public void testSearchWithInvalidInput() {
        // Log in the user
        activityRule.getScenario().onActivity(activity -> {
            FirebaseAuth.getInstance().signInWithEmailAndPassword("martinestrin2@yahoo.com", "WHITEBOXTEST2")
                    .addOnCompleteListener(task -> {
                        if (!task.isSuccessful()) {
                            throw new AssertionError("Failed to log in user for testing.");
                        }
                    });
        });
        activityRule.getScenario().recreate();

        // Navigate to the hike activity via the user profile
        onView(withId(R.id.search_button))
                .check(matches(isDisplayed()))
                .perform(click());

        // Step 1: Enter an invalid hike name in the search field
        onView(withId(R.id.editTextSearchHike))
                .perform(typeText("Invalid Hike Name"), closeSoftKeyboard());

        // Step 2: Click the search button
        onView(withId(R.id.search_button))
                .check(matches(isDisplayed()))
                .perform(click());

        // Step 3: Verify an error message is displayed
        onView(withText("No hike found"))
                .check(matches(isDisplayed()));
    }

    // BLACK BOX TEST 12: Verify Review Submission
    // AUTHOR: DOUG PETERSON
    @Test
    public void testSubmitReview() {
        // Log in the user
        activityRule.getScenario().onActivity(activity -> {
            FirebaseAuth.getInstance().signInWithEmailAndPassword("martinestrin2@yahoo.com", "WHITEBOXTEST2")
                    .addOnCompleteListener(task -> {
                        if (!task.isSuccessful()) {
                            throw new AssertionError("Failed to log in user for testing.");
                        }
                    });
        });
        activityRule.getScenario().recreate();

        // Navigate to the hike activity via the user profile
        onView(withId(R.id.search_button))
                .check(matches(isDisplayed()))
                .perform(click());

        // Navigate to the Review Activity
        onView(withId(R.id.buttonReview))
                .check(matches(isDisplayed()))
                .perform(click());

        // Step 1: Enter a review
        onView(withId(R.id.review_edit_text))
                .perform(typeText("Beautiful hike!"), closeSoftKeyboard());

        // Step 2: Set a rating
        onView(withId(R.id.rating_bar))
                .perform(click());

        // Step 3: Click the submit button
        onView(withId(R.id.submit_review_button))
                .check(matches(isDisplayed()))
                .perform(click());

        // Step 4: Verify that the review is displayed in the review list
        onView(withText("Beautiful hike!"))
                .check(matches(isDisplayed()));
    }

    // BLACK BOX TEST 13: Verify Display of Average Rating
    // AUTHOR: DOUG PETERSON
    @Test
    public void testAverageRatingDisplay() {
        // Log in the user
        activityRule.getScenario().onActivity(activity -> {
            FirebaseAuth.getInstance().signInWithEmailAndPassword("martinestrin2@yahoo.com", "WHITEBOXTEST2")
                    .addOnCompleteListener(task -> {
                        if (!task.isSuccessful()) {
                            throw new AssertionError("Failed to log in user for testing.");
                        }
                    });
        });
        activityRule.getScenario().recreate();

        // Navigate to the hike activity via the user profile
        onView(withId(R.id.search_button))
                .check(matches(isDisplayed()))
                .perform(click());

        // Navigate to the Review Activity
        onView(withId(R.id.buttonReview))
                .check(matches(isDisplayed()))
                .perform(click());

        // Step 1: Verify the Average Rating text is displayed
        onView(withId(R.id.average_rating_text_view))
                .check(matches(isDisplayed()));

        // Step 2: Enter and submit a new review with a rating of 5
        onView(withId(R.id.review_edit_text))
                .perform(typeText("Amazing!"), closeSoftKeyboard());
        onView(withId(R.id.rating_bar))
                .perform(click());
        onView(withId(R.id.submit_review_button))
                .check(matches(isDisplayed()))
                .perform(click());

        // Step 3: Verify that the average rating updates correctly
        onView(withId(R.id.average_rating_text_view))
                .check(matches(withText("Average Rating: 5")));
    }

    // BLACK BOX TEST 14: Verify Duplicate Review Handling
    // AUTHOR: DOUG PETERSON
    @Test
    public void testDuplicateReviewSubmission() {
        // Log in the user
        activityRule.getScenario().onActivity(activity -> {
            FirebaseAuth.getInstance().signInWithEmailAndPassword("martinestrin2@yahoo.com", "WHITEBOXTEST2")
                    .addOnCompleteListener(task -> {
                        if (!task.isSuccessful()) {
                            throw new AssertionError("Failed to log in user for testing.");
                        }
                    });
        });
        activityRule.getScenario().recreate();

        // Navigate to the hike activity via the user profile
        onView(withId(R.id.search_button))
                .check(matches(isDisplayed()))
                .perform(click());

        // Navigate to the Review Activity
        onView(withId(R.id.buttonReview))
                .check(matches(isDisplayed()))
                .perform(click());

        // Step 1: Submit the first review
        onView(withId(R.id.review_edit_text))
                .perform(typeText("First review"), closeSoftKeyboard());
        onView(withId(R.id.rating_bar))
                .perform(click());
        onView(withId(R.id.submit_review_button))
                .check(matches(isDisplayed()))
                .perform(click());

        // Step 2: Submit a second review
        onView(withId(R.id.review_edit_text))
                .perform(typeText("Updated review"), closeSoftKeyboard());
        onView(withId(R.id.rating_bar))
                .perform(click());
        onView(withId(R.id.submit_review_button))
                .check(matches(isDisplayed()))
                .perform(click());

        // Step 3: Verify that the review list shows only the updated review
        onView(withText("Updated review"))
                .check(matches(isDisplayed()));
        onView(withText("First review"))
                .check(doesNotExist());
    }

    // TEST 15: Search for a Hike, Add a Review, and Verify It Appears in Search Results
    @Test
    public void testSearchAndAddReview() {
        // Log in the user
        activityRule.getScenario().onActivity(activity -> {
            FirebaseAuth.getInstance().signInWithEmailAndPassword("martinestrin2@yahoo.com", "WHITEBOXTEST2")
                    .addOnCompleteListener(task -> {
                        if (!task.isSuccessful()) {
                            throw new AssertionError("Failed to log in user for testing.");
                        }
                    });
        });
        activityRule.getScenario().recreate();

        // Step 1: Navigate to the hike activity via the user profile
        onView(withId(R.id.search_button))
                .check(matches(isDisplayed()))
                .perform(click());

        // Step 2: Search for "Griffith Observatory"
        onView(withId(R.id.editTextSearchHike))
                .perform(typeText("Griffith Observatory"), closeSoftKeyboard());
        onView(withId(R.id.search_button))
                .check(matches(isDisplayed()))
                .perform(click());

        // Step 3: Verify the hike details page is displayed
        onView(withText("Griffith Observatory"))
                .check(matches(isDisplayed()));

        // Step 4: Navigate to the Review Activity
        onView(withId(R.id.buttonReview))
                .check(matches(isDisplayed()))
                .perform(click());

        // Step 5: Add a review for the hike
        onView(withId(R.id.review_edit_text))
                .perform(typeText("Stunning views, must visit!"), closeSoftKeyboard());
        onView(withId(R.id.rating_bar))
                .perform(click()); // Assume this sets a 5-star rating
        onView(withId(R.id.submit_review_button))
                .check(matches(isDisplayed()))
                .perform(click());

        // Step 6: Verify that the review is displayed in the review list
        onView(withText("Stunning views, must visit!"))
                .check(matches(isDisplayed()));

        // Step 7: Return to the search activity
        onView(withId(R.id.buttonBackToHike))
                .check(matches(isDisplayed()))
                .perform(click());

        // Step 8: Perform the same search again
        onView(withId(R.id.editTextSearchHike))
                .perform(typeText("Griffith Observatory"), closeSoftKeyboard());
        onView(withId(R.id.search_button))
                .check(matches(isDisplayed()))
                .perform(click());

        // Step 9: Verify the added review is displayed on the hike details page
        onView(withText("Stunning views, must visit!"))
                .check(matches(isDisplayed()));
    }

}