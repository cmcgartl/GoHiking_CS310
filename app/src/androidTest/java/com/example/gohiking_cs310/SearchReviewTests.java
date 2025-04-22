package com.example.gohiking_cs310;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.view.View;
import android.widget.TextView;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.google.firebase.auth.FirebaseAuth;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.assertTrue;

/**
 * SearchReviewTests contains black box tests for hike search and review-related functionality.
 * It verifies valid/invalid search results, review submission and update, average rating display,
 * and proper handling of duplicate reviews.
 */
@RunWith(AndroidJUnit4.class)
public class SearchReviewTests {

    // Launches UserActivity before each test
    @Rule
    public ActivityScenarioRule<UserActivity> activityRule =
            new ActivityScenarioRule<>(UserActivity.class);

    /**
     * Test setup: signs out the user, initializes Espresso Intents,
     * and registers an IdlingResource to sync async Firebase operations with Espresso.
     */
    @Before
    public void setUp() {
        Intents.init(); // For capturing and verifying intents
        FirebaseAuth.getInstance().signOut(); // Ensure clean state

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
     * Cleans up after tests: unregisters resources and signs out the user.
     */
    @After
    public void tearDown() {
        Intents.release();
        TestEnvironment.testHooks = null;
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.getIdlingResource());
        FirebaseAuth.getInstance().signOut();
    }

    /**
     * BLACK BOX TEST 10:
     * Verifies successful search functionality using a valid hike name.
     */
    @Test
    public void testSearchWithValidInput() {
        // Log in
        activityRule.getScenario().onActivity(activity -> {
            FirebaseAuth.getInstance().signInWithEmailAndPassword("newheree@gmail.com", "123456")
                    .addOnCompleteListener(task -> {
                        if (!task.isSuccessful()) {
                            throw new AssertionError("Login failed.");
                        }
                    });
        });
        activityRule.getScenario().recreate();

        // Perform hike search
        onView(withId(R.id.editTextSearchHike))
                .perform(typeText("Griffith Observatory"), closeSoftKeyboard());
        onView(withId(R.id.search_button)).perform(click());
        onView(withId(R.id.buttonShowDetails)).perform(click());

        // Verify hike details dialog appears
        onView(withText("Griffith Observatory"))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));
    }

    /**
     * BLACK BOX TEST 11:
     * Tests system behavior when searching for a hike that doesn’t exist.
     */
    @Test
    public void testSearchWithInvalidInput() {
        activityRule.getScenario().onActivity(activity -> {
            FirebaseAuth.getInstance().signInWithEmailAndPassword("newheree@gmail.com", "123456")
                    .addOnCompleteListener(task -> {
                        if (!task.isSuccessful()) {
                            throw new AssertionError("Login failed.");
                        }
                    });
        });
        activityRule.getScenario().recreate();

        // Invalid search input
        onView(withId(R.id.editTextSearchHike))
                .perform(typeText("Invalid Hike Name"), closeSoftKeyboard());
        onView(withId(R.id.search_button)).perform(click());

        // Validate toast response
        assertTrue(ToastUtil.getLastToastMessage().contains("No hike found"));
    }

    /**
     * BLACK BOX TEST 12:
     * Tests submitting a new review for a hike.
     */
    @Test
    public void testSubmitReview() {
        // Login and navigate to review page
        activityRule.getScenario().onActivity(activity -> {
            FirebaseAuth.getInstance().signInWithEmailAndPassword("newheree@gmail.com", "123456")
                    .addOnCompleteListener(task -> {
                        if (!task.isSuccessful()) {
                            throw new AssertionError("Login failed.");
                        }
                    });
        });
        activityRule.getScenario().recreate();

        onView(withId(R.id.editTextSearchHike))
                .perform(typeText("Griffith Observatory"), closeSoftKeyboard());
        onView(withId(R.id.search_button)).perform(click());

        // Confirm correct activity is shown
        intended(hasComponent(HikeActivity.class.getName()));

        onView(withId(R.id.buttonReview)).perform(click());

        // Submit a review
        onView(withId(R.id.review_edit_text))
                .perform(replaceText("Beautiful hike!"), closeSoftKeyboard());
        onView(withId(R.id.rating_bar)).perform(click());
        onView(withId(R.id.submit_review_button)).perform(click());

        // Confirm review was submitted
        assertTrue(ToastUtil.getLastToastMessage().contains("Beautiful hike!"));
    }

    /**
     * Utility method to pause UI thread during tests.
     */
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

    /**
     * BLACK BOX TEST 13:
     * Verifies that the average rating text view is displayed and updates after review submission.
     */
    @Test
    public void testAverageRatingDisplay() {
        activityRule.getScenario().onActivity(activity -> {
            FirebaseAuth.getInstance().signInWithEmailAndPassword("newheree@gmail.com", "123456")
                    .addOnCompleteListener(task -> {
                        if (!task.isSuccessful()) {
                            throw new AssertionError("Login failed.");
                        }
                    });
        });
        activityRule.getScenario().recreate();

        onView(withId(R.id.editTextSearchHike))
                .perform(typeText("Griffith Observatory"), closeSoftKeyboard());
        onView(withId(R.id.search_button)).perform(click());
        onView(isRoot()).perform(waitFor(1000)); // Wait for intent transition

        intended(hasComponent(HikeActivity.class.getName()));
        onView(withId(R.id.buttonReview)).perform(click());

        onView(withId(R.id.average_rating_text_view)).check(matches(isDisplayed()));

        // Submit a review
        onView(withId(R.id.review_edit_text)).perform(typeText("Amazing!"), closeSoftKeyboard());
        onView(withId(R.id.rating_bar)).perform(click());
        onView(withId(R.id.submit_review_button)).perform(click());

        // Confirm average rating is still displayed
        onView(withId(R.id.average_rating_text_view)).check(matches(isDisplayed()));
    }

    /**
     * BLACK BOX TEST 14:
     * Tests updating a review for the same hike by the same user.
     */
    @Test
    public void testSearchAndMultipleReview() {
        activityRule.getScenario().onActivity(activity -> {
            FirebaseAuth.getInstance().signInWithEmailAndPassword("newheree@gmail.com", "123456")
                    .addOnCompleteListener(task -> {
                        if (!task.isSuccessful()) {
                            throw new AssertionError("Login failed.");
                        }
                    });
        });
        activityRule.getScenario().recreate();

        // Open hike details and go to review section
        onView(withId(R.id.editTextSearchHike))
                .perform(typeText("Griffith Observatory"), closeSoftKeyboard());
        onView(withId(R.id.search_button)).perform(click());

        intended(hasComponent(HikeActivity.class.getName()));
        onView(withId(R.id.buttonReview)).perform(click());

        // Submit initial review
        onView(withId(R.id.review_edit_text)).perform(replaceText("testing123"), closeSoftKeyboard());
        onView(withId(R.id.rating_bar)).perform(click());
        onView(withId(R.id.submit_review_button)).perform(click());
        assertTrue(ToastUtil.getLastToastMessage().contains("testing123"));

        // Update review
        onView(withId(R.id.review_edit_text)).perform(replaceText("Updated review"), closeSoftKeyboard());
        onView(withId(R.id.rating_bar)).perform(click());
        onView(withId(R.id.submit_review_button)).perform(click());
        assertTrue(ToastUtil.getLastToastMessage().contains("Updated review"));
    }

    /**
     * Custom matcher for verifying substring in TextViews.
     */
    public static Matcher<View> withSubstring(final String substring) {
        return new TypeSafeMatcher<View>() {
            @Override
            public boolean matchesSafely(View view) {
                if (!(view instanceof TextView)) return false;
                String text = ((TextView) view).getText().toString();
                return text.contains(substring);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("with substring: " + substring);
            }
        };
    }
}