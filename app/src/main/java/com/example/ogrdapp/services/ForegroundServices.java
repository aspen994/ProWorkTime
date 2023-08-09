package com.example.ogrdapp.services;

import static com.example.ogrdapp.App.CHANNEl_ID;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.ogrdapp.R;
import com.example.ogrdapp.UserMainActivity;

import java.util.Timer;
import java.util.TimerTask;

public class ForegroundServices extends Service   {

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



    @Override
    public void onCreate() {
        super.onCreate();
    }

    //Intent will be text from editText
    // Callled every time is called start service

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

                timer = new Timer();

                timerTask = new TimerTask() {
                    @Override
                    public void run() {

                        if(isPaused==false) {
                            //Updatding notifiaction starts from 0
                            notificationUpdate(timeLongForClock,"Pracujesz już: ");
                            // Increasing value about one second
                            time.postValue(timeLongForClock++);
                        }
                        else{
                            notificationUpdate(timeLongForPause,"Pauza: ");
                         mutableLiveDataTimeForPause.postValue(timeLongForPause++);
                        }

                    }
                };
                timer.scheduleAtFixedRate(timerTask,0, 1000);

                return START_STICKY;
            }


    public void notificationUpdate(Long time,String text)
    {

        if(time==0)
        {
            startPostStamp = System.currentTimeMillis();
        }

        try {
            Intent notificationIntent = new Intent(this, UserMainActivity.class);
            final PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

            final Notification[] notification = {new NotificationCompat.Builder(this, CHANNEl_ID)
                    .setOngoing(true)
                    .setContentTitle(text.equals("Pracujesz już: ")?"Życzę miłej pracy, Szymon:) ":"Dobrze jest odpocząć :)")
                    .setWhen(startPostStamp)
                    .setContentText(text + getTimerText(time))
                    .setSmallIcon(R.drawable.time24_vector)
                    .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
                    .setContentIntent(pendingIntent)
                    .build()};

            startForeground(1, notification[0]);
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
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }



/*
    private String getTimerText(Long time)
    {
        int rounded = (int) Math.round(time);

        int seconds = ((rounded % 86400) % 3600) % 60;
        int minutes = ((rounded % 86400) % 3600) / 60;
        int hours = ((rounded % 86400) / 3600);

        return formatTime(seconds, minutes, hours);
    }


    private String formatTime(int seconds, int minutes, int hours)
    {
        return String.format("%02d",hours) + " : " + String.format("%02d",minutes) + " : " + String.format("%02d",seconds);
    }
*/

    private String getTimerText(long timeInSeconds)
    {
        int rounded = (int) Math.round(timeInSeconds);

        int seconds = ((rounded % 86400) % 3600) % 60;
        int minutes = ((rounded % 86400) % 3600) / 60;
        int hours = ((rounded % 86400) / 3600);

        return formatTime(seconds, minutes, hours);
    }


    private String formatTime(int seconds, int minutes, int hours)
    {
        return String.format("%02d",hours) + " : " + String.format("%02d",minutes) + " : " + String.format("%02d",seconds);
    }

    // When stop is called on destroy
    @Override
    public void onDestroy() {
        super.onDestroy();
        timer.cancel();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


}