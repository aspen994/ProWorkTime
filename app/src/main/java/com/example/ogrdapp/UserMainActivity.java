package com.example.ogrdapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
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
import androidx.lifecycle.ViewModelProvider;

import com.example.ogrdapp.model.TimeModel;
import com.example.ogrdapp.services.ForegroundServices;
import com.example.ogrdapp.view.MainActivity;
import com.example.ogrdapp.view.UserTimeTable;
import com.example.ogrdapp.viewmodel.UserMainActivityViewModel;
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
import java.util.Timer;
import java.util.TimerTask;

public class UserMainActivity extends AppCompatActivity {


    private static final String SHARED_PREFS_TIME_MODEL = "SharedPrefforTimeModel";
    private FirebaseAuth firebaseAuth=FirebaseAuth.getInstance();
    private FirebaseUser fireBaseUser;
    public static final String QRCODE1="Tk6&zE8*odwq7G$u2#IVL1e!Q@JvXrFgS0^NbCn5mO9pDyA4(PcHhY3Za6lWsB)";
    public static final String QRCODE2delay5minutes="yJGZ*q7W#8n6Dv@B1F$%9X4hpYQeS^gU+sa0RwM3zNtVxOcZ2dL5fIHkA6i";
    public static final String SHARED_PREFS="sharedPrefs";
    public static final String TEXT = "text";
    public static final String TIME_MODEL = "text_2";
    public static final String TMP_BEGIN_TIME = "tmp_Begin_Time";
    public static final String TIMER_STARTED ="timerStarted";

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle toogle;

    private TextView userName,date,timeDisplay,textMain,begingTime,endingTime,timerOverall;

    private Timer timer;
    private  TimerTask timerTask;
    private Double time = 0.0;

    private boolean timerStarted = false;
    private Button qr;

    private long tmpBeginTime,tmpEndTime,tmpOverall=0;
    private long delay5minutes = 300000;
    private static final int REQUEST_CODE =22;
    private TimeModel timeModel;
    private ArrayList<TimeModel> arrayList = new ArrayList<>();

    //Firebase Connection
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private CollectionReference collectionReference = db.collection("Users");
    private CollectionReference collectionReferenceTime = db.collection("Time");
    // To Foreground service-------------------------------------------------------------------------
    //private TimerTask timerTask;
    //private Timer timer;

    private long timeLong;
    private boolean flag = true;
    private boolean flagService = true;
    public static boolean active = false;
    public static boolean flagForForegroundService = true;
    String beginingTime = "";
    String endingTTime = "";
    private long tmpBeginTimeFromSharedPreferences;
    private boolean restartFlag = false;
    private boolean flagForSignText = false;
    private boolean isTimerStarted=false;

    BroadcastReceiver broadcastReceiver;

    private  String currentTime = "";

    //ViewModel
    UserMainActivityViewModel userMainActivityViewModel;
    LiveData<Long> timerLiveData;

    boolean flagForBroadCastService = false;
    long addToEndingTime=0;


    // To Foreground service-------------------------------------------------------------------------




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_main);
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.nav_View);
        qr = findViewById(R.id.buttonQR);
        userName = findViewById(R.id.username);
        date = findViewById(R.id.textview_dateToInsert);
        timeDisplay = findViewById(R.id.textView4);
        textMain = findViewById(R.id.textView_begin_work);
        begingTime = findViewById(R.id.begining_time);
        endingTime = findViewById(R.id.ending_time);
        timerOverall = findViewById(R.id.timeOverall);
        userMainActivityViewModel = new ViewModelProvider(this).get(UserMainActivityViewModel.class);
        flagForForegroundService = true;
        Toast.makeText(this, "on Create", Toast.LENGTH_SHORT).show();

        //TODO I Change 26.07.23
        //LiveData<Long> timerLiveData = userMainActivityViewModel.initialValue();
        timerLiveData = userMainActivityViewModel.initialValue();

        timerLiveData.observe(this, new Observer<Long>() {
            @Override
            public void onChanged(Long aLong) {
                timeDisplay.setText(getTimerText(aLong));
               // Log.i("HERE!: " ,getTimerText(aLong));
                if(aLong>0)
                {
                    timerOverall.setText("Przepracowałeś już : " + getTimerText(aLong));

                }
            }
        });


        // Loading and updating data from SharedPreferences
        loadData();
        updateData();

        // Loading proper text Main accroidng if the time of work is started or not.
        if(timerStarted==false)
        {
            textMain.setText("Rozpocznij pracę: ");
        }
        else if(timerStarted)
        {
            textMain.setText("Zatrzymaj pracę: ");
        }

        // Getting current user Id.
        FirebaseUser user = firebaseAuth.getCurrentUser();
        assert  user !=null;
        final String currentUserId = user.getUid();

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
                   // Toast.makeText(UserMainActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        /*// Assign timer to new Object
        timer = new Timer();*/

        date.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));

        firebaseAuth = FirebaseAuth.getInstance();
        // To Foreground service-------------------------------------------------------------------------
        //Permission for the post notification
        if(ContextCompat.checkSelfPermission(UserMainActivity.this, android.Manifest.permission.POST_NOTIFICATIONS)!= PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(UserMainActivity.this,new String[]{android.Manifest.permission.POST_NOTIFICATIONS},101);
        }

        // assign timer
        timer = new Timer();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("Counter");
        //intentFilter.addAction(Intent.Action);

        //userMainActivityViewModel.initialValue().getValue();


        flagForForegroundService = true;
        int counter = 0;
        if(flagService) {
            if (timerTask != null) {
                timerTask.cancel();
            }
            broadcastReceiver = new BroadcastReceiver() {

                @Override
                public void onReceive(Context context, Intent intent) {
                    long longTimeFromBroadcastReceiver = intent.getLongExtra("TimeRemaining", 0);
                        userMainActivityViewModel.setValue(longTimeFromBroadcastReceiver);
                        //userMainActivityViewModel.startTimerSecondTime();
                    Toast.makeText(context, "Here I am", Toast.LENGTH_SHORT).show();
                        startTimerSecondTime();
                        flagForBroadCastService=true;

                      //  Log.i("On Receive ","On receive");

                }
            };
            //Log.i("FLAG SERVICE CHECKING","How many times invoked: " + ++counter);
            registerReceiver(broadcastReceiver, intentFilter);
            flagService = false;
        }


       /* if(flagForBroadCastService)
        {
            userMainActivityViewModel.startTimer();
            Toast.makeText(this, "Start Timer should invoked once", Toast.LENGTH_SHORT).show();
        }*/

        // To Foreground service-------------------------------------------------------------------------
        qr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ScanOptions options = new ScanOptions();
                options.setPrompt("Zeskanuj kod");
                options.setBeepEnabled(true);
                options.setOrientationLocked(true);
                options.setCaptureActivity(Scanner.class);
                flagForSignText=false;
                barLauncher.launch(options);
                restartFlag = false;

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
                    restartFlag=false;
                }
                else if(R.id.action_logout==item.getItemId())
                {
                    firebaseAuth.signOut();
                    startActivity(new Intent(UserMainActivity.this, MainActivity.class));
                    restartFlag=false;
                }
                return true;
            }
        });
        active = true;
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
           tmpBeginTimeFromSharedPreferences = sharedPreferences.getLong(TMP_BEGIN_TIME,tmpBeginTime);
       }
       isTimerStarted = sharedPreferences.getBoolean(TIMER_STARTED,timerStarted);

    }

    public void updateData()
    {
        begingTime.setText(beginingTime);
        timerStarted = isTimerStarted;
        tmpBeginTime = tmpBeginTimeFromSharedPreferences;
    }
    // Block for Shared Preferences -------------------


    // To Foreground service-------------------------------------------------------------------------

    private void startTimer() {

        //timer = new Timer();
        userMainActivityViewModel.setValue(0);
        userMainActivityViewModel.startTimer();

        flag = false;
    }

    private void startTimerSecondTime()
    {
        userMainActivityViewModel.startTimerSecondTime();

        //flag = false;
    }
    public void stopTime()
    {
        userMainActivityViewModel.stopTimerTask();

    /*    if(timerTask!=null)
        {
            timerTask.cancel();
            //timerOverall.setText(checkMethod(timeLong));
            timeLong=0;
        }*/


        /*Intent serviceIntent = new Intent(this, ForegroundServices.class);
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
        try {
            unregisterReceiver(broadcastReceiver);
        }
        catch(IllegalArgumentException e)
        {
            e.printStackTrace();
        }
        Intent serviceIntent = new Intent(this,ForegroundServices.class);
        stopService(serviceIntent);
       // Log.i("Time finnaly",time+"");
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
             //   Toast.makeText(UserMainActivity.this, "Dobry kod", Toast.LENGTH_SHORT).show();


                if(timerStarted == false)
                {
                    timeDisplay.setText("");
                    textMain.setText("Zatrzymaj pracę: ");
                    cleanDataForTimeModel();
                    currentTime = getCurrentTime();
                //  timeModel.setTimeBegin(getCurrentTime());
                    saveTimeModel();
                    begingTime.setText("Rozpoczęto pracę o: " + getCurrentTime());
                    tmpBeginTime = getCurrentTimeInSimpleFormat();
                    startTimer();
                    //timeDisplay.setText(userMainActivityViewModel.startTimer());
                    //flagForSignText =false;
                    timerStarted = true;
                    endingTime.setText("");
                }
                else {
                    timeModel = new TimeModel();
                    timerStarted = false;
                    textMain.setText("Rozpocznij pracę: ");
                    //endingTime.setText("Zakończono pracę o : " + getCurrentTime());

                   // timeModel.setTimeEnd(getCurrentTime());
                    tmpEndTime = getCurrentTimeInSimpleFormat();

                    endingTime.setText("Zakończono pracę o : " + getCurrentTime());
                    timeModel.setTimeBegin(loadAndUpdatedTimeModel());
                    //TODO i get
                    //tmpOverall = timeLong;
                    tmpOverall = userMainActivityViewModel.initialValue().getValue();
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
                        timeModel.setTimeOverall(getTimerText(tmpOverall));
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
                    startTimer();
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
                    timerTask.cancel();
                }

            }

            else {
              //  Toast.makeText(UserMainActivity.this, "Błąd 2", Toast.LENGTH_SHORT).show();
            }
        }
    });


    private void startForegroundServiceToCountTime() {
        Intent intentService = new Intent(this, ForegroundServices.class);
        //Time is in seconds
        intentService.putExtra("TimeValue", timerLiveData.getValue());
        startService(intentService);
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

    private String getTimerText(long milliseconds)
    {
        int seconds = (int) (milliseconds / 1000) % 60 ;
        int minutes = (int) ((milliseconds / (1000*60)) % 60);
        int hours   = (int) ((milliseconds / (1000*60*60)) % 24);

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
        // starting service when time on clock is more than 0 and it's not ending time
        active = false;
        try {
            unregisterReceiver(broadcastReceiver);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        if(!flag) {
            //timerTask.cancel();
            startForegroundServiceToCountTime();
           // Toast.makeText(this, "Run ForeGround", Toast.LENGTH_SHORT).show();
        }
        // saving data when only started time not ending time
        if(endingTime.getText().toString().equals("")) {
            saveData();
        }
        // if end time was set, clearing data.
        else if (!endingTime.getText().toString().equals(""))
        {
            clearData();
        }
      //  Toast.makeText(this, "OnStop", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}