package com.example.leveluplife;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.leveluplife.viewModel.PlayerViewModel;

public class HeroActivity extends AppCompatActivity {

    private PlayerViewModel playerViewModel;

    private TextView tvHeroName, tvHeroClass, tvHeroAvatar;
    private TextView tvStrength, tvIntelligence, tvDexterity;
    private TextView tvHp, tvMana, tvGold;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hero);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        tvHeroName = findViewById(R.id.tvHeroName);
        tvHeroClass = findViewById(R.id.tvHeroClass);
        tvHeroAvatar = findViewById(R.id.tvHeroAvatar);
        tvStrength = findViewById(R.id.tvStrength);
        tvIntelligence = findViewById(R.id.tvIntelligence);
        tvDexterity = findViewById(R.id.tvDexterity);
        tvHp = findViewById(R.id.tvHp);
        tvMana = findViewById(R.id.tvMana);
        tvGold = findViewById(R.id.tvGold);

        playerViewModel = new ViewModelProvider(this).get(PlayerViewModel.class);
        playerViewModel.getPlayer().observe(this, player -> {
            if (player == null) return;

            String name = player.getHeroName() != null ? player.getHeroName() : "Hero";
            tvHeroName.setText(name);

            String heroClass = player.getHeroClass();
            if (heroClass != null) {
                tvHeroClass.setText("Level " + player.getLevel() + " • " + heroClass);
                switch (heroClass) {
                    case "Warrior": tvHeroAvatar.setText("⚔️"); break;
                    case "Mage":    tvHeroAvatar.setText("🧙"); break;
                    case "Ranger":  tvHeroAvatar.setText("🏹"); break;
                }
            } else if (player.getLevel() >= 10) {
                tvHeroClass.setText("Level " + player.getLevel() + " • Choose your class!");
                tvHeroClass.setTextColor(0xFFFFD700);
                tvHeroAvatar.setText("❓");
            } else {
                tvHeroClass.setText("Level " + player.getLevel() + " • Adventurer");
                tvHeroAvatar.setText("🧙");
            }

            // Stats
            tvStrength.setText(" " + player.getStrength());
            tvIntelligence.setText(" " + player.getIntelligence());
            tvDexterity.setText(" " + player.getDexterity());
            tvHp.setText(" " + player.getCurrentHp() + "/" + player.getMaxHp());
            tvMana.setText(" " + player.getCurrentMana() + "/" + player.getMaxMana());
            tvGold.setText(" " + player.getGold());
        });
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
