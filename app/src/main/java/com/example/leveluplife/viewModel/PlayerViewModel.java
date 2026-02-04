package com.example.leveluplife.viewModel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.leveluplife.data.entity.Player;
import com.example.leveluplife.data.model.LevelUpEvent;
import com.example.leveluplife.data.repository.PlayerRepository;

public class PlayerViewModel extends AndroidViewModel {

    private final PlayerRepository repository;

    public PlayerViewModel(@NonNull Application application) {
        super(application);
        repository = PlayerRepository.getInstance(application);  // ✅ Используем singleton
    }


    public LiveData<Player> getPlayer() {
        return repository.getPlayer();  // ✅ Возвращаем напрямую из repository
    }

    public void updatePlayer(Player player) {
        repository.updatePlayer(player);
    }

    public void addXp(long xp) {
        repository.addXp(xp);
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
}
