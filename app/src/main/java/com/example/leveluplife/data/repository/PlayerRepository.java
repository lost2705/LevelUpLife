package com.example.leveluplife.data.repository;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.leveluplife.data.dao.PlayerDao;
import com.example.leveluplife.data.database.AppDatabase;
import com.example.leveluplife.data.entity.Player;
import com.example.leveluplife.data.model.LevelUpEvent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PlayerRepository {
    private static final String TAG = "PlayerRepository";

    private static PlayerRepository instance;
    private final PlayerDao playerDao;
    private final LiveData<Player> player;
    private final ExecutorService executor;
    private final MutableLiveData<LevelUpEvent> levelUpEvent;

    private PlayerRepository(Application application) {
        AppDatabase database = AppDatabase.getDatabase(application);
        playerDao = database.playerDao();
        player = playerDao.getPlayer();
        executor = Executors.newSingleThreadExecutor();
        levelUpEvent = new MutableLiveData<>();
    }

    public static synchronized PlayerRepository getInstance(Application application) {
        if (instance == null) {
            instance = new PlayerRepository(application);
        }
        return instance;
    }

    public LiveData<Player> getPlayer() {
        return player;
    }

    public Player getPlayerSync() {
        return playerDao.getPlayerSync();
    }

    public void updatePlayer(Player player) {
        executor.execute(() -> playerDao.updatePlayer(player));
    }

    public void insertPlayer(Player player) {
        executor.execute(() -> playerDao.insertPlayer(player));
    }

    public void initializePlayerIfNeeded() {
        executor.execute(() -> {
            Player existingPlayer = playerDao.getPlayerSync();
            if (existingPlayer == null) {
                Player newPlayer = new Player();
                playerDao.insertPlayer(newPlayer);
            }
        });
    }

    public void addXp(int baseXp) {
        executor.execute(() -> {
            Player currentPlayer = playerDao.getPlayerSync();
            if (currentPlayer != null) {
                int penalty = currentPlayer.getXpPenalty();
                int actualXp = baseXp * (100 - penalty) / 100;

                Log.d(TAG, "💰 XP Reward:");
                Log.d(TAG, "   Base XP: " + baseXp);
                if (penalty > 0) {
                    Log.d(TAG, "   ⚠️ Penalty: -" + penalty + "%");
                    Log.d(TAG, "   Actual XP: " + actualXp + " (reduced by " + (baseXp - actualXp) + ")");
                } else {
                    Log.d(TAG, "   Actual XP: " + actualXp);
                }

                currentPlayer.setCurrentXp(currentPlayer.getCurrentXp() + actualXp);

                while (currentPlayer.getCurrentXp() >= currentPlayer.getXpToNextLevel()) {
                    int oldMaxHp = currentPlayer.getMaxHp();
                    int oldMaxMana = currentPlayer.getMaxMana();

                    currentPlayer.setCurrentXp(currentPlayer.getCurrentXp() - currentPlayer.getXpToNextLevel());
                    currentPlayer.setLevel(currentPlayer.getLevel() + 1);
                    currentPlayer.setXpToNextLevel((int) (currentPlayer.getXpToNextLevel() * 1.5));
                    currentPlayer.setMaxHp(currentPlayer.getMaxHp() + 10);
                    currentPlayer.setMaxMana(currentPlayer.getMaxMana() + 5);
                    currentPlayer.setTalentPoints(currentPlayer.getTalentPoints() + 1);

                    Log.d(TAG, "🎊 LEVEL UP! New level: " + currentPlayer.getLevel());

                    LevelUpEvent event = new LevelUpEvent(
                            currentPlayer.getLevel(),
                            currentPlayer.getTalentPoints(),
                            oldMaxHp,
                            currentPlayer.getMaxHp(),
                            oldMaxMana,
                            currentPlayer.getMaxMana()
                    );
                    levelUpEvent.postValue(event);
                }

                // Update database
                playerDao.updatePlayer(currentPlayer);
            }
        });
    }

    public void subtractXp(long xp) {
        executor.execute(() -> {
            Player currentPlayer = playerDao.getPlayerSync();
            if (currentPlayer != null) {
                long newXp = Math.max(0, currentPlayer.getCurrentXp() - xp);
                currentPlayer.setCurrentXp(newXp);
                playerDao.updatePlayer(currentPlayer);
            }
        });
    }

    public void addGold(int amount) {
        executor.execute(() -> {
            Player currentPlayer = playerDao.getPlayerSync();
            if (currentPlayer != null) {
                currentPlayer.setGold(currentPlayer.getGold() + amount);
                playerDao.updatePlayer(currentPlayer);
            }
        });
    }

    public void subtractGold(int amount) {
        executor.execute(() -> {
            Player currentPlayer = playerDao.getPlayerSync();
            if (currentPlayer != null) {
                int newGold = Math.max(0, currentPlayer.getGold() - amount);
                currentPlayer.setGold(newGold);
                playerDao.updatePlayer(currentPlayer);
            }
        });
    }

    public void removeXp(int amount) {
        executor.execute(() -> {
            Player currentPlayer = playerDao.getPlayerSync();
            if (currentPlayer != null) {
                int newXp = (int) (currentPlayer.getCurrentXp() - amount);
                if (newXp < 0) newXp = 0;

                currentPlayer.setCurrentXp(newXp);
                playerDao.updatePlayer(currentPlayer);
            }
        });
    }

    public void removeGold(int amount) {
        executor.execute(() -> {
            Player currentPlayer = playerDao.getPlayerSync();
            if (currentPlayer != null) {
                int newGold = currentPlayer.getGold() - amount;
                if (newGold < 0) newGold = 0;

                currentPlayer.setGold(newGold);
                playerDao.updatePlayer(currentPlayer);
            }
        });
    }

    public void addGems(int amount) {
        executor.execute(() -> {
            Player currentPlayer = playerDao.getPlayerSync();
            if (currentPlayer != null) {
                currentPlayer.setGems(currentPlayer.getGems() + amount);
                playerDao.updatePlayer(currentPlayer);
            }
        });
    }

    public LiveData<LevelUpEvent> getLevelUpEvent() {
        return levelUpEvent;
    }

    public void triggerLevelUpEvent(LevelUpEvent event) {
        levelUpEvent.postValue(event);
    }

    private long calculateXpForNextLevel(int level) {
        return 100 + (level * 50);
    }
}
