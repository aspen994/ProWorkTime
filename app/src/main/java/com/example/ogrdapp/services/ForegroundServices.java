package com.example.ogrdapp.services;

import static com.example.ogrdapp.App.CHANNEl_ID;
import static com.example.ogrdapp.UserMainActivity.timerStarted;
import static com.example.ogrdapp.utility.SharedPreferencesConstants.KEY_CHECKING_HOW_MANY_TIMES_SERVICE_RUN;
import static com.example.ogrdapp.utility.SharedPreferencesConstants.KEY_IS_PAUSED;
import static com.example.ogrdapp.utility.SharedPreferencesConstants.KEY_PREFS_SERVICE_STARTED;
import static com.example.ogrdapp.utility.SharedPreferencesConstants.KEY_TIMER_STARTED;
import static com.example.ogrdapp.utility.SharedPreferencesConstants.KEY_TIME_OF_CREATION;
import static com.example.ogrdapp.utility.SharedPreferencesConstants.SHARED_PREFS_FOR_TIME_CREATION_ONLY;
import static com.example.ogrdapp.utility.SharedPreferencesConstants.SHARED_PREFS_OGROD_APP;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ServiceInfo;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.MutableLiveData;

import com.example.ogrdapp.R;
import com.example.ogrdapp.UserMainActivity;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class ForegroundServices extends Service {

    public static final int NOTIFICATION_ID = 0;

    public static MutableLiveData<Long> time = new MutableLiveData<>(0L);
    public static MutableLiveData<Long> mutableLiveDataTimeForPause = new MutableLiveData<>(0L);

    public static long timeLongForClock = 0;
    public static long timeLongForPause = 0;
    public static boolean isPaused;
    private long startPostStamp;
    private Timer timer;
    // First time when we create service
    TimerTask timerTask;
    //View Model
    //Way to count time in foregroundService properly
    PowerManager.WakeLock wakeLock;
    long timeOfCreation;

    private Handler handler;

    public static boolean isServiceStarted =false;
    public int howManyTimesServiceStarted = 0;
    public static final int HOUR_IN_SECONDS = 3600;
    public static final int MINUTE_IN_SECONDS = 60;




    @Override
    public void onCreate() {

        Toast.makeText(this, "Creating a service", Toast.LENGTH_SHORT).show();
        isServiceStarted = loadAndUpdateServiceStartedFromSharedPreferences();
        timeOfCreation = loadAndUpdateTimeCreationFromSharedPreferences();
        timerStarted = getIsTimerStartedFromSharedPreferences();
        isPaused=getIsPausedFromSharedPreferences();
        isServiceStarted=true;

        // POWER MANAGER
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "MyApp::MyWakelockTag");


        countingWorkTimeAndPausedTime();

        super.onCreate();
    }



    private void countingWorkTimeAndPausedTime() {
        HandlerThread handlerThread = new HandlerThread("StopWatchThreadOgrodApp");
        handlerThread.start();
        handler = new android.os.Handler(handlerThread.getLooper());
        Toast.makeText(this, "CountingTimeAndPaused", Toast.LENGTH_SHORT).show();


            handler.post(new Runnable() {
                @Override
                public void run() {
                    timeOfCreation = loadAndUpdateTimeCreationFromSharedPreferences();
                    if(timerStarted) {

                        long currentTimeMillis = new Date().getTime();
                        long toPost = (currentTimeMillis - timeOfCreation) / 1000;

                        notificationUpdate(toPost, "Pracujesz już: ");

                        handler.postDelayed(this, 1000);
                    }
                     else if(isPaused)
                    {

                        long currentTimeMillis = new Date().getTime();
                        long toPost = (currentTimeMillis - timeOfCreation) / 1000;


                        if(toPost<=8*HOUR_IN_SECONDS) {
                            notificationUpdate(toPost, "Odpoczywasz już: ");
                            handler.postDelayed(this, 1000);
                        }
                        else{
                            handler.removeCallbacksAndMessages(null);
                            /*isPaused=false;
                            saveIsPausedToSharedPreferences(isPaused);*/
                        }
                    }
                    else {
                        handler.removeCallbacksAndMessages(null);
                    }
                }
            });


    }

    //Shared Preferences block
    // TODO Make a Layer for SharedPreferences https://www.youtube.com/watch?v=EWIlxY-_pDY&ab_channel=CodingReel
    public void saveIsPausedToSharedPreferences(boolean isPaused)
    {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS_OGROD_APP,MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_IS_PAUSED,isPaused);
        editor.apply();
    }
    private boolean getIsPausedFromSharedPreferences() {
     SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS_OGROD_APP,MODE_PRIVATE);
     return sharedPreferences.getBoolean(KEY_IS_PAUSED,false);
    }


    private boolean getIsTimerStartedFromSharedPreferences() {
        SharedPreferences sharedPreferencesTimeModel = getSharedPreferences(SHARED_PREFS_OGROD_APP,MODE_PRIVATE);
        return sharedPreferencesTimeModel.getBoolean(KEY_TIMER_STARTED, false);
    }


    private boolean loadAndUpdateServiceStartedFromSharedPreferences() {
        SharedPreferences sharedPreferencesTimeModel = getSharedPreferences(SHARED_PREFS_OGROD_APP,MODE_PRIVATE);
        return sharedPreferencesTimeModel.getBoolean(KEY_PREFS_SERVICE_STARTED, false);
    }


    private long loadAndUpdateTimeCreationFromSharedPreferences() {
            SharedPreferences sharedPreferencesTimeModel = getSharedPreferences(SHARED_PREFS_OGROD_APP,MODE_PRIVATE);
            return sharedPreferencesTimeModel.getLong(KEY_TIME_OF_CREATION, 0);
    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }



    //TODO make a layer for notification updated
    public void notificationUpdate(long time,String text)
    {
        startPostStamp = loadAndUpdateTimeCreationFromSharedPreferences();


        try {
            Intent notificationIntent = new Intent(this, UserMainActivity.class);
            final PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

            final Notification notification = new NotificationCompat.Builder(this, CHANNEl_ID)
                    .setOngoing(true)
                    .setContentTitle(text.equals("Pracujesz już: ")?"Życzę miłej pracy, Szymon:) ":"Dobrze jest odpocząć :)")
                    .setContentText(text + getTimerText(time))
                    .setWhen(startPostStamp)
                    .setSmallIcon(R.drawable.time24_vector)
                    .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
                    .setContentIntent(pendingIntent)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setColor(Color.GREEN)
                    .setColorized(true)
                    .build();


            NotificationChannel notificationChannel = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                notificationChannel = new NotificationChannel(CHANNEl_ID, "My timer", NotificationManager.IMPORTANCE_LOW);
            }
            NotificationManager notificationManager = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                notificationManager = getSystemService(NotificationManager.class);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                notificationManager.createNotificationChannel(notificationChannel);
            }

            // TODO I've changed it.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION);
            }
            else{
                startForeground(1,notification);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    // Block for timerText
    // Todo Make a layer for timerText
    private String getTimerText(long totalSecs)
    {

        long hours = totalSecs / 3600;
        long minutes = (totalSecs % 3600) / 60;
        long seconds = totalSecs % 60;

        return formatTime(seconds, minutes, hours);
    }


    private String formatTime(long seconds, long minutes, long hours)
    {
        return String.format("%02d",hours) + " : " + String.format("%02d",minutes) + " : " + String.format("%02d",seconds);
    }

    // When stop is called on destroy
    @Override
    public void onDestroy() {
        Toast.makeText(this, "On destroy service", Toast.LENGTH_SHORT).show();
        if(wakeLock.isHeld())
        {
            wakeLock.release();
        }
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


}