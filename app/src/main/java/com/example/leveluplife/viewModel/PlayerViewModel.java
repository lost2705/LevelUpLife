package com.example.leveluplife.viewModel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.leveluplife.data.entity.Player;
import com.example.leveluplife.data.repository.PlayerRepository;

public class PlayerViewModel extends AndroidViewModel {

    private final PlayerRepository repository;
    private final LiveData<Player> player;

    public PlayerViewModel(@NonNull Application application) {
        super(application);
        repository = new PlayerRepository(application);
        player = repository.getPlayer();
    }

    public LiveData<Player> getPlayer() {
        return player;
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

}
