package com.example.ogrdapp.viewmodel;

import android.app.Application;
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

     /*   Long aLong = timeLong;
        final long[] delay = {1000 - (aLong % 1000)};
        if(delay[0] ==1000)
        {
            delay[0] =0;
        }*/

        timerTask = new TimerTask() {
            @Override
            public void run() {
                //timeLong++;
                timerLiveData.postValue(timeLong++);
                //Log.i("Time units: ",timeLong+"");
                //delay[0] =0;
            }
        };
        timer.scheduleAtFixedRate(timerTask,0, 1);
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
        Log.i("I am exectuing from Start TImer second Time","");

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
        //timerLiveData.setValue(longTimeFromBroadcastReceiver);
    }




}
