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
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.leveluplife.data.entity.CompletedTask;
import com.example.leveluplife.data.entity.Player;
import com.example.leveluplife.data.entity.Task;
import com.example.leveluplife.data.model.LevelUpEvent;
import com.example.leveluplife.ui.dialogs.TaskCreationDialog;
import com.example.leveluplife.ui.dialogs.TaskEditDialog;
import com.example.leveluplife.ui.tasks.TaskAdapter;
import com.example.leveluplife.utils.SoundManager;
import com.example.leveluplife.viewModel.CompletedTaskViewModel;
import com.example.leveluplife.viewModel.PlayerViewModel;
import com.example.leveluplife.viewModel.TaskViewModel;
import com.example.leveluplife.workers.DailyResetWorker;
import com.google.android.material.chip.ChipGroup;
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
    private LiveData<List<Task>> currentTasksLiveData;
    private CompletedTaskViewModel completedTaskViewModel;
    private TextView tvXpPenalty;

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
        completedTaskViewModel = new ViewModelProvider(this).get(CompletedTaskViewModel.class);

        playerViewModel.initializePlayerIfNeeded();
        tvXpPenalty = findViewById(R.id.tvXpPenalty);

        // === RECYCLERVIEW SETUP ===
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        adapter = new TaskAdapter();
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        ChipGroup chipGroupFilters = findViewById(R.id.chipGroupFilters);

        chipGroupFilters.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                int checkedId = checkedIds.get(0);
                String filter;

                if (checkedId == R.id.chipAll) {
                    filter = "ALL";
                } else if (checkedId == R.id.chipDaily) {
                    filter = "DAILY";
                } else if (checkedId == R.id.chipTodo) {
                    filter = "TODO";
                } else if (checkedId == R.id.chipHabit) {
                    filter = "HABIT";
                } else {
                    filter = "ALL";
                }

                applyFilter(filter);
            }
        });

        currentTasksLiveData = taskViewModel.getAllTasks();
        currentTasksLiveData.observe(this, tasks -> {
            adapter.setTasks(tasks);
        });

        playerViewModel.getPlayer().observe(this, player -> {
            if (player != null) {
                updatePlayerUI(player);
            }
        });

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
            task.setLastUpdated(System.currentTimeMillis());

            taskViewModel.updateTask(task);
            adapter.notifyItemChanged(position);

            if (task.isCompleted() && !task.isRewardClaimed()) {
                // ✅ Логируем выполнение в CompletedTasks
                CompletedTask completedTask = new CompletedTask(
                        task.getId(),
                        task.getTitle(),
                        task.getXpReward(),
                        task.getGoldReward(),
                        task.getFrequency()
                );
                completedTaskViewModel.insert(completedTask);

                Player player = playerViewModel.getPlayer().getValue();
                int penalty = (player != null) ? player.getXpPenalty() : 0;

                if (penalty > 0) {
                    int baseXp = task.getXpReward();
                    int actualXp = baseXp * (100 - penalty) / 100;
                    int reduction = baseXp - actualXp;

                    Snackbar.make(findViewById(android.R.id.content),
                                    "+" + actualXp + " XP (⚠️ -" + reduction + " penalty), +" + task.getGoldReward() + " Gold",
                                    Snackbar.LENGTH_LONG)
                            .setBackgroundTint(getResources().getColor(android.R.color.holo_orange_dark))
                            .show();
                } else {
                    Snackbar.make(findViewById(android.R.id.content),
                            "✅ +" + task.getXpReward() + " XP, +" + task.getGoldReward() + " Gold",
                            Snackbar.LENGTH_SHORT).show();
                }

                playerViewModel.addXp(task.getXpReward());
                playerViewModel.addGold(task.getGoldReward());
                soundManager.playTaskComplete();

                task.setRewardClaimed(true);
                taskViewModel.updateTask(task);

            } else if (task.isCompleted()) {
                Snackbar.make(findViewById(android.R.id.content),
                        "✅ Task completed",
                        Snackbar.LENGTH_SHORT).show();
            } else {
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

        // === STATISTICS BUTTON ===
        Button btnStatistics = findViewById(R.id.btn_statistics);
        if (btnStatistics != null) {
            btnStatistics.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, StatisticsActivity.class);
                startActivity(intent);
            });

            btnStatistics.setOnLongClickListener(v -> {
                WorkManager.getInstance(this)
                        .enqueue(new OneTimeWorkRequest.Builder(DailyResetWorker.class).build());

                Toast.makeText(this,
                        "🔧 Daily Reset triggered! Check in 3 seconds",
                        Toast.LENGTH_LONG
                ).show();
                return true;
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

        updatePenaltyIndicator(player.getXpPenalty());

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

    /**
     * ✨ NEW: Update XP Penalty warning indicator with animations
     * @param penalty Current XP penalty percentage (0-50)
     */
    private void updatePenaltyIndicator(int penalty) {
        if (tvXpPenalty == null) return;

        if (penalty > 0) {
            if (tvXpPenalty.getVisibility() == View.GONE) {
                tvXpPenalty.setAlpha(0f);
                tvXpPenalty.setVisibility(View.VISIBLE);
                tvXpPenalty.animate()
                        .alpha(1f)
                        .setDuration(500)
                        .setInterpolator(new android.view.animation.DecelerateInterpolator())
                        .start();
            }

            tvXpPenalty.setText("⚠️ XP Penalty: -" + penalty + "%");

            int color;
            if (penalty >= 30) {
                color = getResources().getColor(android.R.color.holo_red_dark);
                stopPulsatingAnimation(tvXpPenalty);
                startPulsatingAnimation(tvXpPenalty);
            } else if (penalty >= 15) {
                color = getResources().getColor(android.R.color.holo_orange_dark);
                stopPulsatingAnimation(tvXpPenalty);
            } else {
                color = 0xFFFF5252;
                stopPulsatingAnimation(tvXpPenalty);
            }
            tvXpPenalty.setTextColor(color);

            tvXpPenalty.setOnClickListener(v -> {
                new AlertDialog.Builder(this)
                        .setTitle("⚠️ XP Penalty Active")
                        .setMessage(getPenaltyMessage(penalty))
                        .setPositiveButton("Got it!", null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            });

        } else {
            stopPulsatingAnimation(tvXpPenalty);

            if (tvXpPenalty.getVisibility() == View.VISIBLE) {
                tvXpPenalty.animate()
                        .alpha(0f)
                        .setDuration(500)
                        .setInterpolator(new android.view.animation.AccelerateInterpolator())
                        .withEndAction(() -> {
                            tvXpPenalty.setVisibility(View.GONE);
                            tvXpPenalty.setAlpha(1f);
                        })
                        .start();
            }
            tvXpPenalty.setOnClickListener(null);
        }
    }

    /**
     * ✨ NEW: Generate penalty explanation message
     */
    private String getPenaltyMessage(int penalty) {
        int uncompletedTasks = penalty / 5;

        String severity;
        if (penalty >= 30) {
            severity = "🔴 SEVERE";
        } else if (penalty >= 15) {
            severity = "🟠 MODERATE";
        } else {
            severity = "🟡 MINOR";
        }

        return "Your XP rewards are reduced by " + penalty + "%!\n\n" +
                "📉 Penalty Level: " + severity + "\n" +
                "❌ Uncompleted daily tasks: " + uncompletedTasks + "\n\n" +
                "💡 How to remove penalty:\n" +
                "Complete ALL daily tasks before midnight to reset penalty to 0%.\n\n" +
                "🎯 Current effect:\n" +
                "• 100 XP task → " + (100 - penalty) + " XP\n" +
                "• 50 XP task → " + (50 * (100 - penalty) / 100) + " XP";
    }

    /**
     * ✨ NEW: Start pulsating animation for high penalty warnings
     */
    private void startPulsatingAnimation(TextView view) {
        android.animation.ObjectAnimator pulse = android.animation.ObjectAnimator.ofFloat(
                view,
                "alpha",
                1f, 0.4f, 1f
        );
        pulse.setDuration(2000);
        pulse.setRepeatCount(android.animation.ObjectAnimator.INFINITE);
        pulse.setInterpolator(new android.view.animation.AccelerateDecelerateInterpolator());
        pulse.start();

        // Store animator to stop it later
        view.setTag(R.id.tvXpPenalty, pulse);
    }

    /**
     * ✨ NEW: Stop pulsating animation
     */
    private void stopPulsatingAnimation(TextView view) {
        Object animator = view.getTag(R.id.tvXpPenalty);
        if (animator instanceof android.animation.ObjectAnimator) {
            ((android.animation.ObjectAnimator) animator).cancel();
            view.setTag(R.id.tvXpPenalty, null);
        }
        view.setAlpha(1f);
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

    private void applyFilter(String filter) {
        if (currentTasksLiveData != null) {
            currentTasksLiveData.removeObservers(this);
        }

        if (filter.equals("ALL")) {
            currentTasksLiveData = taskViewModel.getAllTasks();
        } else {
            taskViewModel.setFilter(filter);
            currentTasksLiveData = taskViewModel.getFilteredTasks();
        }

        currentTasksLiveData.observe(this, tasks -> {
            adapter.setTasks(tasks);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (soundManager != null) {
            soundManager.release();
        }
        if (tvXpPenalty != null) {
            stopPulsatingAnimation(tvXpPenalty);
        }
    }
}
