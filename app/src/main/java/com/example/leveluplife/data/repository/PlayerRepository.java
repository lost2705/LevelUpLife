package com.example.leveluplife.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.leveluplife.data.dao.PlayerDao;
import com.example.leveluplife.data.database.AppDatabase;
import com.example.leveluplife.data.entity.Player;
import com.example.leveluplife.data.model.LevelUpEvent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PlayerRepository {
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

    public void addXp(int xp) {
        executor.execute(() -> {
            Player player = playerDao.getPlayerSync();
            if (player != null) {
                player.currentXp += xp;
                playerDao.updatePlayer(player);
            }
        });
    }

    public void subtractXp(long xp) {
        executor.execute(() -> {
            Player player = playerDao.getPlayerSync();
            if (player != null) {
                player.currentXp = Math.max(0, player.currentXp - xp);
                playerDao.updatePlayer(player);
            }
        });
    }

    public void addGold(int amount) {
        executor.execute(() -> {
            Player player = playerDao.getPlayerSync();
            if (player != null) {
                player.gold += amount;
                playerDao.updatePlayer(player);
            }
        });
    }

    public void subtractGold(int amount) {
        executor.execute(() -> {
            Player player = playerDao.getPlayerSync();
            if (player != null) {
                player.gold = Math.max(0, player.gold - amount);
                playerDao.updatePlayer(player);
            }
        });
    }

    public void addGems(int amount) {
        executor.execute(() -> {
            Player player = playerDao.getPlayerSync();
            if (player != null) {
                player.gems += amount;
                playerDao.updatePlayer(player);
            }
        });
    }

    public LiveData<LevelUpEvent> getLevelUpEvent() {
        return levelUpEvent;
    }

    public void triggerLevelUpEvent(LevelUpEvent event) {
        levelUpEvent.postValue(event);
    }
}
