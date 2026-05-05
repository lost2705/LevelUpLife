package com.example.leveluplife.viewModel;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.leveluplife.data.database.AppDatabase;
import com.example.leveluplife.data.dao.ShopDao;
import com.example.leveluplife.data.entity.Player;
import com.example.leveluplife.data.entity.ShopItem;
import com.example.leveluplife.data.repository.PlayerRepository;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ShopViewModel extends AndroidViewModel {

    private final ShopDao shopDao;
    private final PlayerRepository playerRepository;
    private final ExecutorService executor;

    private final MutableLiveData<String> purchaseResult = new MutableLiveData<>();

    public ShopViewModel(@NonNull Application application) {
        super(application);
        AppDatabase db = AppDatabase.getDatabase(application);
        shopDao = db.shopDao();
        playerRepository = PlayerRepository.getInstance(application);
        executor = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<ShopItem>> getAvailableItems() {
        return shopDao.getAvailableItems();
    }

    public LiveData<Player> getPlayer() {
        return playerRepository.getPlayer();
    }

    public LiveData<String> getPurchaseResult() {
        return purchaseResult;
    }

    public void purchaseItem(ShopItem item) {
        executor.execute(() -> {
            Player player = playerRepository.getPlayerSync();
            if (player == null) return;

            if (player.getGold() < item.getPrice()) {
                purchaseResult.postValue("NOT_ENOUGH_GOLD");
                return;
            }

            player.setGold(player.getGold() - item.getPrice());

            switch (item.getEffectType()) {
                case "REMOVE_PENALTY":
                    player.setXpPenalty(0);
                    break;
                case "XP_BOOST":
                    getApplication()
                            .getSharedPreferences("shop_effects", Context.MODE_PRIVATE)
                            .edit()
                            .putBoolean("xp_boost_active", true)
                            .apply();
                    break;
                case "HP_POTION":
                    int newHp = Math.min(
                            player.getCurrentHp() + item.getEffectValue(),
                            player.getMaxHp()
                    );
                    player.setCurrentHp(newHp);

                    getApplication()
                            .getSharedPreferences("dungeon_potions", Context.MODE_PRIVATE)
                            .edit()
                            .putInt("hp_potions",
                                    getApplication()
                                            .getSharedPreferences("dungeon_potions", Context.MODE_PRIVATE)
                                            .getInt("hp_potions", 0) + 1)
                            .apply();
                    break;
                case "MANA_POTION":
                    int newMana = Math.min(
                            player.getCurrentMana() + item.getEffectValue(),
                            player.getMaxMana()
                    );
                    player.setCurrentMana(newMana);

                    getApplication()
                            .getSharedPreferences("dungeon_potions", Context.MODE_PRIVATE)
                            .edit()
                            .putInt("mana_potions",
                                    getApplication()
                                            .getSharedPreferences("dungeon_potions", Context.MODE_PRIVATE)
                                            .getInt("mana_potions", 0) + 1)
                            .apply();
                    break;
                case "GEM_PACK":
                    player.setGems(player.getGems() + item.getEffectValue());
                    break;
            }

            playerRepository.updatePlayer(player);
            purchaseResult.postValue("SUCCESS:" + item.getName());
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdown();
    }
}
