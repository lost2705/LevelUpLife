package com.example.leveluplife.data.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.leveluplife.data.dao.PlayerDao;
import com.example.leveluplife.data.dao.TaskDao;
import com.example.leveluplife.data.entity.Player;
import com.example.leveluplife.data.entity.Task;

@Database(
        entities = {Task.class, Player.class},
        version = 2,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase INSTANCE;

    public abstract TaskDao taskDao();
    public abstract PlayerDao playerDao();

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "leveluplife_database"
                            )
                            .addMigrations(MIGRATION_1_2)  // добавили миграцию!
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `player` (" +
                            "`id` INTEGER PRIMARY KEY NOT NULL, " +
                            "`level` INTEGER NOT NULL, " +
                            "`current_xp` INTEGER NOT NULL, " +
                            "`xp_to_next_level` INTEGER NOT NULL, " +
                            "`gold` INTEGER NOT NULL, " +
                            "`gems` INTEGER NOT NULL, " +
                            "`strength` INTEGER NOT NULL, " +
                            "`agility` INTEGER NOT NULL, " +
                            "`intelligence` INTEGER NOT NULL, " +
                            "`charisma` INTEGER NOT NULL, " +
                            "`max_hp` INTEGER NOT NULL, " +
                            "`current_hp` INTEGER NOT NULL, " +
                            "`max_mana` INTEGER NOT NULL, " +
                            "`current_mana` INTEGER NOT NULL, " +
                            "`current_streak` INTEGER NOT NULL, " +
                            "`last_login_date` INTEGER NOT NULL, " +
                            "`available_talent_points` INTEGER NOT NULL, " +
                            "`created_at` INTEGER NOT NULL, " +
                            "`last_updated` INTEGER NOT NULL" +
                            ")"
            );

            long now = System.currentTimeMillis();
            database.execSQL(
                    "INSERT INTO player (id, level, current_xp, xp_to_next_level, " +
                            "gold, gems, strength, agility, intelligence, charisma, " +
                            "max_hp, current_hp, max_mana, current_mana, " +
                            "current_streak, last_login_date, available_talent_points, " +
                            "created_at, last_updated) " +
                            "VALUES (1, 1, 0, 141, 100, 0, 10, 10, 10, 10, " +
                            "150, 150, 80, 80, 0, " + now + ", 0, " +
                            now + ", " + now + ")"
            );
        }
    };
}
