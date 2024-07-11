package com.kbiz.highsocietycheckout.data;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.google.gson.Gson;
import com.kbiz.highsocietycheckout.data.dao.HarvestDAO;
import com.kbiz.highsocietycheckout.data.dao.UserDAO;
import com.kbiz.highsocietycheckout.data.entities.Harvest;
import com.kbiz.highsocietycheckout.data.entities.User;
import com.kbiz.highsocietycheckout.database.AppDatabase;
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
        userDao = db.userDAO();
        harvestDao = db.harvestDAO();

        allUsers = userDao.getAllUsers();
        allHarvests = harvestDao.getAllHarvests();

        List<User> users = allUsers.getValue();
        List<Harvest> harvests = allHarvests.getValue();
        Log.d(TAG, "got users:"+(new Gson()).toJson(users));
        Log.d(TAG, "got harvests:"+(new Gson()).toJson(harvests));

    }

    public void clearAllUsers() {
        AppDatabase.getDatabase(null);
        AppDatabase.databaseWriteExecutor.execute(() -> {
            userDao.clearAllUsers();
        });
    }

    public LiveData<List<User>> getAllUsers() {
        return allUsers;
    }

    public LiveData<List<Harvest>> getAllHarvests() {
        return allHarvests;
    }
}
