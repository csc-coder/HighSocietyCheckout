package com.kbiz.highsocietycheckout.database;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class DatabaseManager {

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
        return database.insert(DatabaseHelper.TABLE_USERS, null, values);
    }

    public long addHarvest(String harvestHash, String userId, long time, double amount) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_HARVEST_ID, harvestHash);
        values.put(DatabaseHelper.COLUMN_USER_ID, userId);
        values.put(DatabaseHelper.COLUMN_TIME, time);
        values.put(DatabaseHelper.COLUMN_AMOUNT, amount);
        return database.insert(DatabaseHelper.TABLE_HARVESTS, null, values);
    }

    public Cursor getAllUsers() {
        return database.query(DatabaseHelper.TABLE_USERS, null, null, null, null, null, null);
    }

    public Cursor getAllHarvests() {
        return database.query(DatabaseHelper.TABLE_HARVESTS, null, null, null, null, null, null);
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
