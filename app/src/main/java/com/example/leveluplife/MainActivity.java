package com.example.leveluplife;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.TextView;

import com.example.leveluplife.data.entity.Task;
import com.example.leveluplife.ui.tasks.TaskAdapter;
import com.example.leveluplife.viewModel.TaskViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {

    private TaskViewModel taskViewModel;
    private TaskAdapter adapter;
    private TextView totalXpText;
    private TextView completedTasksText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        totalXpText = findViewById(R.id.totalXpText);
        completedTasksText = findViewById(R.id.completedTasksText);
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        FloatingActionButton fabAddTask = findViewById(R.id.fabAddTask);

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TaskAdapter();
        recyclerView.setAdapter(adapter);

        // Setup ViewModel
        taskViewModel = new ViewModelProvider(this).get(TaskViewModel.class);

        // Observe Total XP
        taskViewModel.getTotalXp().observe(this, totalXp -> {
            totalXpText.setText("Total XP: " + totalXp);
        });

        // Observe all tasks
        taskViewModel.getAllTasks().observe(this, tasks -> {
            adapter.setTasks(tasks);

            // Calculate completed count locally
            int completedCount = 0;
            if (tasks != null) {
                for (Task task : tasks) {
                    if (task.isCompleted()) {
                        completedCount++;
                    }
                }
                completedTasksText.setText("Completed: " + completedCount + "/" + tasks.size());
            } else {
                completedTasksText.setText("Completed: 0/0");
            }
        });

        // Setup adapter listener
        adapter.setOnTaskCheckedChangeListener((task, isChecked) -> {
            taskViewModel.toggleTaskCompleted(task.getId(), isChecked);
        });

        // FAB click listener
        fabAddTask.setOnClickListener(v -> {
            int randomXp = 50 + (int)(Math.random() * 50);
            Task newTask = new Task(
                    "Task XP:" + randomXp,
                    Task.TaskType.DAILY,
                    Task.AttributeType.STRENGTH,
                    randomXp,  // XP: 50-99
                    5          // Gold: 5
            );
            taskViewModel.insertTask(newTask);
        });
    }
}
