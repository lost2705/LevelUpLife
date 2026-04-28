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
    private static final String PREF_HP_BAR_COLOR = "hp_bar_color";
    private static final String PREF_MANA_BAR_COLOR = "mana_bar_color";
    private static final String PREF_XP_BAR_COLOR = "xp_bar_color";

    private static final int DEFAULT_HP_COLOR = 0xFFFF5252;
    private static final int DEFAULT_MANA_COLOR = 0xFF448AFF;
    private static final int DEFAULT_XP_COLOR = 0xFFFFD700;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        soundManager = SoundManager.getInstance(this);
        prefs = getSharedPreferences("app_settings", MODE_PRIVATE);

        setupBackButton();
        setupSoundToggle();
        setupNotificationsToggle();
        setupBarColorCustomization();
        setupAdminPanel();
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

    private void setupBarColorCustomization() {
        LinearLayout btnBarColors = findViewById(R.id.btnBarColors);
        if (btnBarColors == null) return;

        btnBarColors.setOnClickListener(v -> {
            String[] options = {
                    "❤️ HP Bar",
                    "💙 Mana Bar",
                    "✨ XP Bar"
            };

            new AlertDialog.Builder(this)
                    .setTitle("Choose bar to customize")
                    .setItems(options, (dialog, which) -> {
                        if (which == 0) {
                            showColorPickerDialog("HP Bar", PREF_HP_BAR_COLOR, DEFAULT_HP_COLOR);
                        } else if (which == 1) {
                            showColorPickerDialog("Mana Bar", PREF_MANA_BAR_COLOR, DEFAULT_MANA_COLOR);
                        } else if (which == 2) {
                            showColorPickerDialog("XP Bar", PREF_XP_BAR_COLOR, DEFAULT_XP_COLOR);
                        }
                    })
                    .show();
        });
    }

    private void showColorPickerDialog(String title, String prefKey, int defaultColor) {
        String[] colorNames = {
                "🔴 Red",
                "🟢 Green",
                "🔵 Blue",
                "🟣 Purple",
                "🟡 Gold",
                "🟠 Orange",
                "🩷 Pink",
                "🔄 Reset to default"
        };

        int[] colorValues = {
                0xFFFF5252,
                0xFF4CAF50,
                0xFF448AFF,
                0xFFBB86FC,
                0xFFFFD700,
                0xFFFF9800,
                0xFFE91E63,
                defaultColor
        };

        new AlertDialog.Builder(this)
                .setTitle(title)
                .setItems(colorNames, (dialog, which) -> {
                    prefs.edit().putInt(prefKey, colorValues[which]).apply();
                    soundManager.playTaskComplete();
                })
                .show();
    }

    private void setupAdminPanel() {
        LinearLayout btnAdminPanel = findViewById(R.id.btnAdminPanel);
        if (btnAdminPanel == null) return;

        btnAdminPanel.setOnClickListener(v -> {
            startActivity(new Intent(this, AdminActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
    }

    private void setupResetProgress() {
        LinearLayout btnReset = findViewById(R.id.btnResetProgress);
        if (btnReset == null) return;

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

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}