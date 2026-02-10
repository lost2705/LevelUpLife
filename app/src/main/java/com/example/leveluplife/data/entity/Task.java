package com.example.leveluplife.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "tasks")
public class Task {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String title;
    private TaskType taskType;
    private AttributeType attributeType;
    private int xpReward;
    private int goldReward;
    private boolean completed;
    private String frequency;
    private long lastUpdated;

    // ✅ Enum'ы
    public enum TaskType {
        DAILY,
        TODO,
        HABIT
    }

    public enum AttributeType {
        STRENGTH,
        INTELLIGENCE,
        DEXTERITY,
        CONSTITUTION
    }

    public Task(String title, TaskType taskType, AttributeType attributeType, int xpReward, int goldReward) {
        this.title = title;
        this.taskType = taskType;
        this.attributeType = attributeType;
        this.xpReward = xpReward;
        this.goldReward = goldReward;
        this.completed = false;
        this.frequency = taskType.name();
        this.lastUpdated = System.currentTimeMillis();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public TaskType getTaskType() {
        return taskType;
    }

    public void setTaskType(TaskType taskType) {
        this.taskType = taskType;
    }

    public AttributeType getAttributeType() {
        return attributeType;
    }

    public void setAttributeType(AttributeType attributeType) {
        this.attributeType = attributeType;
    }

    public int getXpReward() {
        return xpReward;
    }

    public void setXpReward(int xpReward) {
        this.xpReward = xpReward;
    }

    public int getGoldReward() {
        return goldReward;
    }

    public void setGoldReward(int goldReward) {
        this.goldReward = goldReward;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}
