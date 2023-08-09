package com.example.ogrdapp;

import static com.example.ogrdapp.services.ForegroundServices.isPaused;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
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

public class UserMainActivity extends AppCompatActivity {


    private static final String SHARED_PREFS_TIME_MODEL = "SharedPrefforTimeModel";
    private static final String SHARED_PREF_PAUSED_TIME = "SharedPreffForPausedTime";
    private static final String PAUSED_TIME = "pausedTime";
    private static final String PAUSED_TIME_BOOLEAN = "booleanIsPaused";
    private FirebaseAuth firebaseAuth=FirebaseAuth.getInstance();
    private FirebaseUser fireBaseUser;
    public static final String QRCODE1="Tk6&zE8*odwq7G$u2#IVL1e!Q@JvXrFgS0^NbCn5mO9pDyA4(PcHhY3Za6lWsB)";
    public static final String QRCODE2delay5minutes="yJGZ*q7W#8n6Dv@B1F$%9X4hpYQeS^gU+sa0RwM3zNtVxOcZ2dL5fIHkA6i";
    public static final String SHARED_PREFS="sharedPrefs";
    public static final String TEXT = "text";
    public static final String TIME_MODEL = "text_2";
    public static final String TMP_BEGIN_TIME = "tmp_Begin_Time";
    public static final String TIMER_STARTED ="timerStarted";
    public static final String SECONDS_STOPWATCH= "secondsStopWatch";


    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle toogle;

    private TextView userName,date,timeDisplay,textMain,begingTime,endingTime,timerOverall;


    private boolean timerStarted = false;
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
    public static boolean flagForForegroundService = true;
    String beginingTime = "";

    private long tmpBeginTimeFromSharedPreferences;
    private boolean isTimerStarted=false;

    private  String currentTime = "";

    LiveData<Long> timerLiveData;

    long addToEndingTime=0;
    public IntentFilter intentFilter;


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

        // Loading and updating data from SharedPreferences (timeStarted, flag for isStarted time  and so on)
        loadData();
        updateData();

        //
        uploadAndLoadPausedTime();


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
                if(!timerStarted) {
                    stopTime();
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
            }
        });

        // - - - - - - - - - - - STOPWORK ON CLICKLISTNER - - - - - - - -//

        LiveData<Long> timePause;
        timePause = ForegroundServices.mutableLiveDataTimeForPause;

        timePause.observe(this, new Observer<Long>() {
            @Override
            public void onChanged(Long aLong) {
                if(isPaused) {
                    holdResumeWork.setText("Pauza: "+getTimerText(aLong));
                }
            }
        });

        holdResumeWork.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                isPaused = !isPaused;

                //TODO Check if it's properly
                holdResumeWork.setText("Zastopuj pracę");
                if(isPaused)
                {
                    Toast.makeText(UserMainActivity.this, "Paused", Toast.LENGTH_SHORT).show();
                    ForegroundServices.timeLongForPause=0;
                    stopWork.setVisibility(View.VISIBLE);
                    holdResumeWork.setVisibility(View.VISIBLE);
                    holdResumeWork.setBackgroundColor(Color.GREEN);
                    stopCountingTime();
                    stopTimeWithoutStoppingService();


                    cleanDataForTimeModel();
                    currentTime = getCurrentTime();
                    saveTimeModel();
                    tmpBeginTime = getCurrentTimeInSimpleFormat();
                 //   timerStarted = true;

                        holdResumeWork.setBackgroundColor(Color.GREEN);
                        qr.setVisibility(View.INVISIBLE);
                        textMain.setVisibility(View.INVISIBLE);



                }
                else {
                    Toast.makeText(UserMainActivity.this, "unPaused", Toast.LENGTH_SHORT).show();
                    holdResumeWork.setBackgroundColor(Color.parseColor("#A214D5"));
                    startCountingTime();
                    startTimerWithoutStartingNewService(0);
                    qr.setVisibility(View.VISIBLE);
                    textMain.setVisibility(View.VISIBLE);

                }



            }

        });


        // ---------------- P - E - R - M -I - S - S - I - O - N -S -------------

        //Permission for the post notification
        if(ContextCompat.checkSelfPermission(UserMainActivity.this, android.Manifest.permission.POST_NOTIFICATIONS)!= PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(UserMainActivity.this,new String[]{android.Manifest.permission.POST_NOTIFICATIONS},101);
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
            startTimer(timeInSeconds);
        }



        //Setting timerLiveData from ForegroundService
        timerLiveData = ForegroundServices.time;

        timerLiveData.observe(this, new Observer<Long>() {
            @Override
            public void onChanged(Long aLong) {
                timeDisplay.setText(getTimerText(aLong));
                if(aLong>0)
                {
                    timerOverall.setText("Przepracowałeś już : " + getTimerText(aLong));

                }
            }
        });

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


    }

    private void startTimerWithoutStartingNewService(long seconds) {
        ForegroundServices.time.setValue(0L);
        ForegroundServices.timeLongForClock =seconds;

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

    private void saveTimeModel()
    {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS_TIME_MODEL,MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(TMP_BEGIN_TIME,currentTime);
        editor.apply();
    }

    private String loadAndUpdatedTimeModel()
    {
        SharedPreferences sharedPreferencesTimeModel = getSharedPreferences(SHARED_PREFS_TIME_MODEL,MODE_PRIVATE);
            currentTime = sharedPreferencesTimeModel.getString(TMP_BEGIN_TIME, currentTime);
            return currentTime;
    }
    public void saveData()
    {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS,MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(TEXT,begingTime.getText().toString());
        //editor.putString(TEXT_2,endingTime.getText().toString());
        editor.putBoolean(TIMER_STARTED,timerStarted);
        editor.putLong(TMP_BEGIN_TIME,tmpBeginTime);

       // Toast.makeText(this, "Save" +ForegroundServices.time.getValue(), Toast.LENGTH_SHORT).show();

        editor.apply();
    }
    public void savePausedTime(long pausedTime)
    {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREF_PAUSED_TIME,MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(PAUSED_TIME,pausedTime);
        editor.putBoolean(PAUSED_TIME_BOOLEAN,isPaused);
        editor.apply();
    }

    public void uploadAndLoadPausedTime()
    {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREF_PAUSED_TIME,MODE_PRIVATE);
        pausedTimeToSharedPref = sharedPreferences.getLong(PAUSED_TIME, pausedTimeToSharedPref);
        isPaused = sharedPreferences.getBoolean(PAUSED_TIME_BOOLEAN,isPaused);
        Toast.makeText(this, "Upload Paused", Toast.LENGTH_SHORT).show();
    }

    public void clearDataForLoadPausedTime()
    {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS,MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(PAUSED_TIME,0);

        editor.apply();
    }

    public void clearData()
    {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS,MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(TEXT,"");
        editor.putBoolean(TIMER_STARTED,false);
        editor.putLong(TMP_BEGIN_TIME,0);

        editor.apply();
    }
    private void cleanDataForTimeModel() {
        SharedPreferences sharedPreferencesTimeModel = getSharedPreferences(SHARED_PREFS_TIME_MODEL,MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferencesTimeModel.edit();

        editor.putString(TMP_BEGIN_TIME,"");

        editor.apply();
    }

    public void loadData()
    {
       SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS,MODE_PRIVATE);
       if(endingTime.getText().toString().equals("")) {
           beginingTime = sharedPreferences.getString(TEXT, begingTime.getText().toString());
       }
        isTimerStarted = sharedPreferences.getBoolean(TIMER_STARTED,timerStarted);
        tmpBeginTimeFromSharedPreferences = sharedPreferences.getLong(TMP_BEGIN_TIME,tmpBeginTime);


    }
    public void loadDataForStopWatch()
    {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS,MODE_PRIVATE);
        ForegroundServices.time.setValue((sharedPreferences.getLong(SECONDS_STOPWATCH,0)));
     //   Toast.makeText(this, "load data" +ForegroundServices.time.getValue(), Toast.LENGTH_SHORT).show();
    }

    public void updateData()
    {
        begingTime.setText(beginingTime);
        timerStarted = isTimerStarted;
        tmpBeginTime = tmpBeginTimeFromSharedPreferences;
    }
    // Block for Shared Preferences -------------------


    // To Foreground service-------------------------------------------------------------------------

    private void startTimer(long seconds) {

        startForegroundServiceToCountTime();
        ForegroundServices.time.setValue(0L);
        ForegroundServices.timeLongForClock =seconds;

        flag = false;
    }

    private void startPausedTime(long seconds) {

        startForegroundServiceToCountTime();
        ForegroundServices.mutableLiveDataTimeForPause.setValue(0L);
        ForegroundServices.timeLongForPause =seconds;

    }


    public void stopTime()
    {
        Intent serviceIntent = new Intent(this,ForegroundServices.class);
        stopService(serviceIntent);
        isPaused=false;
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
        isPaused=true;
        flag=true;
        flagService=false;
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
                    //startForegroundServiceToCountTime();
                    timeDisplay.setText("");
                    textMain.setText("Zatrzymaj pracę: ");
                    cleanDataForTimeModel();
                    currentTime = getCurrentTime();
                //  timeModel.setTimeBegin(getCurrentTime());
                    saveTimeModel();
                    begingTime.setText("Rozpoczęto pracę o: " + getCurrentTime());
                    tmpBeginTime = getCurrentTimeInSimpleFormat();
                    startTimer(0);
                    holdResumeWork.setText("Wstrzymaj pracę");

                    //timeDisplay.setText(userMainActivityViewModel.startTimer());
                    //flagForSignText =false;
                    timerStarted = true;
                    endingTime.setText("");

                    stopWork.setVisibility(View.VISIBLE);
                    holdResumeWork.setVisibility(View.VISIBLE);
                    holdResumeWork.setBackgroundColor(Color.parseColor("#A214D5"));
                }
                else {
                    holdResumeWork.setVisibility(View.INVISIBLE);
                    stopWork.setVisibility(View.INVISIBLE);
                    timeModel = new TimeModel();
                    timerStarted = false;
                    textMain.setText("Rozpocznij pracę: ");
                    //endingTime.setText("Zakończono pracę o : " + getCurrentTime());

                    timeModel.setTimeEnd(getCurrentTime());
                    tmpEndTime = getCurrentTimeInSimpleFormat();

                    endingTime.setText("Zakończono pracę o : " + getCurrentTime());
                    timeModel.setTimeBegin(loadAndUpdatedTimeModel());
                    //TODO i get
                    //tmpOverall = timeLong;
                    tmpOverall = ForegroundServices.time.getValue()*1000;
                    stopTime();

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

            }

            else {
              //  Toast.makeText(UserMainActivity.this, "Błąd 2", Toast.LENGTH_SHORT).show();
            }

            if(result.getContents()!=null && result.getContents().toString().equals(QRCODE2delay5minutes))
            {
               // Toast.makeText(UserMainActivity.this, "Dobry kod", Toast.LENGTH_SHORT).show();



                if(timerStarted == false)
                {
                    timeModel = new TimeModel();
                    timerStarted = true;
                    textMain.setText("Zatrzymaj pracę: ");
                    timeModel.setTimeBegin(getCurrentTime());
                    begingTime.setText("Rozpoczęto pracę o: " + getCurrentTime());
                    tmpBeginTime = getCurrentTimeInSimpleFormat();
                    startTimer(0);
                    // Setting delay for the qr code
                    tmpOverall-=delay5minutes;
                   // Log.i("Logging","logged");
                }
                else
                {
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


    private void startForegroundServiceToCountTime() {
        Intent intentService = new Intent(this, ForegroundServices.class);
        //Time is in seconds
        //intentService.putExtra("TimeValue", timerLiveData.getValue());
        startService(intentService);
        Log.i("Start ForeGround","Start Foregroundservice");
    }


    private long getCurrentTimeInSimpleFormat() {
        //"yyyy-MM-dd HH:mm:ss.SSS"
        return System.currentTimeMillis();
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
        cleanDataForTimeModel();
        currentTime = getCurrentTime();
        //  timeModel.setTimeBegin(getCurrentTime());
        saveTimeModel();
        begingTime.setText("Rozpoczęto pracę o: " + getCurrentTime());
        tmpBeginTime = getCurrentTimeInSimpleFormat();
        //startTimer(0);

        //timeDisplay.setText(userMainActivityViewModel.startTimer());
        //flagForSignText =false;
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
        timerStarted = false;
        textMain.setText("Rozpocznij pracę: ");
        //endingTime.setText("Zakończono pracę o : " + getCurrentTime());

        timeModel.setTimeEnd(getCurrentTime());
        tmpEndTime = getCurrentTimeInSimpleFormat();

        endingTime.setText("Zakończono pracę o : " + getCurrentTime());
        timeModel.setTimeBegin(loadAndUpdatedTimeModel());

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
        super.onStop();
       // Toast.makeText(this, "On stop", Toast.LENGTH_SHORT).show();
        // starting service when time on clock is more than 0 and it's not ending time
        active = false;

            savePausedTime(System.currentTimeMillis());

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
            saveData();
        }
        // if end time was set, clearing data.
        else if (!endingTime.getText().toString().equals(""))
        {
            clearData();
        }
      //  Toast.makeText(this, "OnStop", Toast.LENGTH_SHORT).show();
    }



}