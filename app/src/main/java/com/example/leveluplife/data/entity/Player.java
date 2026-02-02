package com.example.leveluplife.data.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "player")
public class Player {

    @PrimaryKey
    public long id;

    public int level;

    @ColumnInfo(name = "current_xp")
    public long currentXp;

    @ColumnInfo(name = "xp_to_next_level")
    public long xpToNextLevel;

    public int gold;
    public int gems;

    public int strength;
    public int agility;
    public int intelligence;
    public int charisma;

    @ColumnInfo(name = "max_hp")
    public int maxHp;

    @ColumnInfo(name = "current_hp")
    public int currentHp;

    @ColumnInfo(name = "max_mana")
    public int maxMana;

    @ColumnInfo(name = "current_mana")
    public int currentMana;

    @ColumnInfo(name = "current_streak")
    public int currentStreak;

    @ColumnInfo(name = "last_login_date")
    public long lastLoginDate;

    @ColumnInfo(name = "available_talent_points")
    public int availableTalentPoints;

    @ColumnInfo(name = "created_at")
    public long createdAt;

    @ColumnInfo(name = "last_updated")
    public long lastUpdated;

    public Player() {
        this.id = 1;
        this.level = 1;
        this.currentXp = 0;
        this.xpToNextLevel = calculateXpForLevel(2);

        this.gold = 100;
        this.gems = 0;

        this.strength = 10;
        this.agility = 10;
        this.intelligence = 10;
        this.charisma = 10;

        this.maxHp = calculateMaxHp();
        this.currentHp = this.maxHp;
        this.maxMana = calculateMaxMana();
        this.currentMana = this.maxMana;

        this.currentStreak = 0;
        this.lastLoginDate = System.currentTimeMillis();

        this.availableTalentPoints = 0;

        this.createdAt = System.currentTimeMillis();
        this.lastUpdated = System.currentTimeMillis();
    }

    public static long calculateXpForLevel(int level) {
        int baseXp = 100;
        return (long) (baseXp * Math.pow(level, 1.5));
    }

    public int calculateMaxHp() {
        return 100 + (this.strength * 5);
    }

    public int calculateMaxMana() {
        return 50 + (this.intelligence * 3);
    }

    public boolean addXp(long xp) {
        this.currentXp += xp;

        boolean leveledUp = false;

        while (this.currentXp >= this.xpToNextLevel && this.level < 100) {
            this.currentXp -= this.xpToNextLevel;
            this.level++;
            this.xpToNextLevel = calculateXpForLevel(this.level + 1);

            this.availableTalentPoints++;

            leveledUp = true;
        }

        if (this.level >= 100) {
            this.level = 100;
            this.currentXp = 0;
            this.xpToNextLevel = 0;
        }

        this.lastUpdated = System.currentTimeMillis();
        return leveledUp;
    }

    public void recalculateStats() {
        int oldMaxHp = this.maxHp;
        int oldMaxMana = this.maxMana;

        this.maxHp = calculateMaxHp();
        this.maxMana = calculateMaxMana();

        if (oldMaxHp > 0) {
            float hpPercent = (float) this.currentHp / oldMaxHp;
            this.currentHp = (int) (this.maxHp * hpPercent);
        } else {
            this.currentHp = this.maxHp;
        }

        if (oldMaxMana > 0) {
            float manaPercent = (float) this.currentMana / oldMaxMana;
            this.currentMana = (int) (this.maxMana * manaPercent);
        } else {
            this.currentMana = this.maxMana;
        }
    }

    public float getLevelProgress() {
        if (this.xpToNextLevel == 0) return 1.0f;
        return (float) this.currentXp / this.xpToNextLevel;
    }
}
