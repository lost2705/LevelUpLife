package com.example.leveluplife.viewModel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.leveluplife.data.entity.Player;
import com.example.leveluplife.data.model.LevelUpEvent;
import com.example.leveluplife.data.repository.PlayerRepository;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PlayerViewModel extends AndroidViewModel {

    private final PlayerRepository repository;
    private final ExecutorService executor;

    public PlayerViewModel(@NonNull Application application) {
        super(application);
        repository = PlayerRepository.getInstance(application);
        executor = Executors.newSingleThreadExecutor();
    }

    public LiveData<Player> getPlayer() {
        return repository.getPlayer();
    }

    public void updatePlayer(Player player) {
        repository.updatePlayer(player);
    }

    public void addXp(int xp) {
        executor.execute(() -> {
            Player player = repository.getPlayerSync();
            if (player != null) {
                player.currentXp += xp;

                while (player.currentXp >= player.xpToNextLevel) {
                    player.currentXp -= player.xpToNextLevel;
                    player.level++;

                    player.talentPoints++;

                    int oldMaxHp = player.maxHp;
                    int oldMaxMana = player.maxMana;

                    player.maxHp += 25;
                    player.currentHp = player.maxHp;
                    player.maxMana += 12;
                    player.currentMana = player.maxMana;
                    player.xpToNextLevel = (long) (player.xpToNextLevel * 1.5);

                    LevelUpEvent event = new LevelUpEvent(
                            player.level,
                            1,
                            oldMaxHp,
                            player.maxHp,
                            oldMaxMana,
                            player.maxMana
                    );

                    repository.triggerLevelUpEvent(event);
                }

                repository.updatePlayer(player);
            }
        });
    }

    public void addGold(int amount) {
        repository.addGold(amount);
    }

    public void addGems(int amount) {
        repository.addGems(amount);
    }

    public void initializePlayerIfNeeded() {
        repository.initializePlayerIfNeeded();
    }

    public void subtractXp(long xp) {
        repository.subtractXp(xp);
    }

    public void subtractGold(int amount) {
        repository.subtractGold(amount);
    }

    public LiveData<LevelUpEvent> getLevelUpEvent() {
        return repository.getLevelUpEvent();
    }

    public void updateTalents(int strength, int intelligence, int dexterity, int talentPoints) {
        executor.execute(() -> {
            Player player = repository.getPlayerSync();
            if (player != null) {
                player.strength = strength;
                player.intelligence = intelligence;
                player.dexterity = dexterity;
                player.talentPoints = talentPoints;

                player.maxHp = 100 + (strength * 10);
                player.maxMana = 50 + (intelligence * 5);

                if (player.currentHp < player.maxHp) {
                    player.currentHp = player.maxHp;
                }
                if (player.currentMana < player.maxMana) {
                    player.currentMana = player.maxMana;
                }

                repository.updatePlayer(player);
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdown();
    }
}
