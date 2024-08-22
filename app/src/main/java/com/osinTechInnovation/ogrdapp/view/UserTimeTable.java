package com.osinTechInnovation.ogrdapp.view;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.graphics.Canvas;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.osinTechInnovation.ogrdapp.R;
import com.osinTechInnovation.ogrdapp.TimeOverallAdapter;
import com.osinTechInnovation.ogrdapp.model.TimeModel;
import com.osinTechInnovation.ogrdapp.utility.FormattedTime;
import com.osinTechInnovation.ogrdapp.utility.SwipeController;
import com.osinTechInnovation.ogrdapp.utility.SwipeControllerActions;
import com.osinTechInnovation.ogrdapp.viewmodel.AuthViewModel;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class UserTimeTable extends AppCompatActivity {

    private Spinner spinnerMonth, spinnerYear;
    private RecyclerView recyclerView;
    private ArrayList<TimeModel> timeModelArrayList = new ArrayList<>();
    public TextView editTextBeginTime,editTextEndTime,editTextTimeOverall,lack_data;

    private TimeOverallAdapter timeOverallAdapter = new TimeOverallAdapter(this, timeModelArrayList);
    String selectedSpinnerOnYear = "";
    private AuthViewModel authViewModel;
    public String idUserSelectedByAdmin;
    SwipeController swipeController = null;
    ArrayList<TimeModel> arrayListTmp;

    public int hour, minute;
    long overall;
    private Dialog dialog;
    private Button btnYes,btnNo,btnCancel,btnApproved;
    private TextView tvEnterTimeBegging,tvEnterTimeEnd,tvEnterTimeOverall;


    long allTheTime = 0;
    long settledTime = 0;
    int sum = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_time_table);
        spinnerMonth = findViewById(R.id.spinner_month);
        spinnerYear = findViewById(R.id.spinner_year);
        recyclerView = findViewById(R.id.recyclerView);
        lack_data = findViewById(R.id.lack_data);

        arrayListTmp = new ArrayList<>();

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        idUserSelectedByAdmin = getIntent().getStringExtra("Id");

        //Log.i("idUserSelectedByAdmin",idUserSelectedByAdmin);

        if (idUserSelectedByAdmin != null) {

            authViewModel.getAllTimeModelsForAdminSQL(idUserSelectedByAdmin).observe(this, new Observer<List<TimeModel>>() {
                @Override
                public void onChanged(List<TimeModel> timeModelList) {
                    timeModelArrayList.clear();
                    timeModelArrayList.addAll(timeModelList);

                    if(!timeModelArrayList.isEmpty()){
                        lack_data.setVisibility(View.INVISIBLE);
                    }else {
                        lack_data.setVisibility(View.VISIBLE);
                    }
                    activateSpinner();
                    activateSpinnerYear();
                    timeOverallAdapter.notifyDataSetChanged();
                }
            });

        } else {

            authViewModel.getAllTimeModelsForUserSQL().observe(this, new Observer<List<TimeModel>>() {
                @Override
                public void onChanged(List<TimeModel> timeModelList) {
                    timeModelArrayList.clear();
                    timeModelArrayList.addAll(timeModelList);

                    //TODO Tutaj policz

                    for(TimeModel timeModel: timeModelArrayList){
                        allTheTime += timeModel.getTimeOverallInLong();
                        sum++;
                        if(timeModel.getMoneyOverall()){
                        settledTime+= timeModel.getTimeOverallInLong();
                        }


                    }
                    Log.i("Sum of entries", sum+"");
                    Log.i("All time Formated", FormattedTime.formattedTime(allTheTime)+"");
                    Log.i("All time raw", allTheTime+"");
                    Log.i("Settled Time Formated", FormattedTime.formattedTime(settledTime)+"");
                    Log.i("Settled Time raw ", settledTime+"");
                    Log.i("Settled To Settle ", allTheTime-settledTime+"");

                    if(!timeModelArrayList.isEmpty()){
                        lack_data.setVisibility(View.INVISIBLE);
                    }
                    else {
                        lack_data.setVisibility(View.VISIBLE);
                    }
                    activateSpinner();
                    activateSpinnerYear();
                    timeOverallAdapter.notifyDataSetChanged();
                }
            });
        }

        swipeControllerToRecyclerView();


        // Calendar dla spinnerów
        Calendar calendar = Calendar.getInstance();

        spinnerYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedSpinnerOnYear = parent.getItemAtPosition(position).toString();
                if (!timeModelArrayList.isEmpty()) {
                    arrayListTmp.clear();
                    for (TimeModel model : timeModelArrayList) {
                        String s = formatDateWithMonthAndYear(model.getTimeAdded().toDate()).toLowerCase();
                        calendar.set(Calendar.YEAR, Integer.parseInt(selectedSpinnerOnYear));
                        String s1 = formatDateWithMonthAndYear(new Date(calendar.getTime().toInstant().toEpochMilli()));
                        if (s1.equals(s)) {
                            arrayListTmp.add(model);
                        }

                    }
                    recyclerView = findViewById(R.id.recyclerView);
                    timeOverallAdapter = new TimeOverallAdapter(UserTimeTable.this, arrayListTmp);
                    recyclerView.setLayoutManager(new LinearLayoutManager(UserTimeTable.this, LinearLayoutManager.VERTICAL, false));
                    recyclerView.setAdapter(timeOverallAdapter);
                    if(arrayListTmp.isEmpty()) {
                        lack_data.setVisibility(View.VISIBLE);
                    } else {
                        lack_data.setVisibility(View.INVISIBLE);
                    }
                }



            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spinnerMonth.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (!timeModelArrayList.isEmpty()) {
                    arrayListTmp.clear();
                    for (TimeModel model : timeModelArrayList) {
                        String s = formatDateWithMonthAndYear(model.getTimeAdded().toDate()).toLowerCase();
                        calendar.set(Calendar.YEAR, Integer.parseInt(selectedSpinnerOnYear));
                        calendar.set(Calendar.MONTH, position);
                        String s1 = formatDateWithMonthAndYear(new Date(calendar.getTime().toInstant().toEpochMilli())).toLowerCase();

                        if (s1.equals(s)) {
                            arrayListTmp.add(model);
                        }

                    }

                    recyclerView = findViewById(R.id.recyclerView);
                    timeOverallAdapter = new TimeOverallAdapter(UserTimeTable.this, arrayListTmp);
                    recyclerView.setLayoutManager(new LinearLayoutManager(UserTimeTable.this, LinearLayoutManager.VERTICAL, false));
                    recyclerView.setAdapter(timeOverallAdapter);
                    if(arrayListTmp.isEmpty()) {
                        lack_data.setVisibility(View.VISIBLE);
                        Log.i("SIZE ARRAY",arrayListTmp.size()+"");
                    }
                    else {
                        lack_data.setVisibility(View.INVISIBLE);
                    }

                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

    }


    public void swipeControllerToRecyclerView() {

        if (idUserSelectedByAdmin != null) {

            //tutaj gdzieś zablokuj możliwość zmieniania danych które już są rozliczone.
            swipeController = new SwipeController(this,new SwipeControllerActions() {
                @Override
                public void onRightClicked(int position) {

                    dialog = new Dialog(UserTimeTable.this);
                    dialog.setContentView(R.layout.dialog_delete_data);
                    dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
                    dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.custom_dialog_bg));

                    btnYes = dialog.findViewById(R.id.btn_yes);
                    btnNo = dialog.findViewById(R.id.btn_no);
                    dialog.show();

                    btnYes.setOnClickListener((view -> {
                        authViewModel.deleteDateFromFireBase(arrayListTmp.get(position));
                        authViewModel.updateEntriesAmount(arrayListTmp.get(position));
                        TimeModel timeModel = arrayListTmp.get(position);
                        arrayListTmp.remove(position);

                        // Delete for the user
                        timeModel.setTimeOverallInLong(-timeModel.getTimeOverallInLong());
                        authViewModel.updatedDataHoursToFirebaseUser(timeModel);

                        timeOverallAdapter.notifyDataSetChanged();
                        dialog.dismiss();

                    }));

                    btnNo.setOnClickListener((view->{
                        dialog.dismiss();
                    }));

                }

                // Edycja danych
                @Override
                public void onLeftClicked(int position) {

                    if (arrayListTmp.get(position).getMoneyOverall() == false) {

                        dialog = new Dialog(UserTimeTable.this);
                        dialog.setContentView(R.layout.dialog_edit_data);
                        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.custom_dialog_bg));
                        dialog.show();


                        btnApproved = dialog.findViewById(R.id.btn_approved);
                        btnCancel = dialog.findViewById(R.id.btn_cancel);

                        tvEnterTimeBegging = dialog.findViewById(R.id.enter_time_begging);
                        tvEnterTimeEnd = dialog.findViewById(R.id.enter_time_end);
                        tvEnterTimeOverall = dialog.findViewById(R.id.enter_overall_time);

                        tvEnterTimeBegging.setText(arrayListTmp.get(position).getTimeBegin());
                        tvEnterTimeEnd.setText(arrayListTmp.get(position).getTimeEnd());
                        tvEnterTimeOverall.setText(arrayListTmp.get(position).getTimeOverall());

                        btnApproved.setOnClickListener((view -> {
                            if (!tvEnterTimeOverall.getText().toString().equals("Co jest ziomuś ?")) {
                                TimeModel timeModel = arrayListTmp.get(position);

                                long timeInLongToDelete = -timeModel.getTimeOverallInLong();

                                TimeModel timeModel1 = new TimeModel();
                                timeModel1.setId(timeModel.getId());
                                timeModel1.setTimeOverallInLong((timeInLongToDelete + overall));
                                authViewModel.updatedDataHoursToFirebaseUser(timeModel1);


                                authViewModel.updateDataToFirebase(
                                        arrayListTmp.get(position).getDocumentId(),
                                        tvEnterTimeBegging.getText().toString(),
                                        tvEnterTimeEnd.getText().toString(),
                                        tvEnterTimeOverall.getText().toString(),
                                        overall == 0 ? arrayListTmp.get(position).getTimeOverallInLong() : overall,
                                        arrayListTmp.get(position));


                                arrayListTmp.get(position).setTimeBegin(tvEnterTimeBegging.getText().toString());
                                arrayListTmp.get(position).setTimeEnd(tvEnterTimeEnd.getText().toString());
                                arrayListTmp.get(position).setTimeOverall(tvEnterTimeOverall.getText().toString());
                                arrayListTmp.get(position).setTimeOverallInLong(overall == 0 ? arrayListTmp.get(position).getTimeOverallInLong() : overall);

                                //Aktualizacja TimeModelArrayList
                                updateTimeModelArrayList(arrayListTmp.get(position));

                                overall = 0;

                                timeOverallAdapter.notifyDataSetChanged();

                                dialog.dismiss();
                            }
                        }));

                        btnCancel.setOnClickListener((view -> {
                            dialog.dismiss();
                        }));

                        tvEnterTimeBegging.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                timePickerDialog(getString(R.string.set_start_time), tvEnterTimeBegging);

                            }
                        });

                        tvEnterTimeEnd.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                timePickerDialog(getString(R.string.set_an_end_time), tvEnterTimeEnd);

                            }
                        });



                    }

                   /* if (arrayListTmp.get(position).getMoneyOverall() == false) {

                        AlertDialog.Builder builder2 = new AlertDialog.Builder(UserTimeTable.this);
                        LayoutInflater layoutInflater = LayoutInflater.from(UserTimeTable.this);
                        View inflate = layoutInflater.inflate(R.layout.edit_user_time_table, null);
                        builder2.setView(inflate);
                        builder2.setTitle("Edycja danych");


                        editTextBeginTime = inflate.findViewById(R.id.editText_beginTime);
                        editTextEndTime = inflate.findViewById(R.id.editText_endTime);
                        editTextTimeOverall = inflate.findViewById(R.id.editText_timeOverall);

                        editTextBeginTime.setText(arrayListTmp.get(position).getTimeBegin());
                        editTextEndTime.setText(arrayListTmp.get(position).getTimeEnd());
                        editTextTimeOverall.setText(arrayListTmp.get(position).getTimeOverall());

                        editTextBeginTime.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                timePickerDialog("Ustaw godzinę rozpoczęcia", editTextBeginTime);
                            }
                        });

                        editTextEndTime.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                timePickerDialog("Ustaw godzinę zakończenia", editTextEndTime);
                            }
                        });




                        builder2.setPositiveButton("Potwierdź", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                if (!editTextTimeOverall.getText().toString().equals("Co jest ziomuś ?")) {
                                    TimeModel timeModel = arrayListTmp.get(position);

                                    long timeInLongToDelete = -timeModel.getTimeOverallInLong();

                                    TimeModel timeModel1 = new TimeModel();
                                    timeModel1.setId(timeModel.getId());
                                    timeModel1.setTimeOverallInLong((timeInLongToDelete + overall));
                                    authViewModel.updatedDataHoursToFirebaseUser(timeModel1);


                                    authViewModel.updateDataToFirebase(
                                            arrayListTmp.get(position).getDocumentId(),
                                            editTextBeginTime.getText().toString(),
                                            editTextEndTime.getText().toString(),
                                            editTextTimeOverall.getText().toString(),
                                            overall == 0 ? arrayListTmp.get(position).getTimeOverallInLong() : overall,
                                            arrayListTmp.get(position));


                                    arrayListTmp.get(position).setTimeBegin(editTextBeginTime.getText().toString());
                                    arrayListTmp.get(position).setTimeEnd(editTextEndTime.getText().toString());
                                    arrayListTmp.get(position).setTimeOverall(editTextTimeOverall.getText().toString());
                                    arrayListTmp.get(position).setTimeOverallInLong(overall == 0 ? arrayListTmp.get(position).getTimeOverallInLong() : overall);

                                    //Aktualizacja TimeModelArrayList
                                    updateTimeModelArrayList(arrayListTmp.get(position));

                                    overall = 0;
                                    timeOverallAdapter.notifyDataSetChanged();
                                }
                            }
                        });
                        builder2.setNegativeButton("Anuluj", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });

                        AlertDialog alertDialog = builder2.create();
                        alertDialog.show();
                        super.onLeftClicked(position);
                    } else {
                        Toast.makeText(UserTimeTable.this, "Nie możesz edytować rozliczonych danych", Toast.LENGTH_SHORT).show();
                    }*/
                }
            });

            ItemTouchHelper itemTouchhelper = new ItemTouchHelper(swipeController);
            itemTouchhelper.attachToRecyclerView(recyclerView);

            recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
                @Override
                public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
                    swipeController.onDraw(c);
                }
            });
        }

    }

    private void updateTimeModelArrayList(TimeModel timeModel) {
        for (int i = 0; i < timeModelArrayList.size(); i++) {
            if (timeModelArrayList.get(i).getDocumentId().equals(timeModel.getDocumentId())) {
                timeModelArrayList.remove(i);
                timeModelArrayList.add(timeModel);
                break;
            }
        }

    }

    private void timePickerDialog(String titleName, TextView textView) {

        overall = 0;

        TimePickerDialog.OnTimeSetListener onTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minuteOfDay) {
                hour = hourOfDay;
                minute = minuteOfDay;

                if (titleName.equals(getString(R.string.set_an_end_time)) | titleName.equals(getString(R.string.set_start_time))) {

                    textView.setText(String.format(Locale.getDefault(), "%02d:%02d", hour, minute));

                    //TODO

                    String[] split = tvEnterTimeBegging.getText().toString().split(":");
                    String[] split1 = tvEnterTimeEnd.getText().toString().split(":");

                    int hoursEditTextBeginTimeConvertedToMinute = Integer.parseInt(split[0]) * 60;
                    int minutesEditTextBeginTimeInMinutes = Integer.parseInt(split[1]) * 1;
                    int timeInMinutesForBeginTime = hoursEditTextBeginTimeConvertedToMinute + minutesEditTextBeginTimeInMinutes;

                    int hoursEditTextEndTimeConvertedToMinute = Integer.parseInt(split1[0]) * 60;
                    int minutesEditTextEndTimeInMinutes = Integer.parseInt(split1[1]) * 1;

                    int timeInMinutesForEndTime = hoursEditTextEndTimeConvertedToMinute + minutesEditTextEndTimeInMinutes;

                    int overallTimeInMinutes = timeInMinutesForEndTime - timeInMinutesForBeginTime;

                    if (overallTimeInMinutes >= 0) {
                        overall = overallTimeInMinutes * 60 * 1000;

                    } else {
                        overall = 0;
                    }


                    String formattedTimeInHoursAndMinutes = FormattedTime.formattedTimeInHoursAndMinutes(overallTimeInMinutes);
                    if (formattedTimeInHoursAndMinutes.contains("-")) {
                        tvEnterTimeOverall.setText(getString(R.string.the_start_time_cannot_be_later_than_the_start_time));
                    } else {
                        tvEnterTimeOverall.setText(formattedTimeInHoursAndMinutes);
                    }

                }
            }
        };

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, onTimeSetListener, hour, minute, true);
        timePickerDialog.setTitle(titleName);
        timePickerDialog.show();
        timePickerDialog.getButton(TimePickerDialog.BUTTON_POSITIVE).setBackgroundColor(getResources().getColor(R.color.teal_200));
        timePickerDialog.getButton(TimePickerDialog.BUTTON_NEGATIVE).setBackgroundColor(getResources().getColor(R.color.teal_200));


    }

    public void activateSpinner() {
        //Geting the current month
        Month month = LocalDate.now().getMonth();
        int monthToSend = month.getValue() - 1;
        ArrayAdapter<CharSequence> adapterMonth = ArrayAdapter.createFromResource(this, R.array.months, android.R.layout.simple_spinner_item);
        adapterMonth.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMonth.setAdapter(adapterMonth);
        //Setting current month to spinner
        spinnerMonth.setSelection(monthToSend);
    }

    public void activateSpinnerYear() {
        // getting current year
        int year = LocalDate.now().getYear();
        ArrayAdapter<CharSequence> adapterYear = ArrayAdapter.createFromResource(this, R.array.year, android.R.layout.simple_spinner_item);
        adapterYear.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerYear.setAdapter(adapterYear);
        // Setting the current year
        spinnerYear.setSelection(adapterYear.getPosition(String.valueOf(year)));
    }

    private String formatDateWithMonthAndYear(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("LLLLyyyy", getResources().getConfiguration().locale);
        return dateFormat.format(date);
    }

    @Override
    protected void onStart() {

        super.onStart();
    }
}