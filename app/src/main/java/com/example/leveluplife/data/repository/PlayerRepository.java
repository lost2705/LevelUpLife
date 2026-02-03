package com.example.leveluplife.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.leveluplife.data.dao.PlayerDao;
import com.example.leveluplife.data.database.AppDatabase;
import com.example.leveluplife.data.entity.Player;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PlayerRepository {

    private final PlayerDao playerDao;
    private final LiveData<Player> player;
    private final ExecutorService executor;

    public PlayerRepository(Application application) {
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
                boolean leveledUp = p.addXp(xp);
                playerDao.updatePlayer(p);
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

    /**
     * Синхронное добавление XP (для использования внутри транзакций)
     */
    public void addXpSync(long xp) {
        Player p = playerDao.getPlayerSync();
        if (p != null) {
            p.addXp(xp);
            playerDao.updatePlayer(p);
        }
    }

    /**
     * Синхронное добавление Gold
     */
    public void addGoldSync(int amount) {
        Player p = playerDao.getPlayerSync();
        if (p != null) {
            p.gold += amount;
            playerDao.updatePlayer(p);
        }
    }

    /**
     * Синхронное вычитание XP
     */
    public void subtractXpSync(long xp) {
        Player p = playerDao.getPlayerSync();
        if (p != null) {
            p.currentXp = Math.max(0, p.currentXp - xp);

            // Не позволяем уровню упасть ниже 1
            if (p.currentXp < 0 && p.level > 1) {
                p.level--;
                p.xpToNextLevel = Player.calculateXpForLevel(p.level + 1);
                p.currentXp = 0;
            }

            playerDao.updatePlayer(p);
        }
    }

    /**
     * Синхронное вычитание Gold
     */
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
}
