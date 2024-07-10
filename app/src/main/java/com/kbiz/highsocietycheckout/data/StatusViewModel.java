// StatusViewModel.java
package com.kbiz.highsocietycheckout.data;

import android.app.Application;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.Locale;

public class StatusViewModel extends AndroidViewModel {
    private final MutableLiveData<String> statusText = new MutableLiveData<>();
    private final MutableLiveData<String> log = new MutableLiveData<>();

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
        String logLine=getCurrentTimestamp() + "\t-\t" +text;

        String newLog = log.getValue() == null ? logLine : log.getValue() + "\n" + logLine;
        //maintain 5k log
        log.postValue(newLog.length() > 5000 ? newLog.substring(newLog.length() - 5000) : newLog);
        statusText.postValue(text);
    }

    public LiveData<String> getStatusText() {
        return statusText;
    }

    public MutableLiveData<String> getLog() {
        return log;
    }

    public void setLog(String text) {
        log.postValue(text);
    }

    public void clear() {
        statusText.postValue("");
    }

    public static String getCurrentTimestamp() {
        // Get the current date and time
        Date now = new Date();

        // Define the format for the timestamp
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

        // Format the current date and time
        return sdf.format(now);
    }
}
