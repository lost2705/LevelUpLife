package com.example.leveluplife.viewModel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.leveluplife.data.entity.Task;
import com.example.leveluplife.data.repository.TaskRepository;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TaskViewModel extends AndroidViewModel {

    private static final String TAG = "TaskViewModel";

    private final TaskRepository repository;
    private final LiveData<List<Task>> allTasks;
    private final ExecutorService executor;
    private final MutableLiveData<String> currentFilter = new MutableLiveData<>("ALL");

    public TaskViewModel(@NonNull Application application) {
        super(application);
        repository = new TaskRepository(application);
        allTasks = repository.getAllTasks();
        executor = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<Task>> getAllTasks() {
        return allTasks;
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

    public void toggleTaskCompleted(long taskId, boolean isCompleted) {
        executor.execute(() -> {
            Task task = repository.getTaskById(taskId);
            if (task == null) {
                Log.w(TAG, "Task not found: " + taskId);
                return;
            }

            boolean wasCompleted = task.isCompleted();

            if (wasCompleted == isCompleted) {
                Log.d(TAG, "Task status unchanged");
                return;
            }

            task.setCompleted(isCompleted);
            task.setLastUpdated(System.currentTimeMillis());
            repository.updateTask(task);

            Log.d(TAG, "Task completion state updated only: " + task.getTitle());
        });
    }

    public LiveData<List<Task>> getFilteredTasks() {
        String filter = currentFilter.getValue();
        if (filter == null || filter.equals("ALL")) {
            return repository.getAllTasks();
        } else {
            return repository.getTasksByFrequency(filter);
        }
    }

    public void setFilter(String filter) {
        currentFilter.setValue(filter);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdown();
    }
}
