package com.kbiz.highsocietycheckout.data;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.kbiz.highsocietycheckout.data.dao.HarvestDAO;
import com.kbiz.highsocietycheckout.database.AppDatabase;

public class HarvestViewModel extends AndroidViewModel {
    private final MutableLiveData<Integer> harvestAmount = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> availAmount = new MutableLiveData<>(0);
    private final HarvestDAO harvestDao;

    private LiveData<Long> totalHarvest;

    public HarvestViewModel(Application application) {
        super(application);
        AppDatabase db = AppDatabase.getDatabase(application);
        harvestDao = db.harvestDAO();
    }

    public LiveData<Integer> getHarvestAmount() {
        return harvestAmount;
    }

    public void setHarvestAmount(Integer amount) {
        Log.d("LOK_HARVEST_MODEL", "" + amount);
        harvestAmount.postValue(amount);
    }

    public LiveData<Integer> getAvailAmount() {
        return availAmount;
    }

    public void setAvailAmount(Integer availAmount) {
        this.availAmount.postValue(availAmount);
    }

    public void clear() {
        harvestAmount.postValue(0);
        availAmount.postValue(0);
    }
}
