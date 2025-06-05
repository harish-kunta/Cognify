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

    private GameTimer(Builder builder) {
        this.durationMs = builder.durationMs;
        this.tickIntervalMs = builder.tickIntervalMs;
        this.listener = builder.listener;
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

    /** Builder pattern for creating {@link GameTimer} instances in a readable
     *  manner. Example:
     *  <pre>
     *      GameTimer timer = new GameTimer.Builder()
     *              .duration(60000)
     *              .tickInterval(1000)
     *              .listener(myListener)
     *              .build();
     *  </pre>
     */
    public static class Builder {
        private long durationMs;
        private long tickIntervalMs = 1000; // sensible default
        private Listener listener;

        public Builder duration(long ms) {
            this.durationMs = ms;
            return this;
        }

        public Builder tickInterval(long ms) {
            this.tickIntervalMs = ms;
            return this;
        }

        public Builder listener(Listener l) {
            this.listener = l;
            return this;
        }

        public GameTimer build() {
            return new GameTimer(this);
        }
    }
}

