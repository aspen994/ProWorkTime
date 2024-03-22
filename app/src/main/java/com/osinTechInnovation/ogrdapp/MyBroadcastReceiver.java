package com.osinTechInnovation.ogrdapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.osinTechInnovation.ogrdapp.services.ForegroundServices;
import com.osinTechInnovation.ogrdapp.utility.SharedPreferencesDataSource;

public class MyBroadcastReceiver extends BroadcastReceiver {

    private SharedPreferencesDataSource sharedPreferencesDataSource=  SharedPreferencesDataSource.getInstance();
    @Override
    public void onReceive(Context context, Intent intent) {

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
