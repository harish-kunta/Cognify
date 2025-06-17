package com.gigamind.cognify.animation;

import com.gigamind.cognify.util.Constants;

/**
 * Simple service locator for {@link ViewAnimator} instances. Allows swapping
 * implementations for testing or to globally disable animations.
 */
public final class AnimatorProvider {
    private static ViewAnimator defaultAnimator = new DefaultViewAnimator();
    private static final ViewAnimator NO_OP_ANIMATOR = new NoOpViewAnimator();
    private static boolean animationsEnabled = true;

    private AnimatorProvider() {
        // no instances
    }

    /** Returns the current {@link ViewAnimator} implementation. */
    public static ViewAnimator get() {
        return animationsEnabled ? defaultAnimator : NO_OP_ANIMATOR;
    }

    /**
     * Replaces the current animator implementation. Passing {@code null}
     * resets to the default animator.
     */
    public static void set(ViewAnimator animator) {
        defaultAnimator = (animator != null) ? animator : new DefaultViewAnimator();
    }

    /** Enable or disable animations globally. */
    public static void setAnimationsEnabled(boolean enabled) {
        animationsEnabled = enabled;
    }

    /** Returns whether animations are currently enabled. */
    public static boolean isAnimationsEnabled() {
        return animationsEnabled;
    }

    /** Updates the animation state based on shared preferences. */
    public static void updateFromPreferences(android.content.Context context) {
        android.content.SharedPreferences prefs = context.getSharedPreferences(
                Constants.PREF_APP, android.content.Context.MODE_PRIVATE);
        setAnimationsEnabled(prefs.getBoolean(Constants.PREF_ANIMATIONS_ENABLED, true));
    }
}
