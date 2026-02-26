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
import com.example.leveluplife.data.dao.ShopDao;
import com.example.leveluplife.data.dao.TaskDao;
import com.example.leveluplife.data.entity.Achievement;
import com.example.leveluplife.data.entity.CompletedTask;
import com.example.leveluplife.data.entity.Player;
import com.example.leveluplife.data.entity.ShopItem;
import com.example.leveluplife.data.entity.Task;

@Database(
        entities = {Task.class, Player.class, CompletedTask.class, Achievement.class, ShopItem.class},
        version = 8,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    public abstract TaskDao taskDao();
    public abstract PlayerDao playerDao();
    public abstract CompletedTaskDao completedTaskDao();
    public abstract AchievementDao achievementDao();
    public abstract ShopDao shopDao();

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
                                    Migrations.MIGRATION_6_7,
                                    Migrations.MIGRATION_7_8
                            )
                            .addCallback(new RoomDatabase.Callback() {
                                @Override
                                public void onCreate(@NonNull SupportSQLiteDatabase db) {
                                    super.onCreate(db);
                                    new Thread(() -> seedData(getDatabase(context))).start();
                                }
                            })
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    public static void seedData(AppDatabase db) {
        seedAchievements(db);
        seedShopItems(db);
    }

    private static void seedAchievements(AppDatabase db) {
        db.achievementDao().insertAchievements(
                new Achievement(1,  "First Blood",      "Complete your first task",                  "⚔️",  100,  50),
                new Achievement(2,  "Daily Routine",    "Complete 7 daily tasks in a row",           "📅",  250, 100),
                new Achievement(3,  "Level Up!",        "Reach Level 5",                             "⭐",  500, 200),
                new Achievement(4,  "Gold Rush",        "Earn 1000 gold",                            "💰",  300, 150),
                new Achievement(5,  "Task Master",      "Complete 50 tasks",                         "🎯",  400, 200),
                new Achievement(6,  "Perfect Day",      "Complete all daily tasks 5 days in a row",  "✅",  600, 300),
                new Achievement(7,  "Level Master",     "Reach Level 10",                            "👑", 1000, 500),
                new Achievement(8,  "Wealthy",          "Earn 5000 gold",                            "🏦",  700, 400),
                new Achievement(9,  "Strength Builder", "Put 10 points into Strength",               "💪",  350, 175),
                new Achievement(10, "Balanced",         "Put points into all 5 attributes",          "⚖️",  800, 400)
        );
    }

    private static void seedShopItems(AppDatabase db) {
        db.shopDao().insertItems(
                new ShopItem("Penalty Shield", "Removes all XP penalties",     "🛡️", 200, "REMOVE_PENALTY", 0),
                new ShopItem("XP Boost",       "Double XP for your next task", "⚡",  150, "XP_BOOST",       2),
                new ShopItem("HP Potion",      "Restore 50 HP",                "❤️",  100, "HP_POTION",       50),
                new ShopItem("Mana Potion",    "Restore 30 Mana",              "💙",   80, "MANA_POTION",     30),
                new ShopItem("Gem Pack",       "Receive 5 Gems",               "💎",  500, "GEM_PACK",        5)
        );
    }
}
