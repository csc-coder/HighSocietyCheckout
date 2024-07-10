package com.kbiz.highsocietycheckout.data;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class HarvestViewModel extends ViewModel {
    private final MutableLiveData<Integer> harvestAmount = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> availAmount = new MutableLiveData<>(0);


    public LiveData<Integer> getHarvestAmount() {
        return harvestAmount;
    }
    public void setHarvestAmount(Integer amount) {
        Log.d("LOK_HARVEST_MODEL", ""+amount);
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
