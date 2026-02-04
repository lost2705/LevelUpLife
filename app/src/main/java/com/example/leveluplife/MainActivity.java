package com.example.leveluplife;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.leveluplife.data.entity.Player;
import com.example.leveluplife.data.entity.Task;
import com.example.leveluplife.data.model.LevelUpEvent;
import com.example.leveluplife.ui.dialogs.TaskCreationDialog;
import com.example.leveluplife.ui.tasks.TaskAdapter;
import com.example.leveluplife.viewModel.PlayerViewModel;
import com.example.leveluplife.viewModel.TaskViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private TaskViewModel taskViewModel;
    private PlayerViewModel playerViewModel;
    private TaskAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // === VIEWMODEL INITIALIZATION ===
        taskViewModel = new ViewModelProvider(this).get(TaskViewModel.class);
        playerViewModel = new ViewModelProvider(this).get(PlayerViewModel.class);

        // Создаем игрока при первом запуске
        playerViewModel.initializePlayerIfNeeded();

        // === RECYCLERVIEW SETUP ===
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        adapter = new TaskAdapter();
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // === OBSERVERS ===

        // Tasks observer
        taskViewModel.getAllTasks().observe(this, tasks -> {
            adapter.setTasks(tasks);
        });

        // Player observer (обновление UI)
        playerViewModel.getPlayer().observe(this, player -> {
            if (player != null) {
                updatePlayerUI(player);
            }
        });

        // Level-Up Observer
        playerViewModel.getLevelUpEvent().observe(this, event -> {
            if (event != null) {
                showLevelUpDialog(event);
            }
        });

        // === TASK COMPLETION TOGGLE ===
        adapter.setOnTaskCheckedChangeListener((task, isChecked) -> {
            taskViewModel.toggleTaskCompleted(task.getId(), isChecked);
        });

        // === FAB (CREATE TASK) ===
        FloatingActionButton fabAddTask = findViewById(R.id.fabAddTask);
        fabAddTask.setOnClickListener(v -> {
            TaskCreationDialog dialog = new TaskCreationDialog();
            dialog.setOnTaskCreatedListener(task -> {
                taskViewModel.insertTask(task);
            });
            dialog.show(getSupportFragmentManager(), "TaskCreationDialog");
        });

        // === SWIPE TO DELETE ===
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(
                new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {

                    @Override
                    public boolean onMove(@NonNull RecyclerView recyclerView,
                                          @NonNull RecyclerView.ViewHolder viewHolder,
                                          @NonNull RecyclerView.ViewHolder target) {
                        return false;
                    }

                    @Override
                    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                        int position = viewHolder.getAdapterPosition();
                        Task deletedTask = adapter.getTaskAt(position);

                        taskViewModel.deleteTask(deletedTask);

                        Snackbar.make(recyclerView, "Task deleted", Snackbar.LENGTH_LONG)
                                .setAction("UNDO", v -> {
                                    taskViewModel.insertTask(deletedTask);

                                    // Если задача была выполнена — возвращаем награды
                                    if (deletedTask.isCompleted()) {
                                        playerViewModel.addXp(deletedTask.getXpReward());
                                        playerViewModel.addGold(deletedTask.getGoldReward());
                                    }
                                }).show();
                    }
                }
        );
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    /**
     * Обновление Player UI в header
     */
    private void updatePlayerUI(Player player) {
        // Level
        TextView playerLevelText = findViewById(R.id.playerLevelText);
        if (playerLevelText != null) {
            playerLevelText.setText("Level " + player.level);
        }

        // XP Progress Bar
        ProgressBar xpProgressBar = findViewById(R.id.xpProgressBar);
        if (xpProgressBar != null) {
            xpProgressBar.setMax((int) player.xpToNextLevel);
            xpProgressBar.setProgress((int) player.currentXp);
        }

        // XP Text
        TextView xpText = findViewById(R.id.xpText);
        if (xpText != null) {
            xpText.setText(player.currentXp + "/" + player.xpToNextLevel);
        }

        // Gold
        TextView goldText = findViewById(R.id.goldText);
        if (goldText != null) {
            goldText.setText("💰 Gold: " + player.gold);
        }

        // Gems
        TextView gemsText = findViewById(R.id.gemsText);
        if (gemsText != null) {
            gemsText.setText("💎 Gems: " + player.gems);
        }

        // HP
        TextView hpText = findViewById(R.id.hpText);
        if (hpText != null) {
            hpText.setText("❤️ HP: " + player.currentHp + "/" + player.maxHp);
        }

        // Mana
        TextView manaText = findViewById(R.id.manaText);
        if (manaText != null) {
            manaText.setText("💙 Mana: " + player.currentMana + "/" + player.maxMana);
        }
    }

    /**
     * Показать Level-Up Dialog
     */
    private void showLevelUpDialog(LevelUpEvent event) {
        Log.d(TAG, "showLevelUpDialog called! Creating dialog for Level " + event.newLevel);

        // ✅ ИСПРАВЛЕНО: используем правильные поля и методы
        int level = event.newLevel;
        int talentPoints = event.talentPoints;
        int hpGained = event.getHpGain();
        int manaGained = event.getManaGain();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_level_up, null);

        TextView tvLevelUp = dialogView.findViewById(R.id.tv_level_up);
        TextView tvLevelNumber = dialogView.findViewById(R.id.tv_level_number);
        TextView tvTalentPoints = dialogView.findViewById(R.id.tv_talent_points);
        TextView tvMaxHp = dialogView.findViewById(R.id.tv_max_hp);
        TextView tvMaxMana = dialogView.findViewById(R.id.tv_max_mana);
        Button btnAwesome = dialogView.findViewById(R.id.btn_awesome);

        tvLevelUp.setText("🎉 LEVEL UP! 🎉");
        tvLevelNumber.setText("You reached Level " + level + "!");
        tvTalentPoints.setText("⭐ +" + talentPoints + " Talent Point" + (talentPoints > 1 ? "s" : ""));
        tvMaxHp.setText("❤️ Max HP +" + hpGained);
        tvMaxMana.setText("💙 Max Mana +" + manaGained);

        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        btnAwesome.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
}
