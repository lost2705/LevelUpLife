package com.example.leveluplife.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;
import java.time.LocalDateTime;

@Entity(tableName = "tasks")
public class Task {

    public enum TaskType {
        DAILY, WEEKLY, ONE_TIME
    }

    public enum AttributeType {
        STRENGTH, AGILITY, INTELLIGENCE, CHARISMA
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    @PrimaryKey(autoGenerate = true)
    public long id;

    public String title;

    public String description;

    @ColumnInfo(name = "task_type")
    public TaskType taskType;

    @ColumnInfo(name = "attribute_type")
    public AttributeType attributeType;

    @ColumnInfo(name = "xp_reward")
    public int xpReward;

    @ColumnInfo(name = "gold_reward")
    public int goldReward;

    public boolean completed;

    public Task() {}

    public Task(String title, TaskType taskType, AttributeType attributeType,
                int xpReward, int goldReward) {
        this.title = title;
        this.taskType = taskType;
        this.attributeType = attributeType;
        this.xpReward = xpReward;
        this.goldReward = goldReward;
        this.completed = false;
    }
}
