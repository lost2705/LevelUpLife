package com.example.leveluplife;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.leveluplife.adapter.TaskAdapter;
import com.example.leveluplife.data.entity.Player;
import com.example.leveluplife.data.entity.Task;
import com.example.leveluplife.data.model.LevelUpEvent;
import com.example.leveluplife.ui.dialogs.TaskCreationDialog;
import com.example.leveluplife.ui.dialogs.TaskEditDialog;
import com.example.leveluplife.utils.SoundManager;
import com.example.leveluplife.viewModel.PlayerViewModel;
import com.example.leveluplife.viewModel.TaskViewModel;
import com.example.leveluplife.workers.DailyResetWorker;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import nl.dionsegijn.konfetti.core.Party;
import nl.dionsegijn.konfetti.core.PartyFactory;
import nl.dionsegijn.konfetti.core.Position;
import nl.dionsegijn.konfetti.core.emitter.Emitter;
import nl.dionsegijn.konfetti.core.models.Shape;
import nl.dionsegijn.konfetti.xml.KonfettiView;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private TaskViewModel taskViewModel;
    private PlayerViewModel playerViewModel;
    private TaskAdapter adapter;
    private SoundManager soundManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        scheduleDailyReset();

        // === SOUND MANAGER INITIALIZATION ===
        soundManager = SoundManager.getInstance(this);

        // === VIEWMODEL INITIALIZATION ===
        taskViewModel = new ViewModelProvider(this).get(TaskViewModel.class);
        playerViewModel = new ViewModelProvider(this).get(PlayerViewModel.class);

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

        // Player observer
        playerViewModel.getPlayer().observe(this, player -> {
            if (player != null) {
                updatePlayerUI(player);
            }
        });

        // Level-Up Observer
        playerViewModel.getLevelUpEvent().observe(this, event -> {
            if (event != null) {
                soundManager.playLevelUp();
                showLevelUpDialog(event);
            }
        });

        // === TASK COMPLETION TOGGLE ===
        adapter.setOnTaskClickListener((task, position) -> {
            boolean wasCompleted = task.isCompleted();
            task.setCompleted(!wasCompleted);

            if (task.isCompleted()) {
                if (!task.isRewardClaimed()) {
                    playerViewModel.addXp(task.getXpReward());
                    playerViewModel.addGold(task.getGoldReward());
                    soundManager.playTaskComplete();

                    task.setRewardClaimed(true);

                    Snackbar.make(findViewById(android.R.id.content),
                            "✅ +" + task.getXpReward() + " XP, +" + task.getGoldReward() + " Gold",
                            Snackbar.LENGTH_SHORT).show();
                } else {
                    Snackbar.make(findViewById(android.R.id.content),
                            "✅ Task completed (reward already claimed)",
                            Snackbar.LENGTH_SHORT).show();
                }

                taskViewModel.updateTask(task);

            } else {
                taskViewModel.updateTask(task);

                Snackbar.make(findViewById(android.R.id.content),
                        "Task unchecked",
                        Snackbar.LENGTH_SHORT).show();
            }
        });


        adapter.setOnTaskLongClickListener(task -> {
            TaskEditDialog editDialog = TaskEditDialog.newInstance(task);
            editDialog.setOnTaskEditedListener(updatedTask -> {
                taskViewModel.updateTask(updatedTask);
                Toast.makeText(MainActivity.this, "Task updated!", Toast.LENGTH_SHORT).show();
            });
            editDialog.show(getSupportFragmentManager(), "TaskEditDialog");
        });

        // === FAB (CREATE TASK) ===
        FloatingActionButton fabAddTask = findViewById(R.id.fabAddTask);

        fabAddTask.postDelayed(() -> {
            fabAddTask.startAnimation(android.view.animation.AnimationUtils.loadAnimation(
                    this, R.anim.bounce
            ));
        }, 500);

        fabAddTask.setOnClickListener(v -> {
            TaskCreationDialog dialog = new TaskCreationDialog();
            dialog.setOnTaskCreatedListener(task -> {
                taskViewModel.insertTask(task);
            });
            dialog.show(getSupportFragmentManager(), "TaskCreationDialog");
        });

        // === TALENTS BUTTON ===
        Button btnTalents = findViewById(R.id.btn_talents);
        if (btnTalents != null) {
            btnTalents.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, TalentsActivity.class);
                startActivity(intent);
            });
        }

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

                        soundManager.playSwipeDelete();

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
            playerLevelText.setText("Level " + player.getLevel());
        }

        // XP Progress Bar
        ProgressBar xpProgressBar = findViewById(R.id.xpProgressBar);
        if (xpProgressBar != null) {
            int oldProgress = xpProgressBar.getProgress();
            int newProgress = (int) player.getCurrentXp();

            xpProgressBar.setMax((int) player.getXpToNextLevel());

            android.animation.ObjectAnimator animator = android.animation.ObjectAnimator.ofInt(
                    xpProgressBar,
                    "progress",
                    oldProgress,
                    newProgress
            );
            animator.setDuration(800);
            animator.setInterpolator(new android.view.animation.DecelerateInterpolator());
            animator.start();
        }

        // XP Text
        TextView xpText = findViewById(R.id.xpText);
        if (xpText != null) {
            xpText.setText(player.getCurrentXp() + "/" + player.getXpToNextLevel());
        }

        // Gold
        TextView goldText = findViewById(R.id.goldText);
        if (goldText != null) {
            goldText.setText("💰 Gold: " + player.getGold());
        }

        // Gems
        TextView gemsText = findViewById(R.id.gemsText);
        if (gemsText != null) {
            gemsText.setText("💎 Gems: " + player.getGems());
        }

        // HP
        TextView hpText = findViewById(R.id.hpText);
        if (hpText != null) {
            hpText.setText("❤️ HP: " + player.getCurrentHp() + "/" + player.getMaxHp());
        }

        // Mana
        TextView manaText = findViewById(R.id.manaText);
        if (manaText != null) {
            manaText.setText("💙 Mana: " + player.getCurrentMana() + "/" + player.getMaxMana());
        }
    }

    private void showLevelUpDialog(LevelUpEvent event) {
        Log.d(TAG, "showLevelUpDialog called! Creating dialog for Level " + event.newLevel);

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
        KonfettiView konfettiView = dialogView.findViewById(R.id.konfettiView);

        if (tvLevelUp != null) {
            tvLevelUp.setText("🎉 LEVEL UP! 🎉");
        }
        if (tvLevelNumber != null) {
            tvLevelNumber.setText("You reached Level " + level + "!");
        }
        if (tvTalentPoints != null) {
            tvTalentPoints.setText("⭐ +" + talentPoints + " Talent Point" + (talentPoints > 1 ? "s" : ""));
        }
        if (tvMaxHp != null) {
            tvMaxHp.setText("❤️ Max HP +" + hpGained);
        }
        if (tvMaxMana != null) {
            tvMaxMana.setText("💙 Max Mana +" + manaGained);
        }

        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.getWindow().getAttributes().windowAnimations = R.style.LevelUpDialogAnimation;
        }

        if (btnAwesome != null) {
            btnAwesome.setOnClickListener(v -> dialog.dismiss());
        }

        dialog.show();

        if (tvLevelUp != null) {
            tvLevelUp.startAnimation(android.view.animation.AnimationUtils.loadAnimation(
                    this, R.anim.bounce
            ));
        }

        if (konfettiView != null) {
            startConfetti(konfettiView);
        }
    }

    private void startConfetti(KonfettiView konfettiView) {
        List<Integer> colors = Arrays.asList(
                0xFFFFD700,
                0xFFBB86FC,
                0xFFFF5252,
                0xFF448AFF,
                0xFFFFEB3B
        );

        List<Shape> shapes = Arrays.asList(
                Shape.Square.INSTANCE,
                Shape.Circle.INSTANCE
        );

        Party partyLeft = new PartyFactory(
                new Emitter(200L, TimeUnit.MILLISECONDS).max(50)
        )
                .angle(315)
                .spread(45)
                .setSpeedBetween(30f, 60f)
                .timeToLive(3000L)
                .fadeOutEnabled(true)
                .shapes(shapes)
                .colors(colors)
                .position(new Position.Relative(0.5, 0.5))
                .build();

        Party partyRight = new PartyFactory(
                new Emitter(200L, TimeUnit.MILLISECONDS).max(50)
        )
                .angle(225)
                .spread(45)
                .setSpeedBetween(30f, 60f)
                .timeToLive(3000L)
                .fadeOutEnabled(true)
                .shapes(shapes)
                .colors(colors)
                .position(new Position.Relative(0.5, 0.5))
                .build();

        Party partyCenter = new PartyFactory(
                new Emitter(150L, TimeUnit.MILLISECONDS).max(40)
        )
                .angle(270)
                .spread(30)
                .setSpeedBetween(40f, 70f)
                .timeToLive(3000L)
                .fadeOutEnabled(true)
                .shapes(shapes)
                .colors(colors)
                .position(new Position.Relative(0.5, 0.5))
                .build();

        konfettiView.start(partyLeft, partyRight, partyCenter);

        new android.os.Handler().postDelayed(() -> {
            konfettiView.start(partyLeft, partyRight, partyCenter);
        }, 100);
    }

    private void scheduleDailyReset() {
        long delayToMidnight = calculateDelayToMidnight();

        PeriodicWorkRequest resetWork = new PeriodicWorkRequest.Builder(
                DailyResetWorker.class,
                1, TimeUnit.DAYS
        )
                .setInitialDelay(delayToMidnight, TimeUnit.MILLISECONDS)
                .build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "daily_reset",
                ExistingPeriodicWorkPolicy.KEEP,
                resetWork
        );

        Log.d(TAG, "Daily reset scheduled. Next run in " + (delayToMidnight / 1000 / 60) + " minutes");
    }

    private long calculateDelayToMidnight() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.add(Calendar.DAY_OF_MONTH, 1);

        long midnight = calendar.getTimeInMillis();
        long now = System.currentTimeMillis();

        return midnight - now;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (soundManager != null) {
            soundManager.release();
        }
    }
}
