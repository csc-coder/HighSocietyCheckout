package com.kbiz.highsocietycheckout.data.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "users")

public class User {
    @NonNull
    @PrimaryKey
    @ColumnInfo(name = "user_hash")
    public String userHash;

    public User(@NonNull String userHash) {
        this.userHash = userHash;
    }
}
