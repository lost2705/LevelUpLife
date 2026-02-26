package com.example.leveluplife.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.leveluplife.data.entity.ShopItem;

import java.util.List;

@Dao
public interface ShopDao {

    @Query("SELECT * FROM shop_items WHERE available = 1 ORDER BY price ASC")
    LiveData<List<ShopItem>> getAvailableItems();

    @Query("SELECT * FROM shop_items ORDER BY price ASC")
    List<ShopItem> getAllItemsSync();

    @Insert
    void insertItem(ShopItem item);

    @Update
    void updateItem(ShopItem item);

    @Delete
    void deleteItem(ShopItem item);

    @Query("UPDATE shop_items SET available = :available WHERE id = :id")
    void setAvailability(int id, boolean available);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertItems(ShopItem... items);

    @Query("DELETE FROM shop_items")
    void deleteAllItems();
}
