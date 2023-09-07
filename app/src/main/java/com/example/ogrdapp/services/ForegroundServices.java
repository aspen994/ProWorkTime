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


        isServiceStarted = loadAndUpdateServiceStartedFromSharedPreferences();
        timeOfCreation = loadAndUpdateTimeCreationFromSharedPreferences();
        timerStarted = getIsTimerStartedFromSharedPreferences();
        isPaused=getIsPausedFromSharedPreferences();

        //FOR CHECKING LOGING PURPOSE ONLY
        howManyTimesServiceStarted = getHowManyTimesServiceStartedFromSharedPreferences();
        howManyTimesServiceStarted ++;
        Log.i("Times Service Started: ",howManyTimesServiceStarted+"");
        Log.i("Is timer started: ",timerStarted+"");
        saveHowManyTimesServiceStartedToSharedPreferences(howManyTimesServiceStarted);


        // POWER MANAGER
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "MyApp::MyWakelockTag");
      /*  isServiceStarted=true;
        if (isServiceStarted) {
            Toast.makeText(this, "Service started", Toast.LENGTH_SHORT).show();
            //deleteData();
            //timeOfCreation = new Date().getTime();
            //saveDataToSharePreff(timeOfCreation);
            saveDataToSharePreffServiceStarted(true);
        } else {
            //timeOfCreation = loadAndUpdateData();
            Toast.makeText(this, "Service not started", Toast.LENGTH_SHORT).show();
        }
*/

        isServiceStarted=true;
        if (timerStarted||isPaused) {
            Toast.makeText(this, "Service started", Toast.LENGTH_SHORT).show();
            //deleteData();
            //timeOfCreation = new Date().getTime();
            //saveDataToSharePreff(timeOfCreation);
            saveDataToSharePreffServiceStarted(true);
        } else if(!timerStarted&& !isPaused) {
            //timeOfCreation = loadAndUpdateData();
            Toast.makeText(this, "Service not started", Toast.LENGTH_SHORT).show();
        }


        Log.i("LOG timerStarted: ", timerStarted+"");
        Log.i("Log paused",isPaused+"");
        countingWorkTimeAndPausedTime();

/*
        HandlerThread handlerThread = new HandlerThread("StopWatchThreadOgrodApp");
        handlerThread.start();
        handler = new android.os.Handler(handlerThread.getLooper());

        if(timerStarted) {

            handler.post(new Runnable() {
                @Override
                public void run() {

                    //long currentTimeMillis = System.currentTimeMillis();
                    long currentTimeMillis = new Date().getTime();
                    long toPost = (currentTimeMillis - timeOfCreation) / 1000;


                        //Updatding notifiaction starts from 0
                        notificationUpdate(toPost, "Pracujesz już: ");
                        Log.i("TO POST", getTimerText(toPost));
                        Log.i("Current time", currentTimeMillis + "");
                        Log.i("Time of Creation", timeOfCreation + "");
//                    time.postValue(toPost);
                        // Increasing value about one second


                     *//*   notificationUpdate(++timeLongForPause, "Pauza: ");
                        mutableLiveDataTimeForPause.postValue(timeLongForPause);*//*



                    handler.postDelayed(this, 1000);
                }
            });
        }
        else {
            handler.removeCallbacksAndMessages(null);
        }*/

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
                        //timeOfCreation = loadAndUpdateTimeCreationFromSharedPreferences();
                        //long currentTimeMillis = System.currentTimeMillis();
                        long currentTimeMillis = new Date().getTime();
                        long toPost = (currentTimeMillis - timeOfCreation) / 1000;

                        //Toast.makeText(ForegroundServices.this, "timerStarted", Toast.LENGTH_SHORT).show();
                        //Updatding notifiaction starts from 0
                        notificationUpdate(toPost, "Pracujesz już: ");
                        /*Log.i("TO POST", getTimerText(toPost));
                        Log.i("Current time", currentTimeMillis + "");
                        Log.i("Time of Creation", timeOfCreation + "");*/
//                    time.postValue(toPost);
                        // Increasing value about one second


                     /*   notificationUpdate(++timeLongForPause, "Pauza: ");
                        mutableLiveDataTimeForPause.postValue(timeLongForPause);*/
                        handler.postDelayed(this, 1000);
                    }
                     else if(isPaused)
                    {
                       // timeOfCreation = loadAndUpdateTimeCreationFromSharedPreferences();
                        //Toast.makeText(ForegroundServices.this, "isPaused", Toast.LENGTH_SHORT).show();
                        //long currentTimeMillis = System.currentTimeMillis();
                        long currentTimeMillis = new Date().getTime();
                        long toPost = (currentTimeMillis - timeOfCreation) / 1000;


                        if(toPost<=8*HOUR_IN_SECONDS) {
                            //Updatding notifiaction starts from 0
                            notificationUpdate(toPost, "Odpoczywasz już: ");
                            handler.postDelayed(this, 1000);
                        }
                        else{
                            handler.removeCallbacksAndMessages(null);
                            isPaused=false;
                            saveIsPausedToSharedPreferences(isPaused);
                        }
                    }
                    else {
                        Toast.makeText(ForegroundServices.this, "handlerRemove", Toast.LENGTH_SHORT).show();
                        handler.removeCallbacksAndMessages(null);
                    }
                }
            });


    }

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
    private int getHowManyTimesServiceStartedFromSharedPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS_OGROD_APP,MODE_PRIVATE);
        return sharedPreferences.getInt(KEY_CHECKING_HOW_MANY_TIMES_SERVICE_RUN,0);
    }

    private void saveHowManyTimesServiceStartedToSharedPreferences(int howManyTimesServiceStarted) {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS_OGROD_APP,MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(KEY_CHECKING_HOW_MANY_TIMES_SERVICE_RUN,howManyTimesServiceStarted);
        editor.apply();
    }

    private boolean getIsTimerStartedFromSharedPreferences() {
        SharedPreferences sharedPreferencesTimeModel = getSharedPreferences(SHARED_PREFS_OGROD_APP,MODE_PRIVATE);
        return sharedPreferencesTimeModel.getBoolean(KEY_TIMER_STARTED, false);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
    }

    private boolean loadAndUpdateServiceStartedFromSharedPreferences() {
        SharedPreferences sharedPreferencesTimeModel = getSharedPreferences(SHARED_PREFS_OGROD_APP,MODE_PRIVATE);
        return sharedPreferencesTimeModel.getBoolean(KEY_PREFS_SERVICE_STARTED, false);
    }
    private void saveDataToSharePreffServiceStarted(boolean isServiceStarted) {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS_OGROD_APP,MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_PREFS_SERVICE_STARTED,isServiceStarted);
        editor.apply();
    }
    private void deleteDataToSharedPreferences()
    {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS_OGROD_APP,MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putLong(KEY_TIME_OF_CREATION,0);

        editor.apply();
    }

    private long loadAndUpdateTimeCreationFromSharedPreferences() {
            SharedPreferences sharedPreferencesTimeModel = getSharedPreferences(SHARED_PREFS_OGROD_APP,MODE_PRIVATE);
            return sharedPreferencesTimeModel.getLong(KEY_TIME_OF_CREATION, 0);
    }

    /*private void saveDataToSharePreff(long timeOfCreation) {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS_OGROD_APP,MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(KEY_TIME_OF_CREATION,timeOfCreation);
        editor.apply();
    }*/



    public synchronized void countingTime()
{
    handler = new android.os.Handler(getMainLooper());
    handler.post(new Runnable() {
        @Override
        public void run() {
            //wakeLock.acquire();

            if(isPaused==false) {
                //Updatding notifiaction starts from 0
                time.postValue(timeLongForClock++);
                notificationUpdate(timeLongForClock,"Pracujesz już: ");
                // Increasing value about one second
            }
            else{
                mutableLiveDataTimeForPause.postValue(timeLongForPause++);
                notificationUpdate(timeLongForPause,"Pauza: ");
            }

            handler.postDelayed(this,1000);
        }
    });

}
    //Intent will be text from editText
    // Callled every time is called start service

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

                //TODO Watch the behave
                return START_STICKY;
            }


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
        handler.removeCallbacksAndMessages(null);
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