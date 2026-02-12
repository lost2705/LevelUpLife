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

    public void addXp(long xp) {
        repository.addXp(xp);
    }

    public void removeXp(int amount) {
        repository.removeXp(amount);
    }

    public void addGold(int amount) {
        repository.addGold(amount);
    }

    public void removeGold(int amount) {
        repository.removeGold(amount);
    }

    public void addGems(int amount) {
        repository.addGems(amount);
    }

    public void subtractXp(long xp) {
        repository.subtractXp(xp);
    }

    public void subtractGold(int amount) {
        repository.subtractGold(amount);
    }

    public void initializePlayerIfNeeded() {
        repository.initializePlayerIfNeeded();
    }

    public LiveData<LevelUpEvent> getLevelUpEvent() {
        return repository.getLevelUpEvent();
    }

    public void updateTalents(int strength, int intelligence, int dexterity, int talentPoints) {
        executor.execute(() -> {
            Player player = repository.getPlayerSync();
            if (player != null) {
                player.setStrength(strength);
                player.setIntelligence(intelligence);
                player.setDexterity(dexterity);
                player.setTalentPoints(talentPoints);

                // Recalculate stats based on attributes
                int newMaxHp = 100 + (strength * 10);
                int newMaxMana = 50 + (intelligence * 5);

                player.setMaxHp(newMaxHp);
                player.setMaxMana(newMaxMana);

                if (player.getCurrentHp() < newMaxHp) {
                    player.setCurrentHp(newMaxHp);
                }
                if (player.getCurrentMana() < newMaxMana) {
                    player.setCurrentMana(newMaxMana);
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
