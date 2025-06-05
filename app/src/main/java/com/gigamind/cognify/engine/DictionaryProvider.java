package com.gigamind.cognify.engine;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.Executors;

/**
 * Singleton provider for the dictionary. Loads on a background thread, caches in a static Set.
 * Call `getDictionaryAsync(...)` at app startup or before game launch.
 */
public class DictionaryProvider {
    public interface Callback {
        /**
         * Called on the main thread when the dictionary is fully loaded (or if an error occurred).
         * @param dictionary a non‐null Set of uppercase words, or empty Set on error.
         */
        void onLoaded(@NonNull Set<String> dictionary);
    }

    private static volatile Set<String> sDictionary = null;
    private static volatile boolean sIsLoading = false;

    /**
     * Call this as early as possible (e.g. Application.onCreate() or MainActivity.onCreate())
     * so that by the time WordDashActivity wants it, it's already ready.
     */
    public static void preloadDictionary(Context context) {
        if (sDictionary != null || sIsLoading) return;
        sIsLoading = true;

        new Thread(() -> {
            Set<String> dict = new HashSet<>();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(context.getAssets().open("words.txt")))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.length() >= 3) {
                        dict.add(line.toUpperCase(Locale.US));
                    }
                }
            } catch (IOException e) {
                Log.e("DictionaryProvider", "Error preloading dictionary", e);
            }
            sDictionary = dict;
            sIsLoading = false;
            // If you need a callback on main thread, you could post to a Handler here.
        }).start();
    }

    /**
     * If `sDictionary` is already loaded, invokes callback immediately on main thread.
     * Otherwise, kicks off a background load (once) and invokes callback when done.
     */
    public static void getDictionaryAsync(@NonNull Context context, @NonNull Callback callback) {
        if (sDictionary != null) {
            // Already loaded → call back immediately on main thread
            new Handler(Looper.getMainLooper()).post(() -> callback.onLoaded(sDictionary));
            return;
        }
        synchronized (DictionaryProvider.class) {
            if (sDictionary != null) {
                // double‐checked
                new Handler(Looper.getMainLooper()).post(() -> callback.onLoaded(sDictionary));
                return;
            }
            if (sIsLoading) {
                // Already in progress; spin until loaded
                new Thread(() -> {
                    while (sIsLoading) {
                        try { Thread.sleep(50); } catch (InterruptedException ignored) {}
                    }
                    new Handler(Looper.getMainLooper()).post(() -> callback.onLoaded(sDictionary));
                }).start();
                return;
            }
            // Otherwise: start loading
            sIsLoading = true;
            Executors.newSingleThreadExecutor().execute(() -> {
                Set<String> dict = new HashSet<>();
                try {
                    AssetManager am = context.getAssets();
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(am.open("words.txt"))
                    );
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.length() >= com.gigamind.cognify.util.GameConfig.MIN_WORD_LENGTH) {
                            dict.add(line.trim().toUpperCase(Locale.US));
                        }
                    }
                    reader.close();
                } catch (IOException e) {
                    Log.e("DictionaryProvider", "Error loading dictionary", e);
                }
                // freeze into unmodifiable set
                sDictionary = Collections.unmodifiableSet(dict);
                sIsLoading = false;
                new Handler(Looper.getMainLooper()).post(() -> callback.onLoaded(sDictionary));
            });
        }
    }
}

