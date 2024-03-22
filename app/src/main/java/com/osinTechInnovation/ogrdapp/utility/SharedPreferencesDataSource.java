package com.osinTechInnovation.ogrdapp.utility;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesDataSource {

    public static final String SHARED_PREFS_OGROD_APP = "sharedPrefsOgrodAPPv2";
    public static final String PAUSED_TIME = "pausedTime";
    public static final String PAUSED_TIME_BOOLEAN = "booleanIsPaused";

    public static final String KEY_PREFS_SERVICE_STARTED = "KEY_PREFS_SERVICE_STARTED";
    public static final String KEY_TIME_OF_CREATION = "LONG_KEY_FOR_TIME_CREATION";
    public static final String TMP_BEGIN_TIME_STRING = "tmp_Begin_Time_String_FORMAT";
    public static final String TMP_BEGIN_TIME = "tmp_Begin_Time";
    public static final String KEY_TIMER_STARTED ="isTimerStarted";
    public static final String KEY_IS_PAUSED ="keyIsPaused";
    public static final String TEXT = "text";

    public static SharedPreferencesDataSource instance;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;


    public static SharedPreferencesDataSource getInstance()
    {
        if(instance==null) {
            instance = new SharedPreferencesDataSource();
        }
        return instance;
    }

    public void init(Context context)
    {
        sharedPreferences = context.getSharedPreferences(SHARED_PREFS_OGROD_APP,Context.MODE_PRIVATE);
        editor =sharedPreferences.edit();
    }

    public void saveIsPausedToSharedPreferences(boolean isPaused)
    {
        editor.putBoolean(KEY_IS_PAUSED,isPaused);
        editor.apply();
    }

    public boolean getIsPausedFromSharedPreferences() {
        return sharedPreferences.getBoolean(KEY_IS_PAUSED,false);
    }
    public void saveCreationTimeToSharedPref(long time) {
        editor.putLong(KEY_TIME_OF_CREATION,time);
        editor.apply();
    }

    public long getTimeOfCreationFromSharedPreferences()
    {
        return sharedPreferences.getLong(KEY_TIME_OF_CREATION,0);
    }

    public void saveIsTimeStartedToSharedPreferences(boolean isStopWatchActive) {
        editor.putBoolean(KEY_TIMER_STARTED,isStopWatchActive);
        editor.apply();
    }

    public boolean getIsTimerStartedFromSharedPreferences() {
        return sharedPreferences.getBoolean(KEY_TIMER_STARTED,false);
    }

    public void saveTimeModelToSharedPreferences(String currentTime) {
        editor.putString(TMP_BEGIN_TIME_STRING,currentTime);
        editor.apply();
    }

    public String loadAndUpdatedTimeModelFromSharedPreferences()
    {
        return sharedPreferences.getString(TMP_BEGIN_TIME_STRING, "currentTime");
    }

    public void saveDataToSharedPreferences(String beginTime,boolean timerStarted,long tmpBeginTime)
    {
        editor.putString(TEXT,beginTime);
        editor.putBoolean(KEY_TIMER_STARTED,timerStarted);
        editor.putLong(TMP_BEGIN_TIME,tmpBeginTime);
        editor.apply();
    }

    public void savePausedTimeToSharedPreferences(long pausedTime,boolean isPaused)
    {
        editor.putLong(PAUSED_TIME,pausedTime);
        editor.putBoolean(PAUSED_TIME_BOOLEAN,isPaused);
        editor.apply();
    }
    public void clearDataToSharedPreferences()
    {
        editor.putString(TEXT,"");
        editor.putBoolean(KEY_TIMER_STARTED,false);
        editor.putLong(TMP_BEGIN_TIME,0);
        editor.apply();
    }

    public void cleanDataForTimeModelToSharedPreferences() {
        editor.putString(TMP_BEGIN_TIME_STRING,"");
        editor.apply();
    }

    public boolean loadAndUpdateServiceStartedFromSharedPreferences() {
        return sharedPreferences.getBoolean(KEY_PREFS_SERVICE_STARTED, false);
    }

    public long loadAndUpdateTimeCreationFromSharedPreferences() {
        return sharedPreferences.getLong(KEY_TIME_OF_CREATION, 0);
    }

    public long getPausedTimeFromSharedPreferences()
    {
        return sharedPreferences.getLong(PAUSED_TIME,0);
    }

    public boolean getIsTimePausedFromSharedPreferences()
    {
        return sharedPreferences.getBoolean(PAUSED_TIME_BOOLEAN,false);
    }

    public String getTextFromSharedPreferences(String text)
    {
        return sharedPreferences.getString(TEXT,text);
    }


    public long getTmpBeginTimeFromSharedPreferences(long tmpBeginTime) {
        return  sharedPreferences.getLong(TMP_BEGIN_TIME,tmpBeginTime);
    }
}
