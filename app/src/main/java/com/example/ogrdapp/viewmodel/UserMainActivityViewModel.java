package com.example.ogrdapp.viewmodel;

import android.app.Application;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.ogrdapp.repository.AuthRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.Serializable;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

public class UserMainActivityViewModel extends ViewModel implements Serializable {
    private long timeLong=0;
    private static int counter = 0;
    private Timer timer;
    private TimerTask timerTask;
    private MutableLiveData<Long> timerLiveData = new MutableLiveData<>();


    private long startTimerTask() {

        if(timerTask!=null)
        {
            timerTask.cancel();
        }
        timer = new Timer();

        timerTask = new TimerTask() {
            @Override
            public void run() {
                timerLiveData.postValue(timeLong++);
            }
        };
        timer.scheduleAtFixedRate(timerTask,0, 1);
        return timeLong;
    }
    public long startTimerSecondTime() {

        if(timerTask!=null)
        {
            try {
                timerTask.cancel();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        startTimerTask();
        /*timer = new Timer();

        timerTask = new TimerTask() {
            @Override
            public void run() {
                timerLiveData.postValue(timeLong++);
            }
        };
        timer.scheduleAtFixedRate(timerTask, 0, 1000);*/
        return timeLong;
    }
    public void stopTimerTask() {
        if(timerTask!=null)
        {
            timerTask.cancel();
        }

    }

    public long getTimeLong() {
        return timeLong;
    }

    public void setValue(long longTimeFromBroadcastReceiver) {
        timeLong = longTimeFromBroadcastReceiver;
        timerLiveData.postValue(longTimeFromBroadcastReceiver);
    }




}
