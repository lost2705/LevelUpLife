package com.example.leveluplife;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.leveluplife.data.entity.DungeonState;
import com.example.leveluplife.data.entity.Player;
import com.example.leveluplife.viewModel.DungeonViewModel;
import com.example.leveluplife.viewModel.PlayerViewModel;
import com.google.android.material.snackbar.Snackbar;

public class DungeonActivity extends AppCompatActivity {

    private DungeonViewModel dungeonViewModel;
    private PlayerViewModel playerViewModel;

    private TextView tvDungeonStatus;
    private TextView tvCooldown;
    private TextView tvEnemyName;
    private TextView tvEnemyHp;
    private TextView tvPlayerHp;
    private TextView tvPlayerMana;
    private TextView tvTurn;
    private TextView tvReward;
    private TextView tvBattleLog;

    private ProgressBar progressEnemyHp;
    private ProgressBar progressPlayerHp;
    private ProgressBar progressPlayerMana;

    private Button btnStartRun;
    private Button btnAttack;
    private Button btnSkill;
    private Button btnRest;
    private Button btnLeave;

    private View layoutBattleResult;
    private TextView tvBattleResultTitle;
    private TextView tvBattleResultSubtitle;
    private Button btnCloseResult;

    private String lastBattleLog = "";
    private boolean battleFinishedShown = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dungeon);

        dungeonViewModel = new ViewModelProvider(this).get(DungeonViewModel.class);
        playerViewModel = new ViewModelProvider(this).get(PlayerViewModel.class);
        playerViewModel.initializePlayerIfNeeded();

        bindViews();
        setupListeners();
        observeViewModel();
    }

    @Override
    protected void onResume() {
        super.onResume();
        dungeonViewModel.resetCooldownIfExpired();
    }

    private void bindViews() {
        tvDungeonStatus = findViewById(R.id.tvDungeonStatus);
        tvCooldown = findViewById(R.id.tvCooldown);
        tvEnemyName = findViewById(R.id.tvEnemyName);
        tvEnemyHp = findViewById(R.id.tvEnemyHp);
        tvPlayerHp = findViewById(R.id.tvPlayerHp);
        tvPlayerMana = findViewById(R.id.tvPlayerMana);
        tvTurn = findViewById(R.id.tvTurn);
        tvReward = findViewById(R.id.tvReward);
        tvBattleLog = findViewById(R.id.tvBattleLog);

        progressEnemyHp = findViewById(R.id.progressEnemyHp);
        progressPlayerHp = findViewById(R.id.progressPlayerHp);
        progressPlayerMana = findViewById(R.id.progressPlayerMana);

        btnStartRun = findViewById(R.id.btnStartRun);
        btnAttack = findViewById(R.id.btnAttack);
        btnSkill = findViewById(R.id.btnSkill);
        btnRest = findViewById(R.id.btnRest);
        btnLeave = findViewById(R.id.btnLeave);

        layoutBattleResult = findViewById(R.id.layoutBattleResult);
        tvBattleResultTitle = findViewById(R.id.tvBattleResultTitle);
        tvBattleResultSubtitle = findViewById(R.id.tvBattleResultSubtitle);
        btnCloseResult = findViewById(R.id.btnCloseResult);

        tvBattleLog.setMovementMethod(new ScrollingMovementMethod());
        tvBattleLog.setVerticalScrollBarEnabled(true);
    }

    private void setupListeners() {
        btnStartRun.setOnClickListener(v -> {
            lastBattleLog = "";
            battleFinishedShown = false;
            hideBattleResult();
            dungeonViewModel.startDungeonRun();
        });

        btnAttack.setOnClickListener(v -> dungeonViewModel.playerAttack());
        btnSkill.setOnClickListener(v -> dungeonViewModel.playerSkill());
        btnRest.setOnClickListener(v -> dungeonViewModel.playerRest());
        btnLeave.setOnClickListener(v -> dungeonViewModel.abandonRun());
        btnCloseResult.setOnClickListener(v -> finish());
    }

    private void observeViewModel() {
        dungeonViewModel.getDungeonState().observe(this, this::renderDungeonState);
        playerViewModel.getPlayer().observe(this, this::renderPlayerState);

        dungeonViewModel.getBattleLog().observe(this, log -> {
            if (log == null) return;

            tvBattleLog.setText(log);

            tvBattleLog.post(() -> {
                if (tvBattleLog.getLayout() == null) return;

                int scrollAmount = tvBattleLog.getLayout().getLineTop(tvBattleLog.getLineCount()) - tvBattleLog.getHeight();
                tvBattleLog.scrollTo(0, Math.max(scrollAmount, 0));
            });

            String newPart = log;
            if (!lastBattleLog.isEmpty() && log.startsWith(lastBattleLog)) {
                newPart = log.substring(lastBattleLog.length()).trim();
            }
            lastBattleLog = log;

            if (newPart.contains("Reward:")) {
                Snackbar.make(findViewById(android.R.id.content),
                        "🏆 Victory! Rewards received.",
                        Snackbar.LENGTH_LONG).show();
            } else if (newPart.contains("defeated")) {
                Snackbar.make(findViewById(android.R.id.content),
                        "☠️ You were defeated.",
                        Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void renderDungeonState(DungeonState state) {
        if (state == null) {
            tvDungeonStatus.setText("Dungeon status: unavailable");
            tvCooldown.setText("Cooldown: —");
            tvEnemyName.setText("Enemy: —");
            tvEnemyHp.setText("Enemy HP: —");
            tvPlayerHp.setText("Your HP: —");
            tvPlayerMana.setText("Your Mana: —");
            tvReward.setText("Reward: —");
            tvTurn.setVisibility(View.GONE);

            setProgressSafely(progressEnemyHp, 100, 0);
            setProgressSafely(progressPlayerHp, 100, 0);
            setProgressSafely(progressPlayerMana, 100, 0);

            updateButtonsForUnavailableState();
            return;
        }

        String status = safeText(state.getStatus(), "—");
        tvDungeonStatus.setText("Status: " + status);

        if ("IN_PROGRESS".equals(status)) {
            tvTurn.setVisibility(View.VISIBLE);
            tvTurn.setText("Turn: " + Math.max(1, state.getTurnNumber()));
        } else {
            tvTurn.setVisibility(View.GONE);
        }

        if (state.getRewardXp() > 0 || state.getRewardGold() > 0) {
            tvReward.setText("Reward: +" + state.getRewardXp() + " XP, +" + state.getRewardGold() + " Gold");
        } else {
            tvReward.setText("Reward: —");
        }

        Player player = playerViewModel.getPlayer().getValue();

        if ("IN_PROGRESS".equals(status)) {
            int maxHp = player != null ? player.getMaxHp() : Math.max(state.getPlayerCurrentHp(), 1);
            int maxMana = player != null ? player.getMaxMana() : Math.max(state.getPlayerCurrentMana(), 1);

            tvPlayerHp.setText("Your HP: " + state.getPlayerCurrentHp() + "/" + maxHp);
            tvPlayerMana.setText("Your Mana: " + state.getPlayerCurrentMana() + "/" + maxMana);
            tvEnemyName.setText("Enemy: " + safeText(state.getEnemyName(), "—"));
            tvEnemyHp.setText("Enemy HP: " + state.getEnemyCurrentHp() + "/" + state.getEnemyMaxHp());

            animateProgress(progressPlayerHp, Math.max(1, maxHp), Math.max(0, state.getPlayerCurrentHp()));
            animateProgress(progressPlayerMana, Math.max(1, maxMana), Math.max(0, state.getPlayerCurrentMana()));
            animateProgress(progressEnemyHp, Math.max(1, state.getEnemyMaxHp()), Math.max(0, state.getEnemyCurrentHp()));
        } else {
            if (player != null) {
                tvPlayerHp.setText("Your HP: " + player.getCurrentHp() + "/" + player.getMaxHp());
                tvPlayerMana.setText("Your Mana: " + player.getCurrentMana() + "/" + player.getMaxMana());
                animateProgress(progressPlayerHp, Math.max(1, player.getMaxHp()), Math.max(0, player.getCurrentHp()));
                animateProgress(progressPlayerMana, Math.max(1, player.getMaxMana()), Math.max(0, player.getCurrentMana()));
            } else {
                tvPlayerHp.setText("Your HP: —");
                tvPlayerMana.setText("Your Mana: —");
                setProgressSafely(progressPlayerHp, 100, 0);
                setProgressSafely(progressPlayerMana, 100, 0);
            }

            tvEnemyName.setText("Enemy: —");
            tvEnemyHp.setText("Enemy HP: —");
            setProgressSafely(progressEnemyHp, 100, 0);
        }

        renderCooldown(state);
        updateButtons(state);
        handleBattleFinishedState(state);
    }

    private void renderCooldown(DungeonState state) {
        long cooldownUntil = state.getCooldownUntil();
        long now = System.currentTimeMillis();

        if ("COOLDOWN".equals(state.getStatus()) && cooldownUntil > now) {
            long remainingMs = cooldownUntil - now;
            long totalMinutes = remainingMs / 60000;
            long hours = totalMinutes / 60;
            long minutes = totalMinutes % 60;
            tvCooldown.setText("Cooldown: " + hours + "h " + minutes + "m");
        } else {
            tvCooldown.setText("Cooldown: ready");
        }
    }

    private void updateButtons(DungeonState state) {
        String status = state.getStatus();
        boolean inProgress = "IN_PROGRESS".equals(status);
        boolean idle = "IDLE".equals(status);
        boolean cooldownExpired = "COOLDOWN".equals(status)
                && state.getCooldownUntil() <= System.currentTimeMillis();

        boolean finished = "VICTORY".equals(status) || "DEFEAT".equals(status) || "COMPLETED".equals(status);

        btnStartRun.setEnabled((idle || cooldownExpired) && !finished);
        btnAttack.setEnabled(inProgress);
        btnSkill.setEnabled(inProgress);
        btnRest.setEnabled(inProgress);
        btnLeave.setEnabled(inProgress);
    }

    private void updateButtonsForUnavailableState() {
        btnStartRun.setEnabled(false);
        btnAttack.setEnabled(false);
        btnSkill.setEnabled(false);
        btnRest.setEnabled(false);
        btnLeave.setEnabled(false);
    }

    private void renderPlayerState(Player player) {
        if (player == null) return;

        DungeonState state = dungeonViewModel.getDungeonState().getValue();
        boolean inProgress = state != null && "IN_PROGRESS".equals(state.getStatus());

        if (!inProgress) {
            tvPlayerHp.setText("Your HP: " + player.getCurrentHp() + "/" + player.getMaxHp());
            tvPlayerMana.setText("Your Mana: " + player.getCurrentMana() + "/" + player.getMaxMana());

            animateProgress(progressPlayerHp, Math.max(1, player.getMaxHp()), Math.max(0, player.getCurrentHp()));
            animateProgress(progressPlayerMana, Math.max(1, player.getMaxMana()), Math.max(0, player.getCurrentMana()));
        }
    }

    private void handleBattleFinishedState(DungeonState state) {
        String status = state.getStatus();
        boolean finished = "VICTORY".equals(status) || "DEFEAT".equals(status) || "COMPLETED".equals(status);

        if (!finished) {
            battleFinishedShown = false;
            hideBattleResult();
            return;
        }

        if (battleFinishedShown) return;
        battleFinishedShown = true;

        boolean victory = "VICTORY".equals(status) || "COMPLETED".equals(status);
        showBattleResult(victory, state);
    }

    private void showBattleResult(boolean victory, DungeonState state) {
        layoutBattleResult.setVisibility(View.VISIBLE);
        layoutBattleResult.setAlpha(0f);
        layoutBattleResult.setScaleX(0.92f);
        layoutBattleResult.setScaleY(0.92f);

        tvBattleResultTitle.setText(victory ? "Victory!" : "Defeat");
        tvBattleResultTitle.setTextColor(victory ? 0xFF81C784 : 0xFFE57373);
        btnCloseResult.setText(victory ? "Collect and Exit" : "Leave Dungeon");

        if (victory) {
            tvBattleResultSubtitle.setText("Rewards: +" + state.getRewardXp() + " XP, +" + state.getRewardGold() + " Gold");
        } else {
            tvBattleResultSubtitle.setText("The run has ended. Recover and try again.");
        }

        layoutBattleResult.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(260)
                .setInterpolator(new OvershootInterpolator(0.85f))
                .start();

        btnAttack.setEnabled(false);
        btnSkill.setEnabled(false);
        btnRest.setEnabled(false);
        btnLeave.setEnabled(false);
        btnStartRun.setEnabled(false);
    }

    private void hideBattleResult() {
        layoutBattleResult.setVisibility(View.GONE);
        layoutBattleResult.setAlpha(0f);
    }

    private void animateProgress(ProgressBar progressBar, int max, int targetProgress) {
        int safeMax = Math.max(1, max);
        int safeProgress = Math.max(0, Math.min(targetProgress, safeMax));
        if (progressBar.getMax() != safeMax) {
            progressBar.setMax(safeMax);
        }
        ObjectAnimator animator = ObjectAnimator.ofInt(progressBar, "progress", progressBar.getProgress(), safeProgress);
        animator.setDuration(250);
        animator.start();
    }

    private void setProgressSafely(ProgressBar progressBar, int max, int progress) {
        progressBar.setMax(Math.max(1, max));
        progressBar.setProgress(Math.max(0, Math.min(progress, progressBar.getMax())));
    }

    private String safeText(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value;
    }
}