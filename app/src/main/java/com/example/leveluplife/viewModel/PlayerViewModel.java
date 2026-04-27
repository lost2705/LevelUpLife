package com.example.leveluplife.viewModel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.leveluplife.data.dao.AchievementDao;
import com.example.leveluplife.data.dao.CompletedTaskDao;
import com.example.leveluplife.data.database.AppDatabase;
import com.example.leveluplife.data.entity.Achievement;
import com.example.leveluplife.data.entity.Player;
import com.example.leveluplife.data.model.LevelUpEvent;
import com.example.leveluplife.data.repository.PlayerRepository;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PlayerViewModel extends AndroidViewModel {

    private final PlayerRepository repository;
    private final ExecutorService executor;
    private final AchievementDao achievementDao;
    private final CompletedTaskDao completedTaskDao;

    private final MutableLiveData<Achievement> achievementUnlockEvent = new MutableLiveData<>();

    public PlayerViewModel(@NonNull Application application) {
        super(application);
        repository = PlayerRepository.getInstance(application);
        executor = Executors.newSingleThreadExecutor();

        AppDatabase db = AppDatabase.getDatabase(application);
        achievementDao = db.achievementDao();
        completedTaskDao = db.completedTaskDao();
    }

    public LiveData<Player> getPlayer() { return repository.getPlayer(); }
    public void updatePlayer(Player player) { repository.updatePlayer(player); }
    public void removeXp(int amount) { repository.removeXp(amount); }
    public void addGems(int amount) { repository.addGems(amount); }
    public void subtractXp(long xp) { repository.subtractXp(xp); }
    public void subtractGold(int amount) { repository.subtractGold(amount); }
    public void initializePlayerIfNeeded() { repository.initializePlayerIfNeeded(); }
    public LiveData<LevelUpEvent> getLevelUpEvent() { return repository.getLevelUpEvent(); }

    public void addXp(int xp) {
        repository.addXp(xp);
        checkAchievements(); // 👈 проверяем после каждого XP
    }

    public void addGold(int amount) {
        repository.addGold(amount);
        checkAchievements();
    }

    public void removeGold(int amount) {
        repository.removeGold(amount);
    }

    public void updateTalents(int strength, int intelligence, int dexterity, int talentPoints) {
        executor.execute(() -> {
            Player player = repository.getPlayerSync();
            if (player != null) {
                player.setStrength(strength);
                player.setIntelligence(intelligence);
                player.setDexterity(dexterity);
                player.setTalentPoints(talentPoints);

                int newMaxHp   = 100 + (strength * 10);
                int newMaxMana = 50  + (intelligence * 5);
                player.setMaxHp(newMaxHp);
                player.setMaxMana(newMaxMana);

                if (player.getCurrentHp()   < newMaxHp)   player.setCurrentHp(newMaxHp);
                if (player.getCurrentMana() < newMaxMana) player.setCurrentMana(newMaxMana);

                repository.updatePlayer(player);
                checkAchievements();
            }
        });
    }

    public LiveData<Achievement> getAchievementUnlockEvent() {
        return achievementUnlockEvent;
    }

    public void checkAchievements() {
        executor.execute(() -> {
            try {
                List<Achievement> achievements = achievementDao.getAllAchievementsSync();
                if (achievements == null || achievements.isEmpty()) return;

                Player player = repository.getPlayerSync();
                if (player == null) return;

                int totalCompleted = completedTaskDao.getTotalCompletedCount();
                int dailyCompleted = completedTaskDao.getDailyCompletedCount();

                for (Achievement ach : achievements) {
                    if (!ach.unlocked && shouldUnlock(ach, player, totalCompleted, dailyCompleted)) {
                        unlockAchievement(ach);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private boolean shouldUnlock(Achievement ach, Player player,
                                 int totalCompleted, int dailyCompleted) {
        switch (ach.id) {
            case 1:  // First Blood
                return totalCompleted >= 1;
            case 2:  // Daily Routine
                return dailyCompleted >= 7;
            case 3:  // Level 5
                return player.getLevel() >= 5;
            case 4:  // Gold Rush
                return player.getGold() >= 1000;
            case 5:  // Task Master
                return totalCompleted >= 50;
            case 6:  // Perfect Day
                return dailyCompleted >= 20;
            case 7:  // Level Master
                return player.getLevel() >= 10;
            case 8:  // Wealthy
                return player.getGold() >= 5000;
            case 9:  // Strength Builder — 10 points in strength
                return player.getStrength() >= 10;
            case 10:
                return player.getStrength()     > 5
                        && player.getIntelligence() > 5
                        && player.getDexterity()    > 5;
            case 11: // Destiny's Call — любой класс выбран
                return player.getHeroClass() != null;

            case 12: // Iron Will — Warrior
                return "Warrior".equals(player.getHeroClass());

            case 13: // Ancient Knowledge — Mage
                return "Mage".equals(player.getHeroClass());

            case 14: // Shadow Step — Ranger
                return "Ranger".equals(player.getHeroClass());
            default:
                return false;
        }
    }


    private void unlockAchievement(Achievement ach) {
        ach.unlocked    = true;
        ach.unlockedAt  = System.currentTimeMillis();
        achievementDao.updateAchievement(ach);

        repository.addXp(ach.rewardXp);
        repository.addGold(ach.rewardGold);

        achievementUnlockEvent.postValue(ach);
    }

    public void setHeroName(String name) {
        executor.execute(() -> {
            Player player = repository.getPlayerSync();
            if (player == null) {
                player = new Player();
                player.setHeroName(name);
                repository.insertPlayer(player);
            } else {
                player.setHeroName(name);
                repository.updatePlayer(player);
            }
        });
    }

    public void setHeroClass(String heroClass) {
        executor.execute(() -> {
            Player player = repository.getPlayerSync();
            if (player != null) {
                player.setHeroClass(heroClass);

                switch (heroClass) {
                    case "Warrior":
                        player.setMaxHp((int)(player.getMaxHp() * 1.2));
                        player.setCurrentHp(player.getMaxHp());
                        player.setStrength(player.getStrength() + 3);
                        break;
                    case "Mage":
                        player.setMaxMana((int)(player.getMaxMana() * 1.2));
                        player.setCurrentMana(player.getMaxMana());
                        player.setIntelligence(player.getIntelligence() + 3);
                        break;
                    case "Ranger":
                        player.setDexterity((int)(player.getDexterity() * 1.2));
                        break;
                }
                repository.updatePlayer(player);
                checkAchievements();
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdown();
    }
}
