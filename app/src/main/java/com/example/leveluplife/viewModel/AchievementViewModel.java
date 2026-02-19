package com.example.leveluplife.viewModel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.leveluplife.data.dao.AchievementDao;
import com.example.leveluplife.data.database.AppDatabase;
import com.example.leveluplife.data.entity.Achievement;

import java.util.List;

public class AchievementViewModel extends AndroidViewModel {
    private final AppDatabase database;
    private final LiveData<List<Achievement>> allAchievements;
    private final AchievementDao achievementDao;

    public AchievementViewModel(@NonNull Application application) {
        super(application);
        database = AppDatabase.getDatabase(application);
        achievementDao = database.achievementDao();
        allAchievements = achievementDao.getAllAchievements();
    }

    public LiveData<List<Achievement>> getAllAchievements() {
        return allAchievements;
    }

    public int getUnlockedCount() {
        return achievementDao.getUnlockedCount();
    }

    public int getTotalCount() {
        return achievementDao.getTotalCount();
    }

    public List<Achievement> getRecentAchievements() {
        return achievementDao.getRecentAchievements();
    }

    public Achievement getAchievementById(int id) {
        return achievementDao.getAchievementById(id);
    }

    public void updateAchievement(Achievement achievement) {
        new Thread(() -> achievementDao.updateAchievement(achievement)).start();
    }
}
