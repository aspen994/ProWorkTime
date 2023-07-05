package com.example.ogrdapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.ogrdapp.model.TimeModel;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.sql.Time;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;

public class UserTimeTable extends AppCompatActivity {

    private Spinner spinnerMonth, spinnerYear;
    private RecyclerView recyclerView;
    private ArrayList<TimeModel> timeModelArrayList = new ArrayList<>();

    private Button btn_sum;
    TimeOverallAdapter timeOverallAdapter = new TimeOverallAdapter(this, timeModelArrayList);
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private CollectionReference collectionReference = db.collection("Journal");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_time_table);
        spinnerMonth = findViewById(R.id.spinner_month);
        spinnerYear = findViewById(R.id.spinner_year);
        recyclerView = findViewById(R.id.recyclerView);

        btn_sum= findViewById(R.id.sum_btn);

        Intent i = getIntent();
        timeModelArrayList.addAll((ArrayList<TimeModel>) i.getSerializableExtra("timeModel"));

        btn_sum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sum();
            }
        });


        //timeModelArrayList.add((TimeModel) i.getSerializableExtra("timeModel"));
        Log.i("SIZE ARRAYLIST",timeModelArrayList.size()+"");


        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(timeOverallAdapter);
        timeOverallAdapter.notifyDataSetChanged();

        // getting current year
        int year = LocalDate.now().getYear();

        ArrayAdapter<CharSequence> adapterYear = ArrayAdapter.createFromResource(this,R.array.year, android.R.layout.simple_spinner_item);
        adapterYear.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerYear.setAdapter(adapterYear);
        // Setting the current year
        spinnerYear.setSelection(adapterYear.getPosition(String.valueOf(year)));
        spinnerYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String text = parent.getItemAtPosition(position).toString();
                Toast.makeText(UserTimeTable.this, "Wybrany rok : " + text, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //Geting the current month
        Month month = LocalDate.now().getMonth();
        int monthToSend =month.getValue()-1;


        ArrayAdapter<CharSequence> adapterMonth = ArrayAdapter.createFromResource(this,R.array.months, android.R.layout.simple_spinner_item);
        adapterMonth.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMonth.setAdapter(adapterMonth);
        //Setting current month to spinner
        spinnerMonth.setSelection(monthToSend);
        spinnerMonth.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String text = parent.getItemAtPosition(position).toString();
                Toast.makeText(UserTimeTable.this, "Wybrany miesiąc : " + text, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void sum() {

        long sum = 0;

        for (TimeModel timeModel:timeModelArrayList) {
            sum += timeModel.getTimeOverallInLong();
        }

        long seconds = sum / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;

        seconds %= 60;
        minutes %= 60;

        String formattedTime = String.format("%02d:%02d:%02d", hours, minutes, seconds);

        Log.i("TIME OVERALL", formattedTime);
    }


}