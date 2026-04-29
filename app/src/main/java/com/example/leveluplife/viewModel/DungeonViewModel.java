package com.example.leveluplife.viewModel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.leveluplife.data.entity.DungeonState;
import com.example.leveluplife.data.repository.DungeonRepository;

public class DungeonViewModel extends AndroidViewModel {

    private final DungeonRepository repository;
    private final LiveData<DungeonState> dungeonState;
    private final LiveData<String> battleLog;

    public DungeonViewModel(@NonNull Application application) {
        super(application);
        repository = new DungeonRepository(application);
        dungeonState = repository.getDungeonState();
        battleLog = repository.getBattleLog();
        repository.ensureDungeonStateExists();
        repository.resetCooldownIfExpired();
    }

    public LiveData<DungeonState> getDungeonState() {
        return dungeonState;
    }

    public LiveData<String> getBattleLog() {
        return battleLog;
    }

    public void startDungeonRun() {
        repository.startDungeonRun();
    }

    public void playerAttack() {
        repository.playerAttack();
    }

    public void playerSkill() {
        repository.playerSkill();
    }

    public void playerRest() {
        repository.playerRest();
    }

    public void abandonRun() {
        repository.abandonRun();
    }

    public void resetCooldownIfExpired() {
        repository.resetCooldownIfExpired();
    }

    public void resetDungeonCooldown() {
        repository.resetDungeonCooldown();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        repository.shutdown();
    }
}