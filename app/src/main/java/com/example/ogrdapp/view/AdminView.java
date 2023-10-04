package com.example.ogrdapp.view;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Pair;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ogrdapp.R;
import com.example.ogrdapp.TimeOverallAdapter;
import com.example.ogrdapp.databinding.ActivityAdminViewBinding;
import com.example.ogrdapp.model.TimeModel;
import com.example.ogrdapp.model.User;
import com.example.ogrdapp.viewmodel.AuthViewModel;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;

import java.util.ArrayList;
import java.util.List;

public class AdminView extends AppCompatActivity {

    TextView textView;
    EditText editText;
    Button calendar, approveBtn;
    private AuthViewModel authViewModel;
    private ArrayList<User> userModelArrayList;
    private ArrayList<TimeModel> timeModelArrayList;
    private RecyclerView recyclerView;
    private AdapterUserForAdmin adapterUserForAdmin;
    private ActivityAdminViewBinding binding;
    public static int i=0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminViewBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        timeModelArrayList = new ArrayList<>();
        userModelArrayList = new ArrayList<>();

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
                timeModelArrayList.addAll(summingTime((ArrayList<TimeModel>) timeModels));

                binding.recyclerViewAdmin.setHasFixedSize(true);
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(AdminView.this);
                binding.recyclerViewAdmin.setLayoutManager(linearLayoutManager);
                adapterUserForAdmin = new AdapterUserForAdmin(AdminView.this, timeModelArrayList);
                binding.recyclerViewAdmin.addItemDecoration(new DividerItemDecoration(AdminView.this, LinearLayout.VERTICAL));

                Log.i("getTimeForUser","INVOKED");
                for (User user: userModelArrayList) {
                    Log.i("USER: ", user.getUsername());
                }
                Log.i("Time model size: ",timeModels.size()+" ");


                    binding.recyclerViewAdmin.setAdapter(adapterUserForAdmin);
                    adapterUserForAdmin.notifyDataSetChanged();
            }
        });






        MaterialDatePicker materialDatePicker = MaterialDatePicker.Builder.dateRangePicker()
                .setSelection(Pair.create(MaterialDatePicker.thisMonthInUtcMilliseconds(),MaterialDatePicker.todayInUtcMilliseconds())).build();

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
      /*
        for (int i = 0,j=1; j < timeModelArrayList.size(); i++,j++) {
            Log.i("PRINT NAMES FROM: ", timeModelArrayList.get(i).getUserName());

            if(timeModelArrayList.get(i).getUserName().equals(timeModelArrayList.get(j).getUserName()))
            {
                sumTime += timeModelArrayList.get(i).getTimeOverallInLong();
                Log.i("RECORDS FOR JOHN",++k +"");
                Log.i("Name for adapter: ", timeModelArrayList.get(i).getUserName());
                Log.i("LONG FOR ADAPTER: ", timeModelArrayList.get(i).getTimeOverallInLong()+"");
            }
            //if((!(timeModelArrayList.get(i).getUserName().equals(timeModelArrayList.get(j).getUserName())))||((timeModelArrayList.size()-1)==j))
            if((!(timeModelArrayList.get(i).getUserName().equals(timeModelArrayList.get(j).getUserName())))){
                Log.i("LONG FOR ADAPTER: ", timeModelArrayList.get(i).getTimeOverallInLong()+"");
                sumTime += timeModelArrayList.get(i).getTimeOverallInLong();
                TimeModel timeModel = new TimeModel();
                timeModel.setUserName(timeModelArrayList.get(i).getUserName());
                //Log.i("Name for adapter: ", timeModelArrayList.get(i).getUserName());
                timeModel.setId(timeModelArrayList.get(i).getId());
                timeModel.setTimeOverallInLong(sumTime);
                timeModels.add(timeModel);

                sumTime=0;
                Log.i(((!timeModelArrayList.get(i).getUserName().equals(timeModelArrayList.get(j).getUserName()))) + " ","HELoo");
                Log.i("SIZE inside loop 2: ",((timeModelArrayList.size()-1)==j)+"");
                Log.i("Should invoked","here");
            }
            Log.i("SIZE inside loop: ",((timeModelArrayList.size()-1)==j)+"");
            Log.i("J value: ",j+"");
        }*/
        /*timeModel.setUserName(timeModelArrayList.get(0).getUserName());
        timeModel.setTimeOverallInLong(sumTime);
        timeModel.setId(timeModelArrayList.get(0).getId());
        timeModels.add(timeModel);*/
        //Toast.makeText(this, "List size inside: "+ timeModels.size(), Toast.LENGTH_SHORT).show();
        return timeModels;
    }



    private void assignUserToTime(String userId) {
        authViewModel.getTimeForUser(userId);
    }
}