package com.example.gohiking_cs310;

import androidx.test.espresso.idling.CountingIdlingResource;

/**d
 * define EspressoIdlingResource class to handle asynchronous test cases
 * prevent race conditions
 */
public class EspressoIdlingResource {

    // CountingIdlingResource tracks asynchronous operations under the tag "FirebaseLogin"
    private static final CountingIdlingResource resource =
            new CountingIdlingResource("FirebaseLogin");
    public static CountingIdlingResource getIdlingResource() {
        return resource;
    }

    /**
     * Increments the idling resource counter.
     * Should be called when an asynchronous task starts.
     */
    public static void increment() {
        resource.increment();
    }

    /**
     * Decrements the idling resource counter only if it's not already idle.
     * Should be called when an asynchronous task finishes.
     */
    public static void decrement() {
        if (!resource.isIdleNow()) {
            resource.decrement();
        }
    }
}
