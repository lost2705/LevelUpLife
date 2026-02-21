package com.example.leveluplife;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.leveluplife.data.entity.Achievement;
import com.example.leveluplife.ui.achievement.AchievementAdapter;
import com.example.leveluplife.viewModel.AchievementViewModel;

public class AchievementsActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private AchievementAdapter adapter;
    private AchievementViewModel viewModel;
    private TextView tvStats;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_achievements);

        recyclerView = findViewById(R.id.recycler_achievements);
        tvStats = findViewById(R.id.tv_achievements_stats);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AchievementAdapter();
        recyclerView.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(AchievementViewModel.class);

        viewModel.getAllAchievements().observe(this, achievements -> {
            adapter.submitList(achievements);
            if (achievements != null) {
                long unlocked = 0;
                for (Achievement a : achievements) {
                    if (a.isUnlocked()) unlocked++;
                }
                tvStats.setText("Unlocked " + unlocked + "/" + achievements.size());
            }
        });
    }
}
