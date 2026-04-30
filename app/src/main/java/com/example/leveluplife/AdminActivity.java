package com.example.leveluplife;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.leveluplife.data.entity.Player;
import com.example.leveluplife.viewModel.DungeonViewModel;
import com.example.leveluplife.viewModel.PlayerViewModel;

public class AdminActivity extends AppCompatActivity {

    private PlayerViewModel playerViewModel;
    private Player currentPlayer;
    private DungeonViewModel dungeonViewModel;

    private EditText etHeroName, etHeroClass;
    private EditText etLevel, etCurrentXp, etXpToNextLevel, etTalentPoints;
    private EditText etGold, etGems, etXpPenalty;
    private EditText etCurrentHp, etMaxHp, etCurrentMana, etMaxMana;
    private EditText etStrength, etIntelligence, etDexterity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        playerViewModel = new ViewModelProvider(this).get(PlayerViewModel.class);
        dungeonViewModel = new ViewModelProvider(this).get(DungeonViewModel.class);

        bindViews();
        setupBackButton();
        setupPresetButtons();
        setupApplyButton();

        playerViewModel.getPlayer().observe(this, player -> {
            if (player == null) return;
            currentPlayer = player;
            fillFields(player);
        });
    }

    private void bindViews() {
        etHeroName = findViewById(R.id.etHeroName);
        etHeroClass = findViewById(R.id.etHeroClass);

        etLevel = findViewById(R.id.etLevel);
        etCurrentXp = findViewById(R.id.etCurrentXp);
        etXpToNextLevel = findViewById(R.id.etXpToNextLevel);
        etTalentPoints = findViewById(R.id.etTalentPoints);

        etGold = findViewById(R.id.etGold);
        etGems = findViewById(R.id.etGems);
        etXpPenalty = findViewById(R.id.etXpPenalty);

        etCurrentHp = findViewById(R.id.etCurrentHp);
        etMaxHp = findViewById(R.id.etMaxHp);
        etCurrentMana = findViewById(R.id.etCurrentMana);
        etMaxMana = findViewById(R.id.etMaxMana);

        etStrength = findViewById(R.id.etStrength);
        etIntelligence = findViewById(R.id.etIntelligence);
        etDexterity = findViewById(R.id.etDexterity);
    }

    private void setupBackButton() {
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());
    }

    private void setupPresetButtons() {
        Button btnLevel10 = findViewById(R.id.btnLevel10);
        Button btnGoldGems = findViewById(R.id.btnGoldGems);
        Button btnMaxStats = findViewById(R.id.btnMaxStats);
        Button btnSetPenalty = findViewById(R.id.btnSetPenalty);
        Button btnClearPenalty = findViewById(R.id.btnClearPenalty);
        Button btnResetDungeonCooldown = findViewById(R.id.btnResetDungeonCooldown);

        btnLevel10.setOnClickListener(v -> {
            etLevel.setText("10");
            etCurrentXp.setText("0");
            etXpToNextLevel.setText("1000");
            etTalentPoints.setText("5");
        });

        btnGoldGems.setOnClickListener(v -> {
            etGold.setText("5000");
            etGems.setText("100");
        });

        btnMaxStats.setOnClickListener(v -> {
            etCurrentHp.setText("999");
            etMaxHp.setText("999");
            etCurrentMana.setText("999");
            etMaxMana.setText("999");
            etStrength.setText("25");
            etIntelligence.setText("25");
            etDexterity.setText("25");
        });

        btnSetPenalty.setOnClickListener(v -> showPenaltyPicker());

        btnClearPenalty.setOnClickListener(v -> etXpPenalty.setText("0"));

        btnResetDungeonCooldown.setOnClickListener(v -> {
            dungeonViewModel.resetDungeonCooldown();
            Toast.makeText(this, "Dungeon cooldown reset", Toast.LENGTH_SHORT).show();
        });
    }

    private void showPenaltyPicker() {
        String[] options = {
                "5%  (1 missed daily)",
                "10% (2 missed dailies)",
                "15% (3 missed dailies)",
                "20% (4 missed dailies)",
                "25% (5 missed dailies)",
                "30% (6 missed dailies)",
                "40% (8 missed dailies)",
                "50% (10+ missed dailies)"
        };

        int[] values = {5, 10, 15, 20, 25, 30, 40, 50};

        new AlertDialog.Builder(this)
                .setTitle("Set XP penalty")
                .setItems(options, (dialog, which) ->
                        etXpPenalty.setText(String.valueOf(values[which]))
                )
                .show();
    }

    private void setupApplyButton() {
        Button btnApply = findViewById(R.id.btnApply);
        btnApply.setOnClickListener(v -> applyChanges());
    }

    private void fillFields(Player player) {
        etHeroName.setText(player.getHeroName() == null ? "" : player.getHeroName());
        etHeroClass.setText(player.getHeroClass() == null ? "" : player.getHeroClass());

        etLevel.setText(String.valueOf(player.getLevel()));
        etCurrentXp.setText(String.valueOf(player.getCurrentXp()));
        etXpToNextLevel.setText(String.valueOf(player.getXpToNextLevel()));
        etTalentPoints.setText(String.valueOf(player.getTalentPoints()));

        etGold.setText(String.valueOf(player.getGold()));
        etGems.setText(String.valueOf(player.getGems()));
        etXpPenalty.setText(String.valueOf(player.getXpPenalty()));

        etCurrentHp.setText(String.valueOf(player.getCurrentHp()));
        etMaxHp.setText(String.valueOf(player.getMaxHp()));
        etCurrentMana.setText(String.valueOf(player.getCurrentMana()));
        etMaxMana.setText(String.valueOf(player.getMaxMana()));

        etStrength.setText(String.valueOf(player.getStrength()));
        etIntelligence.setText(String.valueOf(player.getIntelligence()));
        etDexterity.setText(String.valueOf(player.getDexterity()));
    }

    private void applyChanges() {
        if (currentPlayer == null) {
            Toast.makeText(this, "Player not loaded yet", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            currentPlayer.setHeroName(etHeroName.getText().toString().trim());

            String heroClass = etHeroClass.getText().toString().trim();
            currentPlayer.setHeroClass(TextUtils.isEmpty(heroClass) ? null : heroClass);

            currentPlayer.setLevel(parseInt(etLevel));
            currentPlayer.setCurrentXp(parseLong(etCurrentXp));
            currentPlayer.setXpToNextLevel(parseLong(etXpToNextLevel));
            currentPlayer.setTalentPoints(parseInt(etTalentPoints));

            currentPlayer.setGold(parseInt(etGold));
            currentPlayer.setGems(parseInt(etGems));
            currentPlayer.setXpPenalty(parseInt(etXpPenalty));

            currentPlayer.setCurrentHp(parseInt(etCurrentHp));
            currentPlayer.setMaxHp(parseInt(etMaxHp));
            currentPlayer.setCurrentMana(parseInt(etCurrentMana));
            currentPlayer.setMaxMana(parseInt(etMaxMana));

            currentPlayer.setStrength(parseInt(etStrength));
            currentPlayer.setIntelligence(parseInt(etIntelligence));
            currentPlayer.setDexterity(parseInt(etDexterity));

            playerViewModel.updatePlayer(currentPlayer);
            playerViewModel.checkAchievements();

            Toast.makeText(this, "Admin changes applied", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Toast.makeText(this, "Invalid values", Toast.LENGTH_SHORT).show();
        }
    }

    private int parseInt(EditText editText) {
        String value = editText.getText().toString().trim();
        return TextUtils.isEmpty(value) ? 0 : Integer.parseInt(value);
    }

    private long parseLong(EditText editText) {
        String value = editText.getText().toString().trim();
        return TextUtils.isEmpty(value) ? 0L : Long.parseLong(value);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}