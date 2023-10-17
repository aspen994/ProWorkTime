package com.example.ogrdapp.view;

import android.graphics.Canvas;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Pair;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ogrdapp.databinding.ActivityAdminViewBinding;
import com.example.ogrdapp.model.TimeModel;
import com.example.ogrdapp.model.User;
import com.example.ogrdapp.utility.SwipeController;
import com.example.ogrdapp.utility.SwipeControllerActions;
import com.example.ogrdapp.viewmodel.AuthViewModel;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public class AdminView extends AppCompatActivity {

    TextView textView;
    EditText editText;
    Button calendar, approveBtn;
    private AuthViewModel authViewModel;
    private ArrayList<User> userModelArrayList;
    private ArrayList<TimeModel> timeModelArrayList;
    private ArrayList<TimeModel> timeModelArrayListForAdmin;

    private ArrayList<TimeModel> brandNewArrayList;
    private ArrayList<TimeModel> arrayListFromInsideSelectDateMethod;
    private RecyclerView recyclerView;
    private AdapterUserForAdmin adapterUserForAdmin;
    private ActivityAdminViewBinding binding;
    public static int i=0;
    SwipeController swipeController;
    Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminViewBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        swipeGestureToRecyclerView();


        binding.btnAproving.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(AdminView.this, "Hello", Toast.LENGTH_SHORT).show();
            }
        });

        //setupRecyclerView();

        timeModelArrayList = new ArrayList<>();
        userModelArrayList = new ArrayList<>();
        timeModelArrayListForAdmin = new ArrayList<>();
        brandNewArrayList= new ArrayList<>();
        arrayListFromInsideSelectDateMethod =new ArrayList<>();

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        binding.btnAproving.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                i = Integer.parseInt(binding.editTextRate.getText().toString());
                adapterUserForAdmin.notifyDataSetChanged();
            }
        });



        authViewModel.getUsersDataAssignedToAdmin();
        authViewModel.getTimeModelArrayListOfUserMutableLiveData().observe(this, new Observer<List<User>>() {
            @Override
            public void onChanged(List<User> user) {
                userModelArrayList.addAll(user);

                for(User users: userModelArrayList)
                {
                    assignUserToTime(users.getUserId());
                    Log.i("getTimeModel","INVOKED");
                }

            }
        });

        authViewModel.getTimeForUserListMutableLiveData().observe(this, new Observer<List<TimeModel>>() {
            @Override
            public void onChanged(List<TimeModel> timeModels) {
                timeModelArrayListForAdmin.addAll(timeModels);
                //2
                timeModelArrayList.addAll(summingTime((ArrayList<TimeModel>) timeModels));
                setupRecyclerView();
            }
        });







        MaterialDatePicker materialDatePicker = MaterialDatePicker.Builder.dateRangePicker()
                .setSelection(Pair.create(MaterialDatePicker.thisMonthInUtcMilliseconds(),MaterialDatePicker.todayInUtcMilliseconds())).build();
        materialDatePicker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener() {
            @Override
            public void onPositiveButtonClick(Object selection) {
                setDataForSelectDate(materialDatePicker.getHeaderText());
                //1
                //checkArrayListMethod();
            }
        });

        binding.btnDataPicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                materialDatePicker.show(getSupportFragmentManager(),"Tag_picker");
                materialDatePicker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener() {
                    @Override
                    public void onPositiveButtonClick(Object selection) {
                        binding.txtPickedDate.setText(materialDatePicker.getHeaderText());
                    }
                });
            }
        });

    }

    private void checkArrayListMethod() {
        for (TimeModel timeModel: timeModelArrayListForAdmin) {
            Log.i("UserName",timeModel.getUserName());
            Log.i("UserName",timeModel.getTimeAdded().toDate().toString());

        }
    }


    public void setDataForSelectDate(String fromDatePicker)
    {

        //1 split date for two different elements in array list.
        fromDatePicker = fromDatePicker.replaceAll(" ","").replaceAll("–"," ");

        String[] split = fromDatePicker.split(" ");

        Date[] daty = new Date[split.length];

        for (int j = 0; j <split.length ; j++) {
            try {
                daty[j] = new SimpleDateFormat("dLLL").parse(split[j]);
            } catch (ParseException e) {
                Log.i("EXCEPTION", e.getMessage());
                throw new RuntimeException(e);
            }
            Log.i("From String to Date",formatDateWithMonthAndYear(daty[j]) + "");
        }


        if(!timeModelArrayListForAdmin.isEmpty()) {

            for (int j=0; j < timeModelArrayListForAdmin.size();  j++) {
                Log.i("NAME FROM DATE",timeModelArrayListForAdmin.get(j).getUserName());
                if(Integer.valueOf(formatDateWithMonthAndYear(timeModelArrayListForAdmin.get(j).getTimeAdded().toDate()))>= Integer.valueOf(formatDateWithMonthAndYear(daty[0]))
                    && Integer.valueOf(formatDateWithMonthAndYear(timeModelArrayListForAdmin.get(j).getTimeAdded().toDate()))<= Integer.valueOf(formatDateWithMonthAndYear(daty[1])) ) {

                    arrayListFromInsideSelectDateMethod.add(timeModelArrayListForAdmin.get(j));
                    /*for (int k = Integer.valueOf(formatDateWithMonthAndYear(daty[0])); k < Integer.valueOf(formatDateWithMonthAndYear(daty[1]))
                            || j < timeModelArrayListForAdmin.size() ; k++, j++) {
                        arrayListFromInsideSelectDateMethod.add(timeModelArrayListForAdmin.get(j));
                        Log.i("HERE", timeModelArrayListForAdmin.get(j).getUserName());
                    }*/
                }
            }
            Log.i("CHECK FROM", arrayListFromInsideSelectDateMethod.size()+"");
            // albo nowa metoda albo tutaj warunkuj i bedzie git.

            brandNewArrayList.clear();
            brandNewArrayList.addAll(summingTimeFromDatePicker(arrayListFromInsideSelectDateMethod));
            arrayListFromInsideSelectDateMethod.clear();


            adapterUserForAdmin = new AdapterUserForAdmin(AdminView.this, brandNewArrayList);
            binding.recyclerViewCardy.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
            binding.recyclerViewCardy.setAdapter(adapterUserForAdmin);
            Toast.makeText(this, "invoked", Toast.LENGTH_SHORT).show();

            adapterUserForAdmin.notifyDataSetChanged();
        }
        else {
            Toast.makeText(this, "EMPTY !", Toast.LENGTH_SHORT).show();
        }
    }

    private ArrayList<TimeModel> summingTimeFromDatePicker(ArrayList<TimeModel> arrayListFromInsideSelectDateMethod) {
        // INVOKED FOR EACH INDIVIDUAL USER, NOT FOR ALL USER
        ArrayList<TimeModel> timeModels = new ArrayList<>();
        for (TimeModel time:arrayListFromInsideSelectDateMethod) {
            Log.i("USER FOR",time.getTimeOverallInLong()+"");
        }

        long summedTime=0;

        for (int i = 0,j=1; j< arrayListFromInsideSelectDateMethod.size(); i++,j++) {
            TimeModel timeModel = new TimeModel();
            if((arrayListFromInsideSelectDateMethod.get(i).getUserName().equals(arrayListFromInsideSelectDateMethod.get(j).getUserName()))
                    && j<arrayListFromInsideSelectDateMethod.size()-1 )
            {
                Log.i("Name 1 if",arrayListFromInsideSelectDateMethod.get(i).getTimeOverallInLong()+"");
                summedTime+= arrayListFromInsideSelectDateMethod.get(i).getTimeOverallInLong();
            }
            if((!(arrayListFromInsideSelectDateMethod.get(i).getUserName().equals(arrayListFromInsideSelectDateMethod.get(j).getUserName())))
                    &&j<arrayListFromInsideSelectDateMethod.size()-1 )
            {
                Log.i("Name 2 if",arrayListFromInsideSelectDateMethod.get(i).getTimeOverallInLong()+"");
            summedTime+=arrayListFromInsideSelectDateMethod.get(i).getTimeOverallInLong();
            timeModel.setUserName(arrayListFromInsideSelectDateMethod.get(i).getUserName());
            timeModel.setTimeOverallInLong(summedTime);
            timeModel.setTimeAdded(arrayListFromInsideSelectDateMethod.get(i).getTimeAdded());
            timeModels.add(timeModel);
            summedTime=0;
            }
            else if (j==arrayListFromInsideSelectDateMethod.size()-1)
            {

                Log.i("HELLO CAN YOU",arrayListFromInsideSelectDateMethod.get(j).getTimeOverallInLong()+"");
                summedTime+= arrayListFromInsideSelectDateMethod.get(j).getTimeOverallInLong();
                summedTime+= arrayListFromInsideSelectDateMethod.get(i).getTimeOverallInLong();

                if(arrayListFromInsideSelectDateMethod.get(j).getUserName().equals(arrayListFromInsideSelectDateMethod.get(i).getUserName()))
                {
                    Log.i("Name 3 if",arrayListFromInsideSelectDateMethod.get(j).getTimeOverallInLong()+"");
                    timeModel.setUserName(arrayListFromInsideSelectDateMethod.get(j).getUserName());
                    timeModel.setTimeAdded(arrayListFromInsideSelectDateMethod.get(j).getTimeAdded());
                    timeModel.setTimeOverallInLong(summedTime);
                    timeModels.add(timeModel);
                    Log.i("TO JEST","TO");
                }
                 if (!(arrayListFromInsideSelectDateMethod.get(j).getUserName().equals(arrayListFromInsideSelectDateMethod.get(j-1).getUserName()))){
                     Log.i("Name 4 if",arrayListFromInsideSelectDateMethod.get(j).getTimeOverallInLong()+"");
                    TimeModel timeModel1 = new TimeModel();
                    timeModel1.setUserName(arrayListFromInsideSelectDateMethod.get(j).getUserName());
                    timeModel1.setTimeOverallInLong(summedTime);
                    timeModel1.setTimeAdded(arrayListFromInsideSelectDateMethod.get(j).getTimeAdded());
                    timeModels.add(timeModel1);
                    Log.i("ALBO TO","TO");
                }

               Log.i("Which record",arrayListFromInsideSelectDateMethod.get(j).getTimeAdded().toDate()+"");
            }
            Log.i("Last record","Last ");
        }
        for (TimeModel timeModel2: timeModels) {
            Log.i("summingTime","summingTimeFromDatePicker");
            Log.i("summingTimeFromDatePicker",timeModel2.getUserName());
            Log.i("summingTimeFromDatePicker",timeModel2.getTimeOverallInLong()+"");
            Log.i("summingTimeFromDatePicker",timeModel2.getTimeAdded().toDate().toString());
            Log.i("summingTime","summingTimeFromDatePicker");

        }
        return timeModels;

        /*long sumTime=0;

        for (int i = 0; i < timeModelArrayList.size(); i++)
        {
            sumTime += timeModelArrayList.get(i).getTimeOverallInLong();
        }
        Log.i("SPACE","EMPTY ROW");
        Log.i("NAME FROM SUMMING:",timeModelArrayList.get(0).getUserName());
//        Log.i("TimeModelAddTime",formatDateWithMonthAndYear(timeModelArrayList.get(0).getTimeAdded().toDate())+"");
        TimeModel timeModel = new TimeModel();
        timeModel.setUserName(timeModelArrayList.get(timeModelArrayList.size()-1).getUserName());
        timeModel.setId(timeModelArrayList.get(timeModelArrayList.size()-1).getId());
        timeModel.setTimeOverallInLong(sumTime);

        timeModels.add(timeModel);

        return timeModels;
        return null;*/
    }

    private void setupRecyclerView() {

        adapterUserForAdmin = new AdapterUserForAdmin(AdminView.this, timeModelArrayList);
        binding.recyclerViewCardy.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        binding.recyclerViewCardy.setAdapter(adapterUserForAdmin);


    }

    private void swipeGestureToRecyclerView() {
        swipeController = new SwipeController(new SwipeControllerActions() {
            @Override
            public void onRightClicked(int position) {
                Toast.makeText(AdminView.this, "DELETE", Toast.LENGTH_SHORT).show();
                adapterUserForAdmin.notifyItemRemoved(position);
                adapterUserForAdmin.notifyItemRangeChanged(position, adapterUserForAdmin.getItemCount());
            }
        });

        ItemTouchHelper itemTouchhelper = new ItemTouchHelper(swipeController);
        itemTouchhelper.attachToRecyclerView(binding.recyclerViewCardy);

        binding.recyclerViewCardy.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
                swipeController.onDraw(c);
            }
        });
    }
    private String formatDateWithMonthAndYear(Date date)
    {
        // d LLL - to na przykład "1 wrz" czyli 1 września.
        //SimpleDateFormat dateFormat = new SimpleDateFormat("d LLL", getResources().getConfiguration().locale);
        SimpleDateFormat dateFormat = new SimpleDateFormat("D", getResources().getConfiguration().locale);
        return dateFormat.format(date);
    }

    private ArrayList<TimeModel> summingTime(ArrayList<TimeModel> timeModelArrayList) {
        // INVOKED FOR EACH INDIVIDUAL USER, NOT FOR ALL USER
        ArrayList<TimeModel> timeModels = new ArrayList<>();

        long sumTime=0;

        for (int i = 0; i < timeModelArrayList.size(); i++)
        {
        sumTime += timeModelArrayList.get(i).getTimeOverallInLong();
        }
        Log.i("SPACE","EMPTY ROW");
        Log.i("NAME FROM SUMMING:",timeModelArrayList.get(0).getUserName());
//        Log.i("TimeModelAddTime",formatDateWithMonthAndYear(timeModelArrayList.get(0).getTimeAdded().toDate())+"");
        TimeModel timeModel = new TimeModel();
        timeModel.setUserName(timeModelArrayList.get(timeModelArrayList.size()-1).getUserName());
        timeModel.setId(timeModelArrayList.get(timeModelArrayList.size()-1).getId());
        timeModel.setTimeOverallInLong(sumTime);

        timeModels.add(timeModel);

        return timeModels;
    }



    private void assignUserToTime(String userId) {
        authViewModel.getTimeForUser(userId);
    }
}