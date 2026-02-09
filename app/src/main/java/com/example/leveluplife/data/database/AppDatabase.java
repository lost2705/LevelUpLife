package com.example.leveluplife.data.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.leveluplife.data.dao.PlayerDao;
import com.example.leveluplife.data.dao.TaskDao;
import com.example.leveluplife.data.entity.Player;
import com.example.leveluplife.data.entity.Task;

@Database(entities = {Task.class, Player.class}, version = 2, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract TaskDao taskDao();
    public abstract PlayerDao playerDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "levelup_database")
                            .addMigrations(Migrations.MIGRATION_1_2)
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
