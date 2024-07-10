// StatusViewModel.java
package com.kbiz.highsocietycheckout.data;

import android.app.Application;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class StatusViewModel extends AndroidViewModel {
    private final MutableLiveData<String> statusText = new MutableLiveData<>();

    public StatusViewModel(@NonNull Application application) {
        super(application);
    }

    public void setStatusText(String text) {
        Log.d("LOK", text);
//        try{
//            Toast.makeText(getApplication(), text, Toast.LENGTH_SHORT).show();
//        } catch(Exception e){
//            /*NOOP eat exception*/
//        }
        statusText.postValue(text);
    }

    public LiveData<String> getStatusText() {
        return statusText;
    }
    public void clear() {
        statusText.postValue("");
    }

}
