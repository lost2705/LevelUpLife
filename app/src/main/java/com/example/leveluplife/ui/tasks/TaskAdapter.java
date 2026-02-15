package com.example.leveluplife.ui.tasks;

import android.graphics.Paint;
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
        Task task = tasks.get(position);
        holder.bind(task, position);
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
            @Override
            public int getOldListSize() {
                return tasks.size();
            }

            @Override
            public int getNewListSize() {
                return newTasks.size();
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                return tasks.get(oldItemPosition).getId() == newTasks.get(newItemPosition).getId();
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                Task oldTask = tasks.get(oldItemPosition);
                Task newTask = newTasks.get(newItemPosition);

                return oldTask.getId() == newTask.getId() &&
                        oldTask.getTitle().equals(newTask.getTitle()) &&
                        oldTask.isCompleted() == newTask.isCompleted() &&
                        oldTask.isRewardClaimed() == newTask.isRewardClaimed() &&
                        oldTask.getXpReward() == newTask.getXpReward() &&
                        oldTask.getGoldReward() == newTask.getGoldReward();
            }
        });

        this.tasks = new ArrayList<>(newTasks);
        diffResult.dispatchUpdatesTo(this);
    }

    public Task getTaskAt(int position) {
        return tasks.get(position);
    }

    class TaskViewHolder extends RecyclerView.ViewHolder {
        private CheckBox checkBox;
        private TextView titleText;
        private TextView xpText;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.taskCheckbox);
            titleText = itemView.findViewById(R.id.taskTitle);
            xpText = itemView.findViewById(R.id.taskXp);
        }

        public void bind(Task task, int position) {

            checkBox.setOnCheckedChangeListener(null);
            itemView.setOnClickListener(null);
            itemView.setOnLongClickListener(null);

            titleText.setText(task.getTitle());
            xpText.setText("⭐ +" + task.getXpReward() + " XP");

            boolean isCompleted = task.isCompleted();
            checkBox.setChecked(isCompleted);
            checkBox.jumpDrawablesToCurrentState();

            if (isCompleted) {
                titleText.setPaintFlags(titleText.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                titleText.setAlpha(0.5f);
            } else {
                titleText.setPaintFlags(titleText.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
                titleText.setAlpha(1.0f);
            }


            itemView.setOnClickListener(v -> {
                int currentPosition = getAdapterPosition();

                if (currentPosition != RecyclerView.NO_POSITION && clickListener != null) {
                    Task currentTask = tasks.get(currentPosition);
                    clickListener.onTaskClick(currentTask, currentPosition);
                } else {
                    android.util.Log.e("TaskAdapter", "Click NOT processed! Position: " + currentPosition + ", listener: " + clickListener);
                }
            });

            itemView.setOnLongClickListener(v -> {
                int currentPosition = getAdapterPosition();
                if (currentPosition != RecyclerView.NO_POSITION && longClickListener != null) {
                    Task currentTask = tasks.get(currentPosition);
                    longClickListener.onTaskLongClick(currentTask);
                    return true;
                }
                return false;
            });
        }
    }
}
