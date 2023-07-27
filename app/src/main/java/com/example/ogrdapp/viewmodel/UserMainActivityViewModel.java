package com.example.ogrdapp.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.ogrdapp.repository.AuthRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

public class UserMainActivityViewModel extends ViewModel {
    private long timeLong;
    private Timer timer;
    private TimerTask timerTask;
    private MutableLiveData<Long> timerLiveData = new MutableLiveData<>();
    //When user startTimer
    public void startTimer()
    {
        startTimerTask();

    }
    public MutableLiveData<Long> initialValue()
    {
        timerLiveData.setValue(timeLong);
        return timerLiveData;
    }

    private String getTimerText(long timeLong)
    {
        int rounded = (int) Math.round(timeLong);

        int seconds = ((rounded % 86400) % 3600) % 60;
        int minutes = ((rounded % 86400) % 3600) / 60;
        int hours = ((rounded % 86400) / 3600);

        return formatTime(seconds, minutes, hours);
    }

    private String formatTime(int seconds, int minutes, int hours)
    {
        return String.format("%02d",hours) + " : " + String.format("%02d",minutes) + " : " + String.format("%02d",seconds);
    }

    private long startTimerTask() {

        timer = new Timer();

        timerTask = new TimerTask() {
            @Override
            public void run() {
                timeLong++;
                timerLiveData.postValue(timeLong);
            }
        };
        timer.scheduleAtFixedRate(timerTask, 0, 1000);
        return timeLong;
    }
    public long startTimerSecondTime() {

        try {
            timerTask.cancel();
        }
        catch (Exception e)
        {
            e.printStackTrace();
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


    public void setValue(long longTimeFromBroadcastReceiver) {
        timeLong = longTimeFromBroadcastReceiver;
        timerLiveData.setValue(longTimeFromBroadcastReceiver);
    }




}
