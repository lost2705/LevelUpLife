package com.example.leveluplife;

import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.leveluplife.data.entity.Task;
import com.example.leveluplife.ui.dialogs.TaskCreationDialog;
import com.example.leveluplife.ui.tasks.TaskAdapter;
import com.example.leveluplife.viewModel.PlayerViewModel;
import com.example.leveluplife.viewModel.TaskViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

public class MainActivity extends AppCompatActivity {

    private TaskViewModel taskViewModel;
    private PlayerViewModel playerViewModel;  // 🆕
    private TaskAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        taskViewModel = new ViewModelProvider(this).get(TaskViewModel.class);
        playerViewModel = new ViewModelProvider(this).get(PlayerViewModel.class);  // 🆕

        playerViewModel.initializePlayerIfNeeded();  // 🆕

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        adapter = new TaskAdapter();
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));


        taskViewModel.getAllTasks().observe(this, tasks -> {
            adapter.setTasks(tasks);
        });

        taskViewModel.getTotalXp().observe(this, totalXp -> {
            TextView totalXpText = findViewById(R.id.totalXpText);
            if (totalXpText != null && totalXp != null) {
                totalXpText.setText("Total XP: " + totalXp);
            }
        });

        taskViewModel.getCompletedTasksCount().observe(this, completed -> {
            taskViewModel.getAllTasks().observe(this, tasks -> {
                TextView completedTasksText = findViewById(R.id.completedTasksText);
                if (completedTasksText != null && completed != null && tasks != null) {
                    completedTasksText.setText("Completed: " + completed + "/" + tasks.size());
                }
            });
        });

        playerViewModel.getPlayer().observe(this, player -> {
            if (player != null) {
                android.util.Log.d("MainActivity", "Player loaded: Level " + player.level +
                        ", Gold: " + player.gold + ", XP: " + player.currentXp);
            }
        });

        adapter.setOnTaskCheckedChangeListener((task, isChecked) -> {
            taskViewModel.toggleTaskCompleted(task.getId(), isChecked);
        });

        FloatingActionButton fabAddTask = findViewById(R.id.fabAddTask);
        fabAddTask.setOnClickListener(v -> {
            TaskCreationDialog dialog = new TaskCreationDialog();
            dialog.setOnTaskCreatedListener(task -> {
                taskViewModel.insertTask(task);
            });
            dialog.show(getSupportFragmentManager(), "TaskCreationDialog");
        });

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(
                new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {

                    @Override
                    public boolean onMove(@NonNull RecyclerView recyclerView,
                                          @NonNull RecyclerView.ViewHolder viewHolder,
                                          @NonNull RecyclerView.ViewHolder target) {
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
                                }).show();
                    }
                }
        );
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }
}
