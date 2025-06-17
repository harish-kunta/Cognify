package com.gigamind.cognify.animation;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.gigamind.cognify.R;

public final class AnimationUtils {
    private AnimationUtils() {
        throw new AssertionError("No instances allowed");
    }

    public static void pulse(View view, float scale, long duration) {
        AnimatorProvider.get().pulse(view, scale, duration);
    }

    public static void shake(View view, float distancePx) {
        AnimatorProvider.get().shake(view, distancePx);
    }

    public static void shake(View view) {
        AnimatorProvider.get().shake(view);
    }

    public static void fadeIn(View view, long duration) {
        AnimatorProvider.get().fadeIn(view, duration);
    }

    public static void fadeOut(View view, long duration) {
        AnimatorProvider.get().fadeOut(view, duration);
    }

    public static void fadeInWithDelay(View view, long delayMs, long duration) {
        AnimatorProvider.get().fadeInWithDelay(view, delayMs, duration);
    }

    /**
     * Plays a simple bounce animation on the given view using the bundled
     * XML animation resource. This keeps XML-based animations alongside the
     * programmatic ones in this utility class.
     */
    public static void bounce(View view) {
        Animation bounce = AnimationUtils.loadAnimation(
                view.getContext(), R.anim.button_bounce);
        view.startAnimation(bounce);
    }
}
