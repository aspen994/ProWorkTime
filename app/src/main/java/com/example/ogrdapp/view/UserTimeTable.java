package com.example.ogrdapp.view;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ogrdapp.R;
import com.example.ogrdapp.TimeOverallAdapter;
import com.example.ogrdapp.model.TimeModel;
import com.example.ogrdapp.utility.FormattedTime;
import com.example.ogrdapp.utility.SwipeController;
import com.example.ogrdapp.utility.SwipeControllerActions;
import com.example.ogrdapp.viewmodel.AuthViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.security.auth.login.LoginException;

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
    public String idUserSelectedByAdmin;
    SwipeController swipeController = null;
    ArrayList<TimeModel> arrayListTmp;
    public TextView editTextBeginTime;
    public TextView editTextEndTime;
    public TextView editTextTimeOverall;
    public int hour,minute;
    long overall;
    public  boolean isChanged;

    private void writeTimeModelForDisplayToSharedPref() {
        Gson gson = new Gson();
        String jsonString = gson.toJson(timeModelArrayList);

        SharedPreferences sharedPreferences =getSharedPreferences("UserTimeTableSharedPreferences",MODE_PRIVATE);

        SharedPreferences.Editor myEdit = sharedPreferences.edit();
        myEdit.putString("timeModelArrayList",jsonString);
        myEdit.commit();
        myEdit.apply();
    }
    @Override
    protected void onPause() {
        if(isChanged)
        {
            writeTimeModelForDisplayToSharedPref();
        }
        isChanged=false;
        super.onPause();
    }

    @Override
    protected void onStop() {

        //writeTimeModelForDisplayToSharedPref();
        super.onStop();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_time_table);
        spinnerMonth = findViewById(R.id.spinner_month);
        spinnerYear = findViewById(R.id.spinner_year);
        recyclerView = findViewById(R.id.recyclerView);
        sumTextView = findViewById(R.id.sum);



        arrayListTmp = new ArrayList<>();

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

  //      Toast.makeText(this, "To Tu powinno", Toast.LENGTH_SHORT).show();
//        authViewModel.getAllIdDocumentFromTimeModel();

        // FOR ADMIN


        // FOR USER

        idUserSelectedByAdmin = getIntent().getStringExtra("Id");
        if(idUserSelectedByAdmin!=null) {

            authViewModel.getTimeForUser(idUserSelectedByAdmin);

            authViewModel.getTimeForUserListMutableLiveData().observe(this, new Observer<List<TimeModel>>() {
                @Override
                public void onChanged(List<TimeModel> timeModels) {
                    timeModelArrayList.clear();
                    timeModelArrayList.addAll(timeModels);
                    activateSpinner();
                    activateSpinnerYear();
                    timeOverallAdapter.notifyDataSetChanged();
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
                    timeOverallAdapter.notifyDataSetChanged();
                }
            });
        }
        
        //readTimeModelArrayList(timeModelArrayList);

        swipeControllerToRecyclerView();

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

                readTimeModelArrayList(timeModelArrayList);

                if(!timeModelArrayList.isEmpty()) {
                    //ArrayList<TimeModel> arrayListTmp = new ArrayList<>();
                    arrayListTmp.clear();
                    for (TimeModel model : timeModelArrayList) {
                        String s = formatDateWithMonthAndYear(model.getTimeAdded().toDate()).toLowerCase();
                        String s1 = selectedSpinnerOnMonth.toLowerCase()+year.toLowerCase();


                        if (s1.equals(s)) {
                            arrayListTmp.add(model);
                        }

                    }
                    recyclerView = findViewById(R.id.recyclerView);
                    timeOverallAdapter = new TimeOverallAdapter(UserTimeTable.this, arrayListTmp);
                    recyclerView.setLayoutManager(new LinearLayoutManager(UserTimeTable.this,LinearLayoutManager.VERTICAL,false));
                    recyclerView.setAdapter(timeOverallAdapter);


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
                    //ArrayList<TimeModel> arrayListTmp = new ArrayList<>();
                    arrayListTmp.clear();
                    for (TimeModel model : timeModelArrayList) {
                        String s = formatDateWithMonthAndYear(model.getTimeAdded().toDate()).toLowerCase();
                        String s1 = month.toLowerCase() + selectedSpinnerOnYear;

                        if (s1.equals(s)) {
                            arrayListTmp.add(model);
                        }

                    }

                    recyclerView = findViewById(R.id.recyclerView);
                    timeOverallAdapter = new TimeOverallAdapter(UserTimeTable.this, arrayListTmp);
                    recyclerView.setLayoutManager(new LinearLayoutManager(UserTimeTable.this,LinearLayoutManager.VERTICAL,false));
                    recyclerView.setAdapter(timeOverallAdapter);
                }

            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });



    }

    private void readTimeModelArrayList(ArrayList<TimeModel> timeModelArrayList) {
        for (TimeModel timeModel: timeModelArrayList) {
            Log.i("read UserTimeTable",timeModel.getMoneyOverall()+"");

        }
    }

    public void swipeControllerToRecyclerView() {

        if (idUserSelectedByAdmin != null) {

            //tutaj gdzieś zablokuj możliwość zmieniania danych które już są rozliczone.
            swipeController = new SwipeController(new SwipeControllerActions() {
                @Override
                public void onRightClicked(int position) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(UserTimeTable.this);
                    builder.setTitle("Jesteś pewien że chcesz usunąć ten wpis ?");
                    builder.setPositiveButton("Tak", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            authViewModel.deleteDateFromFireBase(arrayListTmp.get(position).getDocumentId());
                            TimeModel timeModel = arrayListTmp.get(position);
                            arrayListTmp.remove(position);

                            // Delete for the user

                            timeModel.setTimeOverallInLong(-timeModel.getTimeOverallInLong());
                            authViewModel.updatedDataHoursToFirebaseUser(timeModel);


                            timeOverallAdapter.notifyDataSetChanged();
                        }
                    });
                    builder.setNegativeButton("Nie", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });

                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();


                }

                // Edycja danych
                @Override
                public void onLeftClicked(int position) {

                    if(arrayListTmp.get(position).getMoneyOverall()==false){

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

                    editTextTimeOverall.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {




                             /*timePickerDialog("Ustaw ogólny czas", editTextTimeOverall);
                             Log.i("Overall", overall+"");*/
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
                                        overall == 0 ? arrayListTmp.get(position).getTimeOverallInLong() : overall);

                                Log.i("onLeftClicked", overall + "");

                                arrayListTmp.get(position).setTimeBegin(editTextBeginTime.getText().toString());
                                arrayListTmp.get(position).setTimeEnd(editTextEndTime.getText().toString());
                                arrayListTmp.get(position).setTimeOverall(editTextTimeOverall.getText().toString());
                                arrayListTmp.get(position).setTimeOverallInLong(overall == 0 ? arrayListTmp.get(position).getTimeOverallInLong() : overall);
                                isChanged = true;

                                //Aktualizacja TimeModelArrayList
                                updateTimeModelArrayList(arrayListTmp.get(position));

                                Toast.makeText(UserTimeTable.this, "ZAKTUALIZOWANE", Toast.LENGTH_SHORT).show();

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
                    }
                    else {
                        Toast.makeText(UserTimeTable.this, "Nie możesz edytować rozliczonych danych", Toast.LENGTH_SHORT).show();
                    }
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

    private void updateTimeModelArrayList( TimeModel timeModel) {
        for (int i = 0; i < timeModelArrayList.size(); i++) {
            if(timeModelArrayList.get(i).getDocumentId().equals(timeModel.getDocumentId()))
            {
                timeModelArrayList.remove(i);
                timeModelArrayList.add(timeModel);
                break;
            }
        }

       /* for (int i = 0; i < timeModelArrayList.size(); i++) {
            Log.i("Update TimeModel",timeModelArrayList.get(i).getUserName());
            Log.i("Update TimeModel",timeModelArrayList.get(i).getTimeAdded().toDate().toString());
            Log.i("Update TimeModel",timeModelArrayList.get(i).getTimeBegin());
            Log.i("Update TimeModel",timeModelArrayList.get(i).getTimeEnd());
        }*/

    }

    private void timePickerDialog(String titleName, TextView textView) {

        overall=0;

        TimePickerDialog.OnTimeSetListener onTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minuteOfDay) {
                hour=hourOfDay;
                minute = minuteOfDay;
                Log.i("HourOfDay",hourOfDay+"");
                Log.i("minuteOfDay",minuteOfDay+"");


                if(titleName.equals("Ustaw godzinę zakończenia")| titleName.equals("Ustaw godzinę rozpoczęcia"))
                {
                    Log.i("timePickerDialog", overall +"");
                    textView.setText(String.format(Locale.getDefault(),"%02d:%02d",hour,minute));


                    String[] split = editTextBeginTime.getText().toString().split(":");
                    String[] split1 = editTextEndTime.getText().toString().split(":");

                    int hoursEditTextBeginTimeConvertedToMinute = Integer.parseInt(split[0])*60;
                    int minutesEditTextBeginTimeInMinutes = Integer.parseInt(split[1])*1;
                    int timeInMinutesForBeginTime = hoursEditTextBeginTimeConvertedToMinute+minutesEditTextBeginTimeInMinutes;

                    int hoursEditTextEndTimeConvertedToMinute = Integer.parseInt(split1[0]) * 60;
                    int minutesEditTextEndTimeInMinutes = Integer.parseInt(split1[1])*1;

                    int timeInMinutesForEndTime = hoursEditTextEndTimeConvertedToMinute+minutesEditTextEndTimeInMinutes;

                    int overallTimeInMinutes = timeInMinutesForEndTime - timeInMinutesForBeginTime;

                    if(overallTimeInMinutes>=0) {
                        overall = overallTimeInMinutes * 60 * 1000;
                    }
                    else {
                        overall=0;
                    }

                    Log.i("overallTimeInMinutes",overallTimeInMinutes+"");

                    String formattedTimeInHoursAndMinutes = FormattedTime.formattedTimeInHoursAndMinutes(overallTimeInMinutes);
                    if(formattedTimeInHoursAndMinutes.contains("-")) {
                        Toast.makeText(UserTimeTable.this, "Godzina rozpoczęcia nie może być później niż godzina zaczęcia", Toast.LENGTH_SHORT).show();
                        editTextTimeOverall.setText("Co jest ziomuś ?");
                    }
                    else {
                        editTextTimeOverall.setText(formattedTimeInHoursAndMinutes);
                    }

                }
            }
        };

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,onTimeSetListener,hour,minute,true);
        timePickerDialog.setTitle(titleName);
        timePickerDialog.show();
        timePickerDialog.getButton(TimePickerDialog.BUTTON_POSITIVE).setBackgroundColor(getResources().getColor(R.color.teal_200));
        timePickerDialog.getButton(TimePickerDialog.BUTTON_NEGATIVE).setBackgroundColor(getResources().getColor(R.color.teal_200));


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