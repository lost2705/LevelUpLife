package com.example.leveluplife.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "dungeon_state")
public class DungeonState {

    @PrimaryKey
    public int id = 1;

    @NonNull
    private String status; // IDLE, IN_PROGRESS, VICTORY, DEFEAT, COOLDOWN

    private long startedAt;
    private long finishedAt;
    private long cooldownUntil;

    private int playerCurrentHp;
    private int playerCurrentMana;

    @NonNull
    private String enemyName;
    private int enemyCurrentHp;
    private int enemyMaxHp;

    private int turnNumber;

    private int rewardXp;
    private int rewardGold;

    public DungeonState() {
        this.status = "IDLE";
        this.startedAt = 0L;
        this.finishedAt = 0L;
        this.cooldownUntil = 0L;
        this.playerCurrentHp = 0;
        this.playerCurrentMana = 0;
        this.enemyName = "";
        this.enemyCurrentHp = 0;
        this.enemyMaxHp = 0;
        this.turnNumber = 1;
        this.rewardXp = 0;
        this.rewardGold = 0;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @NonNull
    public String getStatus() {
        return status;
    }

    public void setStatus(@NonNull String status) {
        this.status = status;
    }

    public long getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(long startedAt) {
        this.startedAt = startedAt;
    }

    public long getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(long finishedAt) {
        this.finishedAt = finishedAt;
    }

    public long getCooldownUntil() {
        return cooldownUntil;
    }

    public void setCooldownUntil(long cooldownUntil) {
        this.cooldownUntil = cooldownUntil;
    }

    public int getPlayerCurrentHp() {
        return playerCurrentHp;
    }

    public void setPlayerCurrentHp(int playerCurrentHp) {
        this.playerCurrentHp = playerCurrentHp;
    }

    public int getPlayerCurrentMana() {
        return playerCurrentMana;
    }

    public void setPlayerCurrentMana(int playerCurrentMana) {
        this.playerCurrentMana = playerCurrentMana;
    }

    @NonNull
    public String getEnemyName() {
        return enemyName;
    }

    public void setEnemyName(@NonNull String enemyName) {
        this.enemyName = enemyName;
    }

    public int getEnemyCurrentHp() {
        return enemyCurrentHp;
    }

    public void setEnemyCurrentHp(int enemyCurrentHp) {
        this.enemyCurrentHp = enemyCurrentHp;
    }

    public int getEnemyMaxHp() {
        return enemyMaxHp;
    }

    public void setEnemyMaxHp(int enemyMaxHp) {
        this.enemyMaxHp = enemyMaxHp;
    }

    public int getTurnNumber() {
        return turnNumber;
    }

    public void setTurnNumber(int turnNumber) {
        this.turnNumber = turnNumber;
    }

    public int getRewardXp() {
        return rewardXp;
    }

    public void setRewardXp(int rewardXp) {
        this.rewardXp = rewardXp;
    }

    public int getRewardGold() {
        return rewardGold;
    }

    public void setRewardGold(int rewardGold) {
        this.rewardGold = rewardGold;
    }
}