package com.example.leveluplife.ui.tasks;

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

    public interface OnTaskCheckedChangeListener {
        void onTaskCheckedChanged(Task task, boolean isChecked);
    }

    private OnTaskCheckedChangeListener listener;

    public void setOnTaskCheckedChangeListener(OnTaskCheckedChangeListener listener) {
        this.listener = listener;
    }

    private List<Task> tasks = new ArrayList<>();

    public void setTasks(List<Task> tasks) {
        this.tasks.clear();
        if (tasks != null) this.tasks.addAll(tasks);
        notifyDataSetChanged();
    }

    public Task getTaskAt(int position) {
        if (tasks != null && position >= 0 && position < tasks.size()) {
            return tasks.get(position);
        }
        return null;
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

        holder.titleView.setText(task.getTitle());
        holder.xpView.setText("XP: " + task.getXpReward());

        holder.checkBox.setOnCheckedChangeListener(null);
        holder.checkBox.setChecked(task.isCompleted());
        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (listener != null) {
                listener.onTaskCheckedChanged(task, isChecked);
            }
        });
    }

    @Override
    public int getItemCount() {
        return tasks.size();
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
            titleView.setText(task.title);
            xpView.setText("XP: " + task.xpReward);
            checkBox.setChecked(task.completed);
        }
    }
}
