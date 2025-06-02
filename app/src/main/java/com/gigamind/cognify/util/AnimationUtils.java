package com.gigamind.cognify.util;

import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.GridLayout;

public final class AnimationUtils {
    private AnimationUtils() {
        throw new AssertionError("No instances allowed");
    }

    public static void pulseAnimation(View view, float scale, long duration) {
        view.animate()
                .scaleX(scale)
                .scaleY(scale)
                .setDuration(duration)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .withEndAction(() ->
                        view.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(duration)
                                .setInterpolator(new AccelerateDecelerateInterpolator())
                                .start()
                ).start();
    }

    public static void shakeAnimation(GridLayout view) {
        view.animate()
                .translationX(10)
                .setDuration(50)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .withEndAction(() ->
                        view.animate()
                                .translationX(-10)
                                .setDuration(50)
                                .setInterpolator(new AccelerateDecelerateInterpolator())
                                .withEndAction(() ->
                                        view.animate()
                                                .translationX(0)
                                                .setDuration(50)
                                                .setInterpolator(new AccelerateDecelerateInterpolator())
                                                .start()
                                ).start()
                ).start();
    }

    public static void fadeIn(View view, long duration) {
        view.setAlpha(0f);
        view.setVisibility(View.VISIBLE);
        view.animate()
                .alpha(1f)
                .setDuration(duration)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();
    }

    public static void fadeOut(View view, long duration) {
        view.animate()
                .alpha(0f)
                .setDuration(duration)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .withEndAction(() -> view.setVisibility(View.GONE))
                .start();
    }
}
