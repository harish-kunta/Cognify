// file: com/gigamind/cognify/util/AnimationUtils.java
package com.gigamind.cognify.util;

import android.content.Context;
import android.content.SharedPreferences;
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
        if (!isAnimationsEnabled(view.getContext())) return;
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
     * Shake a view left → right → back to center in quick succession.
     * @param view The target view to shake.
     * @param distancePx How far (in pixels) to translate the view.
     */
    public static void shake(View view, float distancePx) {
        if (!isAnimationsEnabled(view.getContext())) return;
        view.animate()
                .translationX(distancePx)
                .setDuration(50)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .withEndAction(() -> view.animate()
                        .translationX(-distancePx)
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

    /** Convenience overload using a default shake distance of 10px. */
    public static void shake(View view) {
        shake(view, 10f);
    }

    /**
     * Fade a view in over `duration` ms. Makes the view visible.
     */
    public static void fadeIn(View view, long duration) {
        if (!isAnimationsEnabled(view.getContext())) {
            view.setAlpha(1f);
            view.setVisibility(View.VISIBLE);
            return;
        }
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
        if (!isAnimationsEnabled(view.getContext())) {
            view.setAlpha(0f);
            view.setVisibility(View.GONE);
            return;
        }
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
        if (!isAnimationsEnabled(view.getContext())) {
            view.setAlpha(1f);
            view.setVisibility(View.VISIBLE);
            return;
        }
        view.setAlpha(0f);
        view.setVisibility(View.VISIBLE);
        view.animate()
                .alpha(1f)
                .setStartDelay(delayMs)
                .setDuration(duration)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();
    }

    private static boolean isAnimationsEnabled(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(
                Constants.PREF_APP, Context.MODE_PRIVATE);
        return prefs.getBoolean(Constants.PREF_ANIMATIONS_ENABLED, true);
    }
}
