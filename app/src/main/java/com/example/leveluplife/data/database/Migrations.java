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
        }
    };
}
