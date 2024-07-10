package com.kbiz.highsocietycheckout.data.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "users")

public class User {
    @PrimaryKey
    @NonNull
    public String userHash;

    public User(@NonNull String userHash) {
        this.userHash = userHash;
    }
}
