package com.example.leveluplife;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.leveluplife.viewModel.PlayerViewModel;

public class HeroActivity extends AppCompatActivity {

    private PlayerViewModel playerViewModel;

    private TextView tvHeroName, tvHeroClass, tvHeroAvatar;
    private TextView tvStrength, tvIntelligence, tvDexterity;
    private TextView tvHp, tvMana, tvGold;
    private TextView tvClassBonusTitle, tvClassBonusDescription;

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
        tvClassBonusTitle = findViewById(R.id.tvClassBonusTitle);
        tvClassBonusDescription = findViewById(R.id.tvClassBonusDescription);

        playerViewModel = new ViewModelProvider(this).get(PlayerViewModel.class);
        playerViewModel.getPlayer().observe(this, player -> {
            if (player == null) return;

            String name = player.getHeroName() != null && !player.getHeroName().trim().isEmpty()
                    ? player.getHeroName()
                    : "Hero";
            tvHeroName.setText(name);

            String heroClass = player.getHeroClass();
            if (heroClass != null) {
                tvHeroClass.setText("Level " + player.getLevel() + " • " + heroClass);

                switch (heroClass) {
                    case "Warrior":
                        tvHeroAvatar.setText("⚔️");
                        tvHeroClass.setTextColor(Color.parseColor("#FF5252"));
                        tvClassBonusTitle.setText("Warrior Bonus");
                        tvClassBonusTitle.setTextColor(Color.parseColor("#FF5252"));
                        tvClassBonusDescription.setText("+20% Max HP • +3 Strength");
                        break;

                    case "Mage":
                        tvHeroAvatar.setText("🧙");
                        tvHeroClass.setTextColor(Color.parseColor("#448AFF"));
                        tvClassBonusTitle.setText("Mage Bonus");
                        tvClassBonusTitle.setTextColor(Color.parseColor("#448AFF"));
                        tvClassBonusDescription.setText("+20% Max Mana • +3 Intelligence");
                        break;

                    case "Ranger":
                        tvHeroAvatar.setText("🏹");
                        tvHeroClass.setTextColor(Color.parseColor("#4CAF50"));
                        tvClassBonusTitle.setText("Ranger Bonus");
                        tvClassBonusTitle.setTextColor(Color.parseColor("#4CAF50"));
                        tvClassBonusDescription.setText("+20% Dexterity • Agile build");
                        break;

                    default:
                        tvHeroAvatar.setText("🧙");
                        tvHeroClass.setTextColor(Color.parseColor("#BB86FC"));
                        tvClassBonusTitle.setText("Class Bonus");
                        tvClassBonusTitle.setTextColor(Color.parseColor("#BB86FC"));
                        tvClassBonusDescription.setText("Your class bonus will appear here");
                        break;
                }

            } else if (player.getLevel() >= 10) {
                tvHeroClass.setText("Level " + player.getLevel() + " • Choose your class!");
                tvHeroClass.setTextColor(Color.parseColor("#FFD700"));
                tvHeroAvatar.setText("❓");
                tvClassBonusTitle.setText("Class Awakening");
                tvClassBonusTitle.setTextColor(Color.parseColor("#FFD700"));
                tvClassBonusDescription.setText("Reach your destiny and choose a class");
            } else {
                tvHeroClass.setText("Level " + player.getLevel() + " • Adventurer");
                tvHeroClass.setTextColor(Color.parseColor("#BB86FC"));
                tvHeroAvatar.setText("🧙");
                tvClassBonusTitle.setText("Class Locked");
                tvClassBonusTitle.setTextColor(Color.parseColor("#888888"));
                tvClassBonusDescription.setText("Unlock class selection at Level 10");
            }

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