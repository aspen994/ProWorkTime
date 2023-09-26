package com.example.ogrdapp;

import static android.content.Context.MODE_PRIVATE;

import android.app.Activity;
import android.content.SharedPreferences;

public class ServiceHelper extends Activity {

    public static boolean isCountingTimeActive=false;
    public boolean isServiceStarted = loadAndUpdateData();

    public boolean loadAndUpdateData() {
        return false;
    }

}
