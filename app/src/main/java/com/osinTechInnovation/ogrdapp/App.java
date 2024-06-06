package com.osinTechInnovation.ogrdapp;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import com.osinTechInnovation.ogrdapp.utility.SharedPreferencesDataSource;


public class App extends Application {
    public static final String CHANNEL_ID = "countingTimeServiceChannelForOgrodApp";
    public static final String REVENUE_API_KEY = "goog_LCTTJDmQMNssyQgxvsmbZXHROXa";
    public static final String CLIENT_ID = "602672706888-fsep5a5g6cdnpekkug83ti8qsb6likid.apps.googleusercontent.com";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        SharedPreferencesDataSource.getInstance().init(getApplicationContext());

    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Counting Time Service Channel"
                    , NotificationManager.IMPORTANCE_LOW
            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }

    }
}
