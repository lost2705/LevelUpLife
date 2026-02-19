package com.example.leveluplife.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.leveluplife.data.entity.Achievement;

import java.util.List;

@Dao
public interface AchievementDao {

    @Query("SELECT * FROM achievements")
    List<Achievement> getAchievementsForIndexing();

    @Query("SELECT * FROM achievements ORDER BY id")
    LiveData<List<Achievement>> getAllAchievements();

    @Query("SELECT * FROM achievements WHERE unlocked = 1 ORDER BY unlockedAt DESC LIMIT 5")
    List<Achievement> getRecentAchievements();

    @Query("SELECT COUNT(*) FROM achievements WHERE unlocked = 1")
    int getUnlockedCount();

    @Query("SELECT COUNT(*) FROM achievements")
    int getTotalCount();

    @Query("SELECT * FROM achievements WHERE id = :id")
    Achievement getAchievementById(int id);

    @Update
    void updateAchievement(Achievement achievement);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAchievements(Achievement... achievements);

    @Query("SELECT * FROM achievements ORDER BY id")
    List<Achievement> getAllAchievementsSync();
}
