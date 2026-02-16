package com.example.leveluplife.viewModel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.leveluplife.data.entity.CompletedTask;
import com.example.leveluplife.data.repository.CompletedTaskRepository;

import java.util.List;

public class CompletedTaskViewModel extends AndroidViewModel {
    private final CompletedTaskRepository repository;
    private final LiveData<List<CompletedTask>> allCompletedTasks;

    public CompletedTaskViewModel(@NonNull Application application) {
        super(application);
        repository = new CompletedTaskRepository(application);
        allCompletedTasks = repository.getAllCompletedTasks();
    }

    public void insert(CompletedTask completedTask) {
        repository.insert(completedTask);
    }

    public LiveData<List<CompletedTask>> getAllCompletedTasks() {
        return allCompletedTasks;
    }

    public List<CompletedTask> getCompletedTasksSince(long startDate) {
        return repository.getCompletedTasksSince(startDate);
    }

    public int getCompletedTasksCountSince(long startDate) {
        return repository.getCompletedTasksCountSince(startDate);
    }

    public void deleteOldCompletedTasks(long beforeDate) {
        repository.deleteOldCompletedTasks(beforeDate);
    }
}
