package com.example.leveluplife;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.leveluplife.data.entity.Player;
import com.example.leveluplife.utils.SoundManager;
import com.example.leveluplife.viewModel.PlayerViewModel;

public class TalentsActivity extends AppCompatActivity {

    private PlayerViewModel playerViewModel;
    private SoundManager soundManager;

    private TextView tvAvailablePoints;
    private TextView tvStrengthValue;
    private TextView tvIntelligenceValue;
    private TextView tvDexterityValue;

    private Button btnStrengthPlus;
    private Button btnStrengthMinus;
    private Button btnIntelligencePlus;
    private Button btnIntelligenceMinus;
    private Button btnDexterityPlus;
    private Button btnDexterityMinus;
    private Button btnSave;

    private int tempStrength;
    private int tempIntelligence;
    private int tempDexterity;
    private int tempTalentPoints;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_talents);

        soundManager = SoundManager.getInstance(this);
        playerViewModel = new ViewModelProvider(this).get(PlayerViewModel.class);

        tvAvailablePoints = findViewById(R.id.tv_available_points);
        tvStrengthValue = findViewById(R.id.tv_strength_value);
        tvIntelligenceValue = findViewById(R.id.tv_intelligence_value);
        tvDexterityValue = findViewById(R.id.tv_dexterity_value);

        btnStrengthPlus = findViewById(R.id.btn_strength_plus);
        btnStrengthMinus = findViewById(R.id.btn_strength_minus);
        btnIntelligencePlus = findViewById(R.id.btn_intelligence_plus);
        btnIntelligenceMinus = findViewById(R.id.btn_intelligence_minus);
        btnDexterityPlus = findViewById(R.id.btn_dexterity_plus);
        btnDexterityMinus = findViewById(R.id.btn_dexterity_minus);
        btnSave = findViewById(R.id.btn_save_talents);

        playerViewModel.getPlayer().observe(this, player -> {
            if (player != null) {
                initializeTempValues(player);
                updateUI();
            }
        });

        setupButtonListeners();
    }

    private void initializeTempValues(Player player) {
        tempStrength = player.strength;
        tempIntelligence = player.intelligence;
        tempDexterity = player.dexterity;
        tempTalentPoints = player.talentPoints;
    }

    private void setupButtonListeners() {
        // Strength
        btnStrengthPlus.setOnClickListener(v -> {
            if (tempTalentPoints > 0) {
                tempStrength++;
                tempTalentPoints--;
                soundManager.playTaskComplete();
                updateUI();
            } else {
                showNoPointsToast();
            }
        });

        btnStrengthMinus.setOnClickListener(v -> {
            if (tempStrength > 5) {
                tempStrength--;
                tempTalentPoints++;
                updateUI();
            } else {
                Toast.makeText(this, "Minimum value: 5", Toast.LENGTH_SHORT).show();
            }
        });

        btnIntelligencePlus.setOnClickListener(v -> {
            if (tempTalentPoints > 0) {
                tempIntelligence++;
                tempTalentPoints--;
                soundManager.playTaskComplete();
                updateUI();
            } else {
                showNoPointsToast();
            }
        });

        btnIntelligenceMinus.setOnClickListener(v -> {
            if (tempIntelligence > 5) {
                tempIntelligence--;
                tempTalentPoints++;
                updateUI();
            } else {
                Toast.makeText(this, "Minimum value: 5", Toast.LENGTH_SHORT).show();
            }
        });

        // Dexterity
        btnDexterityPlus.setOnClickListener(v -> {
            if (tempTalentPoints > 0) {
                tempDexterity++;
                tempTalentPoints--;
                soundManager.playTaskComplete();
                updateUI();
            } else {
                showNoPointsToast();
            }
        });

        btnDexterityMinus.setOnClickListener(v -> {
            if (tempDexterity > 5) {
                tempDexterity--;
                tempTalentPoints++;
                updateUI();
            } else {
                Toast.makeText(this, "Minimum value: 5", Toast.LENGTH_SHORT).show();
            }
        });

        // Save
        btnSave.setOnClickListener(v -> {
            saveTalents();
        });
    }

    /**
     * Обновление UI
     */
    private void updateUI() {
        tvAvailablePoints.setText("Available Points: " + tempTalentPoints);
        tvStrengthValue.setText(String.valueOf(tempStrength));
        tvIntelligenceValue.setText(String.valueOf(tempIntelligence));
        tvDexterityValue.setText(String.valueOf(tempDexterity));

        boolean hasPoints = tempTalentPoints > 0;
        btnStrengthPlus.setEnabled(hasPoints);
        btnIntelligencePlus.setEnabled(hasPoints);
        btnDexterityPlus.setEnabled(hasPoints);

        btnStrengthMinus.setEnabled(tempStrength > 5);
        btnIntelligenceMinus.setEnabled(tempIntelligence > 5);
        btnDexterityMinus.setEnabled(tempDexterity > 5);
    }

    private void saveTalents() {
        playerViewModel.updateTalents(tempStrength, tempIntelligence, tempDexterity, tempTalentPoints);
        soundManager.playLevelUp();
        Toast.makeText(this, "✅ Talents saved!", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void showNoPointsToast() {
        Toast.makeText(this, "❌ No talent points available!", Toast.LENGTH_SHORT).show();

        if (tvAvailablePoints != null) {
            tvAvailablePoints.startAnimation(
                    android.view.animation.AnimationUtils.loadAnimation(this, R.anim.shake)
            );
        }
    }
}
