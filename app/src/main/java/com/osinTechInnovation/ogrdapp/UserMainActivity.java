package com.osinTechInnovation.ogrdapp;


import static com.osinTechInnovation.ogrdapp.services.ForegroundServices.HOUR_IN_SECONDS;
import static com.osinTechInnovation.ogrdapp.services.ForegroundServices.isPaused;

import android.Manifest;
import android.app.ActivityManager;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.work.BackoffPolicy;
import androidx.work.Constraints;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryPurchasesParams;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.Timestamp;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanIntentResult;
import com.journeyapps.barcodescanner.ScanOptions;
import com.osinTechInnovation.ogrdapp.model.QRModel;
import com.osinTechInnovation.ogrdapp.model.TimeModel;
import com.osinTechInnovation.ogrdapp.scanner.CustomScannerActivity;
import com.osinTechInnovation.ogrdapp.services.ForegroundServices;
import com.osinTechInnovation.ogrdapp.utility.ConnectionClass;
import com.osinTechInnovation.ogrdapp.utility.DecodeDaysAndEntries;
import com.osinTechInnovation.ogrdapp.utility.SharedPreferencesDataSource;
import com.osinTechInnovation.ogrdapp.view.AdminView;
import com.osinTechInnovation.ogrdapp.view.MainActivity;
import com.osinTechInnovation.ogrdapp.view.Subs;
import com.osinTechInnovation.ogrdapp.view.UserTimeTable;
import com.osinTechInnovation.ogrdapp.viewmodel.AuthViewModel;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class UserMainActivity extends AppCompatActivity {

    private BillingClient billingClient;


    public List<QRModel> QRCodeList;
    public static final long MINUTE_IN_SECONDS = 60;

    //Widgets
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle toogle;

    private TextView userName, date, timeDisplay, textMain, begingTime, endingTime, timerOverall;

    private ImageButton qr;
    private Button holdResumeWork, stopWork;

    // Variables
    public static boolean timerStarted = false;

    private long tmpBeginTime= 0;
    public static long delayToAssign;

    private TimeModel timeModel;

    // To Foreground service-------------------------------------------------------------------------
    public static boolean active = false;
    private String beginingTime = "";
    private String currentTime = "";
    private long addToEndingTime = 0;
    public IntentFilter intentFilter;
    Handler handler1 = new Handler(Looper.getMainLooper());
    private long timeOfCreation = 0;

    private SharedPreferencesDataSource sharedPreferencesDataSource = SharedPreferencesDataSource.getInstance();
    private AuthViewModel authViewModel;

    String foreginKey;

    private boolean isAdmin = false;

    public static String android_id;
    public boolean itemShowed = false;

    private GoogleSignInClient googleSignInClient;
    private Dialog dialog,dialogUserOrAdmin;
    private Button btnDialogYes, btnDialogNo;



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

        dialog = new Dialog(UserMainActivity.this);
        dialog.setContentView(R.layout.dialog_logout);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.custom_dialog_bg));

        btnDialogYes = dialog.findViewById(R.id.btn_edit_date);
        btnDialogNo = dialog.findViewById(R.id.btn_settle_the_employee);

        dialogUserOrAdmin = new Dialog(UserMainActivity.this);
        dialogUserOrAdmin.setContentView(R.layout.dialog_chose_user_admin);
        dialogUserOrAdmin.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        dialogUserOrAdmin.getWindow().setBackgroundDrawable(getDrawable(R.drawable.custom_dialog_bg));
        dialogUserOrAdmin.setCancelable(false);


        GoogleSignInOptions options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.serverClientId))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(UserMainActivity.this, options);




        // TODO 24.06.24r
        ConnectionClass.premium = false;

        hideItem();


        stopWork.setEnabled(false);
        holdResumeWork.setEnabled(false);


        stopWork.setText(getString(R.string.wait));
        holdResumeWork.setText(getString(R.string.wait));


        QRCodeList = new ArrayList<>();

        // ------ Billing - SUBSCRIBE-----

        billingClient = BillingClient.newBuilder(this)
                .enablePendingPurchases()
                .setListener(purchasesUpdatedListener)
                .build();


        Menu menu = navigationView.getMenu();
        menu.findItem(R.id.action_buy_subs).setVisible(false);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Loading and updating data from SharedPreferences (timeStarted, flag for isStarted time  and so on)
        loadDataFromSharedPreferences();
        updateData();

        //
        uploadAndLoadPausedTimeFromSharedPreferences();


        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        android_id = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);

        writeToFile(android_id);

        authViewModel.getCheckSubscribtion().observe(UserMainActivity.this, new Observer<String>() {
            @Override
            public void onChanged(String email1) {
                authViewModel.getGetEmailMutableLiveData().observe(UserMainActivity.this, new Observer<String>() {
                    @Override
                    public void onChanged(String email2) {
                        if (email1.equals(email2)) {
                            showItem();
                            ConnectionClass.premium = true;
                        } else {
                            Toast.makeText(UserMainActivity.this, getString(R.string.you_are_subscribed_to_a_different_email_address), Toast.LENGTH_SHORT).show();
                        }

                    }
                });

            }
        });


        // Should start the service if the timer i started.
        if (!isMyServiceRunning(ForegroundServices.class) && (timerStarted || isPaused)) {
            Intent intent = new Intent(UserMainActivity.this, ForegroundServices.class);
            startService(intent);
            /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent);
            }*/
        }

        // After loading data checking if clock is running if it is show the buttons
        if (timerStarted || isPaused) {
            stopWork.setVisibility(View.VISIBLE);
            holdResumeWork.setVisibility(View.VISIBLE);
            if (isPaused && ((new Date().getTime() - getTimeOfCreationFromSharedPreferences()) / 1000) < 8 * HOUR_IN_SECONDS) {
                holdResumeWork.setBackgroundColor(Color.GREEN);
                qr.setVisibility(View.INVISIBLE);
                textMain.setVisibility(View.INVISIBLE);
            } else if (isPaused && ((new Date().getTime() - getTimeOfCreationFromSharedPreferences()) / 1000) > 8 * HOUR_IN_SECONDS) {
                holdResumeWork.setVisibility(View.INVISIBLE);
                qr.setVisibility(View.VISIBLE);
                stopWork.setVisibility(View.INVISIBLE);
            }
        } else {
            stopWork.setVisibility(View.INVISIBLE);
            holdResumeWork.setVisibility(View.INVISIBLE);
        }


        // - - - - - - - - - - - STOPWORK ON CLICKLISTNER - - - - - - - -//
        stopWork.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ServiceHelper.isCountingTimeActive = false;

                stopWork.setVisibility(View.INVISIBLE);
                holdResumeWork.setVisibility(View.INVISIBLE);

                if (!timerStarted) {
                    qr.setVisibility(View.VISIBLE);
                    textMain.setVisibility(View.VISIBLE);
                    Intent serviceIntent = new Intent(UserMainActivity.this, ForegroundServices.class);
                    stopService(serviceIntent);
                } else {
                    stopCountingTime();
                    stopTime();
                    delayToAssign = 0;
                    qr.setVisibility(View.VISIBLE);
                }
                timerStarted = false;
                isPaused = false;

                handler1.removeCallbacksAndMessages(null);

            }
        });

        // - - - - - - - - - - - HOLD_RESUME_WORK ON CLICKLISTNER - - - - - - - -//q
        holdResumeWork.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (timeDisplay.getText().toString().contains("-")) {
                    Toast.makeText(UserMainActivity.this, getString(R.string.wait) + timeDisplay.getText().toString(), Toast.LENGTH_SHORT).show();
                } else {

                    ServiceHelper.isCountingTimeActive = true;


                    isPaused = !isPaused;


                    saveCreationTimeToSharedPref(new Date().getTime());
                    startCountingTimeWithHandler(delayToAssign);
                    if (isPaused) {

                        stopWork.setVisibility(View.VISIBLE);

                        holdResumeWork.setVisibility(View.VISIBLE);
                        holdResumeWork.setBackgroundColor(Color.GREEN);

                        stopCountingTime();
                        stopTimeWithoutStoppingService();

                        cleanDataForTimeModelToSharedPreferences();
                        currentTime = getCurrentTime();
                        saveTimeModelToSharedPreferences();
                        tmpBeginTime = getCurrentTimeInSimpleFormat();

                        qr.setVisibility(View.VISIBLE);
                        textMain.setVisibility(View.VISIBLE);
                        delayToAssign = 0;

                    } else {
                        holdResumeWork.setBackgroundColor(Color.parseColor("#A214D5"));
                        startCountingTime();
                        startTimerWithoutStartingNewService();
                        qr.setVisibility(View.VISIBLE);
                        textMain.setVisibility(View.VISIBLE);
                        holdResumeWork.setText(getString(R.string.hold_work));
                    }

                }
            }

        });


        // ---------------- P - E - R - M -I - S - S - I - O - N -S -------------

        //Permission for the post notification
        if (ContextCompat.checkSelfPermission(UserMainActivity.this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(UserMainActivity.this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 103);
        }
        if (ContextCompat.checkSelfPermission(UserMainActivity.this, Manifest.permission.WAKE_LOCK) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(UserMainActivity.this, new String[]{Manifest.permission.WAKE_LOCK}, 101);
        }


        // Assignment user name and surname to textView from collectionReferences
        authViewModel.getUsernameAndSurname().observe(this, user -> {
            String username = user.getUsername();
            String userSurname = user.getSurName();


            userName.setText(username + " " + userSurname);
            stopWork.setEnabled(true);
            holdResumeWork.setEnabled(true);
            stopWork.setText(getString(R.string.stop_work));
            holdResumeWork.setText(getString(R.string.hold_work));
            //TUTAJ DAJE ID FOREGINKEY DLA QR CODE DO ODCZYTU.

            foreginKey = user.getForeign_key();

            authViewModel.getDataQRCode(foreginKey).observe(this, qrModels -> {
                QRCodeList.clear();
                QRCodeList.addAll(qrModels);
            });
        });

        date.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));

        qr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ScanOptions options = new ScanOptions();
                options.setPrompt(getString(R.string.scan_code));
                options.setBeepEnabled(true);
                options.setOrientationLocked(true);
                options.setCaptureActivity(CustomScannerActivity.class);
                barLauncher.launch(options);

            }
        });


        toogle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toogle);
        toogle.syncState();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        authViewModel.getIfAdminMutableLiveData().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {

                Menu menu = navigationView.getMenu();
                if (aBoolean == true) {
                    isAdmin = true;
                    menu.findItem(R.id.action_buy_subs).setVisible(true);
                    navigationView.refreshDrawableState();

                    query_purchase();
                }

            }
        });


        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                if (R.id.action_time == item.getItemId()) {
                    Intent i = new Intent(UserMainActivity.this, UserTimeTable.class);
                    startActivity(i);
                } else if (R.id.action_admin_panel == item.getItemId()) {
                    Intent i = new Intent(UserMainActivity.this, AdminView.class);
                    startActivity(i);
                } else if (R.id.action_logout == item.getItemId()) {
                    alertDialogLConfirmation();
                } else if (R.id.action_qrCode_management == item.getItemId()) {
                    Intent i = new Intent(UserMainActivity.this, QRCodeManagement.class);
                    startActivity(i);
                } else if (R.id.action_buy_subs == item.getItemId()) {
                    Intent i = new Intent(UserMainActivity.this, Subs.class);
                    startActivity(i);
                }


                return true;
            }
        });

        authViewModel.getLoggedStatus().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean == true) {
                    startActivity(new Intent(UserMainActivity.this, MainActivity.class));
                    finish();
                }
            }
        });

        active = true;

        intentFilter = new IntentFilter();
        intentFilter.addAction("Counter");


        authViewModel.getYouCanCheckNow().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean == true) {
                    authViewModel.getDeviceId().observe(UserMainActivity.this, new Observer<String>() {
                        @Override
                        public void onChanged(String s) {
                            if (!android_id.equals(s)) {
                                authViewModel.signOut();
                            }
                        }
                    });
                }
            }
        });


    }

    //checkTheSubscribtion.

    private void writeToFile(String androidId) {
        authViewModel.writeToFile(androidId);
    }

    private void query_purchase() {
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    ExecutorService executorService = Executors.newSingleThreadExecutor();
                    executorService.execute(() -> {
                        try {

                            billingClient.queryPurchasesAsync(
                                    QueryPurchasesParams.newBuilder()
                                            .setProductType(BillingClient.ProductType.SUBS)
                                            .build(),
                                    (billingResult1, purchaseList) -> {
                                        for (Purchase purchase : purchaseList) {
                                            if (purchase != null && purchase.isAcknowledged()) {
                                                purchase.getPurchaseToken();
                                                ConnectionClass.premium = true;
                                                authViewModel.isSubscriptionAlreadyExist(purchase.getOrderId());
                                                Toast.makeText(UserMainActivity.this, getString(R.string.purchase_token) + purchase.getOrderId(), Toast.LENGTH_SHORT).show();

                                                showItem();

                                            }
                                        }
                                    }

                            );


                        } catch (Exception e) {
                            ConnectionClass.premium = false;
                            hideItem();
                        }

                        runOnUiThread(() -> {
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }


                        });
                    });
                }
            }

            @Override
            public void onBillingServiceDisconnected() {

            }
        });

    }


    // ------ BILLING CLIENT -- SUBS
    private PurchasesUpdatedListener purchasesUpdatedListener = new PurchasesUpdatedListener() {
        @Override
        public void onPurchasesUpdated(BillingResult billingResult, List<Purchase> purchases) {
            if (purchases != null) {
                for (Purchase purchase : purchases) {

                }
            }
        }
    };

    private void alertDialogLConfirmation() {

        dialog.show();

        btnDialogYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                authViewModel.signOut();
                googleSignInClient.signOut();
                dialog.dismiss();
            }
        });

        btnDialogNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        authViewModel.getValueToOpenDialog().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if(aBoolean){
                    dialog.show();
                    dialogUserOrAdmin.show();
                }
            }
        });

    }

    private void showItem() {
        itemShowed = true;
        Menu menu = navigationView.getMenu();
        menu.findItem(R.id.action_admin_panel).setVisible(true);
        menu.findItem(R.id.action_qrCode_management).setVisible(true);
    }

    private void hideItem() {
        itemShowed = false;
        Menu menu = navigationView.getMenu();
        menu.findItem(R.id.action_admin_panel).setVisible(false);
        menu.findItem(R.id.action_qrCode_management).setVisible(false);

    }

    private void openDialog(Context context) {
        DialogLanguage dialogLanguage = new DialogLanguage(context);
        dialogLanguage.show(getSupportFragmentManager(), "example dialog");
    }


    private void startTimerWithoutStartingNewService() {
        timerStarted = true;
    }


    private String getCurrentTime() {
        //"yyyy-MM-dd HH:mm:ss.SSS"
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");//dd/MM/yyyy
        Date now = new Date();
        addToEndingTime = now.getTime();
        String sf = sdf.format(now);
        return sf;
    }

    private String getCurrentTimeWithDelay(long delay) {
        //"yyyy-MM-dd HH:mm:ss.SSS"
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");//dd/MM/yyyy
        Date now = new Date();
        addToEndingTime = now.getTime() + delay;
        String sf = sdf.format(addToEndingTime);
        return sf;
    }


    public String checkMethod(long timeLong) {
        long seconds = timeLong / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;

        seconds %= 60;
        minutes %= 60;

        String formattedTime = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        return formattedTime;
    }

    public void saveIsPausedToSharedPreferences(boolean isPaused) {
        SharedPreferencesDataSource.getInstance().saveIsPausedToSharedPreferences(isPaused);
    }

    private void saveTimeModelToSharedPreferences() {
        sharedPreferencesDataSource.saveTimeModelToSharedPreferences(currentTime);
    }

    private String loadAndUpdatedTimeModelFromSharedPreferences() {
        currentTime = sharedPreferencesDataSource.loadAndUpdatedTimeModelFromSharedPreferences();
        return currentTime;
    }

    public void saveDataToSharedPreferences() {
        sharedPreferencesDataSource.saveDataToSharedPreferences(begingTime.getText().toString(), timerStarted, tmpBeginTime);
    }

    public void savePausedTimeToSharedPreferences(long pausedTime,boolean isPaused) {
        sharedPreferencesDataSource.savePausedTimeToSharedPreferences(pausedTime, isPaused);

    }

    public void uploadAndLoadPausedTimeFromSharedPreferences() {
        isPaused = sharedPreferencesDataSource.getIsTimePausedFromSharedPreferences();
    }


    public void clearDataToSharedPreferences() {
        sharedPreferencesDataSource.clearDataToSharedPreferences();
    }

    private void cleanDataForTimeModelToSharedPreferences() {
        sharedPreferencesDataSource.cleanDataForTimeModelToSharedPreferences();
    }


    public void loadDataFromSharedPreferences() {

        if (endingTime.getText().toString().equals("")) {
            beginingTime = sharedPreferencesDataSource.getTextFromSharedPreferences(begingTime.getText().toString());
        }

        timerStarted = getIsTimerStartedFromSharedPreferences();


    }

    private long getTimeOfCreationFromSharedPreferences() {
        return sharedPreferencesDataSource.getTimeOfCreationFromSharedPreferences();
    }

    private void saveIsTimeStartedToSharedPreferences(boolean isStopWatchActive) {
        sharedPreferencesDataSource.saveIsTimeStartedToSharedPreferences(isStopWatchActive);
    }


    private boolean getIsTimerStartedFromSharedPreferences() {
        return sharedPreferencesDataSource.getIsTimerStartedFromSharedPreferences();
    }


    private void saveCreationTimeToSharedPref(long time) {
        sharedPreferencesDataSource.saveCreationTimeToSharedPref(time);
    }


    public void updateData() {
        begingTime.setText(beginingTime);

        timeOfCreation = getTimeOfCreationFromSharedPreferences();
    }


    // To Foreground service-------------------------------------------------------------------------

    private void startTimer(long miliseconds) {

        ServiceHelper.isCountingTimeActive = true;
        saveCreationTimeToSharedPref(miliseconds);
        timeOfCreation = getTimeOfCreationFromSharedPreferences();
        startForegroundServiceToCountTimeWithWorkManger();
        timerStarted = true;
        Intent i = new Intent(UserMainActivity.this, ForegroundServices.class);
        startService(i);


    }


    private void startCountingTimeWithHandler(long delay) {

        handler1.removeCallbacksAndMessages(null);
        startForegroundServiceToCountTimeWithWorkManger();

        final String[] amountEntriesWithDays = {""};

        DecodeDaysAndEntries decodeDaysAndEntries = new DecodeDaysAndEntries();

        long time = new Date().getTime();

        Duration duration = Duration.ofMillis(time);
        int daysSinceUnixCurrent = (int) duration.toDays();

            authViewModel.getAmountEntriesStart(authViewModel.getUserId()).observe(UserMainActivity.this, new Observer<Map<String, Object>>() {
                @Override
                public void onChanged(Map<String, Object> stringObjectMap) {

                    if (timerStarted) {

                        amountEntriesWithDays[0] = (String) stringObjectMap.get("entriesAmount");
                        if (decodeDaysAndEntries.decodeToAmountEntries(amountEntriesWithDays[0]) < 7
                                && daysSinceUnixCurrent == decodeDaysAndEntries.decodeDays(amountEntriesWithDays[0])
                        ) {

                            switch (decodeDaysAndEntries.decodeToAmountEntries(amountEntriesWithDays[0])) {
                                case 0:
                                    Toast.makeText(UserMainActivity.this, getString(R.string.you_have_6_more_opportunities_to_save_time), Toast.LENGTH_SHORT).show();
                                    break;
                                case 1:
                                    Toast.makeText(UserMainActivity.this, getString(R.string.you_have_5_more_opportunities_to_save_time), Toast.LENGTH_SHORT).show();
                                    break;
                                case 2:
                                    Toast.makeText(UserMainActivity.this, getString(R.string.you_have_4_more_opportunities_to_save_time), Toast.LENGTH_SHORT).show();
                                    break;
                                case 3:
                                    Toast.makeText(UserMainActivity.this, getString(R.string.you_have_3_more_opportunities_to_save_time), Toast.LENGTH_SHORT).show();
                                    break;
                                case 4:
                                    Toast.makeText(UserMainActivity.this, getString(R.string.you_have_2_more_opportunities_to_save_time), Toast.LENGTH_SHORT).show();
                                    break;
                                case 5:
                                    Toast.makeText(UserMainActivity.this, getString(R.string.you_have_1_more_opportunities_to_save_time), Toast.LENGTH_SHORT).show();
                                    break;
                                case 6:
                                    Toast.makeText(UserMainActivity.this, getString(R.string.you_have_0_more_opportunities_to_save_time), Toast.LENGTH_SHORT).show();
                                    break;
                            }

                        } else if (daysSinceUnixCurrent != decodeDaysAndEntries.decodeDays(amountEntriesWithDays[0])) {
                            Toast.makeText(UserMainActivity.this, getString(R.string.you_have_6_more_opportunities_to_save_time), Toast.LENGTH_SHORT).show();

                        } else
                            Toast.makeText(UserMainActivity.this, getString(R.string.too_many_entries_contact_the_admin), Toast.LENGTH_LONG).show();

                    }

                }
            });






        handler1.post(new Runnable() {
            @Override
            public void run() {

                timeOfCreation = getTimeOfCreationFromSharedPreferences();
                long currentTimeInLong = new Date().getTime() - delay;
                long toPost = ((currentTimeInLong - timeOfCreation) / 1000);


                if (timerStarted) {

                    if (delayToAssign > 0) {
                        delayToAssign = -toPost;
                        timeDisplay.setText(getTimerText(toPost));
                        handler1.postDelayed(this, 1000);

                    } else {
                        timeDisplay.setText(getTimerText(toPost));
                        handler1.postDelayed(this, 1000);
                    }


                } else if (isPaused) {
                    if (toPost <= 8 * HOUR_IN_SECONDS) {
                        holdResumeWork.setText(getString(R.string.end_pause) + getTimerText(toPost));
                        handler1.postDelayed(this, 1000);
                    } else {
                        stopWork.setVisibility(View.INVISIBLE);
                        holdResumeWork.setVisibility(View.INVISIBLE);
                        qr.setVisibility(View.VISIBLE);
                        stopTime();
                        isPaused = false;
                        saveIsPausedToSharedPreferences(isPaused);
                        handler1.removeCallbacksAndMessages(null);
                    }


                } else if (!timerStarted && !isPaused) {

                    handler1.removeCallbacksAndMessages(null);
                }

            }
        });

    }

    public void stopTime() {
        timerStarted = false;
        Intent serviceIntent = new Intent(this, ForegroundServices.class);
        stopService(serviceIntent);
        isPaused = false;
        WorkManager.getInstance(UserMainActivity.this).cancelAllWorkByTag("cleanup");
        timeDisplay.setText("00 : 00 : 00");

    }

    public void stopTimeWithoutStoppingService() {
        isPaused = true;
        saveIsPausedToSharedPreferences(isPaused);
    }

// To Foreground service-------------------------------------------------------------------------


    ActivityResultLauncher<ScanOptions> barLauncher = registerForActivityResult(new ScanContract(), new ActivityResultCallback<ScanIntentResult>() {

        @Override
        public void onActivityResult(ScanIntentResult result) {


            if (!QRCodeList.isEmpty()) {
                for (QRModel qrModel : QRCodeList) {
                    if (result.getContents() != null && result.getContents().toString().equals(qrModel.getQRCode())) {

                        if (timerStarted == false) {
                            timerOverall.setText("");
                            timerStarted = true;
                            isPaused = false;
                            saveIsTimeStartedToSharedPreferences(timerStarted);

                            timeDisplay.setText("");
                            textMain.setText(getString(R.string.stop_work));
                            cleanDataForTimeModelToSharedPreferences();
                            currentTime = getCurrentTime();

                            saveTimeModelToSharedPreferences();
                            delayToAssign = qrModel.getDelay() * MINUTE_IN_SECONDS;
                            tmpBeginTime = getCurrentTimeInSimpleFormat();
                            startTimer((new Date().getTime() + (delayToAssign * 1000)));
                            begingTime.setText(getString(R.string.begin_work_at) + getCurrentTimeWithDelay(delayToAssign * 1000));
                            holdResumeWork.setText(getString(R.string.hold_work));
                            startCountingTimeWithHandler(delayToAssign);

                            endingTime.setText("");

                            stopWork.setVisibility(View.VISIBLE);
                            holdResumeWork.setVisibility(View.VISIBLE);
                            holdResumeWork.setBackgroundColor(Color.parseColor("#A214D5"));
                        } else {
                            stopCountingTime();
                            stopTime();
                            delayToAssign = 0;
                            qr.setVisibility(View.VISIBLE);
                            stopWork.setVisibility(View.INVISIBLE);
                            holdResumeWork.setVisibility(View.INVISIBLE);
                        }

                    }
                }
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
        if (!isMyServiceRunning(ForegroundServices.class) && (isPaused)) {
            startWorker();

        }
    }

    private void startWorker() {
        Constraints constraints = new Constraints.Builder().setRequiresBatteryNotLow(true).build();

        PeriodicWorkRequest saveRequest = new PeriodicWorkRequest.Builder(StopWatchWorker.class, 16, TimeUnit.MINUTES).setBackoffCriteria(BackoffPolicy.LINEAR, OneTimeWorkRequest.MIN_BACKOFF_MILLIS, TimeUnit.MILLISECONDS).setConstraints(constraints).addTag("cleanup")
                // Constraints
                .build();

        WorkManager.getInstance(UserMainActivity.this).enqueue(saveRequest);
    }


    private long getCurrentTimeInSimpleFormat() {
        return new Date().getTime();
    }

    private String getTimerText(long totalSecs) {

        long hours = totalSecs / 3600;
        long minutes = (totalSecs % 3600) / 60;
        long seconds = totalSecs % 60;

        return formatTime(seconds, minutes, hours);

    }


    private String formatTime(long seconds, long minutes, long hours) {
        return String.format("%02d", hours) + " : " + String.format("%02d", minutes) + " : " + String.format("%02d", seconds);
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (toogle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void startCountingTime() {

        textMain.setText(getString(R.string.stop_work));
        cleanDataForTimeModelToSharedPreferences();
        currentTime = getCurrentTime();

        saveTimeModelToSharedPreferences();
        begingTime.setText(getString(R.string.begin_work_at) + getCurrentTime());
        tmpBeginTime = getCurrentTimeInSimpleFormat();

        isPaused = false;
        saveIsPausedToSharedPreferences(isPaused);
        timerStarted = true;
        endingTime.setText("");

        stopWork.setVisibility(View.VISIBLE);
        holdResumeWork.setVisibility(View.VISIBLE);
        holdResumeWork.setBackgroundColor(Color.parseColor("#A214D5"));

    }


    public void stopCountingTime() {

        timeModel = new TimeModel();
        timerStarted = false;
        textMain.setText(getString(R.string.begin_work));

        timeModel.setTimeEnd(getCurrentTime());


        endingTime.setText(getString(R.string.end_work_at) + getCurrentTime());
        timeModel.setTimeBegin(loadAndUpdatedTimeModelFromSharedPreferences());

        timerOverall.setText(getString(R.string.overworked) + timeDisplay.getText().toString());

        endingTime.setText(getString(R.string.end_work_at) + getCurrentTime());

        timeModel.setTimeOverall(timeDisplay.getText().toString().contains("-") ? "00:00:00" : timeDisplay.getText().toString().replaceAll(" ", ""));
        timeModel.setTimeOverallInLong((new Date().getTime() - timeOfCreation) > 0 ? new Date().getTime() - timeOfCreation : 0);
        timeModel.setId(authViewModel.getUserId());


        if (userName.getText().toString().equals(getString(R.string.user_number_1))) {
            timeModel.setUserName(getString(R.string.user_number_1));
        } else {
            timeModel.setUserName(userName.getText().toString());
        }


        timeModel.setTimeAdded(new Timestamp(new Date()));
        timeModel.setTimestamp(new Timestamp(new Date()));


        if (timeModel.getTimeOverallInLong() > 0) {

            //Aktualny dzień
            long time = new Date().getTime();

            Duration duration = Duration.ofMillis(time);
            int daysSinceUnixCurrent = (int) duration.toDays();

            final String[] amountEntriesWithDays = {""};

            DecodeDaysAndEntries decodeDaysAndEntries = new DecodeDaysAndEntries();


            authViewModel.getAmountEntries(timeModel.getId()).observe(this, new Observer<Map<String, Object>>() {
                @Override
                public void onChanged(Map<String, Object> s) {
                    amountEntriesWithDays[0] = (String) s.get("entriesAmount");


                    if (decodeDaysAndEntries.decodeToAmountEntries(amountEntriesWithDays[0]) < 7
                            && daysSinceUnixCurrent == decodeDaysAndEntries.decodeDays(amountEntriesWithDays[0])
                    ) {
                        authViewModel.saveTimeModelToFirebase(timeModel);
                        authViewModel.updatedDataHoursToFirebaseUser(timeModel);
                        authViewModel.updateAmountEntries(toUpdateAmountEntries(daysSinceUnixCurrent, amountEntriesWithDays[0], decodeDaysAndEntries));

                        switch (decodeDaysAndEntries.decodeToAmountEntries(amountEntriesWithDays[0])) {
                            case 0:
                                Toast.makeText(UserMainActivity.this, getString(R.string.you_have_6_more_opportunities_to_save_time), Toast.LENGTH_SHORT).show();
                                break;
                            case 1:
                                Toast.makeText(UserMainActivity.this, getString(R.string.you_have_5_more_opportunities_to_save_time), Toast.LENGTH_SHORT).show();
                                break;
                            case 2:
                                Toast.makeText(UserMainActivity.this, getString(R.string.you_have_4_more_opportunities_to_save_time), Toast.LENGTH_SHORT).show();
                                break;
                            case 3:
                                Toast.makeText(UserMainActivity.this, getString(R.string.you_have_3_more_opportunities_to_save_time), Toast.LENGTH_SHORT).show();
                                break;
                            case 4:
                                Toast.makeText(UserMainActivity.this, getString(R.string.you_have_2_more_opportunities_to_save_time), Toast.LENGTH_SHORT).show();
                                break;
                            case 5:
                                Toast.makeText(UserMainActivity.this, getString(R.string.you_have_1_more_opportunities_to_save_time), Toast.LENGTH_SHORT).show();
                                break;
                            case 6:
                                Toast.makeText(UserMainActivity.this, getString(R.string.you_have_0_more_opportunities_to_save_time), Toast.LENGTH_SHORT).show();
                                break;
                        }

                    } else if (daysSinceUnixCurrent != decodeDaysAndEntries.decodeDays(amountEntriesWithDays[0])) {
                        authViewModel.saveTimeModelToFirebase(timeModel);
                        authViewModel.updatedDataHoursToFirebaseUser(timeModel);
                        authViewModel.updateAmountEntries(toUpdateAmountEntries(daysSinceUnixCurrent, amountEntriesWithDays[0], decodeDaysAndEntries));
                        Toast.makeText(UserMainActivity.this, getString(R.string.you_have_6_more_opportunities_to_save_time), Toast.LENGTH_SHORT).show();

                    } else
                        Toast.makeText(UserMainActivity.this, getString(R.string.too_many_entries_contact_the_admin), Toast.LENGTH_LONG).show();



                    }

            });

        }

    }


    @Override
    protected void onResume() {
        super.onResume();

        startCountingTimeWithHandler(delayToAssign);
        hideItem();
        if (isAdmin) {
            query_purchase();
        }




    }

    private String toUpdateAmountEntries(int daysSinceUnixCurrent, String amountEntriesWithDays, DecodeDaysAndEntries decodeDaysAndEntries) {

        int daysFromDB = decodeDaysAndEntries.decodeDays(amountEntriesWithDays);
        int amountEntries = decodeDaysAndEntries.decodeToAmountEntries(amountEntriesWithDays);
        int daysCurrent = daysSinceUnixCurrent;
        if (daysFromDB == daysCurrent) {
            amountEntries++;
            return daysCurrent + "_" + amountEntries;
        } else return daysSinceUnixCurrent + "_" + "1";

    }

    @Override
    protected void onStart() {
        loadDataFromSharedPreferences();
        updateData();
        startCountingTimeWithHandler(delayToAssign);
        startForegroundServiceToCountTimeWithWorkManger();
        authViewModel.getDataQRCode(foreginKey).observe(this, new Observer<List<QRModel>>() {
            @Override
            public void onChanged(List<QRModel> qrModels) {
                QRCodeList.clear();
                QRCodeList.addAll(qrModels);
            }
        });
        super.onStart();
        active = true;
    }


    @Override
    protected void onStop() {
        active = false;
        savePausedTimeToSharedPreferences(System.currentTimeMillis(), isPaused);
        saveIsPausedToSharedPreferences(isPaused);

        // saving data when only started time not ending time
        if (endingTime.getText().toString().equals("")) {
            saveDataToSharedPreferences();
        }
        // if end time was set, clearing data.
        else if (!endingTime.getText().toString().equals("")) {
            clearDataToSharedPreferences();
        }
        super.onStop();
    }


}