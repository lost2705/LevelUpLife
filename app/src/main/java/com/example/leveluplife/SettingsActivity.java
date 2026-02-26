package com.example.leveluplife;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.leveluplife.data.database.AppDatabase;
import com.example.leveluplife.data.repository.PlayerRepository;
import com.example.leveluplife.utils.SoundManager;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class SettingsActivity extends AppCompatActivity {

    private SoundManager soundManager;
    private SharedPreferences prefs;

    private static final String PREF_NOTIFICATIONS = "notifications_enabled";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        soundManager = SoundManager.getInstance(this);
        prefs = getSharedPreferences("app_settings", MODE_PRIVATE);

        setupBackButton();
        setupSoundToggle();
        setupNotificationsToggle();
        setupResetProgress();
    }

    private void setupBackButton() {
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());
    }

    private void setupSoundToggle() {
        SwitchMaterial switchSound = findViewById(R.id.switchSound);
        switchSound.setChecked(soundManager.isSoundEnabled());

        switchSound.setOnCheckedChangeListener((buttonView, isChecked) -> {
            soundManager.setSoundEnabled(isChecked);
            // Play confirmation sound when enabling
            if (isChecked) {
                soundManager.playTaskComplete();
            }
        });
    }

    private void setupNotificationsToggle() {
        SwitchMaterial switchNotifications = findViewById(R.id.switchNotifications);
        switchNotifications.setChecked(prefs.getBoolean(PREF_NOTIFICATIONS, true));

        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) ->
                prefs.edit().putBoolean(PREF_NOTIFICATIONS, isChecked).apply()
        );
    }

    private void setupResetProgress() {
        LinearLayout btnReset = findViewById(R.id.btnResetProgress);

        btnReset.setOnClickListener(v ->
                new AlertDialog.Builder(this)
                        .setTitle("⚠️ Reset Progress")
                        .setMessage("All progress, levels and achievements will be deleted. This cannot be undone.")
                        .setPositiveButton("Reset", (dialog, which) -> resetAllProgress())
                        .setNegativeButton("Cancel", null)
                        .show()
        );
    }

    private void resetAllProgress() {
        SoundManager.resetInstance();
        PlayerRepository.resetInstance();

        new Thread(() -> {
            try {
                AppDatabase db = AppDatabase.getDatabase(getApplicationContext());

                db.taskDao().deleteAllTasks();
                db.playerDao().deletePlayer();
                db.completedTaskDao().deleteAllCompletedTasks();
                db.achievementDao().deleteAllAchievements();
                db.shopDao().deleteAllItems();

                AppDatabase.seedData(db);

                getSharedPreferences("app_settings", MODE_PRIVATE).edit().clear().apply();
                getSharedPreferences("player_prefs", MODE_PRIVATE).edit().clear().apply();
                getSharedPreferences("achievements_prefs", MODE_PRIVATE).edit().clear().apply();

            } catch (Exception e) {
                e.printStackTrace();
            }

            runOnUiThread(() -> {
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            });
        }).start();
    }
}
