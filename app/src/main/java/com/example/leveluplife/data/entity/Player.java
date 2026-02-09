package com.example.leveluplife.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "player")
public class Player {
    @PrimaryKey
    public int id = 1;

    public int level;
    public long currentXp;
    public long xpToNextLevel;
    public int gold;
    public int gems;

    // HP и Mana
    public int maxHp;
    public int currentHp;
    public int maxMana;
    public int currentMana;

    public int strength;
    public int intelligence;
    public int dexterity;
    public int talentPoints;

    // Конструктор
    public Player() {
        this.level = 1;
        this.currentXp = 0;
        this.xpToNextLevel = 100;
        this.gold = 0;
        this.gems = 0;

        this.maxHp = 100;
        this.currentHp = 100;
        this.maxMana = 50;
        this.currentMana = 50;

        this.strength = 5;
        this.intelligence = 5;
        this.dexterity = 5;
        this.talentPoints = 0;
    }
}
