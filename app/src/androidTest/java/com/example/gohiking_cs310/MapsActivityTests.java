package com.example.gohiking_cs310;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.Espresso.pressBackUnconditionally;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.google.firebase.auth.FirebaseAuth;
import androidx.test.espresso.IdlingRegistry;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * MapsActivityTests performs UI tests for the login flow within MapsActivity.
 * It tests both valid and invalid login scenarios using Firebase Authentication.
 * EspressoIdlingResource ensures Firebase async operations are properly synchronized with test execution.
 */

@RunWith(AndroidJUnit4.class)
public class MapsActivityTests {

    // Launches MapsActivity before each test case
    @Rule
    public ActivityScenarioRule<MapsActivity> activityRule =
            new ActivityScenarioRule<>(MapsActivity.class);

    /**
     * Prepares the test environment:
     * - Signs out any previously authenticated user
     * - Registers EspressoIdlingResource for Firebase sync
     */
    @Before
    public void setUp() {
        FirebaseAuth.getInstance().signOut(); // Ensure clean session

        // Register idling hooks to sync with Firebase async events
        TestEnvironment.testHooks = new TestHooks() {
            @Override
            public void increment() {
                EspressoIdlingResource.increment();
            }

            @Override
            public void decrement() {
                EspressoIdlingResource.decrement();
            }
        };
        IdlingRegistry.getInstance().register(EspressoIdlingResource.getIdlingResource());
    }

    /**
     * Cleans up the test environment:
     * - Signs out the user
     * - Unregisters the idling resource to avoid memory leaks
     */
    @After
    public void tearDown() {
        TestEnvironment.testHooks = null;
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.getIdlingResource());
        FirebaseAuth.getInstance().signOut();
    }

    /**
     * Comprehensive test that checks login functionality under three scenarios:
     * Valid email & password
     * Valid email & invalid password
     * Invalid email & valid password
     * Each section validates correct screen transition or toast error.
     */
    @Test
    public void testLoginWithValidAndInvalidCredentials() {
        // === Navigate to Login Screen ===
        onView(withId(R.id.button_login))
                .check(matches(isDisplayed()))
                .perform(click());

        // === VALID LOGIN ===
        onView(withId(R.id.editTextUsername))
                .perform(typeText("white_box_testing_user@gmail.com"), closeSoftKeyboard());

        onView(withId(R.id.editTextPassword))
                .perform(typeText("password123"), closeSoftKeyboard());

        onView(withId(R.id.buttonLogin))
                .perform(click());

        // Verify successful login by checking if user email is displayed in UserActivity
        onView(withId(R.id.username))
                .check(matches(withText("white_box_testing_user@gmail.com")));

        // === Logout to continue testing invalid credentials ===
        onView(withId(R.id.buttonLogOut)).perform(click());

        // === INVALID PASSWORD ===
        onView(withId(R.id.button_login)).perform(click());

        onView(withId(R.id.editTextUsername))
                .perform(clearText(), typeText("white_box_testing_user@gmail.com"), closeSoftKeyboard());

        onView(withId(R.id.editTextPassword))
                .perform(clearText(), typeText("wrongpassword"), closeSoftKeyboard());

        onView(withId(R.id.buttonLogin))
                .perform(click());

        // Assert the presence of a toast error message
        assertTrue(ToastUtil.getLastToastMessage().contains("Login failed"));

        // === INVALID EMAIL ===
        onView(withId(R.id.editTextUsername))
                .perform(clearText(), typeText("notarealuser@example.com"), closeSoftKeyboard());

        onView(withId(R.id.editTextPassword))
                .perform(clearText(), typeText("password123"), closeSoftKeyboard());

        onView(withId(R.id.buttonLogin))
                .perform(click());

        // Assert the presence of a toast error message
        assertTrue(ToastUtil.getLastToastMessage().contains("Login failed"));
    }
}