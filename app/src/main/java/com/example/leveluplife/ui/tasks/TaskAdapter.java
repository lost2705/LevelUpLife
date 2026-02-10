package com.example.leveluplife.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
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
        void onTaskClick(Task task, boolean isChecked);
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
        holder.bind(task);
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
        notifyDataSetChanged();
    }

    public Task getTaskAt(int position) {
        return tasks.get(position);
    }

    class TaskViewHolder extends RecyclerView.ViewHolder {
        private final TextView titleView;
        private final TextView xpView;
        private final CheckBox checkBox;

        TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            titleView = itemView.findViewById(R.id.taskTitle);
            xpView = itemView.findViewById(R.id.taskXp);
            checkBox = itemView.findViewById(R.id.taskCheckbox);
        }

        void bind(Task task) {
            titleView.setText(task.getTitle());
            xpView.setText("XP: +" + task.getXpReward());
            checkBox.setChecked(task.isCompleted());

            checkBox.setOnCheckedChangeListener(null);
            checkBox.setChecked(task.isCompleted());

            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (clickListener != null) {
                    clickListener.onTaskClick(task, isChecked);
                }
            });

            itemView.setOnLongClickListener(v -> {
                if (longClickListener != null) {
                    longClickListener.onTaskLongClick(task);
                    return true;
                }
                return false;
            });
        }
    }
}
