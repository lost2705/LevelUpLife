package com.example.leveluplife.data.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.leveluplife.data.dao.AchievementDao;
import com.example.leveluplife.data.dao.CompletedTaskDao;
import com.example.leveluplife.data.dao.PlayerDao;
import com.example.leveluplife.data.dao.TaskDao;
import com.example.leveluplife.data.entity.Achievement;
import com.example.leveluplife.data.entity.CompletedTask;
import com.example.leveluplife.data.entity.Player;
import com.example.leveluplife.data.entity.Task;

@Database(
        entities = {Task.class, Player.class, CompletedTask.class, Achievement.class},
        version = 7,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    public abstract TaskDao taskDao();
    public abstract PlayerDao playerDao();
    public abstract CompletedTaskDao completedTaskDao();
    public abstract AchievementDao achievementDao();

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
                                    Migrations.MIGRATION_2_3,
                                    Migrations.MIGRATION_3_4,
                                    Migrations.MIGRATION_4_5,
                                    Migrations.MIGRATION_5_6,
                                    Migrations.MIGRATION_6_7
                            )
                            .addCallback(new RoomDatabase.Callback() {
                                @Override
                                public void onCreate(@NonNull SupportSQLiteDatabase db) {
                                    super.onCreate(db);
                                    db.execSQL("INSERT INTO achievements " +
                                            "(id, title, description, icon, rewardXp, rewardGold, unlocked, unlockedAt) VALUES " +
                                            "(1, 'First Blood', 'Complete your first task', '⚔️', 100, 50, 0, 0), " +
                                            "(2, 'Daily Routine', 'Complete 7 daily tasks in a row', '📅', 250, 100, 0, 0), " +
                                            "(3, 'Level Up!', 'Reach Level 5', '⭐', 500, 200, 0, 0), " +
                                            "(4, 'Gold Rush', 'Earn 1000 gold', '💰', 300, 150, 0, 0), " +
                                            "(5, 'Task Master', 'Complete 50 tasks', '🎯', 400, 200, 0, 0), " +
                                            "(6, 'Perfect Day', 'Complete all daily tasks 5 days in a row', '✅', 600, 300, 0, 0), " +
                                            "(7, 'Level Master', 'Reach Level 10', '👑', 1000, 500, 0, 0), " +
                                            "(8, 'Wealthy', 'Earn 5000 gold', '🏦', 700, 400, 0, 0), " +
                                            "(9, 'Strength Builder', 'Put 10 points into Strength', '💪', 350, 175, 0, 0), " +
                                            "(10, 'Balanced', 'Put points into all 5 attributes', '⚖️', 800, 400, 0, 0)"
                                    );
                                }
                            })
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
