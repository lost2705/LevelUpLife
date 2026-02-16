package com.example.leveluplife.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.leveluplife.data.dao.CompletedTaskDao;
import com.example.leveluplife.data.database.AppDatabase;
import com.example.leveluplife.data.entity.CompletedTask;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CompletedTaskRepository {
    private final CompletedTaskDao completedTaskDao;
    private final ExecutorService executor;

    public CompletedTaskRepository(Application application) {
        AppDatabase database = AppDatabase.getDatabase(application);
        completedTaskDao = database.completedTaskDao();
        executor = Executors.newSingleThreadExecutor();
    }

    public void insert(CompletedTask completedTask) {
        executor.execute(() -> completedTaskDao.insertCompletedTask(completedTask));
    }

    public LiveData<List<CompletedTask>> getAllCompletedTasks() {
        return completedTaskDao.getAllCompletedTasks();
    }

    public List<CompletedTask> getCompletedTasksSince(long startDate) {
        return completedTaskDao.getCompletedTasksSince(startDate);
    }

    public int getCompletedTasksCountSince(long startDate) {
        return completedTaskDao.getCompletedTasksCountSince(startDate);
    }

    public void deleteOldCompletedTasks(long beforeDate) {
        executor.execute(() -> completedTaskDao.deleteOldCompletedTasks(beforeDate));
    }
}
