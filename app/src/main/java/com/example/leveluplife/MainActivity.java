package com.example.leveluplife;

import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.leveluplife.data.entity.Player;
import com.example.leveluplife.data.entity.Task;
import com.example.leveluplife.ui.dialogs.TaskCreationDialog;
import com.example.leveluplife.ui.tasks.TaskAdapter;
import com.example.leveluplife.viewModel.PlayerViewModel;
import com.example.leveluplife.viewModel.TaskViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

public class MainActivity extends AppCompatActivity {

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

        playerViewModel.initializePlayerIfNeeded();

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
}
