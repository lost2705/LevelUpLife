package com.example.leveluplife.data.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.leveluplife.data.dao.AchievementDao;
import com.example.leveluplife.data.dao.CompletedTaskDao;
import com.example.leveluplife.data.dao.DungeonStateDao;
import com.example.leveluplife.data.dao.PlayerDao;
import com.example.leveluplife.data.dao.ShopDao;
import com.example.leveluplife.data.dao.TaskDao;
import com.example.leveluplife.data.entity.Achievement;
import com.example.leveluplife.data.entity.CompletedTask;
import com.example.leveluplife.data.entity.DungeonState;
import com.example.leveluplife.data.entity.Player;
import com.example.leveluplife.data.entity.ShopItem;
import com.example.leveluplife.data.entity.Task;

@Database(
        entities = {Task.class, Player.class, CompletedTask.class,
                Achievement.class, ShopItem.class, DungeonState.class},
        version = 12,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    public abstract TaskDao taskDao();
    public abstract PlayerDao playerDao();
    public abstract CompletedTaskDao completedTaskDao();
    public abstract AchievementDao achievementDao();
    public abstract ShopDao shopDao();
    public abstract DungeonStateDao dungeonStateDao();

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
                                    Migrations.MIGRATION_7_8,
                                    Migrations.MIGRATION_8_9,
                                    Migrations.MIGRATION_9_10,
                                    Migrations.MIGRATION_10_11,
                                    Migrations.MIGRATION_11_12
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
        seedDungeonState(db);
    }

    private static void seedAchievements(AppDatabase db) {
        db.achievementDao().insertAchievements(
                new Achievement(1,  "First Blood",        "Complete your first task",                  "⚔️",  100,  50),
                new Achievement(2,  "Daily Routine",      "Complete 7 daily tasks in a row",           "📅",  250, 100),
                new Achievement(3,  "Level Up!",          "Reach Level 5",                             "⭐",  500, 200),
                new Achievement(4,  "Gold Rush",          "Earn 1000 gold",                            "💰",  300, 150),
                new Achievement(5,  "Task Master",        "Complete 50 tasks",                         "🎯",  400, 200),
                new Achievement(6,  "Perfect Day",        "Complete all daily tasks 5 days in a row",  "✅",  600, 300),
                new Achievement(7,  "Level Master",       "Reach Level 10",                            "👑", 1000, 500),
                new Achievement(8,  "Wealthy",            "Earn 5000 gold",                            "🏦",  700, 400),
                new Achievement(9,  "Strength Builder",   "Put 10 points into Strength",               "💪",  350, 175),
                new Achievement(10, "Balanced",           "Put points into all attributes",            "⚖️",  800, 400),
                new Achievement(11, "Destiny's Call",     "Choose your class and begin your journey",  "⚡",  200, 100),
                new Achievement(12, "Iron Will",          "Become a Warrior — master of strength",     "⚔️",  300, 150),
                new Achievement(13, "Ancient Knowledge",  "Become a Mage — seeker of wisdom",          "🧙",  300, 150),
                new Achievement(14, "Shadow Step",        "Become a Ranger — guardian of consistency", "🏹",  300, 150),
                new Achievement(15, "Dungeon First Blood", "Win your first dungeon battle",            "🕳️",  150,  75),
                new Achievement(16, "Dungeon Explorer",    "Win 3 dungeon battles",                    "🗺️",  300, 150),
                new Achievement(17, "Warrior's Trial",     "Win a dungeon battle as a Warrior",        "🛡️",  250, 125),
                new Achievement(18, "Mage's Trial",        "Win a dungeon battle as a Mage",           "📜",  250, 125),
                new Achievement(19, "Ranger's Trial",      "Win a dungeon battle as a Ranger",         "🎯",  250, 125)
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

    private static void seedDungeonState(AppDatabase db) {
        DungeonState state = new DungeonState();
        state.setId(1);
        db.dungeonStateDao().insertOrReplace(state);
    }
}
