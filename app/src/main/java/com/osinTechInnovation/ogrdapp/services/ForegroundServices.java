package com.osinTechInnovation.ogrdapp.services;

import static com.osinTechInnovation.ogrdapp.App.CHANNEL_ID;
import static com.osinTechInnovation.ogrdapp.UserMainActivity.timerStarted;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.osinTechInnovation.ogrdapp.R;
import com.osinTechInnovation.ogrdapp.UserMainActivity;
import com.osinTechInnovation.ogrdapp.utility.SharedPreferencesDataSource;

import java.util.Date;

public class ForegroundServices extends Service {

    public static boolean isServiceStarted = false;
    public static final int HOUR_IN_SECONDS = 3600;
    public static boolean isPaused = false;
    private long startPostStamp;
    private long timeOfCreation;
    private Handler handler;
    private SharedPreferencesDataSource sharedPreferencesDataSource = SharedPreferencesDataSource.getInstance();


    @Override
    public void onCreate() {
        isServiceStarted = loadAndUpdateServiceStartedFromSharedPreferences();
        timeOfCreation = loadAndUpdateTimeCreationFromSharedPreferences();
        timerStarted = getIsTimerStartedFromSharedPreferences();
        isPaused = getIsPausedFromSharedPreferences();
        isServiceStarted = true;

        countingWorkTimeAndPausedTime();

        super.onCreate();
    }


    private void countingWorkTimeAndPausedTime() {
        HandlerThread handlerThread = new HandlerThread("StopWatchThreadOgrodApp");
        handlerThread.start();
        handler = new android.os.Handler(handlerThread.getLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                timeOfCreation = loadAndUpdateTimeCreationFromSharedPreferences();
                if (timerStarted) {

                    long currentTimeMillis = new Date().getTime();
                    long toPost = (currentTimeMillis - timeOfCreation) / 1000;

                    notificationUpdate(toPost, getString(R.string.service_working_status));

                    handler.postDelayed(this, 1000);
                } else if (isPaused) {

                    long currentTimeMillis = new Date().getTime();
                    long toPost = (currentTimeMillis - timeOfCreation) / 1000;

                    if (toPost <= 8 * HOUR_IN_SECONDS) {
                        notificationUpdate(toPost, getString(R.string.service_pause_status));
                        handler.postDelayed(this, 1000);
                    } else {
                        handler.removeCallbacksAndMessages(null);
                    }
                } else {
                    handler.removeCallbacksAndMessages(null);
                }
            }
        });
    }

    private boolean getIsPausedFromSharedPreferences() {
        return sharedPreferencesDataSource.getIsPausedFromSharedPreferences();
    }

    private boolean getIsTimerStartedFromSharedPreferences() {
        return sharedPreferencesDataSource.getIsTimerStartedFromSharedPreferences();
    }


    private boolean loadAndUpdateServiceStartedFromSharedPreferences() {
        return sharedPreferencesDataSource.loadAndUpdateServiceStartedFromSharedPreferences();
    }

    private long loadAndUpdateTimeCreationFromSharedPreferences() {
        return sharedPreferencesDataSource.loadAndUpdateTimeCreationFromSharedPreferences();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    public void notificationUpdate(long time, String text) {

        startPostStamp = loadAndUpdateTimeCreationFromSharedPreferences();

        try {
            Intent notificationIntent = new Intent(this, UserMainActivity.class);
            final PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

            final Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setOngoing(true)
                    .setContentTitle(text.equals(getString(R.string.service_working_status)) ? getString(R.string.service_wishes) : getString(R.string.service_rest_wishes))
                    .setContentText(text + getTimerText(time))
                    .setWhen(startPostStamp)
                    .setSmallIcon(R.drawable.time24_vector)
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.osin_logo_foreground))
                    .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
                    .setContentIntent(pendingIntent)
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                    .setColor(Color.rgb(63, 120, 76))
                    .setColorized(true)
                    .build();


            NotificationChannel notificationChannel = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                notificationChannel = new NotificationChannel(CHANNEL_ID, "My timer", NotificationManager.IMPORTANCE_LOW);
            }
            NotificationManager notificationManager = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                notificationManager = getSystemService(NotificationManager.class);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                notificationManager.createNotificationChannel(notificationChannel);
            }


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION);
            } else {
                startForeground(1, notification);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Block for timerText

    private String getTimerText(long totalSecs) {

        long hours = totalSecs / 3600;
        long minutes = (totalSecs % 3600) / 60;
        long seconds = totalSecs % 60;

        return formatTime(seconds, minutes, hours);
    }


    private String formatTime(long seconds, long minutes, long hours) {
        return String.format("%02d", hours) + " : " + String.format("%02d", minutes) + " : " + String.format("%02d", seconds);
    }

    // When stop is called on destroy
    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


}