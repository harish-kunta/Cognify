// file: com/gigamind/cognify/util/AnimationUtils.java
package com.gigamind.cognify.util;

import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.GridLayout;

public final class AnimationUtils {
    private AnimationUtils() {
        throw new AssertionError("No instances allowed");
    }

    /**
     * Scale a view up to `scale` then back down to 1f, all in `duration` ms.
     */
    public static void pulse(View view, float scale, long duration) {
        view.animate()
                .scaleX(scale)
                .scaleY(scale)
                .setDuration(duration)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .withEndAction(() -> view.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(duration)
                        .setInterpolator(new AccelerateDecelerateInterpolator())
                        .start()
                )
                .start();
    }

    /**
     * Shake a GridLayout left → right → back to center in quick succession.
     */
    public static void shake(GridLayout view) {
        view.animate()
                .translationX(10)
                .setDuration(50)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .withEndAction(() -> view.animate()
                        .translationX(-10)
                        .setDuration(50)
                        .setInterpolator(new AccelerateDecelerateInterpolator())
                        .withEndAction(() -> view.animate()
                                .translationX(0)
                                .setDuration(50)
                                .setInterpolator(new AccelerateDecelerateInterpolator())
                                .start()
                        )
                        .start()
                )
                .start();
    }

    /**
     * Fade a view in over `duration` ms. Makes the view visible.
     */
    public static void fadeIn(View view, long duration) {
        view.setAlpha(0f);
        view.setVisibility(View.VISIBLE);
        view.animate()
                .alpha(1f)
                .setDuration(duration)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();
    }

    /**
     * Fade a view out over `duration` ms, then set GONE.
     */
    public static void fadeOut(View view, long duration) {
        view.animate()
                .alpha(0f)
                .setDuration(duration)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .withEndAction(() -> view.setVisibility(View.GONE))
                .start();
    }

    /**
     * Fade a view in **after** a given delay.
     * (Useful if you want to chain animations in sequence.)
     */
    public static void fadeInWithDelay(View view, long delayMs, long duration) {
        view.setAlpha(0f);
        view.setVisibility(View.VISIBLE);
        view.animate()
                .alpha(1f)
                .setStartDelay(delayMs)
                .setDuration(duration)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();
    }
}
