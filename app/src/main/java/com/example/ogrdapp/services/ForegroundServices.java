package com.example.ogrdapp.services;

import static com.example.ogrdapp.App.CHANNEl_ID;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.ogrdapp.R;
import com.example.ogrdapp.UserMainActivity;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ForegroundServices extends Service {

    private long time;
    String stop ="";
    private Timer timer;
    // First time when we create service
    TimerTask timerTask;
    @Override
    public void onCreate() {
        super.onCreate();
    }

    //Intent will be text from editText
    // Callled every time is called start service

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent!=null) {
            final Long[] input = {intent.getLongExtra("TimeValue", 0)};

            if(!UserMainActivity.active)
            {
                timer = new Timer();

                timerTask = new TimerTask() {
                    @Override
                    public void run() {
                        notificationUpdate(input[0]++);
                        time = input[0];


                        if(UserMainActivity.flagForForegroundService)
                        {
                            Intent intent1 = new Intent();
                            intent1.setAction("Counter");
                            intent1.putExtra("TimeRemaining", time);
                            Log.i("WHAT I AM SENDING", time+"");
                            sendBroadcast(intent1);
                            UserMainActivity.flagForForegroundService = false;
                            Log.i("HOW MANY TIMES","HOW MANY TIMES");
                        }
                    }
                };
                timer.scheduleAtFixedRate(timerTask, 0, 1000);

            }



            //timer = new Timer();
           /* if(!UserMainActivity.active) {
                timer.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
//                        Intent intent1 = new Intent();
  //                      intent1.setAction("Counter");

                        notificationUpdate(input[0]);

                        time = input[0]++;

                        *//*intent1.putExtra("TimeRemaining", input[0]);
                        sendBroadcast(intent1);*//*
                         Log.i("Here I am extecuting", input[0] + " ");

                    }
                }, 0, 1000);

            }*/
/*

            if(UserMainActivity.active) {

                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        timer.cancel();
                        Intent intent1 = new Intent();
                        intent1.setAction("Counter");
                        intent1.putExtra("TimeRemaining", time);
                        sendBroadcast(intent1);
                        Log.i("EXECTUING BROADCAST", getTimerText(time));
                    }

                });
            }
*/





            //time = input[0];


        /*String timerText = getTimerText(time);

        Intent notificationIntent = new Intent(this,MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new NotificationCompat.Builder(this,CHANNEl_ID)
                .setContentTitle("Exmaple Service")
                .setContentText(timerText)
                .setSmallIcon(R.drawable.ic_android)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setAutoCancel(false)
                .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
                .build();
        startForeground(1,notification);*/
        }
        //return START_NOT_STICKY;
        return super.onStartCommand(intent,flags,startId);
    }

    public void notificationUpdate(Long time)
    {
        try {
            Intent notificationIntent = new Intent(this, UserMainActivity.class);
            final PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

            final Notification[] notification = {new NotificationCompat.Builder(this, CHANNEl_ID)
                    .setOngoing(true)
                    .setContentTitle("My timer")
                    .setContentText("Time : " + getTimerText(time))
                    .setSmallIcon(R.drawable.time24_vector)
                    .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
                    .setContentIntent(pendingIntent)
                    .build()};

            startForeground(1, notification[0]);
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEl_ID, "My timer", NotificationManager.IMPORTANCE_LOW);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(notificationChannel);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }


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
