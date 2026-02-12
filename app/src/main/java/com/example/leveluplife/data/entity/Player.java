package com.example.leveluplife.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "player")
public class Player {
    @PrimaryKey
    private int id = 1;

    private int level;
    private long currentXp;
    private long xpToNextLevel;
    private int gold;
    private int gems;

    private int maxHp;
    private int currentHp;
    private int maxMana;
    private int currentMana;

    private int strength;
    private int intelligence;
    private int dexterity;
    private int talentPoints;

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

    public int getId() {
        return id;
    }

    public int getLevel() {
        return level;
    }

    public long getCurrentXp() {
        return currentXp;
    }

    public long getXpToNextLevel() {
        return xpToNextLevel;
    }

    public int getGold() {
        return gold;
    }

    public int getGems() {
        return gems;
    }

    public int getMaxHp() {
        return maxHp;
    }

    public int getCurrentHp() {
        return currentHp;
    }

    public int getMaxMana() {
        return maxMana;
    }

    public int getCurrentMana() {
        return currentMana;
    }

    public int getStrength() {
        return strength;
    }

    public int getIntelligence() {
        return intelligence;
    }

    public int getDexterity() {
        return dexterity;
    }

    public int getTalentPoints() {
        return talentPoints;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void setCurrentXp(long currentXp) {
        this.currentXp = currentXp;
    }

    public void setXpToNextLevel(long xpToNextLevel) {
        this.xpToNextLevel = xpToNextLevel;
    }

    public void setGold(int gold) {
        this.gold = gold;
    }

    public void setGems(int gems) {
        this.gems = gems;
    }

    public void setMaxHp(int maxHp) {
        this.maxHp = maxHp;
    }

    public void setCurrentHp(int currentHp) {
        this.currentHp = currentHp;
    }

    public void setMaxMana(int maxMana) {
        this.maxMana = maxMana;
    }

    public void setCurrentMana(int currentMana) {
        this.currentMana = currentMana;
    }

    public void setStrength(int strength) {
        this.strength = strength;
    }

    public void setIntelligence(int intelligence) {
        this.intelligence = intelligence;
    }

    public void setDexterity(int dexterity) {
        this.dexterity = dexterity;
    }

    public void setTalentPoints(int talentPoints) {
        this.talentPoints = talentPoints;
    }
}