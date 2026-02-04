package com.example.leveluplife.data.model;

public class LevelUpEvent {
    public final int newLevel;
    public final int talentPoints;
    public final int oldMaxHp;
    public final int newMaxHp;
    public final int oldMaxMana;
    public final int newMaxMana;

    public LevelUpEvent(int newLevel, int talentPoints,
                        int oldMaxHp, int newMaxHp,
                        int oldMaxMana, int newMaxMana) {
        this.newLevel = newLevel;
        this.talentPoints = talentPoints;
        this.oldMaxHp = oldMaxHp;
        this.newMaxHp = newMaxHp;
        this.oldMaxMana = oldMaxMana;
        this.newMaxMana = newMaxMana;
    }

    public int getHpGain() {
        return newMaxHp - oldMaxHp;
    }

    public int getManaGain() {
        return newMaxMana - oldMaxMana;
    }
}
