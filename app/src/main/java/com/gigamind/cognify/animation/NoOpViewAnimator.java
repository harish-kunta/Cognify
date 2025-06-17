package com.gigamind.cognify.animation;

import android.view.View;

/**
 * Null object implementation of {@link ViewAnimator} used when animations are
 * disabled. All methods simply update view properties to their final state
 * without performing any animated transitions.
 */
public class NoOpViewAnimator implements ViewAnimator {

    @Override
    public void pulse(View view, float scale, long duration) {
        // no-op
    }

    @Override
    public void shake(View view, float distancePx) {
        // no-op
    }

    @Override
    public void shake(View view) {
        // no-op
    }

    @Override
    public void fadeIn(View view, long duration) {
        view.setAlpha(1f);
        view.setVisibility(View.VISIBLE);
    }

    @Override
    public void fadeOut(View view, long duration) {
        view.setAlpha(0f);
        view.setVisibility(View.GONE);
    }

    @Override
    public void fadeInWithDelay(View view, long delayMs, long duration) {
        view.setAlpha(1f);
        view.setVisibility(View.VISIBLE);
    }
}
