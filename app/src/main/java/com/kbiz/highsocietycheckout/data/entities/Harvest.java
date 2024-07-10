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
    public double amount;

    // Constructor, getters, and setters
}
