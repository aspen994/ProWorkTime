package com.example.ogrdapp;

import static android.content.Context.MODE_PRIVATE;

import android.app.Activity;
import android.content.SharedPreferences;

public class ServiceHelper extends Activity {

    public static boolean isCountingTimeActive=false;
    public boolean isServiceStarted = loadAndUpdateData();

    public boolean loadAndUpdateData() {

      /*  saveDataToSharePreff(false);

        SharedPreferences sharedPreference = getSharedPreferences(SHARED_PREFS_SERVICE_HELPER,MODE_PRIVATE);

        return sharedPreference.getBoolean(KEY_PREFS_SERVICE_STARTED, false);*/
        return false;

    }
   /* private void saveDataToSharePreff(boolean isServiceStarted) {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS_SERVICE_HELPER,MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_PREFS_SERVICE_STARTED,isServiceStarted);
        editor.apply();
    }

    public boolean isServiceStarted() {
        return isServiceStarted;
    }

    public void setServiceStarted(boolean serviceStarted) {
        isServiceStarted = serviceStarted;
        saveDataToSharePreff(serviceStarted);
    }*/
}
