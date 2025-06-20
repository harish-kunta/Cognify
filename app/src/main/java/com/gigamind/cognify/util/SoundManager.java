package com.gigamind.cognify.util;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.gigamind.cognify.R;

public class SoundManager {
    private static SoundManager instance;
    private final SoundPool soundPool;
    private final int buttonSoundId;
    private final int successSoundId;
    private final int welcomeSoundId;
    private final int correctSoundId;
    private final int wrongSoundId;
    private final int heartbeatSoundId;
    private final int bounceSoundId;
    private final int popSoundId;
    private final int swipeSoundId;
    private final int loseSoundId;
    // Reuse existing sounds for additional cues
    private final int toggleSoundId;
    private final int milestoneSoundId;
    private final Context context;
    private final Vibrator vibrator;

    // Track when each sound has finished loading
    private final Map<Integer, Boolean> loadedMap = new HashMap<>();
    private final Set<Integer> pendingSounds = new HashSet<>();

    private SoundManager(Context context) {
        this.context = context.getApplicationContext();
        vibrator = (Vibrator) this.context.getSystemService(Context.VIBRATOR_SERVICE);
        AudioAttributes attrs = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        soundPool = new SoundPool.Builder()
                .setMaxStreams(2)
                .setAudioAttributes(attrs)
                .build();

        buttonSoundId = soundPool.load(context, R.raw.button_click, 1);
        loadedMap.put(buttonSoundId, false);

        successSoundId = soundPool.load(context, R.raw.lesson_complete, 1);
        loadedMap.put(successSoundId, false);

        welcomeSoundId = soundPool.load(context, R.raw.welcome_tone, 1);
        loadedMap.put(welcomeSoundId, false);

        correctSoundId = soundPool.load(context, R.raw.success_sound, 1);
        loadedMap.put(correctSoundId, false);

        wrongSoundId = soundPool.load(context, R.raw.wrong_sound, 1);
        loadedMap.put(wrongSoundId, false);

        heartbeatSoundId = soundPool.load(context, R.raw.heartbeat, 1);
        loadedMap.put(heartbeatSoundId, false);

        bounceSoundId = soundPool.load(context, R.raw.button_bounce, 1);
        loadedMap.put(bounceSoundId, false);

        popSoundId = soundPool.load(context, R.raw.dialog_pop, 1);
        loadedMap.put(popSoundId, false);

        swipeSoundId = soundPool.load(context, R.raw.swipe_sound, 1);
        loadedMap.put(swipeSoundId, false);

        loseSoundId = soundPool.load(context, R.raw.lose_sound, 1);
        loadedMap.put(loseSoundId, false);

        // For toggles and milestone cues we simply reuse existing files
        toggleSoundId = buttonSoundId;
        milestoneSoundId = successSoundId;

        soundPool.setOnLoadCompleteListener((sp, sampleId, status) -> {
            if (status == 0) {
                loadedMap.put(sampleId, true);
                if (pendingSounds.remove(sampleId) && isSoundEnabled()) {
                    sp.play(sampleId, 1f, 1f, 0, 0, 1f);
                }
            }
        });
    }

    public static synchronized SoundManager getInstance(Context context) {
        if (instance == null) {
            instance = new SoundManager(context.getApplicationContext());
        }
        return instance;
    }

    public void playButton() {
        playSound(buttonSoundId);
    }

    public void playSuccess() {
        playSound(successSoundId);
    }

    public void playCorrect() {
        playSound(correctSoundId);
    }

    public void playIncorrect() {
        playSound(wrongSoundId);
    }

    public void playWelcome() {
        playSound(welcomeSoundId);
    }

    public void playHeartbeat() {
        playSound(heartbeatSoundId);
    }

    /** Plays a bouncy click for letter presses */
    public void playBounce() {
        playSound(bounceSoundId);
    }

    /** Subtle pop used when showing dialogs */
    public void playPop() {
        playSound(popSoundId);
    }

    /** Swipe cue for onboarding pages */
    public void playSwipe() {
        playSound(swipeSoundId);
    }

    /** Sad trombone when the player scores zero */
    public void playLose() {
        playSound(loseSoundId);
    }

    /** Plays a short click sound for toggle switches */
    public void playToggle() {
        playSound(toggleSoundId);
    }

    /** Plays a rewarding sound for streak milestones */
    public void playMilestone() {
        playSound(milestoneSoundId);
    }

    private void playSound(int soundId) {
        boolean soundEnabled = isSoundEnabled();
        if (soundEnabled) {
            Boolean isLoaded = loadedMap.get(soundId);
            if (isLoaded != null && isLoaded) {
                soundPool.play(soundId, 1f, 1f, 0, 0, 1f);
            } else {
                pendingSounds.add(soundId);
            }
        }
        triggerHaptic();
    }

    private boolean isSoundEnabled() {
        SharedPreferences prefs = context.getSharedPreferences(
                Constants.PREF_APP, Context.MODE_PRIVATE);
        return prefs.getBoolean(Constants.PREF_SOUND_ENABLED, true);
    }

    private boolean isHapticsEnabled() {
        SharedPreferences prefs = context.getSharedPreferences(
                Constants.PREF_APP, Context.MODE_PRIVATE);
        return prefs.getBoolean(Constants.PREF_HAPTICS_ENABLED, true);
    }

    private void triggerHaptic() {
        if (vibrator == null || !vibrator.hasVibrator()) return;
        if (!isHapticsEnabled()) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(40,
                    VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            vibrator.vibrate(40);
        }
    }

    public void release() {
        soundPool.release();
        loadedMap.clear();
        pendingSounds.clear();
        instance = null;
    }
}