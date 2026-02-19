package com.example.leveluplife.data.database;

import androidx.annotation.NonNull;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

public class Migrations {

    public static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE player ADD COLUMN strength INTEGER NOT NULL DEFAULT 5");
            database.execSQL("ALTER TABLE player ADD COLUMN intelligence INTEGER NOT NULL DEFAULT 5");
            database.execSQL("ALTER TABLE player ADD COLUMN dexterity INTEGER NOT NULL DEFAULT 5");
            database.execSQL("ALTER TABLE player ADD COLUMN talentPoints INTEGER NOT NULL DEFAULT 0");
        }
    };

    public static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE tasks ADD COLUMN frequency TEXT NOT NULL DEFAULT 'ONE_TIME'");
            database.execSQL("ALTER TABLE tasks ADD COLUMN currentStreak INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE tasks ADD COLUMN bestStreak INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE tasks ADD COLUMN lastCompletedDate INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE tasks ADD COLUMN totalCompletions INTEGER NOT NULL DEFAULT 0");

            database.execSQL("CREATE TABLE IF NOT EXISTS completed_tasks (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "taskId INTEGER NOT NULL, " +
                    "taskTitle TEXT, " +
                    "xpEarned INTEGER NOT NULL, " +
                    "goldEarned INTEGER NOT NULL, " +
                    "completedAt INTEGER NOT NULL, " +
                    "frequency TEXT)");
        }
    };

    public static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE tasks ADD COLUMN rewardClaimed INTEGER NOT NULL DEFAULT 0");
        }
    };

    public static final Migration MIGRATION_4_5 = new Migration(4, 5) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE player ADD COLUMN xpPenalty INTEGER NOT NULL DEFAULT 0");
        }
    };

    public static final Migration MIGRATION_5_6 = new Migration(5, 6) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE tasks ADD COLUMN reminderEnabled INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE tasks ADD COLUMN reminderHour INTEGER NOT NULL DEFAULT 9");
            database.execSQL("ALTER TABLE tasks ADD COLUMN reminderMinute INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE tasks ADD COLUMN nextReminderTime INTEGER NOT NULL DEFAULT 0");
        }
    };

    public static final Migration MIGRATION_6_7 = new Migration(6, 7) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE achievements (" +
                    "id INTEGER PRIMARY KEY NOT NULL, " +
                    "title TEXT NOT NULL, " +
                    "description TEXT NOT NULL, " +
                    "icon TEXT NOT NULL, " +
                    "rewardXp INTEGER NOT NULL DEFAULT 0, " +
                    "rewardGold INTEGER NOT NULL DEFAULT 0, " +
                    "unlocked INTEGER NOT NULL DEFAULT 0, " +
                    "unlockedAt INTEGER NOT NULL DEFAULT 0" +
                    ")");

            database.execSQL("INSERT INTO achievements (id, title, description, icon, rewardXp, rewardGold) VALUES " +
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
    };
}
