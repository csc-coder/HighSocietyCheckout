package com.kbiz.highsocietycheckout.data;

import android.app.Application;
import android.database.Cursor;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.google.gson.Gson;
import com.kbiz.highsocietycheckout.data.dao.HarvestDAO;
import com.kbiz.highsocietycheckout.data.dao.UserDAO;
import com.kbiz.highsocietycheckout.data.entities.Harvest;
import com.kbiz.highsocietycheckout.data.entities.User;
import com.kbiz.highsocietycheckout.database.AppDatabase;
import com.kbiz.highsocietycheckout.database.DatabaseHelper;
import com.kbiz.highsocietycheckout.database.DatabaseManager;

import java.util.ArrayList;
import java.util.List;

public class DataViewModel extends AndroidViewModel {
    static final String TAG="LOK_DVM";
    private final UserDAO userDao;
    private final HarvestDAO harvestDao;

    private final LiveData<List<User>> allUsers;
    private final LiveData<List<Harvest>> allHarvests;

    public DataViewModel(Application application) {
        super(application);
        AppDatabase db = AppDatabase.getDatabase(application);
        userDao = db.userDao();
        harvestDao = db.harvestDao();
        DatabaseManager database=DatabaseManager.getInstance();
        Cursor cursor = database.getWritableDatabase().query(DatabaseHelper.TABLE_USERS, null, null, null, null, null, null);

        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    do {
                        // Get data from cursor
                        int userHashIndex = cursor.getColumnIndex("user_hash");
                        String userHash = cursor.getString(userHashIndex);

                        // Use the data as needed
                        Log.d(TAG, "DB User Hash: " + userHash);
                    } while (cursor.moveToNext());
                }
            } finally {
                cursor.close(); // Always close the cursor to release resources
            }
        }
        Log.d(TAG, "got users:"+cursor.getCount());


        allUsers = userDao.getAllUsers();
        allHarvests = harvestDao.getAllHarvests();

        Log.d(TAG, "got users:"+(new Gson()).toJson(allUsers.getValue()));
        Log.d(TAG, "got harvests:"+(new Gson()).toJson(allHarvests.getValue()));

    }

    public LiveData<List<User>> getAllUsers() {
        return allUsers;
    }

    public LiveData<List<Harvest>> getAllHarvests() {
        return allHarvests;
    }
}
