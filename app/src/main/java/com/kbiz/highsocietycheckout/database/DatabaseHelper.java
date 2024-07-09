package com.kbiz.highsocietycheckout.database;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "harvest.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_USERS = "users";
    public static final String COLUMN_USER_HASH = "user_hash";
    public static final String COLUMN_USER_ID = "user_id";

    public static final String TABLE_HARVESTS = "harvests";
    public static final String COLUMN_HARVEST_ID = "harvest_id";
    public static final String COLUMN_TIME = "time";
    public static final String COLUMN_AMOUNT = "amount";

    public static final String TABLE_CREATE_USERS =
            "CREATE TABLE IF NOT EXISTS " + TABLE_USERS + " (" +
                    COLUMN_USER_HASH + " TEXT PRIMARY KEY);";

    public static final String TABLE_CREATE_HARVESTS =
            "CREATE TABLE IF NOT EXISTS " + TABLE_HARVESTS + " (" +
                    COLUMN_HARVEST_ID + " TEXT PRIMARY KEY, " +
                    COLUMN_USER_ID + " TEXT, " +
                    COLUMN_TIME + " INTEGER, " +
                    COLUMN_AMOUNT + " REAL, " +
                    "FOREIGN KEY(" + COLUMN_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_HASH + "));";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE_USERS);
        db.execSQL(TABLE_CREATE_HARVESTS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HARVESTS);
        onCreate(db);
    }

    public boolean tableExists(SQLiteDatabase db, String tableName) {
        Cursor cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name=?", new String[]{tableName});
        boolean exists = (cursor.getCount() > 0);
        cursor.close();
        return exists;
    }
}

