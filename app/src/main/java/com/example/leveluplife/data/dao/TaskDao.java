package com.example.leveluplife.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.leveluplife.data.entity.Task;

import java.util.List;

@Dao
public interface TaskDao {

    @Query("SELECT * FROM tasks ORDER BY id DESC")
    LiveData<List<Task>> getAllTasksLiveData();

    @Query("SELECT * FROM tasks WHERE completed = 0 ORDER BY id ASC")
    LiveData<List<Task>> getPendingTasksLiveData();

    @Query("SELECT * FROM tasks ORDER BY id DESC")
    List<Task> getAllTasks();

    @Query("SELECT * FROM tasks WHERE id = :taskId")
    Task getTaskById(long taskId);

    @Insert
    void insertTask(Task task);

    @Update
    void updateTask(Task task);

    @Delete
    void deleteTask(Task task);

    @Query("SELECT * FROM tasks WHERE frequency = :frequency")
    LiveData<List<Task>> getTasksByFrequency(String frequency);

    @Query("UPDATE tasks SET completed = 0, rewardClaimed = 0, lastUpdated = :timestamp WHERE frequency = 'DAILY'")
    void resetDailyTasks(long timestamp);

    @Query("UPDATE tasks SET completed = 0, rewardClaimed = 0, lastUpdated = :timestamp WHERE frequency = 'WEEKLY'")
    void resetWeeklyTasks(long timestamp);

    @Query("SELECT * FROM tasks WHERE frequency = 'DAILY' AND completed = 1")
    List<Task> getCompletedDailyTasks();

    @Query("SELECT * FROM tasks WHERE frequency = 'WEEKLY' AND completed = 1")
    List<Task> getCompletedWeeklyTasks();

    @Query("SELECT * FROM tasks WHERE frequency = :frequency")
    List<Task> getTasksByFrequencySync(String frequency);
}
