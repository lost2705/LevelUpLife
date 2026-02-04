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

    private final PlayerDao playerDao;
    private final LiveData<Player> player;
    private final ExecutorService executor;
    private final MutableLiveData<LevelUpEvent> levelUpEventLiveData = new MutableLiveData<>();

    private static PlayerRepository instance;

    public static synchronized PlayerRepository getInstance(Application application) {
        if (instance == null) {
            instance = new PlayerRepository(application);
        }
        return instance;
    }

    private PlayerRepository(Application application) {
        AppDatabase database = AppDatabase.getDatabase(application);
        playerDao = database.playerDao();
        player = playerDao.getPlayer();
        executor = Executors.newFixedThreadPool(2);
    }

    public LiveData<Player> getPlayer() {
        return player;
    }

    public void updatePlayer(final Player player) {
        executor.execute(() -> {
            player.lastUpdated = System.currentTimeMillis();
            playerDao.updatePlayer(player);
        });
    }

    public void addXp(final long xp) {
        executor.execute(() -> {
            Player p = playerDao.getPlayerSync();
            if (p != null) {
                LevelUpEvent levelUpEvent = p.addXp(xp);
                playerDao.updatePlayer(p);

                if (levelUpEvent != null) {
                    levelUpEventLiveData.postValue(levelUpEvent);
                }
            }
        });
    }

    public void subtractXp(final long xp) {
        executor.execute(() -> {
            Player p = playerDao.getPlayerSync();
            if (p != null) {
                p.currentXp = Math.max(0, p.currentXp - xp);

                playerDao.updatePlayer(p);
            }
        });
    }

    public void subtractGold(final int amount) {
        executor.execute(() -> {
            Player p = playerDao.getPlayerSync();
            if (p != null) {
                p.gold = Math.max(0, p.gold - amount);
                playerDao.updatePlayer(p);
            }
        });
    }

    public void addXpSync(long xp) {
        Player p = playerDao.getPlayerSync();
        if (p != null) {
            p.addXp(xp);
            playerDao.updatePlayer(p);
        }
    }

    public void addGoldSync(int amount) {
        Player p = playerDao.getPlayerSync();
        if (p != null) {
            p.gold += amount;
            playerDao.updatePlayer(p);
        }
    }

    public void subtractXpSync(long xp) {
        Player p = playerDao.getPlayerSync();
        if (p != null) {
            p.currentXp = Math.max(0, p.currentXp - xp);

            if (p.currentXp < 0 && p.level > 1) {
                p.level--;
                p.xpToNextLevel = Player.calculateXpForLevel(p.level + 1);
                p.currentXp = 0;
            }

            playerDao.updatePlayer(p);
        }
    }

    public void subtractGoldSync(int amount) {
        Player p = playerDao.getPlayerSync();
        if (p != null) {
            p.gold = Math.max(0, p.gold - amount);
            playerDao.updatePlayer(p);
        }
    }

    public void addGold(final int amount) {
        executor.execute(() -> playerDao.addGold(amount));
    }

    public void addGems(final int amount) {
        executor.execute(() -> playerDao.addGems(amount));
    }

    public void initializePlayerIfNeeded() {
        executor.execute(() -> {
            if (playerDao.playerExists() == 0) {
                Player newPlayer = new Player();
                playerDao.insertPlayer(newPlayer);
            }
        });
    }

    public LiveData<LevelUpEvent> getLevelUpEvent() {
        return levelUpEventLiveData;
    }

    public void notifyLevelUp(LevelUpEvent event) {
        android.util.Log.d("PlayerRepository", "notifyLevelUp called with level: " + event.newLevel);
        levelUpEventLiveData.postValue(event);
    }
}
