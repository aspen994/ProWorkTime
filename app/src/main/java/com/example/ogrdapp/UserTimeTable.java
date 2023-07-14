package com.example.ogrdapp;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ogrdapp.model.TimeModel;
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
import java.util.Locale;

public class UserTimeTable extends AppCompatActivity {


    private FirebaseAuth firebaseAuth;

    private FirebaseUser user;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference = db.collection("Time");
    private Spinner spinnerMonth, spinnerYear;
    private RecyclerView recyclerView;
    private ArrayList<TimeModel> timeModelArrayList = new ArrayList<>();
    // commented  16:24 12.07.2023
    boolean flag = true;

    private TimeOverallAdapter timeOverallAdapter = new TimeOverallAdapter(this, timeModelArrayList);
    //private TimeOverallAdapter timeOverallAdapter;

    String selectedSpinnerOnYear="";
    private String selectedSpinnerOnMonth="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_time_table);
        spinnerMonth = findViewById(R.id.spinner_month);
        spinnerYear = findViewById(R.id.spinner_year);
        recyclerView = findViewById(R.id.recyclerView);


        //05.07.23 Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        String userId = user.getUid();
        String email = user.getEmail();

        // Zakomentowałem to 06.07.2023 - For now app working without intent, don't delete it maybe be usefull in next stage of app
        /*Intent i = getIntent();
        timeModelArrayList.addAll((ArrayList<TimeModel>) i.getSerializableExtra("timeModel"));*/

        collectionReference.whereEqualTo("id", userId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if(!queryDocumentSnapshots.isEmpty())
                        {
                            for(QueryDocumentSnapshot timeModels: queryDocumentSnapshots)
                            {
                                /*Journal journal = journals.toObject(Journal.class);
                                journalList.add(journal);
                                */
                                TimeModel timeModel = timeModels.toObject(TimeModel.class);
                                //TODO 06.07.2023 - Sorting the ArrayList to show time in proper order
                                timeModelArrayList.add(timeModel);
                                Collections.sort(timeModelArrayList, new Comparator<TimeModel>() {
                                    @Override
                                    public int compare(TimeModel o1, TimeModel o2) {
                                        return o1.getTimeAdded().compareTo(o2.getTimeAdded());
                                    }
                                });

                                activateSpinner();
                                activateSpinnerYear();
                            }

                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Any Failuer
                        Toast.makeText(UserTimeTable.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

        // It was used before adding the db
  /*      recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(timeOverallAdapter);
        timeOverallAdapter.notifyDataSetChanged();*/

        // getting current year
        int year = LocalDate.now().getYear();

        /*ArrayAdapter<CharSequence> adapterYear = ArrayAdapter.createFromResource(this,R.array.year, android.R.layout.simple_spinner_item);
        adapterYear.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerYear.setAdapter(adapterYear);
        // Setting the current year
        spinnerYear.setSelection(adapterYear.getPosition(String.valueOf(year)));*/
        spinnerYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //Toast.makeText(UserTimeTable.this, "Wybrany rok : " + text, Toast.LENGTH_SHORT).show();
                selectedSpinnerOnYear = parent.getItemAtPosition(position).toString();

                String year = parent.getItemAtPosition(position).toString();

                if(!timeModelArrayList.isEmpty()) {
                    ArrayList<TimeModel> arrayListTmp = new ArrayList<>();

                    for (TimeModel model : timeModelArrayList) {
                        String s = formatDateWithMonthAndYear(model.getTimeAdded().toDate());
                        //String s1 = text.toLowerCase();
                        String s1 = selectedSpinnerOnMonth.toLowerCase()+year.toLowerCase();

                        Log.i("S1 year: ",s1);
                        Log.i("s year", s);
                        if (s1.equals(s)) {
                            arrayListTmp.add(model);
                        }

                    }

                    Log.i("Size of time Model Array List", timeModelArrayList.size() + "");

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




        spinnerMonth.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                String month = parent.getItemAtPosition(position).toString();
                selectedSpinnerOnMonth = parent.getItemAtPosition(position).toString();

                if(!timeModelArrayList.isEmpty()) {
                    ArrayList<TimeModel> arrayListTmp = new ArrayList<>();

                    for (TimeModel model : timeModelArrayList) {
                        String s = formatDateWithMonthAndYear(model.getTimeAdded().toDate());
                        //String s1 = text.toLowerCase();
                        String s1 = month.toLowerCase() + selectedSpinnerOnYear;

                        Log.i("S1: ",s1);
                        Log.i("s", s);
                        if (s1.equals(s)) {
                            arrayListTmp.add(model);
                        }

                    }

                    Log.i("Size of time Model Array List", timeModelArrayList.size() + "");

                    recyclerView.setHasFixedSize(true);
                    recyclerView.setLayoutManager(new LinearLayoutManager(UserTimeTable.this));
                    timeOverallAdapter = new TimeOverallAdapter(UserTimeTable.this, arrayListTmp);
                    recyclerView.setAdapter(timeOverallAdapter);
                    timeOverallAdapter.notifyDataSetChanged();
                    Log.i("size arrayListTmp", arrayListTmp.size() + "");

                }

            }
            int size = 0;
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Toast.makeText(UserTimeTable.this, "Nothing selected", Toast.LENGTH_SHORT).show();
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
        SimpleDateFormat dateFormat = new SimpleDateFormat("LLLLyyyy", Locale.getDefault());
        return dateFormat.format(date);
    }

}