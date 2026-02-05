package com.example.leveluplife.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.SoundPool;

import com.example.leveluplife.R;

public class SoundManager {

    private static SoundManager instance;
    private SoundPool soundPool;
    private SharedPreferences prefs;

    // Sound IDs
    private int levelUpSound;
    private int taskCompleteSound;
    private int xpGainSound;
    private int swipeDeleteSound;

    private static final String PREF_SOUND_ENABLED = "sound_enabled";
    private static final float DEFAULT_VOLUME = 0.7f;

    private SoundManager(Context context) {
        prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE);

        //SoundPool
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(5)
                .setAudioAttributes(audioAttributes)
                .build();

        levelUpSound = soundPool.load(context, R.raw.levelup, 1);
        taskCompleteSound = soundPool.load(context, R.raw.task_complete, 1);
        xpGainSound = soundPool.load(context, R.raw.xp_gain, 1);
        swipeDeleteSound = soundPool.load(context, R.raw.swipe_delete, 1);
    }

    public static synchronized SoundManager getInstance(Context context) {
        if (instance == null) {
            instance = new SoundManager(context.getApplicationContext());
        }
        return instance;
    }

    public void playLevelUp() {
        playSound(levelUpSound, 1.0f);
    }

    public void playTaskComplete() {
        playSound(taskCompleteSound, DEFAULT_VOLUME);
    }

    public void playXpGain() {
        playSound(xpGainSound, DEFAULT_VOLUME);
    }

    public void playSwipeDelete() {
        playSound(swipeDeleteSound, 0.5f);
    }

    private void playSound(int soundId, float volume) {
        if (isSoundEnabled()) {
            soundPool.play(soundId, volume, volume, 1, 0, 1.0f);
        }
    }

    public boolean isSoundEnabled() {
        return prefs.getBoolean(PREF_SOUND_ENABLED, true);
    }

    public void setSoundEnabled(boolean enabled) {
        prefs.edit().putBoolean(PREF_SOUND_ENABLED, enabled).apply();
    }

    public void release() {
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
    }
}
