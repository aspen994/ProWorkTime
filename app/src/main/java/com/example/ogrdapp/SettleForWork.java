package com.example.ogrdapp;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;

import com.example.ogrdapp.model.TimeModel;
import com.example.ogrdapp.utility.DaysOutOfValidate;
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
import java.util.TimeZone;

public class SettleForWork implements LifecycleOwner  {
private Context context;
private FragmentActivity fragmentActivity;
private View inflate;
private Button btn_date;
private TextView dateToDisplay,timeWorkedDisplay, moneyToPaidDisplay;
private EditText bidEnter;
private String userName;
private String id;
private AuthViewModel authViewModel;

private List<TimeModel> timeModelsList;

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
        builder.show();
        setWidgets();
        setClickListenerForAllWidgets();
    }



    private void setWidgets() {
        btn_date = inflate.findViewById(R.id.btn_date);
        dateToDisplay = inflate.findViewById(R.id.date_display);
        timeWorkedDisplay = inflate.findViewById(R.id.time_worked_display);
        bidEnter = inflate.findViewById(R.id.bid_enter);
        moneyToPaidDisplay = inflate.findViewById(R.id.moneyToPaid_display);
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

        bidEnter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(context, "bid Enter", Toast.LENGTH_SHORT).show();
            }
        });

        moneyToPaidDisplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(context, "money to Paid Display", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void invokedCalendar() {
        MaterialDatePicker materialDatePicker = MaterialDatePicker.Builder.dateRangePicker()
                .setCalendarConstraints(DaysOutOfValidate().build())
                .setSelection(Pair.create(MaterialDatePicker.thisMonthInUtcMilliseconds(),MaterialDatePicker.todayInUtcMilliseconds())).build();
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

                calendar1.setTimeInMillis(selection.second);
                formattedDate = format.format(calendar1.getTime());
                stringBuilder.append(formattedDate);

                //setDataForSelectDate(stringBuilder.toString());

                Log.i("SHOW ME THE VALUE", Timestamp.now().toDate() +"");

                //1
                //checkArrayListMethod();

                //binding.txtPickedDate.setText(materialDatePicker.getHeaderText());
                //dateToDisplay.setText(stringBuilder.toString());
                dateToDisplay.setText(materialDatePicker.getHeaderText());
            }
        });
    }

    private CalendarConstraints.Builder DaysOutOfValidate() {

        Log.i("ID from app: ",id);
        Log.i("WHAT IS HERE ",timeModelsList.get(0).getTimeAdded()+" ");

        // Robię metodę która porówna i wyłuska dane dla danego id;


        List<Date> listDates = new ArrayList<>();
        listDates.add(new Date(1699570800000L));
        listDates.add(new Date(1700089200000L));

        CalendarConstraints.Builder constraintsBuilderRange = new CalendarConstraints.Builder();
        constraintsBuilderRange.setValidator(new DaysOutOfValidate(listDates));
        Log.i("Inovked twice","Twice");

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
            Log.i("Settle Name",z.getUserName());
            Log.i("Settle Time Long",z.getTimeOverallInLong()+"");
            //Log.i("Settle Time Added",z.getTimeAdded().toDate().toString());
            Log.i("-----","-----");

        }

        return listToReturn;
    }

    @NonNull
    @Override
    public Lifecycle getLifecycle() {
        return null;
    }
}
