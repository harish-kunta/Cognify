package com.gigamind.cognify.engine;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Handler;
import android.os.Looper;
import com.gigamind.cognify.util.ExceptionLogger;

import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutorService;
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

    private static final Object LOCK = new Object();
    private static volatile Set<String> sDictionary = null;
    private static volatile boolean sIsLoading = false;
    private static final List<Callback> pendingCallbacks = new ArrayList<>();
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    /**
     * Call this as early as possible (e.g. Application.onCreate() or MainActivity.onCreate())
     * so that by the time WordDashActivity wants it, it's already ready.
     */
    public static void preloadDictionary(Context context) {
        getDictionaryAsync(context, dictionary -> { /* preload only */ });
    }

    /**
     * If `sDictionary` is already loaded, invokes callback immediately on main thread.
     * Otherwise, kicks off a background load (once) and invokes callback when done.
     */
    public static void getDictionaryAsync(@NonNull Context context, @NonNull Callback callback) {
        synchronized (LOCK) {
            if (sDictionary != null) {
                new Handler(Looper.getMainLooper()).post(() -> callback.onLoaded(sDictionary));
                return;
            }

            pendingCallbacks.add(callback);
            if (sIsLoading) {
                return;
            }

            sIsLoading = true;
            executor.execute(() -> {
                Set<String> dict = new HashSet<>();
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(context.getAssets().open("words.txt")))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.length() >= com.gigamind.cognify.util.GameConfig.MIN_WORD_LENGTH) {
                            dict.add(line.trim().toUpperCase(Locale.US));
                        }
                    }
                } catch (IOException e) {
                    ExceptionLogger.log("DictionaryProvider", e);
                }

                Set<String> result = Collections.unmodifiableSet(dict);
                synchronized (LOCK) {
                    sDictionary = result;
                    sIsLoading = false;
                    Handler main = new Handler(Looper.getMainLooper());
                    for (Callback cb : pendingCallbacks) {
                        main.post(() -> cb.onLoaded(sDictionary));
                    }
                    pendingCallbacks.clear();
                }
            });
        }
    }
}

