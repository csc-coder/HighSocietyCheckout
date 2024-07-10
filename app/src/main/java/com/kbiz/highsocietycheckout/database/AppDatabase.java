package com.kbiz.highsocietycheckout.database;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import android.content.Context;

import com.kbiz.highsocietycheckout.data.dao.HarvestDAO;
import com.kbiz.highsocietycheckout.data.dao.UserDAO;
import com.kbiz.highsocietycheckout.data.entities.Harvest;
import com.kbiz.highsocietycheckout.data.entities.User;

@Database(entities = {User.class, Harvest.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract UserDAO userDao();
    public abstract HarvestDAO harvestDao();
    static final Migration RESET_MIGRATION = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // Drop existing tables
//            database.execSQL("DROP TABLE IF EXISTS " + DatabaseHelper.TABLE_HARVESTS);
//
//            // Recreate tables
//            database.execSQL(DatabaseHelper.TABLE_CREATE_HARVESTS);
        }
    };
    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "harvest_database")
                            .addMigrations(RESET_MIGRATION)
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
