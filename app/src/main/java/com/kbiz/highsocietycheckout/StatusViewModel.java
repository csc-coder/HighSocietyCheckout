// StatusViewModel.java
package com.kbiz.highsocietycheckout;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class StatusViewModel extends ViewModel {
    private final MutableLiveData<String> statusText = new MutableLiveData<>();

    public void setStatusText(String text) {
        statusText.postValue(text);
    }

    public LiveData<String> getStatusText() {
        return statusText;
    }
}
