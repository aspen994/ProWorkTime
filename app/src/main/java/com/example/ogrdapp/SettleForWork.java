package com.example.ogrdapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.util.Pair;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Observer;

import com.example.ogrdapp.model.TimeModel;
import com.example.ogrdapp.utility.DaysOutOfValidate;
import com.example.ogrdapp.utility.FormattedTime;
import com.example.ogrdapp.viewmodel.AuthViewModel;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public class SettleForWork   {
private Context context;
private FragmentActivity fragmentActivity;
private View inflate;
private Button btn_date,btn_confirm;
private TextView dateToDisplay,timeWorkedDisplay, moneyToPaidDisplay;
private EditText bidEnter;
private String userName;
private String id;
private String email="";
private AuthViewModel authViewModel;
private long timeToSettlement;
private long paycheck;
private long hoursToSettle;
private int bidEnterValue;

private List<TimeModel> timeModelsList;
private List<TimeModel> selectedTimeModelList = new ArrayList<>();

    public SettleForWork(Context context, FragmentActivity fragmentActivity, String userName, String id,AuthViewModel authViewModel,List<TimeModel> timeModelsList) {
        this.context = context;
        this.fragmentActivity = fragmentActivity;
        this.userName = userName;
        this.id = id;
        this.authViewModel = authViewModel;
        this.timeModelsList = timeModelsList;
    }

    public void buildAlertDialog()
    {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        inflate = layoutInflater.inflate(R.layout.settlement_layout, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(inflate);
        builder.setTitle("Rozlicz pracownika: " + userName );
        builder.setPositiveButton("Potwierdź wypłatę", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                withdrawnOperation();
            }
        });

        builder.setNegativeButton("Anuluj", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        builder.show();
        setWidgets();
        setClickListenerForAllWidgets();

    }

    private void withdrawnOperation() {

        String s = moneyToPaidDisplay.getText().toString();

        s = s.replace("zł","");

        int i = Integer.parseInt(s);


        // To dla pętli działa
        for(TimeModel timeModel: selectedTimeModelList)
        {

            int i1 = bidEnterValue * FormattedTime.formattedTimeInIntToPay(timeModel.getTimeOverallInLong());
            Log.i("i1 value",i1+"");
            Log.i("TimeOverall",timeModel.getTimeOverallInLong()+"");
            Log.i("i",i+"");
            authViewModel.updateStatusOfSettled(timeModel.getDocumentId(),true,i1);
            //tu tutaj
            timeModel.setMoneyOverall(true);
            // Zaktualizuj dla listy
        }


        if(i!=0) {
            authViewModel.getDataToUpdatePayCheck(selectedTimeModelList.get(0).getId());
        }

        authViewModel.getPaycheckHoursToSettleMutableLiveData().observeForever(new Observer<Map<String, Object>>() {
            @Override
            public void onChanged(Map<String, Object> stringObjectMap) {
                if(!stringObjectMap.isEmpty())
                {
                    long paycheck1 = (long)stringObjectMap.get("paycheck");
                    long hoursToSettle1 = (long)stringObjectMap.get("hoursToSettle");
                    String email1 = (String) stringObjectMap.get("email");

                    paycheck1 += i;
                    hoursToSettle1-=timeToSettlement;

                    authViewModel.updateStatusOfTimeForUser(email1, hoursToSettle1, paycheck1);


                }
            }
        });


    }

    // To DayValidate
    private List<Calendar> getValidDateToSettle() {

        //long timeOverall = 0;
        List<Calendar> blockedDates = new ArrayList<>();

/*
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeModelsList.get(0).getTimeAdded().toDate().getTime());
*/

        for (int i = 0; i < timeModelsList.size(); i++)
        {
            if (timeModelsList.get(i).getId().equals(id)&&timeModelsList.get(i).getMoneyOverall()==true) {

                long time = timeModelsList.get(i).getTimeAdded().toDate().getTime();
                Calendar calendar1 = Calendar.getInstance();
                calendar1.setTimeInMillis(time);

                blockedDates.add(calendar1);
            }
            if (timeModelsList.get(i).getId().equals(id)&&timeModelsList.get(i).getMoneyOverall()==false) {
                long time = timeModelsList.get(i).getTimeAdded().toDate().getTime();
                Calendar calendar1 = Calendar.getInstance();
                calendar1.setTimeInMillis(time);

                blockedDates.remove(time);
            }
        }

        return blockedDates;
    }


    private void setWidgets() {
        btn_date = inflate.findViewById(R.id.btn_date);
        dateToDisplay = inflate.findViewById(R.id.date_display);
        timeWorkedDisplay = inflate.findViewById(R.id.time_worked_display);
        bidEnter = inflate.findViewById(R.id.bid_enter);
        moneyToPaidDisplay = inflate.findViewById(R.id.moneyToPaid_display);
        btn_confirm = inflate.findViewById(R.id.confirm_button);
    }

    public Button getBtn_date() {
        return btn_date;
    }

    private void setClickListenerForAllWidgets()
    {
        btn_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                invokedCalendar();
            }
        });
        dateToDisplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(context, "dateToDisplay", Toast.LENGTH_SHORT).show();
            }
        });
        timeWorkedDisplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(context, "time Worked Display", Toast.LENGTH_SHORT).show();
            }
        });

        btn_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                bidEnterValue = Integer.parseInt(bidEnter.getText().toString());
                moneyToPaidDisplay.setText(bidEnterValue * FormattedTime.formattedTimeInIntToPay(timeToSettlement)+"zł");
            }
        });

    }

    private void invokedCalendar() {
        MaterialDatePicker materialDatePicker = MaterialDatePicker.Builder.dateRangePicker()
                .setCalendarConstraints(DaysOutOfValidate().build())
                .setSelection(Pair.create(MaterialDatePicker.thisMonthInUtcMilliseconds(),MaterialDatePicker.todayInUtcMilliseconds()))
                //.setTheme(R.style.ThemeOverlay_App_MaterialCalendar)
                .build();
        materialDatePicker.show(fragmentActivity.getSupportFragmentManager(), "S");

        selectDataForGivenUser(id,timeModelsList);

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


                timeWorkedDisplay.setText(FormattedTime.formattedTime(getTimeFromRangeDate(calendar1,calendar2)));

                dateToDisplay.setText(materialDatePicker.getHeaderText());

            }
        });
    }

    private long getTimeFromRangeDate(Calendar calendar1, Calendar calendar2) {

        long time = 0;

        Log.i("LOGGED","getTimeFromRangeDate");
        for(TimeModel x: timeModelsList)
        {
            if(x.getId().equals(id)&&x.getMoneyOverall()==false) {

                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(x.getTimeAdded().toDate().toInstant().toEpochMilli());

                if (calendar1.get(Calendar.YEAR) == calendar.get(Calendar.YEAR) &&
                        calendar1.get(Calendar.DAY_OF_YEAR) <= calendar.get(Calendar.DAY_OF_YEAR) &&
                        calendar2.get(Calendar.DAY_OF_YEAR) >= calendar.get(Calendar.DAY_OF_YEAR))
                {
                    time+= x.getTimeOverallInLong();
                    selectedTimeModelList.add(x);

                }
            }
            // Daty z listy

        }
        timeToSettlement=time;

        return time;
    }

    private CalendarConstraints.Builder DaysOutOfValidate() {

        CalendarConstraints.Builder constraintsBuilderRange = new CalendarConstraints.Builder();
        constraintsBuilderRange.setValidator(new DaysOutOfValidate(getValidDateToSettle()));
        return constraintsBuilderRange;
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

        for(TimeModel z: listToReturn)
        {

            //Log.i("Settle Time Added",z.getTimeAdded().toDate().toString());

        }

        return listToReturn;
    }


}
