package com.example.leveluplife;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.leveluplife.ui.shop.ShopAdapter;
import com.example.leveluplife.viewModel.ShopViewModel;

public class ShopActivity extends AppCompatActivity {

    private ShopViewModel viewModel;
    private TextView tvGoldBalance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop);

        ImageButton btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        tvGoldBalance = findViewById(R.id.tv_gold_balance);

        RecyclerView recyclerView = findViewById(R.id.recycler_shop);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        ShopAdapter adapter = new ShopAdapter(item ->
                viewModel.purchaseItem(item)
        );
        recyclerView.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(ShopViewModel.class);

        viewModel.getAvailableItems().observe(this, adapter::submitList);

        viewModel.getPlayer().observe(this, player -> {
            if (player != null) {
                tvGoldBalance.setText("💰 " + player.getGold() + " Gold");
            }
        });

        // Показываем статус XP Boost
        updateBoostIndicator();

        viewModel.getPurchaseResult().observe(this, result -> {
            if (result == null) return;
            if ("NOT_ENOUGH_GOLD".equals(result)) {
                Toast.makeText(this, "❌ Not enough gold!", Toast.LENGTH_SHORT).show();
            } else if (result.startsWith("SUCCESS:")) {
                String itemName = result.substring(8);
                Toast.makeText(this, "✅ Purchased: " + itemName, Toast.LENGTH_SHORT).show();
                updateBoostIndicator(); // 👈 обновить после покупки
            }
        });

        viewModel.getPurchaseResult().observe(this, result -> {
            if (result == null) return;
            if ("NOT_ENOUGH_GOLD".equals(result)) {
                Toast.makeText(this, "❌ Not enough gold!", Toast.LENGTH_SHORT).show();
            } else if (result.startsWith("SUCCESS:")) {
                String itemName = result.substring(8);
                Toast.makeText(this, "✅ Purchased: " + itemName, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateBoostIndicator() {
        SharedPreferences prefs = getSharedPreferences("shop_effects", MODE_PRIVATE);
        boolean active = prefs.getBoolean("xp_boost_active", false);
        TextView tvBoostStatus = findViewById(R.id.tv_boost_status);
        if (tvBoostStatus != null) {
            tvBoostStatus.setVisibility(active ? View.VISIBLE : View.GONE);
            tvBoostStatus.setText("⚡ XP Boost active — next task gives x2 XP!");
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
