package com.example.leveluplife.viewModel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.leveluplife.data.entity.Task;
import com.example.leveluplife.data.repository.PlayerRepository;
import com.example.leveluplife.data.repository.TaskRepository;

import java.util.List;

public class TaskViewModel extends AndroidViewModel {

    private TaskRepository repository;
    private LiveData<List<Task>> allTasks;

    public TaskViewModel(@NonNull Application application) {
        super(application);

        // Сначала создаем PlayerRepository
        PlayerRepository playerRepository = PlayerRepository.getInstance(application);

        // Передаем его в TaskRepository
        repository = new TaskRepository(application, playerRepository);

        allTasks = repository.getAllTasks();
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
        repository.toggleTaskCompletedWithRewards(taskId, isCompleted, getApplication());
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
}
