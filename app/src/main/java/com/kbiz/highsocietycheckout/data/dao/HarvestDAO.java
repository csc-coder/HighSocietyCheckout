package com.kbiz.highsocietycheckout.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.kbiz.highsocietycheckout.data.entities.Harvest;

import java.util.List;

@Dao
public interface HarvestDAO {
    @Insert
    void insert(Harvest harvest);

    @Query("SELECT * FROM harvests")
    LiveData<List<Harvest>> getAllHarvests();
}
