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

        soundPool.setOnLoadCompleteListener((sp, sampleId, status) -> {
            if (status == 0) {
                loadedMap.put(sampleId, true);
                if (pendingSounds.remove(sampleId)) {
                    sp.play(sampleId, 1f, 1f, 0, 0, 1f);
                    triggerHaptic();
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

    private void playSound(int soundId) {
        if (!isSoundEnabled()) return;
        Boolean isLoaded = loadedMap.get(soundId);
        if (isLoaded != null && isLoaded) {
            soundPool.play(soundId, 1f, 1f, 0, 0, 1f);
            triggerHaptic();
        } else {
            pendingSounds.add(soundId);
        }
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