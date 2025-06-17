package com.gigamind.cognify.util;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;
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

    // Track when each sound has finished loading
    private final Map<Integer, Boolean> loadedMap = new HashMap<>();
    private final Set<Integer> pendingSounds = new HashSet<>();

    private SoundManager(Context context) {
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

        soundPool.setOnLoadCompleteListener((sp, sampleId, status) -> {
            if (status == 0) {
                loadedMap.put(sampleId, true);
                if (pendingSounds.remove(sampleId)) {
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

    public void playWelcome() {
        playSound(welcomeSoundId);
    }

    private void playSound(int soundId) {
        Boolean isLoaded = loadedMap.get(soundId);
        if (isLoaded != null && isLoaded) {
            soundPool.play(soundId, 1f, 1f, 0, 0, 1f);
        } else {
            pendingSounds.add(soundId);
        }
    }

    public void release() {
        soundPool.release();
        loadedMap.clear();
        pendingSounds.clear();
        instance = null;
    }
}