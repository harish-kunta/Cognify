package com.gigamind.cognify.util;

/**
 * Simple service locator for {@link ViewAnimator} instances. Allows swapping
 * implementations for testing or to globally disable animations.
 */
public final class AnimatorProvider {
    private static ViewAnimator instance = new DefaultViewAnimator();

    private AnimatorProvider() {
        // no instances
    }

    /** Returns the current {@link ViewAnimator} implementation. */
    public static ViewAnimator get() {
        return instance;
    }

    /**
     * Replaces the current animator implementation. Passing {@code null}
     * resets to the default animator.
     */
    public static void set(ViewAnimator animator) {
        instance = (animator != null) ? animator : new DefaultViewAnimator();
    }
}
