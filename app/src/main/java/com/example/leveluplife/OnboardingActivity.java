package com.example.leveluplife;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.animation.AnimationUtils;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.leveluplife.viewModel.PlayerViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class OnboardingActivity extends AppCompatActivity {

    private PlayerViewModel playerViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        playerViewModel = new ViewModelProvider(this).get(PlayerViewModel.class);

        TextInputEditText etHeroName = findViewById(R.id.etHeroName);
        MaterialButton btnBegin = findViewById(R.id.btnBegin);

        btnBegin.setOnClickListener(v -> {
            String name = etHeroName.getText() != null
                    ? etHeroName.getText().toString().trim()
                    : "";

            if (TextUtils.isEmpty(name)) {
                etHeroName.startAnimation(
                        AnimationUtils.loadAnimation(this, R.anim.shake));
                Toast.makeText(this,
                        "Please enter your hero name!", Toast.LENGTH_SHORT).show();
                return;
            }

            playerViewModel.setHeroName(name);
            getSharedPreferences("app_settings", MODE_PRIVATE)
                    .edit()
                    .putBoolean("onboarding_complete", true)
                    .apply();

            startActivity(new Intent(this, MainActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            finish();
        });
    }
}
