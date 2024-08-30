package com.osinTechInnovation.ogrdapp;

import static com.osinTechInnovation.ogrdapp.UserMainActivity.timerStarted;
import static com.osinTechInnovation.ogrdapp.services.ForegroundServices.isPaused;

import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.work.ListenableWorker;
import androidx.work.WorkerParameters;

import androidx.work.Worker;

import com.osinTechInnovation.ogrdapp.services.ForegroundServices;
import com.osinTechInnovation.ogrdapp.utility.SharedPreferencesDataSource;

public class StopWatchWorker extends Worker {
    Context context;
    Intent intent;

    private SharedPreferencesDataSource sharedPreferencesDataSource=  SharedPreferencesDataSource.getInstance();

    public StopWatchWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context;
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    @NonNull
    @Override
    public ListenableWorker.Result doWork() {
        intent = new Intent(context, ForegroundServices.class);
        timerStarted = getIsTimerStartedFromSharedPreferences();
        isPaused=getIsPausedFromSharedPreferences();

        if(timerStarted || isPaused)
        {
            context.startService(intent);
            //context.startForegroundService(intent);


            return ListenableWorker.Result.retry();
        }

        else if (!timerStarted && !isPaused) {
            context.stopService(intent);
            return ListenableWorker.Result.success();
        }

        return ListenableWorker.Result.success();
    }

    @Override
    public void onStopped() {
        super.onStopped();
    }

    private boolean getIsPausedFromSharedPreferences() {
        return sharedPreferencesDataSource.getIsPausedFromSharedPreferences();
    }

    private boolean getIsTimerStartedFromSharedPreferences() {
        return sharedPreferencesDataSource.getIsTimerStartedFromSharedPreferences();
    }

}
