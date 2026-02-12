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

    public void addXp(long amount) {
        executor.execute(() -> {
            Player player = playerDao.getPlayerSync();
            if (player != null) {
                long newXp = player.getCurrentXp() + amount;
                long xpToNextLevel = player.getXpToNextLevel();

                while (newXp >= xpToNextLevel) {
                    newXp -= xpToNextLevel;
                    player.setLevel(player.getLevel() + 1);
                    xpToNextLevel = calculateXpForNextLevel(player.getLevel());
                    player.setXpToNextLevel(xpToNextLevel);
                    player.setTalentPoints(player.getTalentPoints() + 1);

                    player.setMaxHp(player.getMaxHp() + 10);
                    player.setCurrentHp(player.getMaxHp());
                    player.setMaxMana(player.getMaxMana() + 5);
                    player.setCurrentMana(player.getMaxMana());

                    player.setStrength(player.getStrength() + 1);
                    player.setIntelligence(player.getIntelligence() + 1);
                    player.setDexterity(player.getDexterity() + 1);

                    levelUpEvent.postValue(new LevelUpEvent(
                            player.getLevel(),
                            player.getMaxHp(),
                            player.getMaxMana(),
                            player.getStrength(),
                            player.getIntelligence(),
                            player.getDexterity()
                    ));
                }

                player.setCurrentXp(newXp);
                playerDao.updatePlayer(player);
            }
        });
    }

    public void subtractXp(long xp) {
        executor.execute(() -> {
            Player player = playerDao.getPlayerSync();
            if (player != null) {
                long newXp = Math.max(0, player.getCurrentXp() - xp);
                player.setCurrentXp(newXp);
                playerDao.updatePlayer(player);
            }
        });
    }

    public void addGold(int amount) {
        executor.execute(() -> {
            Player player = playerDao.getPlayerSync();
            if (player != null) {
                player.setGold(player.getGold() + amount);
                playerDao.updatePlayer(player);
            }
        });
    }

    public void subtractGold(int amount) {
        executor.execute(() -> {
            Player player = playerDao.getPlayerSync();
            if (player != null) {
                int newGold = Math.max(0, player.getGold() - amount);
                player.setGold(newGold);
                playerDao.updatePlayer(player);
            }
        });
    }

    public void removeXp(int amount) {
        executor.execute(() -> {
            Player player = playerDao.getPlayerSync();
            if (player != null) {
                int newXp = (int) (player.getCurrentXp() - amount);
                if (newXp < 0) newXp = 0;

                player.setCurrentXp(newXp);
                playerDao.updatePlayer(player);
            }
        });
    }

    public void removeGold(int amount) {
        executor.execute(() -> {
            Player player = playerDao.getPlayerSync();
            if (player != null) {
                int newGold = player.getGold() - amount;
                if (newGold < 0) newGold = 0;

                player.setGold(newGold);
                playerDao.updatePlayer(player);
            }
        });
    }

    public void addGems(int amount) {
        executor.execute(() -> {
            Player player = playerDao.getPlayerSync();
            if (player != null) {
                player.setGems(player.getGems() + amount);
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

    private long calculateXpForNextLevel(int level) {
        return 100 + (level * 50);
    }
}
