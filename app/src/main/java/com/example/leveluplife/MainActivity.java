package com.example.leveluplife;

import android.content.Intent;
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
import com.example.leveluplife.utils.SoundManager;
import com.example.leveluplife.viewModel.PlayerViewModel;
import com.example.leveluplife.viewModel.TaskViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import nl.dionsegijn.konfetti.xml.KonfettiView;
import nl.dionsegijn.konfetti.core.Party;
import nl.dionsegijn.konfetti.core.PartyFactory;
import nl.dionsegijn.konfetti.core.emitter.Emitter;
import nl.dionsegijn.konfetti.core.models.Shape;
import nl.dionsegijn.konfetti.core.Position;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
        adapter.setOnTaskCheckedChangeListener((task, isChecked) -> {
            taskViewModel.toggleTaskCompleted(task.getId(), isChecked);

            if (isChecked) {
                soundManager.playTaskComplete();
            }
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
            playerLevelText.setText("Level " + player.level);
        }

        // XP Progress Bar
        ProgressBar xpProgressBar = findViewById(R.id.xpProgressBar);
        if (xpProgressBar != null) {
            int oldProgress = xpProgressBar.getProgress();
            int newProgress = (int) player.currentXp;

            xpProgressBar.setMax((int) player.xpToNextLevel);

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (soundManager != null) {
            soundManager.release();
        }
    }
}
