package com.example.gohiking_cs310;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import java.util.concurrent.CountDownLatch;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.firebase.auth.FirebaseAuth;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * ProfilePageTests contains black box UI tests for the UserActivity screen.
 * It validates key user profile features such as button visibility, friend management,
 * custom hike list creation, and hike search functionality using Espresso and FirebaseAuth.
 */

@RunWith(AndroidJUnit4.class)
public class ProfilePageTests {

    // Launches UserActivity before each test method
    @Rule
    public ActivityScenarioRule<UserActivity> activityRule =
            new ActivityScenarioRule<>(UserActivity.class);

    /**
     * BLACK BOX TEST #4:
     * Verifies that essential navigation buttons are visible on the profile screen:
     * - "Back to Home"
     * - "Log Out"
     */
    @Test
    public void testProfileButtonsAndTextViewVisibility() {
        onView(withId(R.id.buttonBackHome))
                .check(matches(isDisplayed()));

        onView(withId(R.id.buttonLogOut))
                .check(matches(isDisplayed()));
    }

    /**
     * BLACK BOX TEST #5:
     * Simulates adding a friend and verifies the friend appears in the user's friend list dialog.
     * Steps:
     * - Sign in with test user
     * - Add a friend by email
     * - Navigate to "My Friends" section
     * - Verify that the dialog contains the expected friend
     */
    @Test
    public void testAddFriendAndVerifyMyFriends() throws InterruptedException {
        CountDownLatch signInLatch = new CountDownLatch(1);

        // Sign in to load authenticated user context
        activityRule.getScenario().onActivity(activity -> {
            FirebaseAuth.getInstance().signInWithEmailAndPassword("newheree@gmail.com", "123456")
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            signInLatch.countDown();
                        } else {
                            throw new AssertionError("Failed to log in user for testing: " + task.getException());
                        }
                    });
        });

        signInLatch.await(); // Wait for authentication to complete
        activityRule.getScenario().recreate(); // Refresh activity with authenticated user

        // Enter friend's email and add
        onView(withId(R.id.editTextAddFriend))
                .perform(typeText("white_box_testing_user@gmail.com"), closeSoftKeyboard());

        onView(withId(R.id.buttonAddFriend))
                .check(matches(isDisplayed()))
                .perform(click());

        // Open My Friends dialog
        onView(withId(R.id.buttonMyFriends))
                .check(matches(isDisplayed()))
                .perform(click());

        // Verify dialog and friend email appear
        onView(withText("Your Friends"))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));

        onView(withText("white_box_testing_user@gmail.com"))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));
    }

    /**
     * BLACK BOX TEST #6:
     * Tests creation of a custom list called "MyFavoriteHikes" from the user profile.
     * Steps:
     * - Log in
     * - Navigate to custom list screen
     * - Enter list name
     * - Submit creation request
     */
    @Test
    public void testAddCustomList() {
        activityRule.getScenario().onActivity(activity -> {
            FirebaseAuth.getInstance().signInWithEmailAndPassword("white_box_testing_user@gmail.com", "password123")
                    .addOnCompleteListener(task -> {
                        if (!task.isSuccessful()) {
                            throw new AssertionError("Failed to log in user for testing.");
                        }
                    });
        });

        activityRule.getScenario().recreate();

        onView(withId(R.id.buttonCustomList))
                .check(matches(isDisplayed()))
                .perform(click());

        onView(withId(R.id.editTextCreateList))
                .perform(typeText("MyFavoriteHikes"), closeSoftKeyboard());

        onView(withId(R.id.buttonCreateList))
                .check(matches(isDisplayed()))
                .perform(click());
    }

    /**
     * BLACK BOX TEST #7:
     * Tests the hike search feature using the query "Griffith Observatory".
     * Steps:
     * - Log in
     * - Search for the hike
     * - Click on the hike item
     * - Verify hike detail dialog appears
     */
    @Test
    public void testSearchForHikeAndVerify() {
        activityRule.getScenario().onActivity(activity -> {
            FirebaseAuth.getInstance().signInWithEmailAndPassword("white_box_testing_user@gmail.com", "password123")
                    .addOnCompleteListener(task -> {
                        if (!task.isSuccessful()) {
                            throw new AssertionError("Failed to log in user for testing.");
                        }
                    });
        });

        activityRule.getScenario().recreate();

        // Enter search query
        onView(withId(R.id.editTextSearchHike))
                .perform(typeText("Griffith Observatory"), closeSoftKeyboard());

        // Perform search
        onView(withId(R.id.search_button))
                .check(matches(isDisplayed()))
                .perform(click());

        // Click "Show Details" on hike result
        onView(withId(R.id.buttonShowDetails))
                .check(matches(isDisplayed()))
                .perform(click());

        // Verify hike detail dialog appears with correct title
        onView(withText("Griffith Observatory"))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));
    }
}