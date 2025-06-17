package com.gigamind.cognify.util;

import android.view.View;

/**
 * Strategy interface defining basic view animations. Implementations can
 * provide different animation behaviors or no-ops for testing.
 */
public interface ViewAnimator {
    void pulse(View view, float scale, long duration);
    void shake(View view, float distancePx);
    void shake(View view); // convenience
    void fadeIn(View view, long duration);
    void fadeOut(View view, long duration);
    void fadeInWithDelay(View view, long delayMs, long duration);
}
