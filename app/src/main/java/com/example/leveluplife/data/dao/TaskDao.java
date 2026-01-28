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
    Task getTaskById(long taskId);  // ← ДОБАВЬ ЭТУ СТРОКУ!

    @Query("SELECT COALESCE(SUM(xp_reward), 0) FROM tasks WHERE completed = 1")
    LiveData<Integer> getTotalXp();

    @Query("SELECT COUNT(*) FROM tasks WHERE completed = 1")
    LiveData<Integer> getCompletedTasksCount();

    @Query("SELECT COUNT(*) FROM tasks WHERE completed = 0")
    LiveData<Integer> getPendingTasksCount();

    @Insert
    void insertTask(Task task);

    @Update
    void updateTask(Task task);

    @Delete
    void deleteTask(Task task);

    @Query("DELETE FROM tasks")
    void deleteAll();
}

