package com.example.ogrdapp.view;

import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.os.ConfigurationCompat;
import androidx.core.os.LocaleListCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ogrdapp.R;
import com.example.ogrdapp.TimeOverallAdapter;
import com.example.ogrdapp.model.TimeModel;
import com.example.ogrdapp.viewmodel.AuthViewModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class UserTimeTable extends AppCompatActivity {


    private FirebaseAuth firebaseAuth;

    private FirebaseUser user;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference = db.collection("Time");
    private Spinner spinnerMonth, spinnerYear;
    private RecyclerView recyclerView;
    private TextView sumTextView;
    private ArrayList<TimeModel> timeModelArrayList = new ArrayList<>();

    boolean flag = true;

    private TimeOverallAdapter timeOverallAdapter = new TimeOverallAdapter(this, timeModelArrayList);
    //private TimeOverallAdapter timeOverallAdapter;

    String selectedSpinnerOnYear="";
    private String selectedSpinnerOnMonth="";
    private int moneyMultiplier = 16;
    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_time_table);
        spinnerMonth = findViewById(R.id.spinner_month);
        spinnerYear = findViewById(R.id.spinner_year);
        recyclerView = findViewById(R.id.recyclerView);
        sumTextView = findViewById(R.id.sum);

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        String id = getIntent().getStringExtra("Id");
        if(id!=null) {
            authViewModel.getTimeForUser(id);

            authViewModel.getTimeForUserListMutableLiveData().observe(this, new Observer<List<TimeModel>>() {
                @Override
                public void onChanged(List<TimeModel> timeModels) {
                    timeModelArrayList.clear();
                    timeModelArrayList.addAll(timeModels);
                    activateSpinner();
                    activateSpinnerYear();
                }
            });
        }
        else{
            authViewModel.getData();

            authViewModel.getTimeModelListMutableLiveData().observe(this, new Observer<List<TimeModel>>() {
                @Override
                public void onChanged(List<TimeModel> timeModels) {
                    timeModelArrayList.clear();
                    timeModelArrayList.addAll(timeModels);
                    activateSpinner();
                    activateSpinnerYear();
                }
            });
        }

        // Zamiast authViewModel.getData();
        // Daj   authViewModel.getTimeForUser(userId);


        //05.07.23 Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        String userId = user.getUid();

        // Zakomentowałem to 06.07.2023 - For now app working without intent, don't delete it maybe be usefull in next stage of app
        /*Intent i = getIntent();
        timeModelArrayList.addAll((ArrayList<TimeModel>) i.getSerializableExtra("timeModel"));*/

        spinnerYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //Toast.makeText(UserTimeTable.this, "Wybrany rok : " + text, Toast.LENGTH_SHORT).show();
                selectedSpinnerOnYear = parent.getItemAtPosition(position).toString();

                String year = parent.getItemAtPosition(position).toString();

                if(!timeModelArrayList.isEmpty()) {
                    ArrayList<TimeModel> arrayListTmp = new ArrayList<>();

                    for (TimeModel model : timeModelArrayList) {
                        String s = formatDateWithMonthAndYear(model.getTimeAdded().toDate()).toLowerCase();
                        String s1 = selectedSpinnerOnMonth.toLowerCase()+year.toLowerCase();
                        Log.i("S_YEAR FROM DATABASE",s);
                        Log.i("S_YEAR FROM APP",s1);

                        if (s1.equals(s)) {
                            arrayListTmp.add(model);
                        }

                    }

                    recyclerView.setHasFixedSize(true);
                    recyclerView.setLayoutManager(new LinearLayoutManager(UserTimeTable.this));
                    timeOverallAdapter = new TimeOverallAdapter(UserTimeTable.this, arrayListTmp);
                    recyclerView.setAdapter(timeOverallAdapter);
                    timeOverallAdapter.notifyDataSetChanged();

                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });




        spinnerMonth.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                String month = parent.getItemAtPosition(position).toString();
                selectedSpinnerOnMonth = parent.getItemAtPosition(position).toString();

                if(!timeModelArrayList.isEmpty()) {
                    ArrayList<TimeModel> arrayListTmp = new ArrayList<>();

                    for (TimeModel model : timeModelArrayList) {
                        String s = formatDateWithMonthAndYear(model.getTimeAdded().toDate()).toLowerCase();
                        String s1 = month.toLowerCase() + selectedSpinnerOnYear;

                        Log.i("S_Month FROM DATABASE",s);
                        Log.i("S_Month FROM APP",s1);

                        if (s1.equals(s)) {
                            arrayListTmp.add(model);
                        }

                    }

                    countingMoneyMethod(arrayListTmp);
                    recyclerView.setHasFixedSize(true);
                    recyclerView.setLayoutManager(new LinearLayoutManager(UserTimeTable.this));
                    timeOverallAdapter = new TimeOverallAdapter(UserTimeTable.this, arrayListTmp);
                    recyclerView.setAdapter(timeOverallAdapter);
                    timeOverallAdapter.notifyDataSetChanged();
                    Log.i("size arrayListTmp", arrayListTmp.size() + "");
                }

            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });



    }

    public void activateSpinner()
    {
        //Geting the current month
        Month month = LocalDate.now().getMonth();
        int monthToSend =month.getValue()-1;
        ArrayAdapter<CharSequence> adapterMonth = ArrayAdapter.createFromResource(this, R.array.months, android.R.layout.simple_spinner_item);
        adapterMonth.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMonth.setAdapter(adapterMonth);
        //Setting current month to spinner
        spinnerMonth.setSelection(monthToSend);
    }
    public void activateSpinnerYear()
    {
        // getting current year
        int year = LocalDate.now().getYear();
        ArrayAdapter<CharSequence> adapterYear = ArrayAdapter.createFromResource(this,R.array.year, android.R.layout.simple_spinner_item);
        adapterYear.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerYear.setAdapter(adapterYear);
        // Setting the current year
        spinnerYear.setSelection(adapterYear.getPosition(String.valueOf(year)));
    }

    private String formatDateWithMonthAndYear(Date date)
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat("LLLLyyyy", getResources().getConfiguration().locale);
        return dateFormat.format(date);
    }

    private void countingMoneyMethod(ArrayList<TimeModel> arrayList)
    {
        long sum = 0;
        for (int i = 0; i < arrayList.size(); i++) {
            sum += arrayList.get(i).getTimeOverallInLong();
        }
        sumTextView.setText((sum/3600000)*moneyMultiplier+" zł");
    }

}