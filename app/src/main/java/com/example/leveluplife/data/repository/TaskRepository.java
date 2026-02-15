package com.example.leveluplife.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.leveluplife.data.dao.TaskDao;
import com.example.leveluplife.data.database.AppDatabase;
import com.example.leveluplife.data.entity.Task;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TaskRepository {
    private final TaskDao taskDao;
    private final LiveData<List<Task>> allTasks;
    private final ExecutorService executor;

    public TaskRepository(Application application) {
        AppDatabase database = AppDatabase.getDatabase(application);
        taskDao = database.taskDao();
        allTasks = taskDao.getAllTasksLiveData();
        executor = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<Task>> getAllTasks() {
        return allTasks;
    }

    public LiveData<List<Task>> getTasksByFrequency(String frequency) {
        return taskDao.getTasksByFrequency(frequency);
    }

    public LiveData<List<Task>> getPendingTasks() {
        return taskDao.getPendingTasksLiveData();
    }

    public void insertTask(final Task task) {
        executor.execute(() -> taskDao.insertTask(task));
    }

    public void updateTask(final Task task) {
        executor.execute(() -> taskDao.updateTask(task));
    }

    public void deleteTask(final Task task) {
        executor.execute(() -> taskDao.deleteTask(task));
    }

    public Task getTaskById(long taskId) {
        return taskDao.getTaskById(taskId);
    }
}
