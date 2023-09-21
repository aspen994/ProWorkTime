package com.example.ogrdapp;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import com.example.ogrdapp.utility.SharedPreferencesDataSource;

public class App extends Application {
    public static final String CHANNEl_ID = "countingTimeServiceChannelForOgrodApp";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        SharedPreferencesDataSource.getInstance().init(getApplicationContext());
    }

    private void createNotificationChannel() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEl_ID,
                    "Counting Time Service Channel"
                    , NotificationManager.IMPORTANCE_DEFAULT
            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }

    }
}
