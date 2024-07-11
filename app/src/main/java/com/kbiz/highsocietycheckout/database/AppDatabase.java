package com.kbiz.highsocietycheckout.database;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.kbiz.highsocietycheckout.data.dao.HarvestDAO;
import com.kbiz.highsocietycheckout.data.dao.UserDAO;
import com.kbiz.highsocietycheckout.data.entities.Harvest;
import com.kbiz.highsocietycheckout.data.entities.User;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {User.class, Harvest.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract UserDAO userDAO();
    public abstract HarvestDAO harvestDAO();

    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static final String TAG="LOK_APP_DB";
    static final Migration RESET_MIGRATION = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // Drop existing tables
//            database.execSQL("DROP TABLE IF EXISTS " + DatabaseHelper.TABLE_HARVESTS);
//
//            // Recreate tables
//            database.execSQL(DatabaseHelper.TABLE_CREATE_HARVESTS);
            Log.d(TAG, "migrating db schema");
            // Create a new table with the correct schema
            database.execSQL("CREATE TABLE harvests_new (harvest_id TEXT NOT NULL PRIMARY KEY, user_hash TEXT NOT NULL, time INTEGER NOT NULL, amount INTEGER NOT NULL)");
            // Copy the data from the old table to the new table
            database.execSQL("INSERT INTO harvests_new (harvest_id, user_hash, time, amount) SELECT harvest_id, user_hash, time, amount FROM harvests");
            // Remove the old table
            database.execSQL("DROP TABLE harvests");
            // Rename the new table to the old table name
            database.execSQL("ALTER TABLE harvests_new RENAME TO harvests");
        }
    };

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "harvest.db")
                            .addMigrations(RESET_MIGRATION)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

}
