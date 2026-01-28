package com.example.leveluplife.viewModel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.example.leveluplife.data.entity.Task;
import com.example.leveluplife.data.repository.TaskRepository;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TaskViewModel extends AndroidViewModel {

    private final TaskRepository repository;
    private final LiveData<List<Task>> allTasks;
    private final ExecutorService executor;  // ← ДОБАВЬ

    public TaskViewModel(@NonNull Application application) {
        super(application);
        repository = new TaskRepository(application);
        allTasks = repository.getAllTasks();
        executor = Executors.newSingleThreadExecutor();  // ← ДОБАВЬ
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdown();  // ← ДОБАВЬ (cleanup)
    }

    public LiveData<List<Task>> getAllTasks() {
        return allTasks;
    }

    public LiveData<List<Task>> getPendingTasks() {
        return repository.getPendingTasks();
    }

    public void insertTask(Task task) {
        repository.insertTask(task);
    }

    public void updateTask(Task task) {
        repository.updateTask(task);
    }

    public void deleteTask(Task task) {
        repository.deleteTask(task);
    }

    public LiveData<Integer> getTotalXp() {
        return repository.getTotalXp();
    }

    public LiveData<Integer> getCompletedTasksCount() {
        return repository.getCompletedTasksCount();
    }

    public LiveData<Integer> getPendingTasksCount() {
        return repository.getPendingTasksCount();
    }

    public void toggleTaskCompleted(long taskId, boolean completed) {
        executor.execute(() -> {
            Task task = repository.getTaskById(taskId);
            if (task != null) {
                task.setCompleted(completed);
                repository.updateTask(task);
            }
        });
    }
}
