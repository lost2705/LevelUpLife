package com.example.leveluplife.ui.achievement;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.leveluplife.R;
import com.example.leveluplife.data.entity.Achievement;

public class AchievementAdapter extends ListAdapter<Achievement, AchievementAdapter.ViewHolder> {

    public AchievementAdapter() {
        super(new DiffCallback());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_achievement, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Achievement achievement = getItem(position);
        holder.icon.setText(achievement.getIcon());
        holder.title.setText(achievement.getTitle());
        holder.desc.setText(achievement.getDescription());

        if (achievement.isUnlocked()) {
            holder.icon.setTextColor(0xFFD700);
            holder.title.setTextColor(0xFFFFFF);
            holder.desc.setTextColor(0xCCCCCC);
            holder.status.setText("✅ UNLOCKED");
            holder.status.setTextColor(0x4CAF50);
            holder.itemView.setAlpha(1.0f);
        } else {
            holder.icon.setTextColor(0x666666);
            holder.title.setTextColor(0x999999);
            holder.desc.setTextColor(0x666666);
            holder.status.setText("🔒 LOCKED");
            holder.status.setTextColor(0x666666);
            holder.itemView.setAlpha(0.6f);
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView icon, title, desc, status;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.achievement_icon);
            title = itemView.findViewById(R.id.achievement_title);
            desc = itemView.findViewById(R.id.achievement_description);
            status = itemView.findViewById(R.id.achievement_status);
        }
    }

    static class DiffCallback extends DiffUtil.ItemCallback<Achievement> {
        @Override
        public boolean areItemsTheSame(@NonNull Achievement oldItem, @NonNull Achievement newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @SuppressLint("DiffUtilEquals")
        @Override
        public boolean areContentsTheSame(@NonNull Achievement oldItem, @NonNull Achievement newItem) {
            return oldItem.equals(newItem);
        }
    }
}
