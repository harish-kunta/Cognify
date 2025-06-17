// file: com/gigamind/cognify/util/AnimationUtils.java
package com.gigamind.cognify.util;

import android.view.View;

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
}
