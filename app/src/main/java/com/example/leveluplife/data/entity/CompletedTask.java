package com.example.leveluplife.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "completed_tasks")
public class CompletedTask {
    @PrimaryKey(autoGenerate = true)
    private long id;

    private long taskId;
    private String taskTitle;
    private int xpEarned;
    private int goldEarned;
    private long completedAt;
    private String frequency;

    public CompletedTask(long taskId, String taskTitle, int xpEarned, int goldEarned, String frequency) {
        this.taskId = taskId;
        this.taskTitle = taskTitle;
        this.xpEarned = xpEarned;
        this.goldEarned = goldEarned;
        this.completedAt = System.currentTimeMillis();
        this.frequency = frequency;
    }

    // Getters и Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getTaskId() { return taskId; }
    public void setTaskId(long taskId) { this.taskId = taskId; }

    public String getTaskTitle() { return taskTitle; }
    public void setTaskTitle(String taskTitle) { this.taskTitle = taskTitle; }

    public int getXpEarned() { return xpEarned; }
    public void setXpEarned(int xpEarned) { this.xpEarned = xpEarned; }

    public int getGoldEarned() { return goldEarned; }
    public void setGoldEarned(int goldEarned) { this.goldEarned = goldEarned; }

    public long getCompletedAt() { return completedAt; }
    public void setCompletedAt(long completedAt) { this.completedAt = completedAt; }

    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }
}
