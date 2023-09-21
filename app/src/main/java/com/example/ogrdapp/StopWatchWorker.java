package com.example.ogrdapp;

import static com.example.ogrdapp.UserMainActivity.timerStarted;
import static com.example.ogrdapp.services.ForegroundServices.isPaused;
import static com.example.ogrdapp.utility.SharedPreferencesDataSource.KEY_IS_PAUSED;
import static com.example.ogrdapp.utility.SharedPreferencesDataSource.KEY_TIMER_STARTED;
import static com.example.ogrdapp.utility.SharedPreferencesDataSource.SHARED_PREFS_OGROD_APP;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.ListenableWorker;
import androidx.work.WorkerParameters;

import androidx.work.Worker;

import com.example.ogrdapp.services.ForegroundServices;
import com.example.ogrdapp.utility.SharedPreferencesDataSource;

public class StopWatchWorker extends Worker {
    Context context;
    Intent intent;

    private SharedPreferencesDataSource sharedPreferencesDataSource=  SharedPreferencesDataSource.getInstance();

    public StopWatchWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context;
    }


    @NonNull
    @Override
    public ListenableWorker.Result doWork() {
        intent = new Intent(context, ForegroundServices.class);
        timerStarted = getIsTimerStartedFromSharedPreferences();
        isPaused=getIsPausedFromSharedPreferences();

        if(timerStarted || isPaused)
        {
            context.startService(intent);
            Log.i("START SERVICE","START SERVICE");

            return ListenableWorker.Result.retry();
        }

        else if (!timerStarted && !isPaused) {
            context.stopService(intent);
            Log.i("STOP SERVICE","STOP SERVICE");
            return ListenableWorker.Result.success();
        }

        return ListenableWorker.Result.success();
    }

    @Override
    public void onStopped() {
        super.onStopped();
    }

    private boolean getIsPausedFromSharedPreferences() {
        /*SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFS_OGROD_APP,Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(KEY_IS_PAUSED,false);*/
        return sharedPreferencesDataSource.getIsPausedFromSharedPreferences();
    }

    private boolean getIsTimerStartedFromSharedPreferences() {
        /*SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFS_OGROD_APP,Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(KEY_TIMER_STARTED,false);*/

        return sharedPreferencesDataSource.getIsTimerStartedFromSharedPreferences();
    }

}
