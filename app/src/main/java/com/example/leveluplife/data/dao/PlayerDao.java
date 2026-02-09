package com.example.leveluplife.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.leveluplife.data.entity.Player;

@Dao
public interface PlayerDao {

    @Query("SELECT * FROM player WHERE id = 1")
    LiveData<Player> getPlayer();

    @Query("SELECT * FROM player WHERE id = 1")
    Player getPlayerSync();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertPlayer(Player player);

    @Update
    void updatePlayer(Player player);

    @Query("SELECT COUNT(*) FROM player WHERE id = 1")
    int playerExists();
}
