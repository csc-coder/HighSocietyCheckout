package com.kbiz.highsocietycheckout.data.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "harvests")
public class Harvest {
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "harvest_id")
    public String harvestId;

    @NonNull
    @ColumnInfo(name = "user_hash")
    public String userHash;

    @NonNull
    @ColumnInfo(name = "time")
    public long time;

    @NonNull
    @ColumnInfo(name = "amount")
    public long amount;

    public Harvest(@NonNull String harvestId, @NonNull String userHash, @NonNull long time, @NonNull long amount) {
        this.harvestId = harvestId;
        this.userHash = userHash;
        this.time = time;
        this.amount = amount;
    }
}
