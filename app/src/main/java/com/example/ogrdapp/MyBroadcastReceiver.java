package com.example.ogrdapp;

import static android.content.Context.MODE_PRIVATE;
import static com.example.ogrdapp.utility.SharedPreferencesDataSource.KEY_IS_PAUSED;
import static com.example.ogrdapp.utility.SharedPreferencesDataSource.KEY_TIMER_STARTED;

import static com.example.ogrdapp.utility.SharedPreferencesDataSource.SHARED_PREFS_OGROD_APP;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.ogrdapp.services.ForegroundServices;
import com.example.ogrdapp.utility.SharedPreferencesDataSource;

public class MyBroadcastReceiver extends BroadcastReceiver {

    private SharedPreferencesDataSource sharedPreferencesDataSource=  SharedPreferencesDataSource.getInstance();
    @Override
    public void onReceive(Context context, Intent intent) {

        Log.i("ACTION",intent.getAction());
        Log.i("Start time Broadcast",getIsTimeStarted(context)+"");
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())&& (getIsTimeStarted(context) || getIsTimePaused(context) )) {
            Intent i = new Intent(context, ForegroundServices.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startService(i);
        }
    }

    private boolean getIsTimePaused(Context context) {
        return sharedPreferencesDataSource.getIsPausedFromSharedPreferences();
    }

    private boolean getIsTimeStarted(Context context) {
        return sharedPreferencesDataSource.getIsTimerStartedFromSharedPreferences();
    }
}
