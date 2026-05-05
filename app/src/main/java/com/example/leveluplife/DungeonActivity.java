package com.example.leveluplife;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.leveluplife.data.entity.DungeonState;
import com.example.leveluplife.data.entity.Player;
import com.example.leveluplife.viewModel.DungeonViewModel;
import com.example.leveluplife.viewModel.PlayerViewModel;

public class DungeonActivity extends AppCompatActivity {

    private DungeonViewModel dungeonViewModel;
    private PlayerViewModel playerViewModel;

    private TextView tvDungeonStatus;
    private TextView tvCooldown;
    private TextView tvTurn;
    private TextView tvEnemyName;
    private TextView tvEnemyHp;
    private TextView tvPlayerHp;
    private TextView tvPlayerMana;
    private TextView tvReward;
    private TextView tvBattleLog;
    private TextView tvPotions;

    private ProgressBar progressEnemyHp;
    private ProgressBar progressPlayerHp;
    private ProgressBar progressPlayerMana;

    private Button btnStartRun;
    private Button btnAttack;
    private Button btnSkill;
    private Button btnUsePotion;
    private Button btnRest;
    private Button btnLeave;
    private Button btnCloseResult;

    private View layoutBattleResult;
    private TextView tvBattleResultTitle;
    private TextView tvBattleResultSubtitle;

    private int currentMaxHp = 100;
    private int currentMaxMana = 50;
    private DungeonState latestDungeonState;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dungeon);

        dungeonViewModel = new ViewModelProvider(this).get(DungeonViewModel.class);
        playerViewModel = new ViewModelProvider(this).get(PlayerViewModel.class);

        bindViews();
        setupListeners();
        setupObservers();

        tvBattleLog.setMovementMethod(new ScrollingMovementMethod());
        updatePotionInfo();
    }

    private void bindViews() {
        tvDungeonStatus = findViewById(R.id.tvDungeonStatus);
        tvCooldown = findViewById(R.id.tvCooldown);
        tvTurn = findViewById(R.id.tvTurn);
        tvEnemyName = findViewById(R.id.tvEnemyName);
        tvEnemyHp = findViewById(R.id.tvEnemyHp);
        tvPlayerHp = findViewById(R.id.tvPlayerHp);
        tvPlayerMana = findViewById(R.id.tvPlayerMana);
        tvReward = findViewById(R.id.tvReward);
        tvBattleLog = findViewById(R.id.tvBattleLog);
        tvPotions = findViewById(R.id.tvPotions);

        progressEnemyHp = findViewById(R.id.progressEnemyHp);
        progressPlayerHp = findViewById(R.id.progressPlayerHp);
        progressPlayerMana = findViewById(R.id.progressPlayerMana);

        btnStartRun = findViewById(R.id.btnStartRun);
        btnAttack = findViewById(R.id.btnAttack);
        btnSkill = findViewById(R.id.btnSkill);
        btnUsePotion = findViewById(R.id.btnUsePotion);
        btnRest = findViewById(R.id.btnRest);
        btnLeave = findViewById(R.id.btnLeave);
        btnCloseResult = findViewById(R.id.btnCloseResult);

        layoutBattleResult = findViewById(R.id.layoutBattleResult);
        tvBattleResultTitle = findViewById(R.id.tvBattleResultTitle);
        tvBattleResultSubtitle = findViewById(R.id.tvBattleResultSubtitle);
    }

    private void setupListeners() {
        btnStartRun.setOnClickListener(v -> dungeonViewModel.startDungeonRun());
        btnAttack.setOnClickListener(v -> dungeonViewModel.playerAttack());
        btnSkill.setOnClickListener(v -> dungeonViewModel.playerSkill());
        btnUsePotion.setOnClickListener(v -> showPotionChooser());
        btnRest.setOnClickListener(v -> dungeonViewModel.playerRest());
        btnLeave.setOnClickListener(v -> dungeonViewModel.abandonRun());

        btnCloseResult.setOnClickListener(v -> {
            layoutBattleResult.setVisibility(View.GONE);
            layoutBattleResult.setAlpha(0f);
        });
    }

    private void setupObservers() {
        playerViewModel.getPlayer().observe(this, player -> {
            updatePlayerCaps(player);

            if (latestDungeonState != null) {
                updateDungeonUi(latestDungeonState);
            }
        });

        dungeonViewModel.getDungeonState().observe(this, state -> {
            latestDungeonState = state;
            updateDungeonUi(state);
            updatePotionInfo();
        });

        dungeonViewModel.getBattleLog().observe(this, log -> {
            tvBattleLog.setText(log == null || log.trim().isEmpty()
                    ? "Battle log will appear here..."
                    : log);

            tvBattleLog.post(() -> {
                if (tvBattleLog.getLayout() == null) return;

                int scrollAmount = tvBattleLog.getLayout()
                        .getLineTop(tvBattleLog.getLineCount()) - tvBattleLog.getHeight();

                tvBattleLog.scrollTo(0, Math.max(scrollAmount, 0));
            });
        });
    }

    private void updatePlayerCaps(Player player) {
        if (player == null) return;

        currentMaxHp = Math.max(1, player.getMaxHp());
        currentMaxMana = Math.max(1, player.getMaxMana());

        progressPlayerHp.setMax(currentMaxHp);
        progressPlayerMana.setMax(currentMaxMana);

        progressPlayerHp.invalidate();
        progressPlayerMana.invalidate();
    }

    private void updateDungeonUi(DungeonState state) {
        if (state == null) return;

        String status = state.getStatus() == null ? "IDLE" : state.getStatus();
        tvDungeonStatus.setText("Status: " + status);

        long now = System.currentTimeMillis();
        if (state.getCooldownUntil() > now) {
            long remainingMs = state.getCooldownUntil() - now;
            long totalMinutes = remainingMs / 60000;
            long hours = totalMinutes / 60;
            long minutes = totalMinutes % 60;
            tvCooldown.setText("Cooldown: " + hours + "h " + minutes + "m");
        } else {
            tvCooldown.setText("Cooldown: ready");
        }

        boolean inProgress = "IN_PROGRESS".equals(status);
        tvTurn.setVisibility(inProgress ? View.VISIBLE : View.GONE);
        tvTurn.setText("Turn: " + Math.max(1, state.getTurnNumber()));

        tvEnemyName.setText("Enemy: " + safeText(state.getEnemyName(), "—"));
        tvEnemyHp.setText("Enemy HP: " + Math.max(0, state.getEnemyCurrentHp()) + "/" + Math.max(0, state.getEnemyMaxHp()));

        int enemyMax = Math.max(1, state.getEnemyMaxHp());
        int enemyCurrent = Math.min(Math.max(0, state.getEnemyCurrentHp()), enemyMax);
        progressEnemyHp.setMax(enemyMax);
        progressEnemyHp.setProgress(enemyCurrent);

        int playerHp = Math.min(Math.max(0, state.getPlayerCurrentHp()), currentMaxHp);
        int playerMana = Math.min(Math.max(0, state.getPlayerCurrentMana()), currentMaxMana);

        progressPlayerHp.setMax(currentMaxHp);
        progressPlayerMana.setMax(currentMaxMana);

        progressPlayerHp.setProgress(playerHp);
        progressPlayerMana.setProgress(playerMana);

        tvPlayerHp.setText("Your HP: " + playerHp + "/" + currentMaxHp);
        tvPlayerMana.setText("Your Mana: " + playerMana + "/" + currentMaxMana);

        tvReward.setText("Reward: +" + Math.max(0, state.getRewardXp()) + " XP, +" + Math.max(0, state.getRewardGold()) + " Gold");

        updateButtons(status);
        updateBattleResultOverlay(state);

        progressPlayerHp.invalidate();
        progressPlayerMana.invalidate();
        progressEnemyHp.invalidate();
    }

    private void updateButtons(String status) {
        boolean inProgress = "IN_PROGRESS".equals(status);
        boolean finished = "VICTORY".equals(status) || "DEFEAT".equals(status);

        btnStartRun.setEnabled(!inProgress);
        btnAttack.setEnabled(inProgress);
        btnSkill.setEnabled(inProgress);
        btnUsePotion.setEnabled(inProgress);
        btnRest.setEnabled(inProgress);
        btnLeave.setEnabled(inProgress);

        if (!finished) {
            layoutBattleResult.setVisibility(View.GONE);
            layoutBattleResult.setAlpha(0f);
        }
    }

    private void updateBattleResultOverlay(DungeonState state) {
        String status = state.getStatus();

        if ("VICTORY".equals(status)) {
            layoutBattleResult.setVisibility(View.VISIBLE);
            layoutBattleResult.setAlpha(1f);
            tvBattleResultTitle.setText("Victory!");
            tvBattleResultSubtitle.setText("You cleared the dungeon.");
        } else if ("DEFEAT".equals(status)) {
            layoutBattleResult.setVisibility(View.VISIBLE);
            layoutBattleResult.setAlpha(1f);
            tvBattleResultTitle.setText("Defeat");
            tvBattleResultSubtitle.setText("The dungeon cast you out.");
        }
    }

    private void updatePotionInfo() {
        SharedPreferences prefs = getSharedPreferences("dungeon_potions", MODE_PRIVATE);
        int hpPotions = prefs.getInt("hp_potions", 0);
        int manaPotions = prefs.getInt("mana_potions", 0);

        tvPotions.setText("Potions: HP " + hpPotions + " | Mana " + manaPotions);
    }

    private void showPotionChooser() {
        SharedPreferences prefs = getSharedPreferences("dungeon_potions", MODE_PRIVATE);
        int hpPotions = prefs.getInt("hp_potions", 0);
        int manaPotions = prefs.getInt("mana_potions", 0);

        String[] options = new String[] {
                "HP Potion (" + hpPotions + ")",
                "Mana Potion (" + manaPotions + ")"
        };

        new AlertDialog.Builder(this)
                .setTitle("Choose potion")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        dungeonViewModel.useHpPotionInDungeon();
                    } else if (which == 1) {
                        dungeonViewModel.useManaPotionInDungeon();
                    }
                    updatePotionInfo();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private String safeText(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value;
    }
}