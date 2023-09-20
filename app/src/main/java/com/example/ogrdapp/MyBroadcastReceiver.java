package com.example.ogrdapp;

import static android.content.Context.MODE_PRIVATE;
import static com.example.ogrdapp.utility.SharedPreferencesConstants.KEY_IS_PAUSED;
import static com.example.ogrdapp.utility.SharedPreferencesConstants.KEY_TIMER_STARTED;

import static com.example.ogrdapp.utility.SharedPreferencesConstants.SHARED_PREFS_OGROD_APP;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.example.ogrdapp.services.ForegroundServices;

public class MyBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        Log.i("ACTION",intent.getAction());
        Log.i("Start time Broadcast",getIsTimeStarted(context)+"");
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())&& (getIsTimeStarted(context) || getIsTimePaused(context) )) {
            //Toast.makeText(context, "MY APP", Toast.LENGTH_SHORT).show();
            Intent i = new Intent(context, ForegroundServices.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startService(i);
        }
    }

    private boolean getIsTimePaused(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFS_OGROD_APP,MODE_PRIVATE);
        return sharedPreferences.getBoolean(KEY_IS_PAUSED,false);
    }

    private boolean getIsTimeStarted(Context context) {
        SharedPreferences sharedPreferencesTimeModel = context.getSharedPreferences(SHARED_PREFS_OGROD_APP,MODE_PRIVATE);
        return sharedPreferencesTimeModel.getBoolean(KEY_TIMER_STARTED, false);
    }
}
