package com.gigamind.cognify.util;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;

import com.gigamind.cognify.R;

public class SoundManager {
    private static SoundManager instance;
    private final SoundPool soundPool;
    private final int buttonSoundId;
    private final int successSoundId;
    private final int welcomeSoundId;
    private boolean loaded;

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
        successSoundId = soundPool.load(context, R.raw.lesson_complete, 1);
        welcomeSoundId = soundPool.load(context, R.raw.welcome_tone, 1);
        soundPool.setOnLoadCompleteListener((sp, sampleId, status) -> {
            if (status == 0) {
                loaded = true;
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
        if (loaded) {
            soundPool.play(buttonSoundId, 1f, 1f, 0, 0, 1f);
        }
    }

    public void playSuccess() {
        if (loaded) {
            soundPool.play(successSoundId, 1f, 1f, 0, 0, 1f);
        }
    }

    public void playWelcome() {
        if (loaded) {
            soundPool.play(welcomeSoundId, 1f, 1f, 0, 0, 1f);
        }
    }

    public void release() {
        soundPool.release();
        instance = null;
    }
}