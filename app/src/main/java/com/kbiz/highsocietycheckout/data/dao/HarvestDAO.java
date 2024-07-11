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

    @Query("SELECT IFNULL(SUM(amount), 0) FROM harvests WHERE user_hash = :userHash AND strftime('%Y-%m', datetime(time / 1000, 'unixepoch')) = strftime('%Y-%m', 'now')")
    LiveData<Long> getTotalHarvestForCurrentMonth(String userHash);
}
