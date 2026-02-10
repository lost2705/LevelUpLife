package com.example.leveluplife.data.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.leveluplife.data.dao.CompletedTaskDao;
import com.example.leveluplife.data.dao.PlayerDao;
import com.example.leveluplife.data.dao.TaskDao;
import com.example.leveluplife.data.entity.CompletedTask;
import com.example.leveluplife.data.entity.Player;
import com.example.leveluplife.data.entity.Task;

@Database(
        entities = {Task.class, Player.class, CompletedTask.class},
        version = 3,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    public abstract TaskDao taskDao();
    public abstract PlayerDao playerDao();
    public abstract CompletedTaskDao completedTaskDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "levelup_database"
                            )
                            .addMigrations(
                                    Migrations.MIGRATION_1_2,
                                    Migrations.MIGRATION_2_3
                            )
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
