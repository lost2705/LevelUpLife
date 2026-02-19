package com.example.leveluplife.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "achievements")
public class Achievement {
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @PrimaryKey
    public int id;

    public String title;
    public String description;
    public String icon;

    public int rewardXp;
    public int rewardGold;

    public boolean unlocked;
    public long unlockedAt;

    public Achievement() {}

    public Achievement(int id, String title, String description,
                       String icon, int xp, int gold) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.icon = icon;
        this.rewardXp = xp;
        this.rewardGold = gold;
        this.unlocked = false;
        this.unlockedAt = 0;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public int getRewardXp() { return rewardXp; }
    public void setRewardXp(int rewardXp) { this.rewardXp = rewardXp; }

    public int getRewardGold() { return rewardGold; }
    public void setRewardGold(int rewardGold) { this.rewardGold = rewardGold; }

    public boolean isUnlocked() { return unlocked; }
    public void setUnlocked(boolean unlocked) { this.unlocked = unlocked; }

    public long getUnlockedAt() { return unlockedAt; }
    public void setUnlockedAt(long unlockedAt) { this.unlockedAt = unlockedAt; }
}
