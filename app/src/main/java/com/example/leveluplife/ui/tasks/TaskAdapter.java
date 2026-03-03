package com.example.leveluplife.ui.tasks;

import android.graphics.Paint;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.leveluplife.R;
import com.example.leveluplife.data.entity.Task;

import java.util.ArrayList;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<Task> tasks = new ArrayList<>();
    private OnTaskClickListener clickListener;
    private OnTaskLongClickListener longClickListener;

    public interface OnTaskClickListener {
        void onTaskClick(Task task, int position);
    }

    public interface OnTaskLongClickListener {
        void onTaskLongClick(Task task);
    }

    public void setOnTaskClickListener(OnTaskClickListener listener) {
        this.clickListener = listener;
    }

    public void setOnTaskLongClickListener(OnTaskLongClickListener listener) {
        this.longClickListener = listener;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.task_item, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        holder.bind(tasks.get(position), position);
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    public void setTasks(List<Task> newTasks) {
        if (this.tasks.isEmpty()) {
            this.tasks = new ArrayList<>(newTasks);
            notifyDataSetChanged();
            return;
        }

        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override public int getOldListSize() { return tasks.size(); }
            @Override public int getNewListSize() { return newTasks.size(); }

            @Override
            public boolean areItemsTheSame(int oldPos, int newPos) {
                return tasks.get(oldPos).getId() == newTasks.get(newPos).getId();
            }

            @Override
            public boolean areContentsTheSame(int oldPos, int newPos) {
                Task o = tasks.get(oldPos);
                Task n = newTasks.get(newPos);
                return o.getId() == n.getId()
                        && o.getTitle().equals(n.getTitle())
                        && o.isCompleted() == n.isCompleted()
                        && o.isRewardClaimed() == n.isRewardClaimed()
                        && o.getXpReward() == n.getXpReward()
                        && o.getGoldReward() == n.getGoldReward();
            }
        });

        this.tasks = new ArrayList<>(newTasks);
        diffResult.dispatchUpdatesTo(this);
    }

    public Task getTaskAt(int position) {
        return tasks.get(position);
    }

    class TaskViewHolder extends RecyclerView.ViewHolder {
        private final CheckBox taskCheckbox;
        private final TextView taskTitle;
        private final TextView taskXp;
        private final TextView taskGold;
        private final TextView attributeIcon;
        private final View attributeStripe;
        private final TextView taskTypeBadge;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            taskCheckbox    = itemView.findViewById(R.id.taskCheckbox);
            taskTitle       = itemView.findViewById(R.id.taskTitle);
            taskXp          = itemView.findViewById(R.id.taskXp);
            taskGold        = itemView.findViewById(R.id.taskGold);
            attributeIcon   = itemView.findViewById(R.id.attributeIcon);
            attributeStripe = itemView.findViewById(R.id.attributeStripe);
            taskTypeBadge   = itemView.findViewById(R.id.taskTypeBadge);
        }

        public void bind(Task task, int position) {
            taskCheckbox.setOnCheckedChangeListener(null);
            itemView.setOnClickListener(null);
            itemView.setOnLongClickListener(null);

            taskTitle.setText(task.getTitle());
            taskXp.setText("⭐ +" + task.getXpReward() + " XP");
            taskGold.setText("💰 +" + task.getGoldReward() + " Gold");

            boolean completed = task.isCompleted();
            taskCheckbox.setChecked(completed);
            taskCheckbox.jumpDrawablesToCurrentState();

            if (completed) {
                taskTitle.setPaintFlags(taskTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                taskTitle.setAlpha(0.4f);
                taskXp.setAlpha(0.4f);
                taskGold.setAlpha(0.4f);
            } else {
                taskTitle.setPaintFlags(taskTitle.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
                taskTitle.setAlpha(1.0f);
                taskXp.setAlpha(1.0f);
                taskGold.setAlpha(1.0f);
            }

            taskTypeBadge.setText(task.getTaskType().name());

            String icon;
            int color;
            switch (task.getAttributeType()) {
                case STRENGTH:
                    icon = "💪"; color = 0xFFFF5252; break;
                case INTELLIGENCE:
                    icon = "🧠"; color = 0xFF448AFF; break;
                case DEXTERITY:
                    icon = "⚡"; color = 0xFFFFD700; break;
                case CONSTITUTION:
                    icon = "🛡️"; color = 0xFF4CAF50; break;
                default:
                    icon = "⭐"; color = 0xFFBB86FC; break;
            }
            attributeIcon.setText(icon);
            attributeStripe.setBackgroundColor(color);

            taskTypeBadge.setTextColor(color);
            GradientDrawable badgeBg = new GradientDrawable();
            badgeBg.setShape(GradientDrawable.RECTANGLE);
            badgeBg.setCornerRadius(12f);
            badgeBg.setStroke(2, color);
            badgeBg.setColor(0x15000000);
            taskTypeBadge.setBackground(badgeBg);

            itemView.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && clickListener != null) {
                    clickListener.onTaskClick(tasks.get(pos), pos);
                }
            });

            itemView.setOnLongClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && longClickListener != null) {
                    longClickListener.onTaskLongClick(tasks.get(pos));
                    return true;
                }
                return false;
            });
        }
    }
}
