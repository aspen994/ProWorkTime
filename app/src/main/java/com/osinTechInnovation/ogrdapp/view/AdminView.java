package com.osinTechInnovation.ogrdapp.view;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Pair;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.osinTechInnovation.ogrdapp.databinding.ActivityAdminViewBinding;
import com.osinTechInnovation.ogrdapp.model.TimeModel;
import com.osinTechInnovation.ogrdapp.model.TimeModelForDisplay;
import com.osinTechInnovation.ogrdapp.model.User;
import com.osinTechInnovation.ogrdapp.viewmodel.AuthViewModel;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;

public class AdminView extends AppCompatActivity {

    private AuthViewModel authViewModel;
    private LinkedList<User> userModelArrayList;
    private LinkedList<TimeModelForDisplay> timeModelForDisplayArrayList;
    private LinkedList<TimeModel> timeModelArrayListForAdmin;

    private LinkedList<TimeModelForDisplay> brandNewArrayList;
    private LinkedList<TimeModel> arrayListFromInsideSelectDateMethod;
    public LinkedList<TimeModel> listOfAllRecordsForUser;

    private AdapterUserForAdmin adapterUserForAdmin;
    private ActivityAdminViewBinding binding;
    //public static int i=0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminViewBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        timeModelForDisplayArrayList = new LinkedList<>();

        userModelArrayList = new LinkedList<>();

        timeModelArrayListForAdmin = new LinkedList<>();
        brandNewArrayList = new LinkedList<>();
        arrayListFromInsideSelectDateMethod = new LinkedList<>();
        listOfAllRecordsForUser = new LinkedList<>();

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        authViewModel.getUsersDataAssignedToAdmin();
        authViewModel.getUserArrayListOfUserMutableLiveData().observe(this, new Observer<List<User>>() {
            @Override
            public void onChanged(List<User> user) {
                userModelArrayList.addAll(user);

                for (User users : userModelArrayList) {
                    assignTimeModelForUser(users.getUserId());

                }
            }
        });

        MaterialDatePicker materialDatePicker = MaterialDatePicker.Builder.dateRangePicker()
                .setSelection(Pair.create(MaterialDatePicker.thisMonthInUtcMilliseconds(), MaterialDatePicker.todayInUtcMilliseconds())).build();
        materialDatePicker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener<Pair<Long, Long>>() {
            @Override
            public void onPositiveButtonClick(Pair<Long, Long> selection) {

                StringBuilder stringBuilder = new StringBuilder();

                Calendar calendar1 = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                calendar1.setTimeInMillis(selection.first);
                SimpleDateFormat format = new SimpleDateFormat("ddMMyyyy");
                String formattedDate = format.format(calendar1.getTime());
                stringBuilder.append(formattedDate + "-");


                calendar1.setTimeInMillis(selection.second);
                formattedDate = format.format(calendar1.getTime());
                stringBuilder.append(formattedDate);


                setDataForSelectDate(stringBuilder.toString());

            }
        });


        binding.btnDataPicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                materialDatePicker.show(getSupportFragmentManager(), "Tag_picker");
                materialDatePicker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener() {
                    @Override
                    public void onPositiveButtonClick(Object selection) {
                        binding.txtPickedDate.setText(materialDatePicker.getHeaderText());
                    }
                });
            }
        });

    }

    public void setDataForSelectDate(String fromDatePicker) {

        //1 split date for two different elements in array list
        String[] split = fromDatePicker.split("-");
        //Toast.makeText(this, "HERE", Toast.LENGTH_SHORT).show();

        Date[] daty = new Date[2];
        try {
            daty[0] = new SimpleDateFormat("ddMMyyyy").parse(split[0]);
            daty[1] = new SimpleDateFormat("ddMMyyyy").parse(split[1]);
        } catch (ParseException e) {
            throw new RuntimeException();
        }

        Calendar start = Calendar.getInstance();
        start.setTime(daty[0]);
        Calendar end = Calendar.getInstance();
        end.setTime(daty[1]);


        ArrayList<String> arrayListOfSelectedDate = new ArrayList<>();


        for (Date date = start.getTime(); start.before(end) || start.equals(end); start.add(Calendar.DATE, 1), date = start.getTime()) {
            arrayListOfSelectedDate.add(formatDateWithMonthAndYear(date));
        }

        if (!timeModelArrayListForAdmin.isEmpty()) {
            for (int j = 0; j < timeModelArrayListForAdmin.size(); j++) {
                for (int k = 0; k < arrayListOfSelectedDate.size(); k++) {
                    if (formatDateWithMonthAndYear(timeModelArrayListForAdmin.get(j).getTimeAdded().toDate()).equals(arrayListOfSelectedDate.get(k))) {
                        arrayListFromInsideSelectDateMethod.add(timeModelArrayListForAdmin.get(j));
                        break;
                    }
                }
            }
            brandNewArrayList.clear();
            brandNewArrayList.addAll(summingTimeFromDatePicker(arrayListFromInsideSelectDateMethod));
            arrayListFromInsideSelectDateMethod.clear();

            // TEN ADAPTER JEST ZACZYTYWANY PRZY ZAWĘŻENIU DAT
            adapterUserForAdmin = new AdapterUserForAdmin(AdminView.this, brandNewArrayList, this, listOfAllRecordsForUser);
            binding.recyclerViewCardy.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
            binding.recyclerViewCardy.setAdapter(adapterUserForAdmin);

            adapterUserForAdmin.notifyDataSetChanged();
        }

    }

    private ArrayList<TimeModelForDisplay> summingTimeFromDatePicker(LinkedList<TimeModel> arrayListFromInsideSelectDateMethod) {
        // INVOKED FOR EACH INDIVIDUAL USER, NOT FOR ALL USER
        ArrayList<TimeModelForDisplay> timeModels = new ArrayList<>();
        long settledHours = 0;
        long summedTime = 0;
        int summedMoney = 0;
        long leftHours = 0;


        for (int i = 0, j = 1; j < arrayListFromInsideSelectDateMethod.size(); i++, j++) {

            if ((arrayListFromInsideSelectDateMethod.get(i).getId().equals(arrayListFromInsideSelectDateMethod.get(j).getId()))) {
                summedMoney += arrayListFromInsideSelectDateMethod.get(i).getWithdrawnMoney();
                summedTime += arrayListFromInsideSelectDateMethod.get(i).getTimeOverallInLong();
                if (arrayListFromInsideSelectDateMethod.get(i).getMoneyOverall() == false) {
                    leftHours += arrayListFromInsideSelectDateMethod.get(i).getTimeOverallInLong();
                }
                if (arrayListFromInsideSelectDateMethod.get(i).getMoneyOverall()) {
                    settledHours += arrayListFromInsideSelectDateMethod.get(i).getTimeOverallInLong();
                }

                if (j == arrayListFromInsideSelectDateMethod.size() - 1) {

                    if (arrayListFromInsideSelectDateMethod.get(j).getMoneyOverall() == false) {
                        leftHours += arrayListFromInsideSelectDateMethod.get(j).getTimeOverallInLong();
                    }
                    if (arrayListFromInsideSelectDateMethod.get(j).getMoneyOverall() == true) {
                        settledHours += arrayListFromInsideSelectDateMethod.get(j).getTimeOverallInLong();
                    }

                    summedMoney += arrayListFromInsideSelectDateMethod.get(j).getWithdrawnMoney();

                    summedTime += arrayListFromInsideSelectDateMethod.get(j).getTimeOverallInLong();
                    TimeModelForDisplay timeModel = new TimeModelForDisplay();
                    timeModel.setUserName(arrayListFromInsideSelectDateMethod.get(i).getUserName());
                    timeModel.setTimeOverallInLong(summedTime);
                    timeModel.setId(arrayListFromInsideSelectDateMethod.get(i).getId());
                    timeModel.setTimeOverallInLongLefToSettle(leftHours);
                    timeModel.setWithdrawnMoney(summedMoney);
                    timeModels.add(timeModel);
                    leftHours = 0;
                    summedTime = 0;
                    summedMoney = 0;
                    settledHours = 0;
                }
            } else if (!(arrayListFromInsideSelectDateMethod.get(i).getId().equals(arrayListFromInsideSelectDateMethod.get(j).getId()))) {
                if (j < arrayListFromInsideSelectDateMethod.size() - 1) {

                    if (arrayListFromInsideSelectDateMethod.get(i).getMoneyOverall() == false) {
                        leftHours += arrayListFromInsideSelectDateMethod.get(i).getTimeOverallInLong();
                    }
                    if (arrayListFromInsideSelectDateMethod.get(i).getMoneyOverall()) {
                        settledHours += arrayListFromInsideSelectDateMethod.get(i).getTimeOverallInLong();
                    }

                    summedMoney += arrayListFromInsideSelectDateMethod.get(i).getWithdrawnMoney();
                    summedTime += arrayListFromInsideSelectDateMethod.get(i).getTimeOverallInLong();
                    TimeModelForDisplay timeModel = new TimeModelForDisplay();
                    timeModel.setUserName(arrayListFromInsideSelectDateMethod.get(i).getUserName());
                    timeModel.setId(arrayListFromInsideSelectDateMethod.get(i).getId());
                    timeModel.setTimeOverallInLong(summedTime);
                    timeModel.setWithdrawnMoney(summedMoney);
                    timeModel.setTimeOverallInLongLefToSettle(leftHours);
                    timeModels.add(timeModel);
                    summedTime = 0;
                    summedMoney = 0;
                    leftHours = 0;
                    settledHours = 0;
                } else if (j == arrayListFromInsideSelectDateMethod.size() - 1) {
                    TimeModelForDisplay[] timeModels1 = new TimeModelForDisplay[2];
                    timeModels1[0] = new TimeModelForDisplay();
                    timeModels1[1] = new TimeModelForDisplay();
                    summedTime += arrayListFromInsideSelectDateMethod.get(i).getTimeOverallInLong();
                    summedMoney += arrayListFromInsideSelectDateMethod.get(i).getWithdrawnMoney();


                    if (arrayListFromInsideSelectDateMethod.get(i).getMoneyOverall() == false) {
                        leftHours += arrayListFromInsideSelectDateMethod.get(i).getTimeOverallInLong();
                    }
                    if (arrayListFromInsideSelectDateMethod.get(i).getMoneyOverall()) {
                        settledHours += arrayListFromInsideSelectDateMethod.get(i).getTimeOverallInLong();
                    }


                    timeModels1[0].setUserName(arrayListFromInsideSelectDateMethod.get(i).getUserName());
                    timeModels1[0].setTimeOverallInLong(summedTime);
                    timeModels1[0].setWithdrawnMoney(summedMoney);
                    timeModels1[0].setTimeOverallInLongLefToSettle(leftHours);
                    timeModels1[0].setId(arrayListFromInsideSelectDateMethod.get(i).getId());
                    timeModels.add(timeModels1[0]);
                    summedTime = 0;
                    summedMoney = 0;
                    leftHours = 0;
                    settledHours = 0;
                    summedTime += arrayListFromInsideSelectDateMethod.get(j).getTimeOverallInLong();
                    summedMoney += arrayListFromInsideSelectDateMethod.get(j).getWithdrawnMoney();

                    if (arrayListFromInsideSelectDateMethod.get(j).getMoneyOverall() == false) {
                        leftHours += arrayListFromInsideSelectDateMethod.get(j).getTimeOverallInLong();
                    }
                    if (arrayListFromInsideSelectDateMethod.get(j).getMoneyOverall()) {
                        settledHours += arrayListFromInsideSelectDateMethod.get(j).getTimeOverallInLong();
                    }
                    timeModels1[1].setUserName(arrayListFromInsideSelectDateMethod.get(j).getUserName());
                    timeModels1[1].setTimeOverallInLong(summedTime);
                    timeModels1[1].setWithdrawnMoney(summedMoney);
                    timeModels1[1].setTimeOverallInLongLefToSettle(leftHours);
                    timeModels1[1].setId(arrayListFromInsideSelectDateMethod.get(i).getId());

                    timeModels.add(timeModels1[1]);
                    summedTime = 0;
                    summedMoney = 0;
                    leftHours = 0;
                    settledHours = 0;
                }
            }
        }
        return timeModels;

    }

    private void setupRecyclerView() {
        // TEN ADAPTER JEST ZACZYTYWANY PRZY PIERWSZYM URUCHOMIENIU
        adapterUserForAdmin = new AdapterUserForAdmin(AdminView.this, timeModelForDisplayArrayList, this, listOfAllRecordsForUser);
        binding.recyclerViewCardy.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        binding.recyclerViewCardy.setAdapter(adapterUserForAdmin);
    }

    private String formatDateWithMonthAndYear(Date date) {
        // d LLL - to na przykład "1 wrz" czyli 1 września.
        //SimpleDateFormat dateFormat = new SimpleDateFormat("d LLL", getResources().getConfiguration().locale);
        SimpleDateFormat dateFormat = new SimpleDateFormat("Dyyyy", getResources().getConfiguration().locale);
        return dateFormat.format(date);
    }

    private List<TimeModelForDisplay> summingTime(List<TimeModel> timeModelArrayList) {
        // INVOKED FOR EACH INDIVIDUAL USER, NOT FOR ALL USER
        List<TimeModelForDisplay> timeModels = new ArrayList<>();

        for (TimeModel timeModel:timeModelArrayList) {
//            Log.i("summing name",timeModel.getUserName());
            Log.i("summing documentId",timeModel.getDocumentId());
        }
        long sumTime = 0;
        double summedMoney = 0;
        long leftHours = 0;


        for (int i = 0; i < timeModelArrayList.size(); i++) {
            sumTime += timeModelArrayList.get(i).getTimeOverallInLong();
            summedMoney += timeModelArrayList.get(i).getWithdrawnMoney();
            summedMoney = UserOverall.round(summedMoney, 2);

            if (timeModelArrayList.get(i).getMoneyOverall() == false) {
                leftHours += timeModelArrayList.get(i).getTimeOverallInLong();
            }

        }


        TimeModelForDisplay timeModelForDisplay = new TimeModelForDisplay();
        if(timeModelArrayList.get(timeModelArrayList.size() - 1).getUserName().equals("Użytkownik nr 1"))
        {
            for (TimeModel timeModel:timeModelArrayList) {
                if(!timeModel.getUserName().equals("Użytkownik nr 1"))
                {
                    timeModelForDisplay.setUserName(timeModel.getUserName());
                    break;
                }
            }
        }else{
            timeModelForDisplay.setUserName(timeModelArrayList.get(timeModelArrayList.size() - 1).getUserName());
        }

        //Log.i("USERNAME FOR ADMIN",timeModelArrayList.get(timeModelArrayList.size() - 1).getUserName());
        //Log.i("------","-------");
        timeModelForDisplay.setId(timeModelArrayList.get(timeModelArrayList.size() - 1).getId());
        timeModelForDisplay.setTimeOverallInLong(sumTime);
        timeModelForDisplay.setWithdrawnMoney(summedMoney);
        timeModelForDisplay.setTimeOverallInLongLefToSettle(leftHours);

        Log.i("timeModelForDisplay",timeModelForDisplay.getUserName());
        Log.i("SummedMoney",summedMoney+"");
        Log.i("SummedTime",sumTime+"");
        Log.i("SummedLeftHours",leftHours+"");



        timeModels.add(timeModelForDisplay);
        return timeModels;
    }


    public void assignTimeModelForUser(String userId) {
        authViewModel.getAllTimeModelsForAdminSQL(userId).observe(AdminView.this, new Observer<List<TimeModel>>() {
            @Override
            public void onChanged(List<TimeModel> timeModels) {

                // Czyści wszystkie wpisy jeśli się powtórzą.
                if (!timeModelArrayListForAdmin.isEmpty() && !timeModels.isEmpty()) {
                    for (TimeModel timeModel : timeModelArrayListForAdmin) {
                        if (timeModels.get(0).getDocumentId().equals(timeModel.getDocumentId())) {
                            timeModelArrayListForAdmin.clear();
                            listOfAllRecordsForUser.clear();
                            timeModelForDisplayArrayList.clear();

                            break;
                        }
                    }
                }

                if (!timeModels.isEmpty()) {
                    timeModelArrayListForAdmin.addAll(timeModels);
                    listOfAllRecordsForUser.addAll(timeModels);
                    //2 //  ze względu na to ,że pobiera dużo list. Trzeba zrobić metodę ,która będzie porównawała listy i dodawała nowe bez dupilkatów.
                    // to działa tak że pobiera dla jednego użytkownika i potem dodaje. zrób Tak żeby nie dodawało tej samej listy.
                    timeModelForDisplayArrayList.addAll(summingTime(timeModels));
                    adapterUserForAdmin.notifyDataSetChanged();
                }
            }
        });
        setupRecyclerView();
    }
}