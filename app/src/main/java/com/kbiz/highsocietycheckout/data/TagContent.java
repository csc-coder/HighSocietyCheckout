package com.kbiz.highsocietycheckout.data;

import androidx.lifecycle.ViewModel;

import java.util.ArrayList;

public class TagContent extends ViewModel {
    private long readTime=-1;
    private ArrayList<String> nDefRecords;
    public ArrayList<String> getnDefRecords() {
        return nDefRecords;
    }

    public void setnDefRecords(ArrayList<String> nDefRecords) {
        this.nDefRecords = nDefRecords;
        readTime=System.nanoTime();
    }

    public long getReadTime() {
        return readTime;
    }

    public boolean isEmpty() {
        return this.nDefRecords ==null || this.nDefRecords.isEmpty();
    }
}
