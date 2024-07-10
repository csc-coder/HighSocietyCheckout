package com.kbiz.highsocietycheckout.database;
import static com.kbiz.highsocietycheckout.database.DatabaseHelper.COLUMN_AMOUNT;
import static com.kbiz.highsocietycheckout.database.DatabaseHelper.COLUMN_HARVEST_ID;
import static com.kbiz.highsocietycheckout.database.DatabaseHelper.COLUMN_TIME;
import static com.kbiz.highsocietycheckout.database.DatabaseHelper.COLUMN_USER_HASH;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import androidx.annotation.NonNull;

import com.kbiz.highsocietycheckout.data.entities.Harvest;
import com.kbiz.highsocietycheckout.data.entities.User;

import java.util.ArrayList;

public class DatabaseManager {

    private static final String TAG="LOK_DB_MGR";
    private final DatabaseHelper dbHelper;
    private SQLiteDatabase database;
    private static DatabaseManager instance;

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            throw new RuntimeException("init DatabaseManager first.");
        }
        return instance;
    }
    public static synchronized DatabaseManager getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseManager(context);
        }
        return instance;
    }

    private DatabaseManager(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public void open() {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public SQLiteDatabase getWritableDatabase() {
        return dbHelper.getWritableDatabase();
    }

    public long addUser(String userHash) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_USER_HASH, userHash);
        Log.d(TAG,"adding new user hash to db:"+userHash);
        return database.insert(DatabaseHelper.TABLE_USERS, null, values);
    }

    public long addHarvest(String harvestHash, String userHash, long time, double amount) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_HARVEST_ID, harvestHash);
        values.put(COLUMN_USER_HASH, userHash);
        values.put(COLUMN_TIME, time);
        values.put(COLUMN_AMOUNT, amount);
        return database.insert(DatabaseHelper.TABLE_HARVESTS, null, values);
    }

    public Cursor getUsersCursor() {
        return database.query(DatabaseHelper.TABLE_USERS, null, null, null, null, null, null);
    }

    public @NonNull ArrayList<User> getUsers() {
        Cursor cursor = getUsersCursor();

        ArrayList<User> userHashes=new ArrayList<>();
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    do {
                        // Get data from cursor
                        int userHashIndex = cursor.getColumnIndex("user_hash");
                        String userHash = cursor.getString(userHashIndex);

                        // Use the data as needed
                        Log.d(TAG, "DB User Hash: " + userHash);
                        //store hash
                        userHashes.add(new User(userHash));
                    } while (cursor.moveToNext());
                }
            } finally {
                cursor.close(); // Always close the cursor to release resources
            }
        }
        return userHashes;
    }

    public Cursor getHarvestsCursor() {
        return database.query(DatabaseHelper.TABLE_HARVESTS, null, null, null, null, null, null);
    }

    public @NonNull ArrayList<Harvest> getHarvests() {
        Cursor cursor = getHarvestsCursor();
        //TODO refactor to use DAOs
        ArrayList<Harvest> harvests=new ArrayList<>();
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    do {
                        // Get data from cursor
                        int harvestIDIndex = cursor.getColumnIndex(COLUMN_HARVEST_ID);
                        String harvestId = cursor.getString(harvestIDIndex);

                        int userIdIndex = cursor.getColumnIndex(COLUMN_USER_HASH);
                        String userHash = cursor.getString(userIdIndex);

                        int timeIndex = cursor.getColumnIndex(COLUMN_TIME);
                        Long time = cursor.getLong(timeIndex);

                        int amountIndex = cursor.getColumnIndex(COLUMN_AMOUNT);
                        Long amount = cursor.getLong(amountIndex);

                        // Use the data as needed
                        Log.d(TAG, "harvest record: " + harvestIDIndex+", "+userHash+", "+time+", "+amount);
                        //store hash
                        harvests.add(new Harvest(harvestId, userHash,  time, amount));
                    } while (cursor.moveToNext());
                }
            } finally {
                cursor.close(); // Always close the cursor to release resources
            }
        }
        return harvests;
    }

    public boolean userHashExists(String userHash) {
        String query = "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_USERS + " WHERE " + DatabaseHelper.COLUMN_USER_HASH + " = ?";
        Cursor cursor = database.rawQuery(query, new String[]{userHash});
        boolean exists = false;
        if (cursor != null) {
            cursor.moveToFirst();
            exists = (cursor.getInt(0) > 0);
            cursor.close();
        }
        return exists;
    }

    public DatabaseHelper getDatabaseHelper(){
        return this.dbHelper;
    }
}
