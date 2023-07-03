package com.example.ogrdapp;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ogrdapp.model.TimeModel;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.auth.User;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanIntentResult;
import com.journeyapps.barcodescanner.ScanOptions;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class UserMainActivity extends AppCompatActivity {


    private FirebaseAuth firebaseAuth=FirebaseAuth.getInstance();
    public static final String QRCODE1="Tk6&zE8*odwq7G$u2#IVL1e!Q@JvXrFgS0^NbCn5mO9pDyA4(PcHhY3Za6lWsB)";

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle toogle;

    private TextView userName,date,timeDisplay,textMain,begingTime,endingTime,timerOverall;

    private Timer timer;
    private TimerTask timerTask;
    private Double time = 0.0;

    private boolean timerStarted = false;
    private Button qr;

    private long tmpBeginTime,tmpEndTime,tmpOverall;
    private static final int REQUEST_CODE =22;
    private TimeModel timeModel;
    private ArrayList<TimeModel> arrayList = new ArrayList<>();

    //Firebase Connection
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private CollectionReference collectionReference = db.collection("Users");

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
            if(result.getContents()!=null && result.getContents().toString().equals(QRCODE1))
            {
                Toast.makeText(UserMainActivity.this, "Dobry kod", Toast.LENGTH_SHORT).show();



                if(timerStarted == false)
                {
                    timeModel = new TimeModel();
                    timerStarted = true;
                    textMain.setText("Zatrzymaj pracę: ");
                    /*//TODO I've commeted this to test new feature
                    // If ending time has a text do not change beging time
                    if(TextUtils.isEmpty(endingTime.getText().toString())) {
                        begingTime.setText("Rozpoczęto pracę o: " + getCurrentTime());
                    }*/



                    timeModel.setTimeBegin(getCurrentTime());
                    begingTime.setText("Rozpoczęto pracę o: " + getCurrentTime());
                    tmpBeginTime = getCurrentTimeInSimpleFormat();
                    startTimer();
                }
                else
                {
                    timerStarted = false;
                    textMain.setText("Rozpocznij pracę: ");
                    endingTime.setText("Zakończono pracę o : " + getCurrentTime());
                    timeModel.setTimeEnd(getCurrentTime());
                    tmpEndTime = getCurrentTimeInSimpleFormat();

                    // Counting time overall
                    tmpOverall += tmpEndTime - tmpBeginTime;

                    //Log.i("TIME",String.valueOf(tmpOverall));

                    /*SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");//dd/MM/yyyy
                    Date local = new Date(tmpOverall);
                    String sf = sdf.format(local);*/

                    long seconds = tmpOverall / 1000;
                    long minutes = seconds / 60;
                    long hours = minutes / 60;

                    seconds %= 60;
                    minutes %= 60;

                    String formattedTime = String.format("%02d:%02d:%02d", hours, minutes, seconds);


                    /*Intent i = new Intent(UserMainActivity.this,UserTimeTable.class);
                    i.putExtra("key",formattedTime);*/

                    timeModel.setTimeOverall(formattedTime);
                    timeModel.setTimeOverallInLong(tmpOverall);

                    arrayList.add(timeModel);


                    timerOverall.setText(formattedTime);

                    timerTask.cancel();
                }



               /* if(isRunning)
                {
                    textMain.setText("Zakończ pracę ");
                    startStopCountingTime();
                }
                else {
                    textMain.setText("Rozpocznij pracę: ");
                    //isRunning=false;
                }*/

                /*AlertDialog.Builder builder = new AlertDialog.Builder(UserMainActivity.this);
                builder.setTitle("Result");
                builder.setMessage(result.getContents());
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    }
                }).show();*/
            }
            else {
                Toast.makeText(UserMainActivity.this, "Błąd 2", Toast.LENGTH_SHORT).show();
            }
        }
    });

    private String getCurrentTime() {
        //"yyyy-MM-dd HH:mm:ss.SSS"
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");//dd/MM/yyyy
        Date now = new Date();
        String sf = sdf.format(now);
        return sf;
    }

    private long getCurrentTimeInSimpleFormat() {
        //"yyyy-MM-dd HH:mm:ss.SSS"

        return System.currentTimeMillis();
    }


    private void startStopCountingTime() {

     /*   isRunning = true;

            //seconds = 0;

            final Handler handler = new Handler();

        handler.post(new Runnable() {
                @Override
                public void run() {
                    int hours = seconds / 3600;
                    int minutes = (seconds % 3600) / 60;
                    int secs = seconds % 60;

                    String time = String.format("%02d:%02d:%02d", hours, minutes, secs);
                    timeDisplay.setText(time);

                    if (isRunning) {
                        seconds++;
                    }

                    handler.postDelayed(this, 1000);

                }
            });*/

    }

    @Override
    protected void onRestart() {
        Toast.makeText(this, "Restartuje widok", Toast.LENGTH_SHORT).show();
        /*timeModel = new TimeModel();
        timeModel.setTimeBegin(begingTime.getText().toString());
        timeModel.setTimeEnd(endingTime.getText().toString());*/
        super.onRestart();
    }

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
        //arrayList.add(timeModel);


        Toast.makeText(this, "On create invoked", Toast.LENGTH_SHORT).show();



        FirebaseUser user = firebaseAuth.getCurrentUser();
        Log.i("111USER111",user.getEmail());
        assert  user !=null;
        final String currentUserId = user.getUid();
        Log.i("!!!USER!!!",currentUserId);


        // TODO 01.06.23 17:58 zakomentowałem
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

        timer = new Timer();


        date.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));

        firebaseAuth = FirebaseAuth.getInstance();

        //userName.setText(firebaseAuth.getCurrentUser().getEmail().toString());

        qr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               /* startActivity(new Intent(UserMainActivity.this,Scanner.class));
                Intent openCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(openCamera,REQUEST_CODE);*/



                ScanOptions options = new ScanOptions();
                options.setPrompt("Zeskanuj kod");
                options.setBeepEnabled(true);
                options.setOrientationLocked(true);
                options.setCaptureActivity(Scanner.class);
                barLauncher.launch(options);

            }
        });




        toogle = new ActionBarDrawerToggle(this,drawerLayout,R.string.open,R.string.close);
        drawerLayout.addDrawerListener(toogle);
        toogle.syncState();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                if(R.id.action_time==item.getItemId())
                {
                    Intent i = new Intent(UserMainActivity.this,UserTimeTable.class);
                    Log.i("SIZE ARRAY LIST FROM MAIN",arrayList.size()+"");
                    i.putExtra("timeModel",arrayList);
                    startActivity(i);
                    //startActivity(new Intent(UserMainActivity.this,UserTimeTable.class));
                }
                else if(R.id.action_logout==item.getItemId())
                {
                    firebaseAuth.signOut();
                    startActivity(new Intent(UserMainActivity.this,MainActivity.class));
                }
                return true;
            }
        });


    }

    private void startTimer()
    {
        timerTask = new TimerTask()
        {
            @Override
            public void run()
            {
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        time++;
                        timeDisplay.setText(getTimerText());
                    }
                });
            }

        };
        timer.scheduleAtFixedRate(timerTask, 0 ,1000);
    }

    private String getTimerText()
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

  /*  @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Bitmap photo = (Bitmap)data.getExtras().get("data");
    }*/
    /* //2-Adding menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // R.menu.menu - works without defining id menu.

        //TODO: Może zamień miejscami i zadziała
        getMenuInflater().inflate(R.menu.menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        return super.onOptionsItemSelected(item);
    }*/
}