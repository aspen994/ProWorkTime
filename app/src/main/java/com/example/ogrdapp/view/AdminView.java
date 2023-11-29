package com.example.ogrdapp.view;

import android.graphics.Canvas;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

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
import com.google.firebase.Timestamp;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class AdminView extends AppCompatActivity {

    private AuthViewModel authViewModel;
    private ArrayList<User> userModelArrayList;
    private ArrayList<TimeModel> timeModelArrayList;
    private ArrayList<TimeModel> timeModelArrayListForAdmin;

    private ArrayList<TimeModel> brandNewArrayList;
    private ArrayList<TimeModel> arrayListFromInsideSelectDateMethod;

    private AdapterUserForAdmin adapterUserForAdmin;
    private ActivityAdminViewBinding binding;
    public static int i=0;
    SwipeController swipeController;
    public static final String TAG_ADMIN_VIEW ="ADMIN VIEW";

    public List<TimeModel> listaDlaSettleWork;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminViewBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        swipeGestureToRecyclerView();


        timeModelArrayList = new ArrayList<>();
        userModelArrayList = new ArrayList<>();
        timeModelArrayListForAdmin = new ArrayList<>();
        brandNewArrayList= new ArrayList<>();
        arrayListFromInsideSelectDateMethod =new ArrayList<>();
        listaDlaSettleWork = new ArrayList<>();

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
                }

            }
        });

        authViewModel.getTimeForUserListMutableLiveData().observe(this, new Observer<List<TimeModel>>() {
            @Override
            public void onChanged(List<TimeModel> timeModels) {

                timeModelArrayListForAdmin.addAll(timeModels);
                listaDlaSettleWork.addAll(timeModels);

                Log.i("HOW MANY","HELLO");
                for(TimeModel x: timeModelArrayListForAdmin)
                {
                    Log.i(TAG_ADMIN_VIEW,x.getUserName());
                    Log.i(TAG_ADMIN_VIEW,x.getId());
                    Log.i(TAG_ADMIN_VIEW,x.getTimeOverallInLong()+" ");
                    x.setMoneyOverall("HELLO");
                    Log.i(TAG_ADMIN_VIEW,x.getMoneyOverall());
                    Log.i(TAG_ADMIN_VIEW,x.getTimeAdded().toDate().toString());
                    Log.i("-----","-----");

                }

                //2
                timeModelArrayList.addAll(summingTime((ArrayList<TimeModel>) timeModels));

        /*        for(TimeModel x: timeModelArrayList)
                {
                    Log.i(TAG_ADMIN_VIEW,x.getUserName());
                    Log.i(TAG_ADMIN_VIEW,x.getTimeOverallInLong()+" ");
                }*/



                setupRecyclerView();
            }
        });

        /*MaterialDatePicker.Builder builder = MaterialDatePicker.Builder.datePicker();
        builder.setSelection(Pair.create(MaterialDatePicker.thisMonthInUtcMilliseconds(),MaterialDatePicker.todayInUtcMilliseconds()));

        MaterialDatePicker <Long> materialDatePicker = builder.build();*/

        MaterialDatePicker materialDatePicker = MaterialDatePicker.Builder.dateRangePicker()
                .setSelection(Pair.create(MaterialDatePicker.thisMonthInUtcMilliseconds(),MaterialDatePicker.todayInUtcMilliseconds())).build();
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


                setDataForSelectDate(stringBuilder.toString());

                Log.i("SHOW ME THE VALUE",Timestamp.now().toDate() +"");

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

    public void setDataForSelectDate(String fromDatePicker)
    {

        //1 split date for two different elements in array list
        String[] split = fromDatePicker.split("-");


        Date[] daty = new Date[2];
        try {
            daty[0]= new SimpleDateFormat("ddMMyyyy").parse(split[0]);
            daty[1]= new SimpleDateFormat("ddMMyyyy").parse(split[1]);
        }
        catch (ParseException e)
        {
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

        if(!timeModelArrayListForAdmin.isEmpty()) {
            for (int j=0; j < timeModelArrayListForAdmin.size();  j++) {
                for (int k = 0; k < arrayListOfSelectedDate.size(); k++) {
                    if(formatDateWithMonthAndYear(timeModelArrayListForAdmin.get(j).getTimeAdded().toDate()).equals(arrayListOfSelectedDate.get(k)))
                    {
                        arrayListFromInsideSelectDateMethod.add(timeModelArrayListForAdmin.get(j));
                        break;
                    }
                }
            }

            // albo nowa metoda albo tutaj warunkuj i bedzie git.

            brandNewArrayList.clear();
            brandNewArrayList.addAll(summingTimeFromDatePicker(arrayListFromInsideSelectDateMethod));
            arrayListFromInsideSelectDateMethod.clear();



            adapterUserForAdmin = new AdapterUserForAdmin(AdminView.this, brandNewArrayList,this,authViewModel,listaDlaSettleWork);
            binding.recyclerViewCardy.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
            binding.recyclerViewCardy.setAdapter(adapterUserForAdmin);

            adapterUserForAdmin.notifyDataSetChanged();
        }

    }

    private ArrayList<TimeModel> summingTimeFromDatePicker(ArrayList<TimeModel> arrayListFromInsideSelectDateMethod) {
        // INVOKED FOR EACH INDIVIDUAL USER, NOT FOR ALL USER
        ArrayList<TimeModel> timeModels = new ArrayList<>();
        long summedTime=0;

        for (int i = 0,j=1; j < arrayListFromInsideSelectDateMethod.size(); i++,j++) {
            if((arrayListFromInsideSelectDateMethod.get(i).getUserName().equals(arrayListFromInsideSelectDateMethod.get(j).getUserName())))
            {
                summedTime += arrayListFromInsideSelectDateMethod.get(i).getTimeOverallInLong();
                if(j== arrayListFromInsideSelectDateMethod.size()-1)
                {
                    summedTime += arrayListFromInsideSelectDateMethod.get(j).getTimeOverallInLong();
                    TimeModel timeModel = new TimeModel();
                    timeModel.setUserName(arrayListFromInsideSelectDateMethod.get(i).getUserName());
                    timeModel.setTimeOverallInLong(summedTime);
                    timeModel.setId(arrayListFromInsideSelectDateMethod.get(i).getId());
                    timeModels.add(timeModel);
                    summedTime=0;
                }
            }
            else if(!((arrayListFromInsideSelectDateMethod.get(i).getUserName().equals(arrayListFromInsideSelectDateMethod.get(j).getUserName()))))
            {
                if(j<arrayListFromInsideSelectDateMethod.size()-1) {
                    summedTime += arrayListFromInsideSelectDateMethod.get(i).getTimeOverallInLong();
                    TimeModel timeModel = new TimeModel();
                    timeModel.setUserName(arrayListFromInsideSelectDateMethod.get(i).getUserName());
                    timeModel.setId(arrayListFromInsideSelectDateMethod.get(i).getId());
                    timeModel.setTimeOverallInLong(summedTime);
                    timeModels.add(timeModel);
                    summedTime = 0;
                }
                else if(j==arrayListFromInsideSelectDateMethod.size()-1)
                {
                 TimeModel [] timeModels1 = new TimeModel[2];
                 timeModels1[0] = new TimeModel();
                 timeModels1[1] = new TimeModel();
                 summedTime += arrayListFromInsideSelectDateMethod.get(i).getTimeOverallInLong();
                    timeModels1[0].setUserName(arrayListFromInsideSelectDateMethod.get(i).getUserName());
                    timeModels1[0].setTimeOverallInLong(summedTime);
                    timeModels1[0].setId(arrayListFromInsideSelectDateMethod.get(i).getId());
                 timeModels.add(timeModels1[0]);
                 summedTime = 0;
                 summedTime += arrayListFromInsideSelectDateMethod.get(j).getTimeOverallInLong();
                    timeModels1[1].setUserName(arrayListFromInsideSelectDateMethod.get(j).getUserName());
                    timeModels1[1].setTimeOverallInLong(summedTime);
                    timeModels1[1].setId(arrayListFromInsideSelectDateMethod.get(i).getId());
                 timeModels.add(timeModels1[1]);
                 summedTime=0;
                }
            }
        }
        return timeModels;

    }

    private void setupRecyclerView() {

        adapterUserForAdmin = new AdapterUserForAdmin(AdminView.this, timeModelArrayList,this,authViewModel,listaDlaSettleWork);
        binding.recyclerViewCardy.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        binding.recyclerViewCardy.setAdapter(adapterUserForAdmin);


    }

    private void swipeGestureToRecyclerView() {
        swipeController = new SwipeController(new SwipeControllerActions() {
            @Override
            public void onRightClicked(int position) {
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
        SimpleDateFormat dateFormat = new SimpleDateFormat("Dyyyy", getResources().getConfiguration().locale);
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