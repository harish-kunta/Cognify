package com.gigamind.cognify.util;

import android.os.CountDownTimer;

/**
 * Utility class that wraps {@link CountDownTimer} and exposes tick/finish events
 * via a listener interface. This keeps timer logic separate from Activities and
 * makes it easier to test.
 */
public class GameTimer {

    public interface Listener {
        void onTick(long millisRemaining);
        void onFinish();
    }

    private final long durationMs;
    private final long tickIntervalMs;
    private final Listener listener;
    private CountDownTimer timer;

    public GameTimer(long durationMs, long tickIntervalMs, Listener listener) {
        this.durationMs = durationMs;
        this.tickIntervalMs = tickIntervalMs;
        this.listener = listener;
    }

    public void start() {
        stop();
        timer = new CountDownTimer(durationMs, tickIntervalMs) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (listener != null) {
                    listener.onTick(millisUntilFinished);
                }
            }

            @Override
            public void onFinish() {
                if (listener != null) {
                    listener.onFinish();
                }
            }
        }.start();
    }

    public void stop() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }
}
