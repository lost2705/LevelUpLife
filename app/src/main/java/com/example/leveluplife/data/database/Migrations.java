package com.example.leveluplife.data.database;

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
}
