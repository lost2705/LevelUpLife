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

    private TaskDao taskDao;
    private LiveData<List<Task>> allTasks;
    private ExecutorService executor = Executors.newFixedThreadPool(4);

    public TaskRepository(Application application) {
        AppDatabase database = AppDatabase.getDatabase(application);
        taskDao = database.taskDao();
        allTasks = taskDao.getAllTasksLiveData();
    }

    public LiveData<List<Task>> getAllTasks() {
        return allTasks;
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

    public LiveData<Integer> getTotalXp() {
        return taskDao.getTotalXp();
    }

    public LiveData<Integer> getCompletedTasksCount() {
        return taskDao.getCompletedTasksCount();
    }

    public LiveData<Integer> getPendingTasksCount() {
        return taskDao.getPendingTasksCount();
    }

    public Task getTaskById(long taskId) {
        return taskDao.getTaskById(taskId);
    }
}
