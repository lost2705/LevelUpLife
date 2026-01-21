package com.example.leveluplife;

import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.leveluplife.data.entity.Task;
import com.example.leveluplife.ui.tasks.TaskAdapter;
import com.example.leveluplife.viewModel.TaskViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "DIAG_MAIN"; // ← НОВЫЙ TAG для фильтра
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
        recyclerView.setAdapter(adapter);

        taskViewModel.getAllTasks().observe(this, tasks -> {
            Log.d(TAG, "🔥 5. LIVE DATA CALLBACK! Count: " +
                    (tasks == null ? "NULL" : tasks.size()));
            if (tasks != null && !tasks.isEmpty()) {
                for (int i = 0; i < Math.min(3, tasks.size()); i++) {
                    Task t = tasks.get(i);
                    Log.d(TAG, "🔥 5. Task[" + i + "]: " + t.title +
                            " id=" + t.id + " xp=" + t.xpReward);
                }
            }
            adapter.setTasks(tasks);
            Log.d(TAG, "🔥 5. Adapter updated");
        });
        Log.d(TAG, "🔥 6. LiveData observe registered");

        FloatingActionButton fab = findViewById(R.id.fabAddTask);
        fab.setOnClickListener(v -> {
            Log.d(TAG, "🔥 7. FAB CLICKED");
            Task newTask = new Task(
                    "FAB: " + System.currentTimeMillis(),
                    Task.TaskType.DAILY,
                    Task.AttributeType.STRENGTH,
                    50 + (int)(Math.random() * 50),
                    5
            );
            Log.d(TAG, "🔥 7. Inserting: " + newTask.title);
            taskViewModel.insertTask(newTask);
            Log.d(TAG, "🔥 7. Insert called");
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
