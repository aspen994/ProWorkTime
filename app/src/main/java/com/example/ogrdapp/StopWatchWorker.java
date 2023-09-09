package com.example.ogrdapp;

import static com.example.ogrdapp.UserMainActivity.timerStarted;
import static com.example.ogrdapp.services.ForegroundServices.isPaused;
import static com.example.ogrdapp.utility.SharedPreferencesConstants.KEY_IS_PAUSED;
import static com.example.ogrdapp.utility.SharedPreferencesConstants.KEY_TIMER_STARTED;
import static com.example.ogrdapp.utility.SharedPreferencesConstants.SHARED_PREFS_OGROD_APP;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.work.ListenableWorker;
import androidx.work.WorkerParameters;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.example.ogrdapp.services.ForegroundServices;

public class StopWatchWorker extends Worker {
    Context context;
    Intent intent;

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

        //if(ServiceHelper.isCountingTimeActive&&!ForegroundServices.isServiceStarted)

        if(timerStarted || isPaused)
        {
            context.startService(intent);
            Log.i("START SERVICE","START SERVICE");

            return ListenableWorker.Result.retry();
        }

        //else if (!ServiceHelper.isCountingTimeActive)

        else if (!timerStarted && !isPaused) {
            context.stopService(intent);
            Log.i("STOP SERVICE","STOP SERVICE");
            return ListenableWorker.Result.success();
        }

        return ListenableWorker.Result.success();
    }

    @Override
    public void onStopped() {
        Toast.makeText(context, "Worker stopped", Toast.LENGTH_SHORT).show();
        super.onStopped();
    }

    private boolean getIsPausedFromSharedPreferences() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFS_OGROD_APP,Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(KEY_IS_PAUSED,false);
    }

    private boolean getIsTimerStartedFromSharedPreferences() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFS_OGROD_APP,Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(KEY_TIMER_STARTED,false);
    }

}
