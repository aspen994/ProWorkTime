package com.example.ogrdapp;


import static com.example.ogrdapp.services.ForegroundServices.HOUR_IN_SECONDS;
import static com.example.ogrdapp.services.ForegroundServices.MINUTE_IN_SECONDS;
import static com.example.ogrdapp.services.ForegroundServices.isPaused;
import static com.example.ogrdapp.utility.SharedPreferencesConstants.KEY_IS_PAUSED;
import static com.example.ogrdapp.utility.SharedPreferencesConstants.KEY_TIME_OF_CREATION;
import static com.example.ogrdapp.utility.SharedPreferencesConstants.PAUSED_TIME;
import static com.example.ogrdapp.utility.SharedPreferencesConstants.PAUSED_TIME_BOOLEAN;
import static com.example.ogrdapp.utility.SharedPreferencesConstants.SECONDS_STOPWATCH;
import static com.example.ogrdapp.utility.SharedPreferencesConstants.SHARED_PREFS_FOR_TIME_CREATION_ONLY;
import static com.example.ogrdapp.utility.SharedPreferencesConstants.SHARED_PREFS_OGROD_APP;
import static com.example.ogrdapp.utility.SharedPreferencesConstants.TEXT;
import static com.example.ogrdapp.utility.SharedPreferencesConstants.KEY_TIMER_STARTED;
import static com.example.ogrdapp.utility.SharedPreferencesConstants.TMP_BEGIN_TIME;
import static com.example.ogrdapp.utility.SharedPreferencesConstants.TMP_BEGIN_TIME_STRING;


import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.work.BackoffPolicy;
import androidx.work.Constraints;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.ogrdapp.model.TimeModel;
import com.example.ogrdapp.scanner.CustomScannerActivity;
import com.example.ogrdapp.services.ForegroundServices;
import com.example.ogrdapp.view.MainActivity;
import com.example.ogrdapp.view.UserTimeTable;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanIntentResult;
import com.journeyapps.barcodescanner.ScanOptions;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class UserMainActivity extends AppCompatActivity {



    private FirebaseAuth firebaseAuth=FirebaseAuth.getInstance();
    private FirebaseUser fireBaseUser;
    public static final String QRCODE1="Tk6&zE8*odwq7G$u2#IVL1e!Q@JvXrFgS0^NbCn5mO9pDyA4(PcHhY3Za6lWsB)";
    public static final String QRCODE2delay5minutes="yJGZ*q7W#8n6Dv@B1F$%9X4hpYQeS^gU+sa0RwM3zNtVxOcZ2dL5fIHkA6i";

    public static final String TIME_MODEL = "text_2";





    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle toogle;

    private TextView userName,date,timeDisplay,textMain,begingTime,endingTime,timerOverall;


    public static boolean timerStarted = false;
    private ImageButton qr;
    private Button holdResumeWork, stopWork;

    private long tmpBeginTime,tmpEndTime,tmpOverall=0;
    private long delay5minutes = 300000;

    private long pausedTimeToSharedPref;
    private static final int REQUEST_CODE =22;
    private TimeModel timeModel;
    private ArrayList<TimeModel> arrayList = new ArrayList<>();

    //Firebase Connection
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private CollectionReference collectionReference = db.collection("Users");
    private CollectionReference collectionReferenceTime = db.collection("Time");
    // To Foreground service-------------------------------------------------------------------------
    private boolean flag = true;
    public  boolean flagService = true;
    public static boolean active = false;
    //public static boolean isModeCountingActive = false;
    public static boolean flagForForegroundService = true;
    public static boolean isStopwatchActive = false;
    String beginingTime = "";

    private long tmpBeginTimeFromSharedPreferences;
    private boolean isTimerStarted=false;

    private  String currentTime = "";

    LiveData<Long> timerLiveData;

    long addToEndingTime=0;
    public IntentFilter intentFilter;
    Handler handler1 = new Handler(Looper.getMainLooper());


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_main);
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.nav_View);
        qr = findViewById(R.id.buttonQR);
        holdResumeWork = findViewById(R.id.hold_resume_work);
        stopWork = findViewById(R.id.stop_work);
        userName = findViewById(R.id.username);
        date = findViewById(R.id.textview_dateToInsert);
        timeDisplay = findViewById(R.id.textView4);
        textMain = findViewById(R.id.textView_begin_work);
        begingTime = findViewById(R.id.begining_time);
        endingTime = findViewById(R.id.ending_time);
        timerOverall = findViewById(R.id.timeOverall);
        Toast.makeText(this, "on create ", Toast.LENGTH_SHORT).show();


        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


        // Loading and updating data from SharedPreferences (timeStarted, flag for isStarted time  and so on)
        loadDataFromSharedPreferences();
        updateData();

        //
        uploadAndLoadPausedTimeFromSharedPreferences();


        // Should start the service if the timer i started.
        if(!isMyServiceRunning(ForegroundServices.class)&&(timerStarted||isPaused))
        {
            Intent intent = new Intent(UserMainActivity.this,ForegroundServices.class);
            startService(intent);
        }




        // After loading data checking if clock is running if it is show the buttons
        if(timerStarted || isPaused)
        {
            stopWork.setVisibility(View.VISIBLE);
            holdResumeWork.setVisibility(View.VISIBLE);
            if(isPaused)
            {
                holdResumeWork.setBackgroundColor(Color.GREEN);
                qr.setVisibility(View.INVISIBLE);
                textMain.setVisibility(View.INVISIBLE);
            }
        }
        else
        {
            stopWork.setVisibility(View.INVISIBLE);
            holdResumeWork.setVisibility(View.INVISIBLE);
        }


        if(!isMyServiceRunning(ForegroundServices.class)&&pausedTimeToSharedPref>0&& isPaused)
        {
            long l = (System.currentTimeMillis() - pausedTimeToSharedPref) / 1000;
            Toast.makeText(this, "PAUSED TIME RESUME", Toast.LENGTH_SHORT).show();
            startPausedTime(l);
        }

        fireBaseUser = firebaseAuth.getCurrentUser();
        assert  fireBaseUser !=null;
        final String currentUserId = fireBaseUser.getUid();


        // - - - - - - - - - - - STOPWORK ON CLICKLISTNER - - - - - - - -//
        stopWork.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ServiceHelper.isCountingTimeActive = false;

                if(!timerStarted) {
                    //TODO Zmieniłem 06.09.2023
                    //stopTime();
                    stopWork.setVisibility(View.INVISIBLE);
                    holdResumeWork.setVisibility(View.INVISIBLE);
                    qr.setVisibility(View.VISIBLE);
                    textMain.setVisibility(View.VISIBLE);
                }
                else{
                    stopCountingTime();
                    stopTime();
                    stopWork.setVisibility(View.INVISIBLE);
                    holdResumeWork.setVisibility(View.INVISIBLE);
                }
                handler1.removeCallbacksAndMessages(null);
                timerStarted=false;
            }
        });

        // - - - - - - - - - - - STOPWORK ON CLICKLISTNER - - - - - - - -//

        LiveData<Long> timePause;
        timePause = ForegroundServices.mutableLiveDataTimeForPause;



        holdResumeWork.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ServiceHelper.isCountingTimeActive = true;

                isPaused = !isPaused;

                //TODO Check if it's properly
                holdResumeWork.setText("Zastopuj pracę");
                if(isPaused)
                {
                    Toast.makeText(UserMainActivity.this, "Paused", Toast.LENGTH_SHORT).show();
                    stopWork.setVisibility(View.VISIBLE);
                    holdResumeWork.setVisibility(View.VISIBLE);
                    holdResumeWork.setBackgroundColor(Color.GREEN);
                    saveCreationTimeToSharedPref(new Date().getTime());
                    startCountingTimeWithHandler();
                    stopCountingTime();
                    stopTimeWithoutStoppingService();


                    cleanDataForTimeModelToSharedPreferences();
                    currentTime = getCurrentTime();
                    saveTimeModelToSharedPreferences();
                    tmpBeginTime = getCurrentTimeInSimpleFormat();
                 //   timerStarted = true;

                        //holdResumeWork.setBackgroundColor(Color.GREEN);
                        qr.setVisibility(View.INVISIBLE);
                        textMain.setVisibility(View.INVISIBLE);



                }
                else {
                    Toast.makeText(UserMainActivity.this, "unPaused", Toast.LENGTH_SHORT).show();
                    holdResumeWork.setBackgroundColor(Color.parseColor("#A214D5"));
                    saveCreationTimeToSharedPref(new Date().getTime());
                    startCountingTimeWithHandler();
                    startCountingTime();
                    startTimerWithoutStartingNewService();
                    qr.setVisibility(View.VISIBLE);
                    textMain.setVisibility(View.VISIBLE);

                }



            }

        });


        // ---------------- P - E - R - M -I - S - S - I - O - N -S -------------

        //Permission for the post notification
        if(ContextCompat.checkSelfPermission(UserMainActivity.this, android.Manifest.permission.POST_NOTIFICATIONS)!= PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(UserMainActivity.this,new String[]{android.Manifest.permission.POST_NOTIFICATIONS},103);
        }
        if(ContextCompat.checkSelfPermission(UserMainActivity.this, android.Manifest.permission.WAKE_LOCK)!= PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(UserMainActivity.this,new String[]{android.Manifest.permission.WAKE_LOCK},101);
        }

        // ---------------- P - E - R - M -I - S - S - I - O - N -S -------------  >

        // Starting time for stopWatch when phone down or app crash and time was counted to start ( Connected with Shared Pref)
        // Checking is mySrevice runing if not check if ending time is equal to to"" and beging time has text
        if(!isMyServiceRunning(ForegroundServices.class) && endingTime.getText().toString().equals("") && !begingTime.getText().toString().equals(""))
        {
            //Retreving data do tmpBeginTime for Shared preff
            // Divied by 1000 to get from milliseconds to seconds
            long timeInSeconds = (getCurrentTimeInSimpleFormat() - tmpBeginTime) / 1000;
            // Setting static variable timeLong for the difrenece between time.
            //startTimer(timeInSeconds);
        }



        //Setting timerLiveData from ForegroundService
        //timerLiveData = ForegroundServices.time;

       /* timerLiveData.observe(this, new Observer<Long>() {
            @Override
            public void onChanged(Long aLong) {
                timeDisplay.setText(getTimerText(aLong));
                if(aLong>0)
                {
                    timerOverall.setText("Przepracowałeś już : " + getTimerText(aLong));

                }
            }
        });*/

        /*// TODO Set the layer for Database
        // Getting current user Id.
        FirebaseUser user = firebaseAuth.getCurrentUser();
        assert  user !=null;
        final String currentUserId = user.getUid();*/

        // Assignment user name and surname to textView from collectionReferences
        collectionReference.whereEqualTo("userId",currentUserId).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                // 16.06.23 Zmieniłem z if(!value.isEmpty()) na  error == null i potem znów.
                if(!value.isEmpty()) //  error == null
                {
                    for(QueryDocumentSnapshot snapshot: value)
                    {
                        String username = snapshot.getString("username");
                        String surName = snapshot.getString("surName");

                        userName.setText(username+" " + surName);
                    }
                }
                else {
                    Toast.makeText(UserMainActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });


        date.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));

        firebaseAuth = FirebaseAuth.getInstance();

        qr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ScanOptions options = new ScanOptions();
                options.setPrompt("Zeskanuj kod");
                options.setBeepEnabled(true);
                options.setOrientationLocked(true);
                //options.setCaptureActivity(Scanner.class);
                options.setCaptureActivity(CustomScannerActivity.class);
                barLauncher.launch(options);

            }
        });


        toogle = new ActionBarDrawerToggle(this,drawerLayout,R.string.open,R.string.close);
        drawerLayout.addDrawerListener(toogle);
        toogle.syncState();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // For menu on the left swipe
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                if(R.id.action_time==item.getItemId())
                {
                    Intent i = new Intent(UserMainActivity.this, UserTimeTable.class);
                    //Log.i("SIZE ARRAY LIST FROM MAIN",arrayList.size()+"");
                    startActivity(i);
                }
                else if(R.id.action_logout==item.getItemId())
                {
                    firebaseAuth.signOut();
                    startActivity(new Intent(UserMainActivity.this, MainActivity.class));
                }
                return true;
            }
        });

        /* intentFilter = new IntentFilter();
         intentFilter.addAction("com.osin.myBroadcastMessage");*/

       /* if(flagService) {
            Log.i("FLAG SERVICE IS :",flagService+"");
            if (timerTask != null) {
                timerTask.cancel();
            }

            broadcastReceiver = new BroadcastReceiver() {

                @Override
                public void onReceive(Context context, Intent intent) {
                    long longTimeFromBroadcastReceiver = intent.getLongExtra("TimeRemaining", 0);
                    Log.i("OnReceive",timeDisplay.getText().toString());
                    //userMainActivityViewModel.setValue(0);
                    userMainActivityViewModel.setValue(longTimeFromBroadcastReceiver);
                    //startTimerSecondTime();
                    flagForBroadCastService=true;
                    flagForForegroundService=true;

                    //  Log.i("On Receive ","On receive");

                }
            };
            registerReceiver(broadcastReceiver,intentFilter);
            //Log.i("FLAG SERVICE CHECKING","How many times invoked: " + ++counter);
            Log.i("REGISTER RECEIVER","REGISTER RECEIVER");
            flagService = false;

        }*/

        active = true;

        intentFilter = new IntentFilter();
        intentFilter.addAction("Counter");
        flagForForegroundService =true;


        if(flagService) {
        /*    Log.i("FLAG SERVICE IS :",flagService+"");
            if (timerTask != null) {
                timerTask.cancel();
            }

            //Toast.makeText(this, "flag service", Toast.LENGTH_SHORT).show();
            broadcastReceiver = new BroadcastReceiver() {

                @Override
                public void onReceive(Context context, Intent intent) {
                    long longTimeFromBroadcastReceiver = intent.getLongExtra("TimeRemaining", 0);
                    Log.i("On receive", timeDisplay.getText().toString());
                    Toast.makeText(UserMainActivity.this, timeDisplay.getText().toString(), Toast.LENGTH_SHORT).show();
                    if(timeDisplay.getText().toString().equals("00 : 00 : 00")&& kolejnaJebanaFlaga) {
                        userMainActivityViewModel.setValue(0);
                        Log.i("On receive", timeDisplay.getText().toString());
                        userMainActivityViewModel.setValue(longTimeFromBroadcastReceiver);
                        startTimerSecondTime();
                        flagForBroadCastService = true;
                        //flagForForegroundService = true;
                        Toast.makeText(UserMainActivity.this, "Executing broadcast", Toast.LENGTH_SHORT).show();
                        kolejnaJebanaFlaga = false;
                    }
                    //  Log.i("On Receive ","On receive");

                }
            };
            registerReceiver(broadcastReceiver,intentFilter);
            //Log.i("FLAG SERVICE CHECKING","How many times invoked: " + ++counter);
            Log.i("REGISTER RECEIVER","REGISTER RECEIVER");*/
            flagService = false;

        }

      /*  Handler handler1 = new Handler(Looper.getMainLooper());
        ServiceHelper.isCountingTimeActive = getIsTimeStarted();
        long timeOfCreation = getTimeOfCreation();
        if(isTimerStarted)
        {
            handler1.post(new Runnable() {
                @Override
                public void run() {

                    long currentTimeInLong = new Date().getTime();
                    timeDisplay.setText(getTimerText((currentTimeInLong-timeOfCreation)/1000));
                    Log.i("TIME OF CREATION: ", timeOfCreation+"");
                    Log.i("Current Time: ", currentTime+"");
                    Log.i("Stop watch: ", getTimerText((currentTimeInLong-timeOfCreation)/1000)+"");

                    handler1.postDelayed(this,1000);

                }
            });
        }*/

        //startCountingTimeWithHandler();

    }

    private void showWakelockDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Wakelock Dialog")
                .setMessage("Czy chcesz aktywować mechanizm wakelock?")
                .setPositiveButton("Tak", (dialog, which) -> {
                    // Aktywacja wakelocka

                    dialog.dismiss();
                })
                .setNegativeButton("Nie", (dialog, which) -> {
                    dialog.dismiss();
                    finish(); // Zamknięcie aktywności
                })
                .setOnDismissListener(dialog -> {
                    // Zwolnienie wakelocka przy zamknięciu dialogu
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void startTimerWithoutStartingNewService() {
        timerStarted=true;


        flag = false;
    }

    private String getTimeMethod(long l) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");//dd/MM/yyyy
        Date now = new Date(l);
        String sf = sdf.format(now);
        return sf;
    }
    private String getCurrentTime() {
        //"yyyy-MM-dd HH:mm:ss.SSS"
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");//dd/MM/yyyy
        Date now = new Date();
        addToEndingTime = now.getTime();
        String sf = sdf.format(now);
        return sf;
    }


    public String checkMethod(long timeLong)
    {
        long seconds = timeLong / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;

        seconds %= 60;
        minutes %= 60;

        String formattedTime = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        return formattedTime;
    }

    // Block for Shared Preferences -------------------

    public void saveIsPausedToSharedPreferences(boolean isPaused)
    {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS_OGROD_APP,MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_IS_PAUSED,isPaused);
        editor.apply();
    }

    private void saveTimeModelToSharedPreferences()
    {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS_OGROD_APP,MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(TMP_BEGIN_TIME_STRING,currentTime);
        editor.apply();
    }

    private String loadAndUpdatedTimeModelFromSharedPreferences()
    {
        SharedPreferences sharedPreferencesTimeModel = getSharedPreferences(SHARED_PREFS_OGROD_APP,MODE_PRIVATE);
            currentTime = sharedPreferencesTimeModel.getString(TMP_BEGIN_TIME_STRING, currentTime);
            return currentTime;
    }
    public void saveDataToSharedPreferences()
    {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS_OGROD_APP,MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(TEXT,begingTime.getText().toString());
        //editor.putString(TEXT_2,endingTime.getText().toString());
        editor.putBoolean(KEY_TIMER_STARTED,timerStarted);
        editor.putLong(TMP_BEGIN_TIME,tmpBeginTime);

       // Toast.makeText(this, "Save" +ForegroundServices.time.getValue(), Toast.LENGTH_SHORT).show();

        editor.apply();
    }
    public void savePausedTimeToSharedPreferences(long pausedTime)
    {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS_OGROD_APP,MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(PAUSED_TIME,pausedTime);
        editor.putBoolean(PAUSED_TIME_BOOLEAN,isPaused);
        editor.apply();
    }

    public void uploadAndLoadPausedTimeFromSharedPreferences()
    {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS_OGROD_APP,MODE_PRIVATE);
        pausedTimeToSharedPref = sharedPreferences.getLong(PAUSED_TIME, pausedTimeToSharedPref);
        isPaused = sharedPreferences.getBoolean(PAUSED_TIME_BOOLEAN,isPaused);
        Toast.makeText(this, "Upload Paused", Toast.LENGTH_SHORT).show();
    }

    public void clearDataForLoadPausedTimeToSharedPreferences()
    {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS_OGROD_APP,MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(PAUSED_TIME,0);

        editor.apply();
    }

    public void clearDataToSharedPreferences()
    {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS_OGROD_APP,MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(TEXT,"");
        editor.putBoolean(KEY_TIMER_STARTED,false);
        editor.putLong(TMP_BEGIN_TIME,0);

        editor.apply();
    }
    private void cleanDataForTimeModelToSharedPreferences() {
        SharedPreferences sharedPreferencesTimeModel = getSharedPreferences(SHARED_PREFS_OGROD_APP,MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferencesTimeModel.edit();

        editor.putString(TMP_BEGIN_TIME_STRING,"");

        editor.apply();
    }

    public void loadDataFromSharedPreferences()
    {
       SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS_OGROD_APP,MODE_PRIVATE);
       if(endingTime.getText().toString().equals("")) {
           beginingTime = sharedPreferences.getString(TEXT, begingTime.getText().toString());
       }
        timerStarted = sharedPreferences.getBoolean(KEY_TIMER_STARTED,timerStarted);
        tmpBeginTimeFromSharedPreferences = sharedPreferences.getLong(TMP_BEGIN_TIME,tmpBeginTime);


    }
    public void loadDataForStopWatchFromSharedPreferences()
    {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS_OGROD_APP,MODE_PRIVATE);
        ForegroundServices.time.setValue((sharedPreferences.getLong(SECONDS_STOPWATCH,0)));
     //   Toast.makeText(this, "load data" +ForegroundServices.time.getValue(), Toast.LENGTH_SHORT).show();
    }

    public void updateData()
    {
        begingTime.setText(beginingTime);
        //timerStarted = isTimerStarted;
        //isTimerStarted = isTimerStarted;
        tmpBeginTime = tmpBeginTimeFromSharedPreferences;

        //startCountingTimeWithHandler();
    }
    // Block for Shared Preferences -------------------


    // To Foreground service-------------------------------------------------------------------------

    private void startTimer(long miliseconds) {

        ServiceHelper.isCountingTimeActive = true;
        saveCreationTimeToSharedPref(miliseconds);
        startForegroundServiceToCountTimeWithWorkManger();
        timerStarted=true;
        Intent i = new Intent(UserMainActivity.this,ForegroundServices.class);
        startService(i);
        //ForegroundServices.time.setValue(0L);
        //ForegroundServices.timeLongForClock =seconds;

        flag = false;
        //TODO I COMMENTED 02.09.2023
        //startCountingTimeWithHandler();
    }

    private void startCountingTimeWithHandler() {
        /*HandlerThread handlerThread = new HandlerThread("countingTimeForMain");
        handlerThread.start();*/


        ServiceHelper.isCountingTimeActive = getIsTimeStartedFromSharedPreferences();
        Toast.makeText(this, "6", Toast.LENGTH_SHORT).show();
        timerStarted= getIsTimeStartedFromSharedPreferences();
//        isTimerStarted=getIsTimeStarted();
        Log.i("Start Counting time",timeDisplay.getText().toString());
        Log.i("Is timer started: ",isTimerStarted+"");
        Log.i("Is pasused started",isPaused+"");

        //{
            Toast.makeText(this, "STARTED", Toast.LENGTH_SHORT).show();
            handler1.post(new Runnable() {
                @Override
                public void run() {
                    long timeOfCreation = getTimeOfCreationFromSharedPreferences();
                    //&& (timeDisplay.getText().toString().equals("00 : 00 : 00")) || timeDisplay.getText().toString().equals("")
                    if (timerStarted )
                    {

                            //Toast.makeText(UserMainActivity.this, "RUN THREAD", Toast.LENGTH_SHORT).show();
                            //   if(timerStarted&&(timeDisplay.getText().toString().equals("00 : 00 : 00"))||timeDisplay.getText().toString().equals("")) {

                            long currentTimeInLong = new Date().getTime();
                            timeDisplay.setText(getTimerText((currentTimeInLong - timeOfCreation) / 1000));
                        //Toast.makeText(UserMainActivity.this, "Timer started v2: "+((currentTimeInLong - timeOfCreation) / 1000) , Toast.LENGTH_SHORT).show();
                            /*Log.i("TIME OF CREATION: ", timeOfCreation + "");
                            Log.i("Current Time: ", currentTime + "");
                            Log.i("Stop watch: ", getTimerText((currentTimeInLong - timeOfCreation) / 1000) + "");*/

                            handler1.postDelayed(this, 1000);

                }
                    else if (isPaused) {

                                long currentTimeInLong = new Date().getTime();
                                long toPost = ((currentTimeInLong - timeOfCreation)/1000);
                        //Toast.makeText(UserMainActivity.this, "to Post: "+ toPost, Toast.LENGTH_SHORT).show();

                        if(toPost<=8*HOUR_IN_SECONDS) {
                                holdResumeWork.setText("Zakończ pauzę: "+getTimerText(toPost));
                                Log.i("TIME OF CREATION: ", timeOfCreation + "");
                                Log.i("Current Time: ", currentTime + "");
                                Log.i("Stop watch from IS PAUSED: ", getTimerText((currentTimeInLong - timeOfCreation) / 1000) + "");
                                handler1.postDelayed(this,1000);
                        }
                        else{
                            stopCountingTime();
                            stopTime();
                            stopWork.setVisibility(View.INVISIBLE);
                            holdResumeWork.setVisibility(View.INVISIBLE);
                            qr.setVisibility(View.VISIBLE);

                            handler1.removeCallbacksAndMessages(null);
                            isPaused=false;
                            saveIsPausedToSharedPreferences(isPaused);
                        }



                    }
                    else if(!timerStarted&&!isPaused) {

                        handler1.removeCallbacksAndMessages(null);
                    }
                    //}  else if (isPaused) {

                   // }
                  //  else if(isPaused==false &&isTimerStarted==false){
                    /*    Toast.makeText(UserMainActivity.this, "5", Toast.LENGTH_SHORT).show();
                        handler1.removeCallbacksAndMessages(null);*/
                  //  }


            }});
    }

    private long getTimeOfCreationFromSharedPreferences()
    {
        SharedPreferences sharedPreferencesTimeModel = getSharedPreferences(SHARED_PREFS_OGROD_APP,Context.MODE_PRIVATE);
        return sharedPreferencesTimeModel.getLong(KEY_TIME_OF_CREATION,0);
    }


    private boolean getIsTimeStartedFromSharedPreferences() {
        SharedPreferences sharedPreferencesTimeModel = getSharedPreferences(SHARED_PREFS_OGROD_APP,MODE_PRIVATE);
        return sharedPreferencesTimeModel.getBoolean(KEY_TIMER_STARTED, false);
    }

    private void saveIsTimeStartedToSharedPreferences(boolean isStopWatchActive){
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS_OGROD_APP,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_TIMER_STARTED,isStopWatchActive);
        editor.apply();
    }

    private void saveIsTimeStartedFORSPECIALCASEINFOREGROUNDTOREPARIToSharedPreferences(boolean isStopWatchActive){
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS_OGROD_APP,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_TIMER_STARTED,isStopWatchActive);
        editor.apply();
    }

    private void saveCreationTimeToSharedPref(long time) {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS_OGROD_APP,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(KEY_TIME_OF_CREATION,time);
        editor.apply();
    }

    private SharedPreferences getSharedPreferences()
    {
        return this.getSharedPreferences(SHARED_PREFS_OGROD_APP, Context.MODE_PRIVATE);
    }
    private void startPausedTime(long seconds) {

        ServiceHelper.isCountingTimeActive = true;
        startForegroundServiceToCountTimeWithWorkManger();
        ForegroundServices.mutableLiveDataTimeForPause.setValue(0L);
        ForegroundServices.timeLongForPause =seconds;

    }


    public void stopTime()
    {
        Toast.makeText(this, "STop time invoked", Toast.LENGTH_SHORT).show();
        ServiceHelper.isCountingTimeActive = false;
        timerStarted=false;
        Intent serviceIntent = new Intent(this,ForegroundServices.class);
        stopService(serviceIntent);
        isPaused=false;
        WorkManager.getInstance(UserMainActivity.this).cancelAllWorkByTag("cleanup");
        timeDisplay.setText("00 : 00 : 00");

        //userMainActivityViewModel.stopTimerTask();

    /*    if(timerTask!=null)
        {
            timerTask.cancel();
            //timerOverall.setText(checkMethod(timeLong));
            timeLong=0;
        }*/

/*
        Intent serviceIntent = new Intent(this, ForegroundServices.class);
        stopService(serviceIntent);
        //Log.i("Time finnaly",time+"");
        flag=true;
        flagService=false;
        if(broadcastReceiver!=null) {
            timeLong=0;
            try {
                unregisterReceiver(broadcastReceiver);
            }
            catch(IllegalArgumentException e)
            {
                e.printStackTrace();
            }

        }*/
       /* if(broadcastReceiver!=null) {
            try {
                unregisterReceiver(broadcastReceiver);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }*/

       // Log.i("Time finnaly",time+"");
        flag=true;
        flagService=false;

    }

    public void stopTimeWithoutStoppingService()
    {
        ServiceHelper.isCountingTimeActive = false;
        isPaused=true;
        flag=true;
        flagService=false;
        saveIsPausedToSharedPreferences(isPaused);

    }

// To Foreground service-------------------------------------------------------------------------

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode== REQUEST_CODE && resultCode == RESULT_OK)
        {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
        }

    }

    ActivityResultLauncher<ScanOptions> barLauncher = registerForActivityResult(new ScanContract(), new ActivityResultCallback<ScanIntentResult>() {

        @Override
        public void onActivityResult(ScanIntentResult result) {

            fireBaseUser = firebaseAuth.getCurrentUser();
            assert  fireBaseUser !=null;
            final String currentUserId = fireBaseUser.getUid();

            // For QRCODE 1
            if(result.getContents()!=null && result.getContents().toString().equals(QRCODE1))
            {

                if(timerStarted == false)
                {
                    timerOverall.setText("");
                    Toast.makeText(UserMainActivity.this, "QR", Toast.LENGTH_SHORT).show();
                    ServiceHelper.isCountingTimeActive = true;
                    timerStarted = true;
                    saveIsTimeStartedToSharedPreferences(timerStarted);
                    //saveIsTimeStartedFORSPECIALCASEINFOREGROUNDTOREPARIToSharedPreferences(timerStarted);

                    //startForegroundServiceToCountTime();
                    timeDisplay.setText("");
                    textMain.setText("Zatrzymaj pracę: ");
                    cleanDataForTimeModelToSharedPreferences();
                    currentTime = getCurrentTime();
                //  timeModel.setTimeBegin(getCurrentTime());
                    saveTimeModelToSharedPreferences();
                    begingTime.setText("Rozpoczęto pracę o: " + getCurrentTime());
                    tmpBeginTime = getCurrentTimeInSimpleFormat();
                    startTimer(new Date().getTime());
                    holdResumeWork.setText("Wstrzymaj pracę");
                    startCountingTimeWithHandler();

                    //timeDisplay.setText(userMainActivityViewModel.startTimer());
                    //flagForSignText =false;

                    endingTime.setText("");

                    stopWork.setVisibility(View.VISIBLE);
                    holdResumeWork.setVisibility(View.VISIBLE);
                    holdResumeWork.setBackgroundColor(Color.parseColor("#A214D5"));
                }
                else {
                    ServiceHelper.isCountingTimeActive = false;
                    holdResumeWork.setVisibility(View.INVISIBLE);
                    stopWork.setVisibility(View.INVISIBLE);
                    timeModel = new TimeModel();
                    Toast.makeText(UserMainActivity.this, "Timer started 2", Toast.LENGTH_SHORT).show();
                    timerStarted = false;
                    saveIsTimeStartedToSharedPreferences(timerStarted);
                    saveIsTimeStartedFORSPECIALCASEINFOREGROUNDTOREPARIToSharedPreferences(timerStarted);
                    textMain.setText("Rozpocznij pracę: ");
                    //endingTime.setText("Zakończono pracę o : " + getCurrentTime());

                    timeModel.setTimeEnd(getCurrentTime());
                    tmpEndTime = getCurrentTimeInSimpleFormat();

                    endingTime.setText("Zakończono pracę o : " + getCurrentTime());
                    timeModel.setTimeBegin(loadAndUpdatedTimeModelFromSharedPreferences());
                    //TODO i get
                    //tmpOverall = timeLong;
                    //TODO I commented 02.09.2023 - 12:37
                    //tmpOverall = ForegroundServices.time.getValue()*1000;
                    stopTime();

                    if (tmpOverall <=0) {
                        //TODO I commented 02.09.2023 - 12:37
                       /* tmpOverall += tmpEndTime - tmpBeginTime;
                        long seconds = tmpOverall / 1000;
                        //long seconds = timeLong / 1000;
                        long minutes = seconds / 60;
                        long hours = minutes / 60;

                        seconds %= 60;
                        minutes %= 60;

                        String formattedTime = String.format("%02d:%02d:%02d", hours, minutes, seconds);
                        String s = formatTime((int) seconds, (int) minutes, (int) hours);
                        timerOverall.setText("Przepracowałeś : " + s);
                        timeModel.setTimeOverall(s);
                        timeModel.setTimeOverallInLong(tmpOverall);*/
                    } else if (tmpOverall>0) {
                        //timerOverall.setText(getTimerText(tmpOverall));
                        timerOverall.setText("Przepracowałeś : " + timeDisplay.getText().toString());
                        timeModel.setTimeOverall(checkMethod(tmpOverall));
                        //timeModel.setTimeOverall(endingTime.getText().toString());
                        //timeModel.setTimeOverallInLong(tmpOverall*1000);
                        timeModel.setTimeOverallInLong(tmpOverall);
                    }
                    // TODO Overhere
                    //endingTime.setText("Zakończono pracę o : " + getCurrentTime());



                    //timeModel.setTimeOverall(checkMethod(timeLong));
                    //timeModel.setTimeOverallInLong(timeLong);
                    timeModel.setId(currentUserId);
                    timeModel.setUserName(userName.getText().toString());
                    timeModel.setTimeAdded(new Timestamp(new Date()));

                    arrayList.add(timeModel);

                    collectionReferenceTime.add(timeModel).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                         //   Toast.makeText(UserMainActivity.this, "Data added sucesfully", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                          //  Toast.makeText(UserMainActivity.this, "Fail on adding data", Toast.LENGTH_SHORT).show();
                        }
                    });
//                    timerTask.cancel();
                    tmpOverall=0;
                }

            }

            else {
              //  Toast.makeText(UserMainActivity.this, "Błąd 2", Toast.LENGTH_SHORT).show();
            }

            if(result.getContents()!=null && result.getContents().toString().equals(QRCODE2delay5minutes))
            {
               // Toast.makeText(UserMainActivity.this, "Dobry kod", Toast.LENGTH_SHORT).show();



                if(timerStarted == false)
                {
                    Toast.makeText(UserMainActivity.this, "QR_5_Delay", Toast.LENGTH_SHORT).show();
                    timeModel = new TimeModel();
                    timerStarted = true;
                    textMain.setText("Zatrzymaj pracę: ");
                    timeModel.setTimeBegin(getCurrentTime());
                    begingTime.setText("Rozpoczęto pracę o: " + getCurrentTime());
                    tmpBeginTime = getCurrentTimeInSimpleFormat();
                    // TODO TU JEST BŁĄD NA BANK, daj new Date.getTime zamiast 0
                    startTimer(0);
                    // Setting delay for the qr code
                    tmpOverall-=delay5minutes;
                   // Log.i("Logging","logged");
                }
                else
                {
                    Toast.makeText(UserMainActivity.this, "Timer started 3", Toast.LENGTH_SHORT).show();
                    timerStarted = false;
                    textMain.setText("Rozpocznij pracę: ");
                    //endingTime.setText("Zakończono pracę o : " + getCurrentTime());
                    timeModel.setTimeEnd(getCurrentTime());
                    tmpEndTime = getCurrentTimeInSimpleFormat();

                    // Counting time overall
                    tmpOverall = (tmpEndTime - tmpBeginTime);


                    if(tmpOverall<0) {
                    tmpOverall=0;
                    }
                    else {
                        long seconds = tmpOverall / 1000;
                        long minutes = seconds / 60;
                        long hours = minutes / 60;

                        seconds %= 60;
                        minutes %= 60;

                      //  String formattedTime = String.format("%02d:%02d:%02d", hours, minutes, seconds);
                        String s = formatTime((int) hours, (int) minutes, (int) seconds);
                        timeModel.setTimeOverall(s);
                        timeModel.setTimeOverallInLong(tmpOverall);
                        timeModel.setId(currentUserId);
                        timeModel.setUserName(userName.getText().toString());
                        timeModel.setTimeAdded(new Timestamp(new Date()));

                        arrayList.add(timeModel);

                        collectionReferenceTime.add(timeModel).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                          //      Toast.makeText(UserMainActivity.this, "Data added sucesfully 5 minutes dellay", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                            //    Toast.makeText(UserMainActivity.this, "Fail on adding data", Toast.LENGTH_SHORT).show();
                            }
                        });
                        // QR with dellay
                        timerOverall.setText(s);
                    }

                }

            }

            else {
              //  Toast.makeText(UserMainActivity.this, "Błąd 2", Toast.LENGTH_SHORT).show();
            }
        }
    });


    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }


    private void startForegroundServiceToCountTimeWithWorkManger() {
        /*Intent intentService = new Intent(this, ForegroundServices.class);
        //Time is in seconds
        //intentService.putExtra("TimeValue", timerLiveData.getValue());
        startService(intentService);
        Log.i("Start ForeGround","Start Foregroundservice");*/
        startWorker();
    }

    private void startWorker()
    {
        Constraints constraints = new Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .build();

        PeriodicWorkRequest saveRequest =
                new PeriodicWorkRequest.Builder(StopWatchWorker.class, 16, TimeUnit.MINUTES)
                        .setBackoffCriteria(
                                BackoffPolicy.LINEAR,
                                OneTimeWorkRequest.MIN_BACKOFF_MILLIS,
                                TimeUnit.MILLISECONDS)
                        .setConstraints(constraints)
                        .addTag("cleanup")
                        // Constraints
                        .build();

        WorkManager.getInstance(UserMainActivity.this).enqueue(saveRequest);
    }


    private long getCurrentTimeInSimpleFormat() {
        //"yyyy-MM-dd HH:mm:ss.SSS"
        //return System.currentTimeMillis();
        return new Date().getTime();
    }



  /*  private String getTimerText()
    {
        int rounded = (int) Math.round(time);

        int seconds = ((rounded % 86400) % 3600) % 60;
        int minutes = ((rounded % 86400) % 3600) / 60;
        int hours = ((rounded % 86400) / 3600);

        return formatTime(seconds, minutes, hours);
    }

    private String getTimerText(long timeLong)
    {
        int rounded = (int) Math.round(timeLong);

        int seconds = ((rounded % 86400) % 3600) % 60;
        int minutes = ((rounded % 86400) % 3600) / 60;
        int hours = ((rounded % 86400) / 3600);

        return formatTime(seconds, minutes, hours);
    }


    private String formatTime(int seconds, int minutes, int hours)
    {
        return String.format("%02d",hours) + " : " + String.format("%02d",minutes) + " : " + String.format("%02d",seconds);
    }*/

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


    @Override
    public void onBackPressed() {
        if(drawerLayout.isDrawerOpen(GravityCompat.START))
        {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(toogle.onOptionsItemSelected(item))
        {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void startCountingTime()
    {
        //startForegroundServiceToCountTime();
        textMain.setText("Zatrzymaj pracę: ");
        cleanDataForTimeModelToSharedPreferences();
        currentTime = getCurrentTime();
        //  timeModel.setTimeBegin(getCurrentTime());
        saveTimeModelToSharedPreferences();
        begingTime.setText("Rozpoczęto pracę o: " + getCurrentTime());
        tmpBeginTime = getCurrentTimeInSimpleFormat();
        //startTimer(0);

        //timeDisplay.setText(userMainActivityViewModel.startTimer());
        //flagForSignText =false;
        isPaused=false;
        saveIsPausedToSharedPreferences(isPaused);
        timerStarted = true;
        endingTime.setText("");

        stopWork.setVisibility(View.VISIBLE);
        holdResumeWork.setVisibility(View.VISIBLE);
        holdResumeWork.setBackgroundColor(Color.parseColor("#A214D5"));
    }

    public void stopCountingTime()
    {
        // How to delete this
        fireBaseUser = firebaseAuth.getCurrentUser();
        assert  fireBaseUser !=null;
        final String currentUserId = fireBaseUser.getUid();

        timeModel = new TimeModel();
        Toast.makeText(this, "Timer started 4", Toast.LENGTH_SHORT).show();
        timerStarted = false;
        textMain.setText("Rozpocznij pracę: ");
        //endingTime.setText("Zakończono pracę o : " + getCurrentTime());

        timeModel.setTimeEnd(getCurrentTime());
        tmpEndTime = getCurrentTimeInSimpleFormat();

        endingTime.setText("Zakończono pracę o : " + getCurrentTime());
        timeModel.setTimeBegin(loadAndUpdatedTimeModelFromSharedPreferences());

        tmpOverall = ForegroundServices.time.getValue()*1000;

        if (tmpOverall <=0) {

            tmpOverall += tmpEndTime - tmpBeginTime;
            long seconds = tmpOverall / 1000;
            //long seconds = timeLong / 1000;
            long minutes = seconds / 60;
            long hours = minutes / 60;

            seconds %= 60;
            minutes %= 60;

            String formattedTime = String.format("%02d:%02d:%02d", hours, minutes, seconds);
            String s = formatTime((int) seconds, (int) minutes, (int) hours);
            timerOverall.setText("Przepracowałeś : " + s);
            timeModel.setTimeOverall(s);
            timeModel.setTimeOverallInLong(tmpOverall);
        } else if (tmpOverall>0) {
            //timerOverall.setText(getTimerText(tmpOverall));
            timerOverall.setText("Przepracowałeś : " + timeDisplay.getText().toString());
            timeModel.setTimeOverall(checkMethod(tmpOverall));
            //timeModel.setTimeOverall(endingTime.getText().toString());
            //timeModel.setTimeOverallInLong(tmpOverall*1000);
            timeModel.setTimeOverallInLong(tmpOverall);
        }
        // TODO Overhere
        //endingTime.setText("Zakończono pracę o : " + getCurrentTime());



        //timeModel.setTimeOverall(checkMethod(timeLong));
        //timeModel.setTimeOverallInLong(timeLong);
        timeModel.setId(currentUserId);
        timeModel.setUserName(userName.getText().toString());
        timeModel.setTimeAdded(new Timestamp(new Date()));

        arrayList.add(timeModel);

        collectionReferenceTime.add(timeModel).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                //   Toast.makeText(UserMainActivity.this, "Data added sucesfully", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //  Toast.makeText(UserMainActivity.this, "Fail on adding data", Toast.LENGTH_SHORT).show();
            }
        });
//                    timerTask.cancel();
        tmpOverall=0;
    }


    @Override
    protected void onStart() {
        //startCountingTimeWithHandler();
        Toast.makeText(this, "on Start", Toast.LENGTH_SHORT).show();
        loadDataFromSharedPreferences();
        updateData();
        startCountingTimeWithHandler();
        super.onStart();
       /* IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("Counter");
        //intentFilter.addAction(Intent.Action);
        unregisterReceiver(broadcastReceiver);
        flagService = true;
        if(flagService) {
            if (timerTask != null) {
                timerTask.cancel();
            }
            broadcastReceiver = new BroadcastReceiver() {

                @Override
                public void onReceive(Context context, Intent intent) {
                    timerTask.cancel();
                    Long longTimeFromBroadcastReceiver = intent.getLongExtra("TimeRemaining", 0);
                    timeLong = longTimeFromBroadcastReceiver;
                    timeDisplay.setText(getTimerText(timeLong));
                }
            };

            registerReceiver(broadcastReceiver, intentFilter);
            flagService = false;
        }*/


        active = true;
       // Toast.makeText(this, "On start", Toast.LENGTH_SHORT).show();
    }




    @Override
    protected void onStop() {

        Toast.makeText(this, "on Stop", Toast.LENGTH_SHORT).show();
        //startCountingTimeWithHandler();
       // Toast.makeText(this, "On stop", Toast.LENGTH_SHORT).show();
        // starting service when time on clock is more than 0 and it's not ending time
        //handler1.removeCallbacksAndMessages(null);
        active = false;

            savePausedTimeToSharedPreferences(System.currentTimeMillis());


        // TODO Before was if(!flag)
        if(!flag) {
            if (!isMyServiceRunning(ForegroundServices.class)) {
                //timerTask.cancel();
                //startForegroundServiceToCountTime();
                // Toast.makeText(this, "Run ForeGround", Toast.LENGTH_SHORT).show();
            }
        }
        // saving data when only started time not ending time
        if(endingTime.getText().toString().equals("")) {
          //  Toast.makeText(this, "Invoked", Toast.LENGTH_SHORT).show();
            Toast.makeText(this, "I am saving start time", Toast.LENGTH_SHORT).show();
            saveDataToSharedPreferences();
        }
        // if end time was set, clearing data.
        else if (!endingTime.getText().toString().equals(""))
        {
            Toast.makeText(this, "I am clearing start Time", Toast.LENGTH_SHORT).show();
            clearDataToSharedPreferences();
        }
      //  Toast.makeText(this, "OnStop", Toast.LENGTH_SHORT).show();
        super.onStop();
    }



}