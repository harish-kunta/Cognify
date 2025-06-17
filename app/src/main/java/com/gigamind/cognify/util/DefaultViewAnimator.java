package com.gigamind.cognify.util;

import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

/**
 * Default implementation of {@link ViewAnimator} that plays actual animations
 * using ViewPropertyAnimator APIs.
 */
public class DefaultViewAnimator implements ViewAnimator {


    @Override
    public void pulse(View view, float scale, long duration) {
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
                        .start())
                .start();
    }

    @Override
    public void shake(View view, float distancePx) {
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
                                .start())
                        .start())
                .start();
    }

    @Override
    public void shake(View view) {
        shake(view, 10f);
    }

    @Override
    public void fadeIn(View view, long duration) {
        view.setAlpha(0f);
        view.setVisibility(View.VISIBLE);
        view.animate()
                .alpha(1f)
                .setDuration(duration)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();
    }

    @Override
    public void fadeOut(View view, long duration) {
        view.animate()
                .alpha(0f)
                .setDuration(duration)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .withEndAction(() -> view.setVisibility(View.GONE))
                .start();
    }

    @Override
    public void fadeInWithDelay(View view, long delayMs, long duration) {
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
