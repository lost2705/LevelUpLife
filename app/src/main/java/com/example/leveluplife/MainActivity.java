package com.example.leveluplife;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.leveluplife.data.entity.Task;
import com.example.leveluplife.ui.tasks.TaskAdapter;
import com.example.leveluplife.viewModel.TaskViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {

    private TaskViewModel taskViewModel;
    private TaskAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        taskViewModel = new ViewModelProvider(this).get(TaskViewModel.class);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TaskAdapter();
        adapter.setOnTaskCheckedChangeListener((task, isChecked) -> {
            taskViewModel.toggleTaskCompleted(task.getId(), isChecked);
        });
        recyclerView.setAdapter(adapter);

        taskViewModel.getAllTasks().observe(this, tasks -> {
            if (tasks != null && !tasks.isEmpty()) {
                for (int i = 0; i < Math.min(3, tasks.size()); i++) {
                    Task t = tasks.get(i);
                }
            }
            adapter.setTasks(tasks);
        });

        FloatingActionButton fab = findViewById(R.id.fabAddTask);
        fab.setOnClickListener(v -> {
            Task newTask = new Task(
                    "Новая задача XP:" + (50 + (int)(Math.random() * 50)),
                    Task.TaskType.DAILY,
                    Task.AttributeType.STRENGTH,
                    50 + (int)(Math.random() * 50),  // XP
                    5                                // Gold
            );
            taskViewModel.insertTask(newTask);
        });

        Task testTask = new Task(
                "TEST: Пробежать 5км",
                Task.TaskType.DAILY,
                Task.AttributeType.AGILITY,
                100,
                10
        );
        taskViewModel.insertTask(testTask);
    }
}
