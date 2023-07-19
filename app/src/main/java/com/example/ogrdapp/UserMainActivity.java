package com.example.ogrdapp;

import android.content.Intent;
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
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.ogrdapp.model.TimeModel;
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


    private FirebaseAuth firebaseAuth=FirebaseAuth.getInstance();
    private FirebaseUser fireBaseUser;
    public static final String QRCODE1="Tk6&zE8*odwq7G$u2#IVL1e!Q@JvXrFgS0^NbCn5mO9pDyA4(PcHhY3Za6lWsB)";
    public static final String QRCODE2delay5minutes="yJGZ*q7W#8n6Dv@B1F$%9X4hpYQeS^gU+sa0RwM3zNtVxOcZ2dL5fIHkA6i";

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle toogle;

    private TextView userName,date,timeDisplay,textMain,begingTime,endingTime,timerOverall;

    private Timer timer;
    private TimerTask timerTask;
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

        qr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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
                    startActivity(i);
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


            if(result.getContents()!=null && result.getContents().toString().equals(QRCODE1))
            {
                Toast.makeText(UserMainActivity.this, "Dobry kod", Toast.LENGTH_SHORT).show();



                if(timerStarted == false)
                {
                    timeModel = new TimeModel();
                    timerStarted = true;
                    textMain.setText("Zatrzymaj pracę: ");
                    timeModel.setTimeBegin(getCurrentTime());
                    begingTime.setText("Rozpoczęto pracę o: " + getCurrentTime());
                    tmpBeginTime = getCurrentTimeInSimpleFormat();
                    startTimer();
                }
                else {
                    timerStarted = false;
                    textMain.setText("Rozpocznij pracę: ");
                    endingTime.setText("Zakończono pracę o : " + getCurrentTime());
                    timeModel.setTimeEnd(getCurrentTime());
                    tmpEndTime = getCurrentTimeInSimpleFormat();


                    // Counting time overall
                    tmpOverall += tmpEndTime - tmpBeginTime;

                    if (tmpOverall < 0) {
                        tmpOverall = 0;
                    }

                    if(tmpOverall!=0)
                    {
                    long seconds = tmpOverall / 1000;
                    long minutes = seconds / 60;
                    long hours = minutes / 60;

                    seconds %= 60;
                    minutes %= 60;

                    String formattedTime = String.format("%02d:%02d:%02d", hours, minutes, seconds);

                    timeModel.setTimeOverall(formattedTime);
                    timeModel.setTimeOverallInLong(tmpOverall);
                    timeModel.setId(currentUserId);
                    timeModel.setUserName(userName.getText().toString());
                    timeModel.setTimeAdded(new Timestamp(new Date()));

                    arrayList.add(timeModel);

                    collectionReferenceTime.add(timeModel).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            Toast.makeText(UserMainActivity.this, "Data added sucesfully", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(UserMainActivity.this, "Fail on adding data", Toast.LENGTH_SHORT).show();
                        }
                    });

                    timerOverall.setText(formattedTime);

                }

                    timerTask.cancel();
                }

            }

            else {
                Toast.makeText(UserMainActivity.this, "Błąd 2", Toast.LENGTH_SHORT).show();
            }

            if(result.getContents()!=null && result.getContents().toString().equals(QRCODE2delay5minutes))
            {
                Toast.makeText(UserMainActivity.this, "Dobry kod", Toast.LENGTH_SHORT).show();



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
                    Log.i("Logging","logged");
                }
                else
                {
                    timerStarted = false;
                    textMain.setText("Rozpocznij pracę: ");
                    endingTime.setText("Zakończono pracę o : " + getCurrentTime());
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

                        String formattedTime = String.format("%02d:%02d:%02d", hours, minutes, seconds);
                        timeModel.setTimeOverall(formattedTime);
                        timeModel.setTimeOverallInLong(tmpOverall);
                        timeModel.setId(currentUserId);
                        timeModel.setUserName(userName.getText().toString());
                        timeModel.setTimeAdded(new Timestamp(new Date()));

                        arrayList.add(timeModel);

                        collectionReferenceTime.add(timeModel).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                Toast.makeText(UserMainActivity.this, "Data added sucesfully 5 minutes dellay", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(UserMainActivity.this, "Fail on adding data", Toast.LENGTH_SHORT).show();
                            }
                        });

                        timerOverall.setText(formattedTime);
                    }
                    timerTask.cancel();
                }

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

}