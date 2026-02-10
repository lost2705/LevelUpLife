package com.example.leveluplife.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.leveluplife.data.entity.CompletedTask;

import java.util.List;

@Dao
public interface CompletedTaskDao {

    @Insert
    void insertCompletedTask(CompletedTask task);

    @Query("SELECT * FROM completed_tasks ORDER BY completedAt DESC")
    LiveData<List<CompletedTask>> getAllCompletedTasks();

    @Query("SELECT * FROM completed_tasks WHERE completedAt >= :startDate ORDER BY completedAt DESC")
    List<CompletedTask> getCompletedTasksSince(long startDate);

    @Query("SELECT COUNT(*) FROM completed_tasks WHERE completedAt >= :startDate")
    int getCompletedTasksCountSince(long startDate);

    @Query("DELETE FROM completed_tasks WHERE completedAt < :beforeDate")
    void deleteOldCompletedTasks(long beforeDate);
}
