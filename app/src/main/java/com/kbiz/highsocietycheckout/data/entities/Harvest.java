package com.kbiz.highsocietycheckout.data.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "harvests")
public class Harvest {
    @PrimaryKey
    @NonNull
    public String harvestId;
    public String userId;
    public long time;
    public long amount;

    public Harvest(@NonNull String harvestId, String userId, long time, long amount) {
        this.harvestId = harvestId;
        this.userId = userId;
        this.time = time;
        this.amount = amount;
    }

    // Constructor, getters, and setters
}
