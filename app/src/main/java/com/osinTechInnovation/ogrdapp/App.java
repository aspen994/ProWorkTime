package com.osinTechInnovation.ogrdapp;

import static java.security.AccessController.getContext;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import com.osinTechInnovation.ogrdapp.utility.SharedPreferencesDataSource;
import com.osinTechInnovation.ogrdapp.viewmodel.AuthViewModel;

import android.provider.Settings.Secure;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStore;


public class App extends Application {
    public static final String CHANNEL_ID = "countingTimeServiceChannelForOgrodApp";
    public static final String REVENUE_API_KEY = "goog_LCTTJDmQMNssyQgxvsmbZXHROXa";
    public static final String CLIENT_ID = "602672706888-fsep5a5g6cdnpekkug83ti8qsb6likid.apps.googleusercontent.com";

    private AuthViewModel authViewModel;
    public static  String android_id;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        SharedPreferencesDataSource.getInstance().init(getApplicationContext());


        android_id = Settings.Secure.getString(this.getContentResolver(), Secure.ANDROID_ID);

    }



    public void generatedAndroidId(){
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        executorService.execute(()->{
            for (int i = 0; i < 10; i++) {

                try{
                    Thread.sleep(1000);
                }
                catch (Exception e){
                    e.printStackTrace();
                }

            }
        });



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
