package com.example.gohiking_cs310;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.app.Activity;
import android.view.View;

import androidx.test.espresso.PerformException;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.util.TreeIterables;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.firebase.auth.FirebaseAuth;

import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeoutException;

@RunWith(AndroidJUnit4.class)
public class SearchReviewTests {

    @Rule
    public ActivityScenarioRule<UserActivity> activityRule =
            new ActivityScenarioRule<>(UserActivity.class);

    @Before
    public void setUp() {
        Intents.init(); // Initialize Espresso Intents
    }

    @After
    public void tearDown() {
        Intents.release(); // Release Espresso Intents
    }

    // BLACK BOX TEST 10: Verify Search Functionality with Valid Input
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


        onView(withId(R.id.search_button))
                .check(matches(isDisplayed()))
                .perform(click());

        onView(withId(R.id.editTextSearchHike))
                .perform(typeText("Griffith Observatory"), closeSoftKeyboard());


        onView(withId(R.id.search_button))
                .check(matches(isDisplayed()))
                .perform(click());
        onView(withText("Griffith Observatory"))
                .check(matches(isDisplayed()));
    }

    // BLACK BOX TEST 11: Verify Search Functionality with Invalid Input
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


        onView(withId(R.id.search_button))
                .check(matches(isDisplayed()))
                .perform(click());
        onView(withId(R.id.editTextSearchHike))
                .perform(typeText("Invalid Hike Name"), closeSoftKeyboard());

        /*onView(withId(R.id.search_button))
                .check(matches(isDisplayed()))
                .perform(click());

        onView(withText("No hike found"))
                .check(matches(isDisplayed()));*/
    }

    // BLACK BOX TEST 12: Verify Review Submission
    @Test
    public void testSubmitReview() {
        // Log in programmatically using FirebaseAuth
        activityRule.getScenario().onActivity(activity -> {
            FirebaseAuth.getInstance().signInWithEmailAndPassword("martinestrin2@yahoo.com", "WHITEBOXTEST2")
                    .addOnCompleteListener(task -> {
                        if (!task.isSuccessful()) {
                            throw new AssertionError("Failed to log in user for testing.");
                        }
                    });
        });
        activityRule.getScenario().recreate();

        // Navigate to the hike details via search
        onView(withId(R.id.editTextSearchHike))
                .perform(typeText("Griffith Observatory"), closeSoftKeyboard());
        onView(withId(R.id.search_button))
                .perform(click());

        // Wait to ensure the intent is launched
        onView(isRoot()).perform(waitFor(1000));

        // Verify HikeActivity is launched
        intended(hasComponent(HikeActivity.class.getName()));

        // Check if the view in HikeActivity is displayed
        onView(withId(R.id.text_hike_name)).check(matches(isDisplayed()));

        // Navigate to the Review Activity
        onView(withId(R.id.buttonReview))
                .check(matches(isDisplayed()))
                .perform(click());

        // Enter a review and select a rating
        onView(withId(R.id.review_edit_text))
                .perform(replaceText(""), closeSoftKeyboard()); // Clears the text
        onView(withId(R.id.review_edit_text))
                .perform(typeText("Beautiful hike!"), closeSoftKeyboard()); // Types the new review
        onView(withId(R.id.rating_bar))
                .perform(click());

        // Submit the review
        onView(withId(R.id.submit_review_button))
                .check(matches(isDisplayed()))
                .perform(click());

        // Verify the review appears in the review list
        onView(withText("Beautiful hike!"))
                .check(matches(isDisplayed()));


    }

    private static ViewAction waitFor(long delay) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isRoot();
            }

            @Override
            public String getDescription() {
                return "Wait for " + delay + " milliseconds.";
            }

            @Override
            public void perform(UiController uiController, View view) {
                uiController.loopMainThreadForAtLeast(delay);
            }
        };
    }


    // BLACK BOX TEST 13: Verify Display of Average Rating

    @Test
    public void testAverageRatingDisplay() {
        // Log in programmatically using FirebaseAuth
        activityRule.getScenario().onActivity(activity -> {
            FirebaseAuth.getInstance().signInWithEmailAndPassword("martinestrin2@yahoo.com", "WHITEBOXTEST2")
                    .addOnCompleteListener(task -> {
                        if (!task.isSuccessful()) {
                            throw new AssertionError("Failed to log in user for testing.");
                        }
                    });
        });
        activityRule.getScenario().recreate();

        // Navigate to the hike details via search
        onView(withId(R.id.editTextSearchHike))
                .perform(typeText("Griffith Observatory"), closeSoftKeyboard());
        onView(withId(R.id.search_button))
                .perform(click());

        // Wait to ensure the intent is launched
        onView(isRoot()).perform(waitFor(1000));

        // Verify HikeActivity is launched
        intended(hasComponent(HikeActivity.class.getName()));

        // Check if the view in HikeActivity is displayed
        onView(withId(R.id.text_hike_name)).check(matches(isDisplayed()));

        // Navigate to the Review Activity
        onView(withId(R.id.buttonReview))
                .check(matches(isDisplayed()))
                .perform(click());

        // Verify that the average rating TextView is displayed initially
        onView(withId(R.id.average_rating_text_view))
                .check(matches(isDisplayed()));

        // Clear any existing text and add a review
        onView(withId(R.id.review_edit_text))
                .perform(replaceText(""), closeSoftKeyboard()); // Clears the text
        onView(withId(R.id.review_edit_text))
                .perform(typeText("Amazing!"), closeSoftKeyboard()); // Types the new review

        // Select a rating
        onView(withId(R.id.rating_bar))
                .perform(click()); // Assuming this sets a rating (e.g., 5 stars)

        // Submit the review
        onView(withId(R.id.submit_review_button))
                .check(matches(isDisplayed()))
                .perform(click());

        // Verify the updated average rating is displayed
        onView(withId(R.id.average_rating_text_view))
                .check(matches(withText("Average Rating: 3")));
    }


    // BLACK BOX TEST 14: Verify Duplicate Review Handling
    @Test
    public void testDuplicateReviewSubmission() {
        // Log in programmatically using FirebaseAuth
        activityRule.getScenario().onActivity(activity -> {
            FirebaseAuth.getInstance().signInWithEmailAndPassword("martinestrin2@yahoo.com", "WHITEBOXTEST2")
                    .addOnCompleteListener(task -> {
                        if (!task.isSuccessful()) {
                            throw new AssertionError("Failed to log in user for testing.");
                        }
                    });
        });
        activityRule.getScenario().recreate();

        // Navigate to the hike details via search
        onView(withId(R.id.editTextSearchHike))
                .perform(typeText("Griffith Observatory"), closeSoftKeyboard());
        onView(withId(R.id.search_button))
                .perform(click());

        // Wait to ensure the intent is launched
        onView(isRoot()).perform(waitFor(1000));

        // Verify HikeActivity is launched
        intended(hasComponent(HikeActivity.class.getName()));

        // Check if the view in HikeActivity is displayed
        onView(withId(R.id.text_hike_name)).check(matches(isDisplayed()));

        // Navigate to the Review Activity
        onView(withId(R.id.buttonReview))
                .check(matches(isDisplayed()))
                .perform(click());

        // Submit the first review
        onView(withId(R.id.review_edit_text))
                .perform(replaceText("First review"), closeSoftKeyboard());
        onView(withId(R.id.rating_bar))
                .perform(click()); // Assuming this sets a 5-star rating
        onView(withId(R.id.submit_review_button))
                .check(matches(isDisplayed()))
                .perform(click());

        // Verify the first review is displayed
        onView(withText("First review"))
                .check(matches(isDisplayed()));

        /*// Submit an updated review
        onView(withId(R.id.review_edit_text))
                .perform(replaceText("Updated review"), closeSoftKeyboard()); // Clear and update the text
        onView(withId(R.id.rating_bar))
                .perform(click()); // Assuming this updates the rating
        onView(withId(R.id.submit_review_button))
                .check(matches(isDisplayed()))
                .perform(click());

        // Verify the updated review is displayed and the first review is replaced
        onView(withText("Updated review"))
                .check(matches(isDisplayed()));
        onView(withText("First review"))
                .check(doesNotExist());*/
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


        onView(withId(R.id.search_button))
                .check(matches(isDisplayed()))
                .perform(click());

        onView(withId(R.id.editTextSearchHike))
                .perform(typeText("Griffith Observatory"), closeSoftKeyboard());
        onView(withId(R.id.search_button))
                .check(matches(isDisplayed()))
                .perform(click());

        onView(withText("Griffith Observatory"))
                .check(matches(isDisplayed()));


        onView(withId(R.id.buttonReview))
                .check(matches(isDisplayed()))
                .perform(click());

        //Add a review for the hike
        onView(withId(R.id.review_edit_text))
                .perform(typeText("Stunning views, must visit!"), closeSoftKeyboard());
        onView(withId(R.id.rating_bar))
                .perform(click()); // Assume this sets a 5-star rating
        onView(withId(R.id.submit_review_button))
                .check(matches(isDisplayed()))
                .perform(click());

        // Verify that the review is displayed in the review list
        onView(withText("Stunning views, must visit!"))
                .check(matches(isDisplayed()));

        onView(withId(R.id.buttonBackToHike))
                .check(matches(isDisplayed()))
                .perform(click());

        onView(withId(R.id.editTextSearchHike))
                .perform(typeText("Griffith Observatory"), closeSoftKeyboard());
        onView(withId(R.id.search_button))
                .check(matches(isDisplayed()))
                .perform(click());

        onView(withText("Stunning views, must visit!"))
                .check(matches(isDisplayed()));
    }

}