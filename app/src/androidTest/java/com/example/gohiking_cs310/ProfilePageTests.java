package com.example.gohiking_cs310;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
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
public class ProfilePageTests {

    @Rule
    public ActivityScenarioRule<UserActivity> activityRule =
            new ActivityScenarioRule<>(UserActivity.class);

    // BLACK BOX TEST #4: Verify Buttons and Text View Visibility
    // AUTHOR: Martin Estrin
    @Test
    public void testProfileButtonsAndTextViewVisibility() {
        // Verify "Back" button is displayed
        onView(withId(R.id.buttonBackHome))
                .check(matches(isDisplayed()));

        // Verify "Log Out" button is displayed
        onView(withId(R.id.buttonLogOut))
                .check(matches(isDisplayed()));
    }

    // BLACK BOX TEST #5: Add a Friend and Verify in My Friends Section
    // AUTHOR: Martin Estrin
    @Test
    public void testAddFriendAndVerifyMyFriends() {
        activityRule.getScenario().onActivity(activity -> {
            FirebaseAuth.getInstance().signInWithEmailAndPassword("martinestrin2@yahoo.com", "WHITEBOXTEST2")
                    .addOnCompleteListener(task -> {
                        if (!task.isSuccessful()) {
                            throw new AssertionError("Failed to log in user for testing.");
                        }
                    });
        });
        activityRule.getScenario().recreate();

        // Enter email in the "Add Friend" field
        onView(withId(R.id.editTextAddFriend))
                .perform(typeText("dougpete@gmail.com"), closeSoftKeyboard());

        // Click on the "Add Friend" button
        onView(withId(R.id.buttonAddFriend))
                .check(matches(isDisplayed()))
                .perform(click());

        // Navigate to the "My Friends" section
        onView(withId(R.id.buttonMyFriends))
                .check(matches(isDisplayed()))
                .perform(click());

        // Verify the "Your Friends" dialog is displayed
        onView(withText("Your Friends"))
                .check(matches(isDisplayed()));

        // Check that "dougpete@gmail.com" appears in the list of friends
        onView(withText("dougpete@gmail.com"))
                .check(matches(isDisplayed()));
    }

    // BLACK BOX TEST #6: Add a Custom List Named "MyFavoriteHikes"
    // AUTHOR: Martin Estrin
    @Test
    public void testAddCustomList() {
        activityRule.getScenario().onActivity(activity -> {
            FirebaseAuth.getInstance().signInWithEmailAndPassword("martinestrin2@yahoo.com", "WHITEBOXTEST2")
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        throw new AssertionError("Failed to log in user for testing.");
                    }
                });
        });
        activityRule.getScenario().recreate();

        // Step 1: Navigate to the "Manage Custom Lists" section
        onView(withId(R.id.buttonCustomList))
                .check(matches(isDisplayed()))
                .perform(click());

        // Step 2: Enter the name of the new custom list
        onView(withId(R.id.editTextCreateList))
                .perform(typeText("MyFavoriteHikes"), closeSoftKeyboard());

        // Step 3: Click the "Create New List" button
        onView(withId(R.id.buttonCreateList))
                .check(matches(isDisplayed()))
                .perform(click());

    }

    // BLACK BOX TEST #7: Search for "Griffith Observatory" and Verify Hike Page
    // AUTHOR: Martin Estrin
    @Test
    public void testSearchForHikeAndVerify() {
        activityRule.getScenario().onActivity(activity -> {
            FirebaseAuth.getInstance().signInWithEmailAndPassword("martinestrin2@yahoo.com", "WHITEBOXTEST2")
                    .addOnCompleteListener(task -> {
                        if (!task.isSuccessful()) {
                            throw new AssertionError("Failed to log in user for testing.");
                        }
                    });
        });
        activityRule.getScenario().recreate();

        // Step 1: Enter "Griffith Observatory" in the search field
        onView(withId(R.id.editTextSearchHike))
                .perform(typeText("Griffith Observatory"), closeSoftKeyboard());

        // Step 2: Click the search button
        onView(withId(R.id.search_button))
                .check(matches(isDisplayed()))
                .perform(click());

        // Step 3: Verify that the hike details page is displayed
        onView(withText("Griffith Observatory"))
                .check(matches(isDisplayed()));
    }

}