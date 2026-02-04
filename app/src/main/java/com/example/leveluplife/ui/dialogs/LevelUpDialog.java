package com.example.leveluplife.ui.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.leveluplife.R;

public class LevelUpDialog extends DialogFragment {

    private int newLevel;
    private int talentPoints;
    private int hpGain;
    private int manaGain;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // Получаем данные из Bundle
        if (getArguments() != null) {
            newLevel = getArguments().getInt("level");
            talentPoints = getArguments().getInt("talentPoints");
            hpGain = getArguments().getInt("hpGain");
            manaGain = getArguments().getInt("manaGain");
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_level_up, null);

        // ✅ ИСПОЛЬЗУЕМ ПРАВИЛЬНЫЕ ID ИЗ XML
        TextView tvLevelUp = view.findViewById(R.id.tv_level_up);
        TextView tvLevelNumber = view.findViewById(R.id.tv_level_number);
        TextView tvTalentPoints = view.findViewById(R.id.tv_talent_points);
        TextView tvMaxHp = view.findViewById(R.id.tv_max_hp);
        TextView tvMaxMana = view.findViewById(R.id.tv_max_mana);
        Button btnAwesome = view.findViewById(R.id.btn_awesome);

        // Заполнение данных
        tvLevelUp.setText("🎉 LEVEL UP! 🎉");
        tvLevelNumber.setText("You reached Level " + newLevel + "!");
        tvTalentPoints.setText("⭐ +" + talentPoints + " Talent Point" + (talentPoints > 1 ? "s" : ""));
        tvMaxHp.setText("❤️ Max HP +" + hpGain);
        tvMaxMana.setText("💙 Max Mana +" + manaGain);

        btnAwesome.setOnClickListener(v -> dismiss());

        builder.setView(view);

        return builder.create();
    }

    /**
     * Статический метод для создания диалога с параметрами
     */
    public static LevelUpDialog newInstance(int level, int talentPoints, int hpGain, int manaGain) {
        LevelUpDialog dialog = new LevelUpDialog();
        Bundle args = new Bundle();
        args.putInt("level", level);
        args.putInt("talentPoints", talentPoints);
        args.putInt("hpGain", hpGain);
        args.putInt("manaGain", manaGain);
        dialog.setArguments(args);
        return dialog;
    }
}
