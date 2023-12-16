package com.example.ogrdapp.view;


import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Pair;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ogrdapp.R;
import com.example.ogrdapp.model.TimeModel;
import com.example.ogrdapp.utility.DaysOutOfValidate;
import com.example.ogrdapp.utility.FormattedTime;
import com.example.ogrdapp.viewmodel.AuthViewModel;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public class UserOverall extends AppCompatActivity {

    TextView username,dateDisplay,toPay,workedTime;
    EditText bidEnter;
    Button confirmBidButton,confirmPaymentButton,cancelPaymentButton,selectedDateButton;

    List<TimeModel> listOfAllRecordsForUser;
    List<TimeModel> selectedTimeModelList = new ArrayList<>();

    String userName;
    String id;

    private long timeToSettlement;
    private int bidEnterValue;

    private AuthViewModel authViewModel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_overall);

        // 1-set widgets
        username = findViewById(R.id.username);
        dateDisplay = findViewById(R.id.date_display);
        toPay = findViewById(R.id.to_pay);
        bidEnter = findViewById(R.id.bid_enter);
        confirmBidButton = findViewById(R.id.confirm_bid_button);
        confirmPaymentButton = findViewById(R.id.confirm_payment_button);
        cancelPaymentButton = findViewById(R.id.cancel_payment_button);
        workedTime = findViewById(R.id.worked_time);
        selectedDateButton = findViewById(R.id.date_selected_button);

        //2- get Data from Intent
        Intent intent = getIntent();
        listOfAllRecordsForUser = (List<TimeModel>) intent.getSerializableExtra("List");
        this.userName = intent.getStringExtra("UserName");
        this.id = intent.getStringExtra("Id");

        username.setText(userName);

        //4- Getting AuthViewModel
        authViewModel= new ViewModelProvider(this).get(AuthViewModel.class);

        //3- onClick listeners for widgets
        selectedDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                invokedCalendar();
            }
        });

        confirmBidButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                bidEnterValue = Integer.parseInt(bidEnter.getText().toString());
                toPay.setText(bidEnterValue * FormattedTime.formattedTimeInInt(timeToSettlement)+"zł");
            }
        });

        confirmPaymentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                withdrawnOperation();
            }
        });

        cancelPaymentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
               /* Intent intent =  new Intent(UserOverall.this, AdminView.class);
                startActivity(intent);*/
            }
        });


    }

    private void withdrawnOperation() {


        String s = toPay.getText().toString();

        s = s.replace("zł","");

        int i = 0;

        if(!s.equals(""))
        {
            i = Integer.parseInt(s);
        }


        // To dla pętli działa
        for(TimeModel timeModel: selectedTimeModelList)
        {
            int i1 = bidEnterValue * FormattedTime.formattedTimeInInt(timeModel.getTimeOverallInLong());
            authViewModel.updateStatusOfSettled(timeModel.getDocumentId(),true,i1);
            //tu tutaj
            // TODO TUTAJ WYŁĄCZAM
            //timeModel.setMoneyOverall(true);
            // Zaktualizuj dla listy
        }

      /*  for (int j = 0; j <selectedTimeModelList.size() ; j++) {
            for (int k = 0; k < listOfAllRecordsForUser.size(); k++) {
             if(selectedTimeModelList.get(j).getDocumentId().equals(listOfAllRecordsForUser.get(k).getDocumentId()))
             {
                 // TODO TUTAJ WYŁĄCZAM
              //listOfAllRecordsForUser.get(k).setMoneyOverall(true);
             }
            }
        }
*/

        if(i!=0) {
            authViewModel.getDataToUpdatePayCheck(selectedTimeModelList.get(0).getId());
        }

        int finalIrrational = i;
        authViewModel.getPaycheckHoursToSettleMutableLiveData().observe(this,new Observer<Map<String, Object>>() {
            @Override
            public void onChanged(Map<String, Object> stringObjectMap) {
                if(!stringObjectMap.isEmpty())
                {
                    long paycheck1 = (long)stringObjectMap.get("paycheck");
                    long hoursToSettle1 = (long)stringObjectMap.get("hoursToSettle");
                    String email1 = (String) stringObjectMap.get("email");

                    paycheck1 += finalIrrational;
                    hoursToSettle1-=timeToSettlement;

                    Log.i("Update User","Update User");

                    // TODO tutaj updateuje się dane dla Usera.
                    authViewModel.updateStatusOfTimeForUser(email1, hoursToSettle1, paycheck1);

                }
            }
        });
        finish();
        Intent intent =  new Intent(UserOverall.this, AdminView.class);
        startActivity(intent);

    }

    private void updateListOfAllRecord(List<TimeModel> selectedTimeModelList, List<TimeModel> listOfAllRecordsForUser) {
        for (int i = 0;i< selectedTimeModelList.size();i++) {
            for (int j = 0; j <listOfAllRecordsForUser.size(); j++) {
                if(selectedTimeModelList.get(i).getDocumentId().equals(listOfAllRecordsForUser.get(j).getDocumentId()))
                {
                    //listOfAllRecordsForUser.get(j).setMoneyOverall(true);
                }
            }
        }
    }



    private void invokedCalendar() {
        MaterialDatePicker materialDatePicker = MaterialDatePicker.Builder.dateRangePicker()
                .setCalendarConstraints(DaysOutOfValidate().build())
                .setSelection(Pair.create(MaterialDatePicker.thisMonthInUtcMilliseconds(),MaterialDatePicker.todayInUtcMilliseconds()))
                //.setTheme(R.style.ThemeOverlay_App_MaterialCalendar)
                .build();
        materialDatePicker.show(getSupportFragmentManager(), "S");

        materialDatePicker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener<Pair<Long, Long>>() {
            @Override
            public void onPositiveButtonClick(Pair<Long,Long> selection) {

                StringBuilder stringBuilder = new StringBuilder();

                Calendar calendar1 = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                calendar1.setTimeInMillis(selection.first);

                SimpleDateFormat format = new SimpleDateFormat("ddMMyyyy");
                String formattedDate = format.format(calendar1.getTime());
                stringBuilder.append(formattedDate+"-");

                Calendar calendar2 = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                calendar2.setTimeInMillis(selection.second);

                formattedDate = format.format(calendar1.getTime());
                stringBuilder.append(formattedDate);


                workedTime.setText(FormattedTime.formattedTime(getTimeFromRangeDate(calendar1,calendar2)));

                dateDisplay.setText(materialDatePicker.getHeaderText());

            }
        });
    }


    // TUTAJ POBIERA SOBIE Z LISTY DANE ŻEBY OBLICZYĆ CZAS ETAP 1
    private long getTimeFromRangeDate(Calendar calendar1, Calendar calendar2) {

        long time = 0;


      /*  for (int i = 0; i < listOfAllRecordsForUser.size(); i++) {
            if(listOfAllRecordsForUser.get(i).getId().equals(id) && !listOfAllRecordsForUser.get(i).getMoneyOverall()) {


                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(listOfAllRecordsForUser.get(i).getTimeAdded().toDate().toInstant().toEpochMilli());

                if (calendar1.get(Calendar.YEAR) == calendar.get(Calendar.YEAR) &&
                        calendar1.get(Calendar.DAY_OF_YEAR) <= calendar.get(Calendar.DAY_OF_YEAR) &&
                        calendar2.get(Calendar.DAY_OF_YEAR) >= calendar.get(Calendar.DAY_OF_YEAR))
                {

                    time += listOfAllRecordsForUser.get(i).getTimeOverallInLong();
                    selectedTimeModelList.add(listOfAllRecordsForUser.get(i));
                    Log.i("DOCUMENT ID 1",listOfAllRecordsForUser.get(i).getDocumentId());
                    Log.i("DOCUMENT ID 2",listOfAllRecordsForUser.get(i).getMoneyOverall()+"");
                }
            }
        }*/

        for(TimeModel x: listOfAllRecordsForUser)
        {
            if(x.getId().equals(id) && x.getMoneyOverall()==false) {


                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(x.getTimeAdded().toDate().toInstant().toEpochMilli());

                if (calendar1.get(Calendar.YEAR) == calendar.get(Calendar.YEAR) &&
                        calendar1.get(Calendar.DAY_OF_YEAR) <= calendar.get(Calendar.DAY_OF_YEAR) &&
                        calendar2.get(Calendar.DAY_OF_YEAR) >= calendar.get(Calendar.DAY_OF_YEAR))
                {

                    time += x.getTimeOverallInLong();
                    selectedTimeModelList.add(x);
                    Log.i("DOCUMENT ID",x.getDocumentId());
                }
            }
            // Daty z listy
        }

        timeToSettlement=time;

        Log.i("TIME",time+"");

        return time;
    }



    private List<TimeModel> selectDataForGivenUser(String id, List<TimeModel> timeModelsList) {
        List<TimeModel> listToReturn = new ArrayList<>();
        for(TimeModel x: timeModelsList)
        {
            if(id.equals(x.getId()))
            {
                listToReturn.add(x);
            }
        }

        return listToReturn;
    }
    private CalendarConstraints.Builder DaysOutOfValidate() {

        CalendarConstraints.Builder constraintsBuilderRange = new CalendarConstraints.Builder();
        constraintsBuilderRange.setValidator(new DaysOutOfValidate(getValidDateToSettle()));
        return constraintsBuilderRange;
    }
    private List<Calendar> getValidDateToSettle() {


        List<Calendar> blockedDates = new ArrayList<>();

        for (int i = 0; i < listOfAllRecordsForUser.size(); i++)
        {
            if (listOfAllRecordsForUser.get(i).getId().equals(id)&& listOfAllRecordsForUser.get(i).getMoneyOverall()) {

                long time = listOfAllRecordsForUser.get(i).getTimeAdded().toDate().getTime();
                Calendar calendar1 = Calendar.getInstance();
                calendar1.setTimeInMillis(time);
                blockedDates.add(calendar1);
                Log.i("VALIDATE",listOfAllRecordsForUser.get(i).getMoneyOverall()+"");
                Log.i("VALIDATE_2",listOfAllRecordsForUser.get(i).getDocumentId());
            }
        }

        return blockedDates;
    }

}