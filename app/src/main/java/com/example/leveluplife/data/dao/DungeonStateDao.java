package com.example.leveluplife.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.leveluplife.data.entity.DungeonState;

@Dao
public interface DungeonStateDao {

    @Query("SELECT * FROM dungeon_state WHERE id = 1 LIMIT 1")
    LiveData<DungeonState> getDungeonState();

    @Query("SELECT * FROM dungeon_state WHERE id = 1 LIMIT 1")
    DungeonState getDungeonStateSync();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrReplace(DungeonState dungeonState);

    @Update
    void update(DungeonState dungeonState);

    @Query("DELETE FROM dungeon_state")
    void clearAll();
}