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

import com.google.android.gms.maps.model.Marker;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class MapsActivityTests {

    @Rule
    public ActivityScenarioRule<MapsActivity> activityRule =
            new ActivityScenarioRule<>(MapsActivity.class);

    // BLACK BOX TEST #1: Login and Signup Buttons and Map Visibility
    // AUTHOR: Martin Estrin
    @Test
    public void testLoginSignupButtonsAndMapVisibility() {
        // Check if the Login button is displayed
        onView(withId(R.id.button_login))
                .check(matches(isDisplayed()));

        // Check if the Signup button is displayed
        onView(withId(R.id.button_signup))
                .check(matches(isDisplayed()));

        // Check if the Map view is displayed
        onView(withId(R.id.map))
                .check(matches(isDisplayed()));
    }


    // BLACK BOX TEST #2: User Login: Navigates to Login page and logs in as martinestrin2@yahoo.com
    // AUTHOR: Martin Estrin
    @Test
    public void testNavigateToLoginAndPerformLogin() {
        // Navigate from MapsActivity to Login activity
        onView(withId(R.id.button_login))
                .check(matches(isDisplayed()))
                .perform(click());

        // Verify navigation to Login activity
        onView(withId(R.id.editTextUsername))
                .check(matches(isDisplayed()));

        // Enter email in the email field
        onView(withId(R.id.editTextUsername))
                .perform(typeText("martinestrin2@yahoo.com"), closeSoftKeyboard());

        // Enter password in the password field
        onView(withId(R.id.editTextPassword))
                .perform(typeText("WHITEBOXTEST2"), closeSoftKeyboard());

        // Click the Login button
        onView(withId(R.id.buttonLogin))
                .perform(click());
    }


    // BLACK BOX TEST #3: User Sign-Up: Navigates to Sign-Up page and creates a new user
    // AUTHOR: Martin Estrin
    @Test
    public void testNavigateToSignUpAndPerformSignUp() {
        // Navigate from MapsActivity to SignUp activity
        onView(withId(R.id.button_signup))
                .check(matches(isDisplayed()))
                .perform(click());

        // Verify navigation to SignUp activity
        onView(withId(R.id.editTextUsername))
                .check(matches(isDisplayed()));

        // Enter email in the email field
        onView(withId(R.id.editTextUsername))
                .perform(typeText("white_box_test_user@example.com"), closeSoftKeyboard());

        // Enter username in the username field
        onView(withId(R.id.username))
                .perform(typeText("Test User"), closeSoftKeyboard());

        // Enter password in the password field
        onView(withId(R.id.editTextPassword))
                .perform(typeText("password123"), closeSoftKeyboard());

        // Click the Sign-Up button
        onView(withId(R.id.buttonSignUp))
                .perform(click());
    }

}