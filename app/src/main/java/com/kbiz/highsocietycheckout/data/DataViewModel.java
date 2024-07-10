package com.kbiz.highsocietycheckout.data;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.kbiz.highsocietycheckout.data.dao.HarvestDAO;
import com.kbiz.highsocietycheckout.data.dao.UserDAO;
import com.kbiz.highsocietycheckout.data.entities.Harvest;
import com.kbiz.highsocietycheckout.data.entities.User;
import com.kbiz.highsocietycheckout.database.AppDatabase;

import java.util.List;

public class DataViewModel extends AndroidViewModel {
    private final UserDAO userDao;
    private final HarvestDAO harvestDao;

    private final LiveData<List<User>> allUsers;
    private final LiveData<List<Harvest>> allHarvests;

    public DataViewModel(Application application) {
        super(application);
        AppDatabase db = AppDatabase.getDatabase(application);
        userDao = db.userDao();
        harvestDao = db.harvestDao();

        allUsers = userDao.getAllUsers();
        allHarvests = harvestDao.getAllHarvests();
    }

    public LiveData<List<User>> getAllUsers() {
        return allUsers;
    }

    public LiveData<List<Harvest>> getAllHarvests() {
        return allHarvests;
    }
}
