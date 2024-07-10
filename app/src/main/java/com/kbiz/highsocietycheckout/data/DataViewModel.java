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


        allUsers = userDao.getAllUsers();
        allHarvests = harvestDao.getAllHarvests();

        ArrayList<User> users = database.getUsers();
        ArrayList<Harvest> harvests = database.getHarvests();
        Log.d(TAG, "got users:"+(new Gson()).toJson(users));
        Log.d(TAG, "got harvests:"+(new Gson()).toJson(allHarvests.getValue()));

    }


    public LiveData<List<User>> getAllUsers() {
        return allUsers;
    }

    public LiveData<List<Harvest>> getAllHarvests() {
        return allHarvests;
    }
}
