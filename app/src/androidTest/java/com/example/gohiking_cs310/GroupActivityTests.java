package com.example.gohiking_cs310;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.withDecorView;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static com.google.common.base.CharMatcher.is;
import static java.util.function.Predicate.not;

import androidx.test.espresso.UiController;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import android.app.Activity;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.regex.Matcher;

@RunWith(AndroidJUnit4.class)
public class GroupActivityTests {

    @Rule
    public ActivityScenarioRule<JoinAndViewGroups> activityRule =
            new ActivityScenarioRule<>(JoinAndViewGroups.class);

    // BLACK BOX TEST #8: Verify Buttons in the Group Activities Page
    @Test
    public void testGroupActivitiesButtonsVisibility() {
        onView(withId(R.id.creategroup))
                .check(matches(isDisplayed()));
        onView(withId(R.id.go_home))
                .check(matches(isDisplayed()));
    }

    // BLACK BOX TEST #9: Create a New Group
    @Test
    public void testCreateNewGroup() {
        activityRule.getScenario().onActivity(activity -> {
            FirebaseAuth.getInstance().signInWithEmailAndPassword("martinestrin2@yahoo.com", "WHITEBOXTEST2")
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        throw new AssertionError("Failed to log in user for testing.");
                    }
                });
        });

        onView(withId(R.id.creategroup))
                .check(matches(isDisplayed()));

    }

}