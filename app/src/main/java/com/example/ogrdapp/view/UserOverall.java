package com.example.ogrdapp.view;


import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Pair;
import androidx.fragment.app.FragmentActivity;
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
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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

    public static boolean isWithdrawn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_overall);

        isWithdrawn=false;

        getSupportActionBar().hide();
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
                toPay.setText(round((bidEnterValue * FormattedTime.formattedTimeInDoubleToSave(timeToSettlement)),2)+"zł");
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
            }
        });


    }
    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }
    private void withdrawnOperation() {


        String s = toPay.getText().toString();

        s = s.replace("zł","");

        double i = 0;

        if(!s.equals(""))
        {
            i = Double.parseDouble(s);

        }

        if(i!=0.0) {
        // To dla pętli działa
        for(TimeModel timeModel: selectedTimeModelList)
        {
            double i1 = bidEnterValue * FormattedTime.formattedTimeInDoubleToSave(timeModel.getTimeOverallInLong());
            double round = round(i1, 2);

            authViewModel.updateStatusOfSettled(timeModel.getDocumentId(),true,round,new Timestamp(new Date()),timeModel);
            isWithdrawn= true;

        }
            authViewModel.getDataToUpdatePayCheck(selectedTimeModelList.get(0).getId());

            double finalIrrational = i;
            authViewModel.getPaycheckHoursToSettleMutableLiveData().observe(this,new Observer<Map<String, Object>>() {
                @Override
                public void onChanged(Map<String, Object> stringObjectMap) {
                    if(!stringObjectMap.isEmpty())
                    {
                        double paycheck1 = (double) stringObjectMap.get("paycheck");
                        long hoursToSettle1 = (long)stringObjectMap.get("hoursToSettle");
                        String email1 = (String) stringObjectMap.get("email");

                        paycheck1 += finalIrrational;
                        paycheck1=round(paycheck1,2);
                        hoursToSettle1-=timeToSettlement;

                        // tutaj updateuje się dane dla Usera.
                        authViewModel.updateStatusOfTimeForUser(email1, hoursToSettle1, paycheck1);

                        finish();
                        Intent intent =  new Intent(UserOverall.this, AdminView.class);
                        startActivity(intent);
                    }
                }
            });

        }
        else{
            Toast.makeText(this, "Nie zatwierdziłeś stawki albo nie wybrałeś godzin", Toast.LENGTH_SHORT).show();
        }


    }

    private void invokedCalendar() {
        MaterialDatePicker materialDatePicker = MaterialDatePicker.Builder.dateRangePicker()
                .setCalendarConstraints(DaysOutOfValidate().build())
                .setSelection(Pair.create(MaterialDatePicker.thisMonthInUtcMilliseconds(),MaterialDatePicker.todayInUtcMilliseconds()))
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
                }
            }
        }

        timeToSettlement=time;

        return time;
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
            if (listOfAllRecordsForUser.get(i).getId().equals(id) && listOfAllRecordsForUser.get(i).getMoneyOverall()) {
                long time = listOfAllRecordsForUser.get(i).getTimeAdded().toDate().getTime();
                Calendar calendar1 = Calendar.getInstance();
                calendar1.setTimeInMillis(time);
                calendar1.set(Calendar.HOUR_OF_DAY, 0);
                calendar1.set(Calendar.MINUTE, 0);
                calendar1.set(Calendar.SECOND, 0);
                calendar1.set(Calendar.MILLISECOND, 0);
                if(i>0) {
                    long time1 = listOfAllRecordsForUser.get(i - 1).getTimeAdded().toDate().getTime();
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(time1);
                    calendar.set(Calendar.HOUR_OF_DAY, 0);
                    calendar.set(Calendar.MINUTE, 0);
                    calendar.set(Calendar.SECOND, 0);
                    calendar.set(Calendar.MILLISECOND, 0);
                    if (calendar1.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)
                            && calendar1.get(Calendar.DAY_OF_YEAR) != calendar.get(Calendar.DAY_OF_YEAR))
                    {
                        blockedDates.add(calendar1);
                    }
                }
            }
            else if (listOfAllRecordsForUser.get(i).getId().equals(id) && !listOfAllRecordsForUser.get(i).getMoneyOverall()){
                long time = listOfAllRecordsForUser.get(i).getTimeAdded().toDate().getTime();
                Calendar calendar1 = Calendar.getInstance();
                calendar1.setTimeInMillis(time);
                calendar1.set(Calendar.HOUR_OF_DAY, 0);
                calendar1.set(Calendar.MINUTE, 0);
                calendar1.set(Calendar.SECOND, 0);
                calendar1.set(Calendar.MILLISECOND, 0);

                blockedDates.remove(calendar1);

            }
        }

        return blockedDates;
    }

}