package com.example.leveluplife;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.OvershootInterpolator;
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

import com.example.leveluplife.data.entity.Achievement;
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
import com.example.leveluplife.workers.ReminderWorker;
import com.google.android.material.bottomnavigation.BottomNavigationView;
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

        SharedPreferences prefs = getSharedPreferences("app_settings", MODE_PRIVATE);
        if (!prefs.getBoolean("onboarding_complete", false)) {
            startActivity(new Intent(this, OnboardingActivity.class));
            finish();
            return;
        }

        requestNotificationPermission();
        scheduleDailyReset();
        scheduleReminderChecker();

        soundManager = SoundManager.getInstance(this);

        taskViewModel = new ViewModelProvider(this).get(TaskViewModel.class);
        playerViewModel = new ViewModelProvider(this).get(PlayerViewModel.class);
        completedTaskViewModel = new ViewModelProvider(this).get(CompletedTaskViewModel.class);

        playerViewModel.initializePlayerIfNeeded();
        tvXpPenalty = findViewById(R.id.tvXpPenalty);
        applyBarColors();

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        adapter = new TaskAdapter();
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        ChipGroup chipGroupFilters = findViewById(R.id.chipGroupFilters);
        chipGroupFilters.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                int checkedId = checkedIds.get(0);
                String filter;
                if (checkedId == R.id.chipAll)        filter = "ALL";
                else if (checkedId == R.id.chipDaily) filter = "DAILY";
                else if (checkedId == R.id.chipTodo)  filter = "TODO";
                else if (checkedId == R.id.chipHabit) filter = "HABIT";
                else                                  filter = "ALL";
                applyFilter(filter);
            }
        });

        currentTasksLiveData = taskViewModel.getAllTasks();
        currentTasksLiveData.observe(this, tasks -> {
            adapter.setTasks(tasks);
            updateEmptyState(tasks);
        });

        playerViewModel.getPlayer().observe(this, player -> {
            if (player != null) updatePlayerUI(player);
        });

        playerViewModel.getLevelUpEvent().observe(this, event -> {
            if (event != null) {
                soundManager.playLevelUp();
                showLevelUpDialog(event);
            }
        });

        playerViewModel.getAchievementUnlockEvent().observe(this, achievement -> {
            if (achievement != null) showAchievementUnlockedDialog(achievement);
        });

        adapter.setOnTaskClickListener((task, position) -> {
            boolean wasCompleted = task.isCompleted();
            task.setCompleted(!wasCompleted);
            task.setLastUpdated(System.currentTimeMillis());

            taskViewModel.updateTask(task);
            adapter.notifyItemChanged(position);

            if (task.isCompleted() && !task.isRewardClaimed()) {
                CompletedTask completedTask = new CompletedTask(
                        task.getId(), task.getTitle(),
                        task.getXpReward(), task.getGoldReward(), task.getFrequency());
                completedTaskViewModel.insert(completedTask);

                Player player = playerViewModel.getPlayer().getValue();
                int penalty = (player != null) ? player.getXpPenalty() : 0;

                int baseXp = task.getXpReward();
                int xpWithClassBonus = applyClassBonus(baseXp, task, player);
                int xpWithBoost = applyXpBoost(xpWithClassBonus);  // 👈 новая строка
                int finalXp = xpWithBoost;

                if (penalty > 0) {
                    finalXp = xpWithBoost * (100 - penalty) / 100;
                    int reduction = xpWithClassBonus - finalXp;
                    Snackbar.make(findViewById(android.R.id.content),
                                    "+" + finalXp + " XP (⚠️ -" + reduction + " penalty), +" +
                                            task.getGoldReward() + " Gold",
                                    Snackbar.LENGTH_LONG)
                            .setBackgroundTint(getResources().getColor(android.R.color.holo_orange_dark))
                            .show();
                } else {
                    Snackbar.make(findViewById(android.R.id.content),
                            "✅ +" + finalXp + " XP, +" + task.getGoldReward() + " Gold",
                            Snackbar.LENGTH_SHORT).show();
                }

                playerViewModel.addXp(finalXp);
                playerViewModel.addGold(task.getGoldReward());
                soundManager.playTaskComplete();

                task.setRewardClaimed(true);
                taskViewModel.updateTask(task);

            } else if (task.isCompleted()) {
                Snackbar.make(findViewById(android.R.id.content),
                        "✅ Task completed", Snackbar.LENGTH_SHORT).show();
            } else {
                Snackbar.make(findViewById(android.R.id.content),
                        "Task unchecked", Snackbar.LENGTH_SHORT).show();
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

        FloatingActionButton fabAddTask = findViewById(R.id.fabAddTask);
        fabAddTask.postDelayed(() ->
                fabAddTask.startAnimation(
                        android.view.animation.AnimationUtils.loadAnimation(this, R.anim.bounce)
                ), 500);

        fabAddTask.setOnClickListener(v -> {
            TaskCreationDialog dialog = new TaskCreationDialog();
            dialog.setOnTaskCreatedListener(task -> taskViewModel.insertTask(task));
            dialog.show(getSupportFragmentManager(), "TaskCreationDialog");
        });

        fabAddTask.setOnLongClickListener(v -> {
            soundManager.playTaskComplete();
            WorkManager.getInstance(this)
                    .enqueue(new OneTimeWorkRequest.Builder(ReminderWorker.class).build());
            Toast.makeText(this, "🔔 ReminderWorker triggered!", Toast.LENGTH_SHORT).show();
            return true;
        });

        findViewById(R.id.btnSettings).setOnClickListener(v ->
                startActivity(new Intent(this, SettingsActivity.class))
        );

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_stats) {
                startActivity(new Intent(this, StatisticsActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            } else if (id == R.id.nav_talents) {
                startActivity(new Intent(this, TalentsActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            } else if (id == R.id.nav_trophies) {
                startActivity(new Intent(this, AchievementsActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            } else if (id == R.id.nav_shop) {
                startActivity(new Intent(this, ShopActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            } else if (id == R.id.nav_hero) {
                startActivity(new Intent(this, HeroActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
            bottomNav.post(() -> bottomNav.getMenu().setGroupCheckable(0, true, false));
            return false;
        });

        findViewById(R.id.btnSettings).setOnClickListener(v -> {
            startActivity(new Intent(this, SettingsActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

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

    private void scheduleDailyReset() {
        long delayToMidnight = calculateDelayToMidnight();
        PeriodicWorkRequest resetWork = new PeriodicWorkRequest.Builder(
                DailyResetWorker.class, 1, TimeUnit.DAYS)
                .setInitialDelay(delayToMidnight, TimeUnit.MILLISECONDS)
                .build();
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "daily_reset", ExistingPeriodicWorkPolicy.KEEP, resetWork);
    }

    private void scheduleReminderChecker() {
        PeriodicWorkRequest reminderWork = new PeriodicWorkRequest.Builder(
                ReminderWorker.class, 15, TimeUnit.MINUTES)
                .build();
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "reminder_checker", ExistingPeriodicWorkPolicy.KEEP, reminderWork);
    }

    private void requestNotificationPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                    != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 1001);
            }
        }
    }

    private long calculateDelayToMidnight() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        return calendar.getTimeInMillis() - System.currentTimeMillis();
    }

    private void updatePlayerUI(Player player) {
        TextView playerLevelText = findViewById(R.id.playerLevelText);
        if (playerLevelText != null) playerLevelText.setText("Level " + player.getLevel());

        ProgressBar xpProgressBar = findViewById(R.id.xpProgressBar);
        if (xpProgressBar != null) {
            int oldProgress = xpProgressBar.getProgress();
            xpProgressBar.setMax((int) player.getXpToNextLevel());
            android.animation.ObjectAnimator animator = android.animation.ObjectAnimator.ofInt(
                    xpProgressBar, "progress", oldProgress, (int) player.getCurrentXp());
            animator.setDuration(800);
            animator.setInterpolator(new android.view.animation.DecelerateInterpolator());
            animator.start();
        }

        TextView xpText = findViewById(R.id.xpText);
        if (xpText != null) xpText.setText(player.getCurrentXp() + "/" + player.getXpToNextLevel());

        updatePenaltyIndicator(player.getXpPenalty());

        TextView goldText = findViewById(R.id.goldText);
        if (goldText != null) goldText.setText("💰 Gold: " + player.getGold());

        TextView gemsText = findViewById(R.id.gemsText);
        if (gemsText != null) gemsText.setText("💎 Gems: " + player.getGems());

        TextView hpText = findViewById(R.id.hpText);
        if (hpText != null) hpText.setText("❤️ HP: " + player.getCurrentHp() + "/" + player.getMaxHp());

        TextView manaText = findViewById(R.id.manaText);
        if (manaText != null) manaText.setText("💙 Mana: " + player.getCurrentMana() + "/" + player.getMaxMana());

        applyBarColors();
    }

    private void applyBarColors() {
        SharedPreferences prefs = getSharedPreferences("app_settings", MODE_PRIVATE);

        int hpColor = prefs.getInt("hp_bar_color", 0xFFFF5252);
        int manaColor = prefs.getInt("mana_bar_color", 0xFF448AFF);
        int xpColor = prefs.getInt("xp_bar_color", 0xFFFFD700);

        ProgressBar hpBar = findViewById(R.id.hpProgressBar);
        ProgressBar manaBar = findViewById(R.id.manaProgressBar);
        ProgressBar xpBar = findViewById(R.id.xpProgressBar);

        if (hpBar != null) {
            hpBar.setProgressTintList(android.content.res.ColorStateList.valueOf(hpColor));
        }

        if (manaBar != null) {
            manaBar.setProgressTintList(android.content.res.ColorStateList.valueOf(manaColor));
        }

        if (xpBar != null) {
            xpBar.setProgressTintList(android.content.res.ColorStateList.valueOf(xpColor));
        }
    }

    private void updatePenaltyIndicator(int penalty) {
        if (tvXpPenalty == null) return;

        if (penalty > 0) {
            if (tvXpPenalty.getVisibility() == View.GONE) {
                tvXpPenalty.setAlpha(0f);
                tvXpPenalty.setVisibility(View.VISIBLE);
                tvXpPenalty.animate().alpha(1f).setDuration(500)
                        .setInterpolator(new android.view.animation.DecelerateInterpolator()).start();
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
            tvXpPenalty.setOnClickListener(v ->
                    new AlertDialog.Builder(this)
                            .setTitle("⚠️ XP Penalty Active")
                            .setMessage(getPenaltyMessage(penalty))
                            .setPositiveButton("Got it!", null)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show()
            );
        } else {
            stopPulsatingAnimation(tvXpPenalty);
            if (tvXpPenalty.getVisibility() == View.VISIBLE) {
                tvXpPenalty.animate().alpha(0f).setDuration(500)
                        .setInterpolator(new android.view.animation.AccelerateInterpolator())
                        .withEndAction(() -> {
                            tvXpPenalty.setVisibility(View.GONE);
                            tvXpPenalty.setAlpha(1f);
                        }).start();
            }
            tvXpPenalty.setOnClickListener(null);
        }
    }

    private String getPenaltyMessage(int penalty) {
        int uncompletedTasks = penalty / 5;
        String severity = penalty >= 30 ? "🔴 SEVERE" : penalty >= 15 ? "🟠 MODERATE" : "🟡 MINOR";
        return "Your XP rewards are reduced by " + penalty + "%!\n\n" +
                "📉 Penalty Level: " + severity + "\n" +
                "❌ Uncompleted daily tasks: " + uncompletedTasks + "\n\n" +
                "💡 How to remove penalty:\n" +
                "Complete ALL daily tasks before midnight to reset penalty to 0%.\n\n" +
                "🎯 Current effect:\n" +
                "• 100 XP task → " + (100 - penalty) + " XP\n" +
                "• 50 XP task → " + (50 * (100 - penalty) / 100) + " XP";
    }

    private void startPulsatingAnimation(TextView view) {
        android.animation.ObjectAnimator pulse = android.animation.ObjectAnimator.ofFloat(
                view, "alpha", 1f, 0.4f, 1f);
        pulse.setDuration(2000);
        pulse.setRepeatCount(android.animation.ObjectAnimator.INFINITE);
        pulse.setInterpolator(new android.view.animation.AccelerateDecelerateInterpolator());
        pulse.start();
        view.setTag(R.id.tvXpPenalty, pulse);
    }

    private void stopPulsatingAnimation(TextView view) {
        Object animator = view.getTag(R.id.tvXpPenalty);
        if (animator instanceof android.animation.ObjectAnimator) {
            ((android.animation.ObjectAnimator) animator).cancel();
            view.setTag(R.id.tvXpPenalty, null);
        }
        view.setAlpha(1f);
    }

    private void showLevelUpDialog(LevelUpEvent event) {
        Log.d(TAG, "showLevelUpDialog called! Level " + event.newLevel);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_level_up, null);

        TextView tvLevelUp = dialogView.findViewById(R.id.tv_level_up);
        TextView tvLevelNumber = dialogView.findViewById(R.id.tv_level_number);
        TextView tvTalentPoints = dialogView.findViewById(R.id.tv_talent_points);
        TextView tvMaxHp = dialogView.findViewById(R.id.tv_max_hp);
        TextView tvMaxMana = dialogView.findViewById(R.id.tv_max_mana);
        android.widget.Button btnAwesome = dialogView.findViewById(R.id.btn_awesome);
        KonfettiView konfettiView = dialogView.findViewById(R.id.konfettiView);

        if (tvLevelUp != null) tvLevelUp.setText("🎉 LEVEL UP! 🎉");
        if (tvLevelNumber != null)  tvLevelNumber.setText("You reached Level " + event.newLevel + "!");
        if (tvTalentPoints != null) tvTalentPoints.setText("⭐ +" + event.talentPoints + " Talent Point" + (event.talentPoints > 1 ? "s" : ""));
        if (tvMaxHp != null) tvMaxHp.setText("❤️ Max HP +" + event.getHpGain());
        if (tvMaxMana != null) tvMaxMana.setText("💙 Max Mana +" + event.getManaGain());

        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.getWindow().getAttributes().windowAnimations = R.style.LevelUpDialogAnimation;
        }

        if (btnAwesome != null) btnAwesome.setOnClickListener(v -> {
            dialog.dismiss();

            Player player = playerViewModel.getPlayer().getValue();
            if (event.newLevel >= 10 && player != null && player.getHeroClass() == null) {
                new Handler(Looper.getMainLooper()).postDelayed(
                        this::showClassSelectionDialog, 300);
            }
        });

        dialog.show();

        if (tvLevelUp != null) {
            tvLevelUp.startAnimation(
                    android.view.animation.AnimationUtils.loadAnimation(this, R.anim.bounce));
        }
        if (konfettiView != null) startConfetti(konfettiView);
    }

    private void startConfetti(KonfettiView konfettiView) {
        List<Integer> colors = Arrays.asList(
                0xFFFFD700, 0xFFBB86FC, 0xFFFF5252, 0xFF448AFF, 0xFFFFEB3B);
        List<Shape> shapes = Arrays.asList(Shape.Square.INSTANCE, Shape.Circle.INSTANCE);

        Party party = new PartyFactory(new Emitter(200L, TimeUnit.MILLISECONDS).max(50))
                .angle(270).spread(60)
                .setSpeedBetween(30f, 60f)
                .timeToLive(3000L)
                .fadeOutEnabled(true)
                .shapes(shapes).colors(colors)
                .position(new Position.Relative(0.5, 0.0))
                .build();

        konfettiView.start(party);
    }

    private void applyFilter(String filter) {
        if (currentTasksLiveData != null) currentTasksLiveData.removeObservers(this);

        if (filter.equals("ALL")) {
            currentTasksLiveData = taskViewModel.getAllTasks();
        } else {
            taskViewModel.setFilter(filter);
            currentTasksLiveData = taskViewModel.getFilteredTasks();
        }
        currentTasksLiveData.observe(this, tasks -> {
            adapter.setTasks(tasks);
            updateEmptyState(tasks);
        });
    }

    private void showAchievementUnlockedDialog(Achievement achievement) {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            View view = LayoutInflater.from(this)
                    .inflate(R.layout.dialog_achievement_unlocked, null);

            ((TextView) view.findViewById(R.id.achievement_icon)).setText(achievement.getIcon());
            ((TextView) view.findViewById(R.id.achievement_title)).setText(achievement.getTitle());
            ((TextView) view.findViewById(R.id.achievement_description)).setText(achievement.getDescription());
            ((TextView) view.findViewById(R.id.achievement_xp)).setText("✨ +" + achievement.getRewardXp() + " XP");
            ((TextView) view.findViewById(R.id.achievement_gold)).setText("💰 +" + achievement.getRewardGold() + " Gold");

            View iconView = view.findViewById(R.id.achievement_icon);
            iconView.setScaleX(0f);
            iconView.setScaleY(0f);
            iconView.animate().scaleX(1f).scaleY(1f).setDuration(400)
                    .setInterpolator(new OvershootInterpolator()).start();

            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setView(view)
                    .setCancelable(false)
                    .create();

            if (dialog.getWindow() != null)
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

            view.findViewById(R.id.btn_awesome).setOnClickListener(v -> dialog.dismiss());
            view.findViewById(R.id.btn_view_all).setOnClickListener(v -> {
                dialog.dismiss();
                startActivity(new Intent(MainActivity.this, AchievementsActivity.class));
            });

            dialog.show();
        }, 1500);
    }

    private void updateEmptyState(List<Task> tasks) {
        View emptyState = findViewById(R.id.emptyState);
        RecyclerView recyclerView = findViewById(R.id.recyclerView);

        if (tasks == null || tasks.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyState.setVisibility(View.VISIBLE);
            emptyState.setAlpha(0f);
            emptyState.animate().alpha(1f).setDuration(400).start();
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyState.setVisibility(View.GONE);
        }
    }

    private void showClassSelectionDialog() {
        Player player = playerViewModel.getPlayer().getValue();
        if (player != null && player.getHeroClass() != null) {
            return;
        }

        View view = LayoutInflater.from(this)
                .inflate(R.layout.dialog_class_selection, null);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(view)
                .setCancelable(false)
                .create();

        if (dialog.getWindow() != null)
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        view.findViewById(R.id.cardWarrior).setOnClickListener(v ->
                onClassSelected("Warrior", dialog));
        view.findViewById(R.id.cardMage).setOnClickListener(v ->
                onClassSelected("Mage", dialog));
        view.findViewById(R.id.cardRanger).setOnClickListener(v ->
                onClassSelected("Ranger", dialog));

        dialog.show();
    }

    private void onClassSelected(String heroClass, AlertDialog dialog) {
        dialog.dismiss();
        playerViewModel.setHeroClass(heroClass);
        soundManager.playLevelUp();

        String emoji = heroClass.equals("Warrior") ? "⚔️" :
                heroClass.equals("Mage")    ? "🧙" : "🏹";

        Snackbar.make(
                findViewById(android.R.id.content),
                emoji + " You are now a " + heroClass + "!",
                Snackbar.LENGTH_LONG
        ).show();
    }

    private int applyClassBonus(int baseXp, Task task, Player player) {
        if (player == null || player.getHeroClass() == null) return baseXp;

        String heroClass = player.getHeroClass();
        int xp = baseXp;

        try {
            Task.AttributeType attr = task.getAttributeType();
            Task.TaskType type = task.getTaskType();

            switch (heroClass) {
                case "Warrior":
                    if (attr == Task.AttributeType.STRENGTH) {
                        xp = (int) Math.round(baseXp * 1.10);
                    }
                    break;

                case "Mage":
                    if (attr == Task.AttributeType.INTELLIGENCE) {
                        xp = (int) Math.round(baseXp * 1.10);
                    }
                    break;

                case "Ranger":
                    if (type == Task.TaskType.DAILY) {
                        xp = (int) Math.round(baseXp * 1.10);
                    }
                    break;
            }
        } catch (Exception e) {
            Log.e(TAG, "applyClassBonus error", e);
        }

        return xp;
    }

    private int applyXpBoost(int xp) {
        SharedPreferences prefs = getSharedPreferences("shop_effects", MODE_PRIVATE);
        boolean boostActive = prefs.getBoolean("xp_boost_active", false);
        if (boostActive) {
            prefs.edit().putBoolean("xp_boost_active", false).apply();
            Snackbar.make(findViewById(android.R.id.content),
                            "⚡ XP Boost activated! x2 XP this task!",
                            Snackbar.LENGTH_SHORT)
                    .setBackgroundTint(0xFF448AFF)
                    .show();
            return xp * 2;
        }
        return xp;
    }

    @Override
    protected void onResume() {
        super.onResume();
        applyBarColors();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (soundManager != null) soundManager.release();
        if (tvXpPenalty != null) stopPulsatingAnimation(tvXpPenalty);
    }
}