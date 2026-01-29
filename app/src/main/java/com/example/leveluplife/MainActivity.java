package com.example.leveluplife;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.TextView;

import com.example.leveluplife.data.entity.Task;
import com.example.leveluplife.ui.dialogs.TaskCreationDialog;
import com.example.leveluplife.ui.tasks.TaskAdapter;
import com.example.leveluplife.viewModel.TaskViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

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

        fabAddTask.setOnClickListener(v -> {
            TaskCreationDialog dialog = new TaskCreationDialog();
            dialog.setOnTaskCreatedListener(task -> {
                taskViewModel.insertTask(task);
            });
            dialog.show(getSupportFragmentManager(), "TaskCreationDialog");
        });


        // Swipe to delete
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Task deletedTask = adapter.getTaskAt(position);

                taskViewModel.deleteTask(deletedTask);

                Snackbar.make(recyclerView, "Task deleted", Snackbar.LENGTH_LONG)
                        .setAction("UNDO", v -> {
                            taskViewModel.insertTask(deletedTask);
                        })
                        .show();
            }
        });

        itemTouchHelper.attachToRecyclerView(recyclerView);

    }
}
