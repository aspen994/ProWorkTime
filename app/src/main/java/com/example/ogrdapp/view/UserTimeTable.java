package com.example.ogrdapp.view;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
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
import com.example.ogrdapp.utility.SwipeController;
import com.example.ogrdapp.utility.SwipeControllerActions;
import com.example.ogrdapp.viewmodel.AuthViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
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
    public String idUserSelectedByAdmin;
    SwipeController swipeController = null;
    ArrayList<TimeModel> arrayListTmp;
    public TextView editTextBeginTime;
    public TextView editTextEndTime;
    public TextView editTextTimeOverall;
    public int hour,minute;
    long overall;


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

    public void swipeControllerToRecyclerView() {

        if (idUserSelectedByAdmin != null) {
            swipeController = new SwipeController(new SwipeControllerActions() {
                @Override
                public void onRightClicked(int position) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(UserTimeTable.this);
                    builder.setTitle("Jesteś pewien że chcesz usunąć ten wpis ?");
                    builder.setPositiveButton("Tak", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            authViewModel.deleteDateFromFireBase(arrayListTmp.get(position).getDocumentId());
                            arrayListTmp.remove(position);
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

                @Override
                public void onLeftClicked(int position) {
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
                            timePickerDialog("Ustaw godzinę rozpoczęcia",editTextBeginTime);
                        }
                    });

                    editTextEndTime.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            timePickerDialog("Ustaw godzinę zakończenia",editTextEndTime);
                        }
                    });

                    editTextTimeOverall.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                             overall= timePickerDialog("Ustaw ogólny czas", editTextTimeOverall);
                        }
                    });


                    builder2.setPositiveButton("Potwierdź", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                                authViewModel.updateDataToFirebase(
                                        arrayListTmp.get(position).getDocumentId(),
                                        editTextBeginTime.getText().toString(),
                                        editTextEndTime.getText().toString(),
                                        editTextTimeOverall.getText().toString(),
                                        overall==0?arrayListTmp.get(position).getTimeOverallInLong():overall);

                                arrayListTmp.get(position).setTimeBegin(editTextBeginTime.getText().toString());
                                arrayListTmp.get(position).setTimeEnd(editTextEndTime.getText().toString());
                                arrayListTmp.get(position).setTimeOverall(editTextTimeOverall.getText().toString());
                                arrayListTmp.get(position).setTimeOverallInLong(overall==0?arrayListTmp.get(position).getTimeOverallInLong():overall);
                                Toast.makeText(UserTimeTable.this, "ZAKTUALIZOWANE", Toast.LENGTH_SHORT).show();

                                overall=0;
                                timeOverallAdapter.notifyDataSetChanged();

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

    private long timePickerDialog(String titleName, TextView textView) {

        overall=0;
        TimePickerDialog.OnTimeSetListener onTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minuteOfDay) {
                hour=hourOfDay;
                minute = minuteOfDay;
                overall +=minuteOfDay*60000;
                overall +=hourOfDay * 3600000;
                textView.setText(String.format(Locale.getDefault(),"%02d:%02d",hour,minute));
            }
        };

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,onTimeSetListener,hour,minute,true);
        timePickerDialog.setTitle(titleName);
        timePickerDialog.show();
        timePickerDialog.getButton(TimePickerDialog.BUTTON_POSITIVE).setBackgroundColor(getResources().getColor(R.color.teal_200));
        timePickerDialog.getButton(TimePickerDialog.BUTTON_NEGATIVE).setBackgroundColor(getResources().getColor(R.color.teal_200));

        return overall;

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