package com.example.ogrdapp.view;

import static com.example.ogrdapp.view.UserOverall.isWithdrawn;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
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
import com.example.ogrdapp.model.TimeModelForDisplay;
import com.example.ogrdapp.model.User;
import com.example.ogrdapp.utility.SwipeController;
import com.example.ogrdapp.utility.SwipeControllerActions;
import com.example.ogrdapp.viewmodel.AuthViewModel;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.lang.reflect.Type;
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
    public static int i=0;
    public static boolean isDownloaded;
    SwipeController swipeController;
    public static final String TAG_ADMIN_VIEW ="ADMIN VIEW";
    String jsonString2;


    @Override
    protected void onPause() {
        SharedPreferences preferences = getSharedPreferences("UserTimeTableSharedPreferences", MODE_PRIVATE);
        preferences.edit().remove("timeModelArrayList").commit();

        super.onPause();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if(isWithdrawn)
        {
            finish();
        }
        SharedPreferences sharedPreferences1 = getSharedPreferences("UserTimeTableSharedPreferences",MODE_PRIVATE);
        jsonString2 = sharedPreferences1.getString("timeModelArrayList",null);

        Log.i("onRestart Jstrong",jsonString2==null?"true":"false");


        if(jsonString2!=null)
        {
            Gson gson1 = new Gson();
            ArrayList<TimeModel> timeModels;
            Type type = new TypeToken<ArrayList<TimeModel>>(){}.getType();
            timeModels = gson1.fromJson(jsonString2,type);

            findTimeModelForDisplayToUpdateAndClearIt(timeModels.get(0).getId());
            timeModelForDisplayArrayList.addAll(summingTime(timeModels));
            setupRecyclerView();
        }


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminViewBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        timeModelForDisplayArrayList = new LinkedList<>();
        //Czyszczę bo każda kolejne zmiany potem dodają wiecej użytkowników w ADMIN VIEW
        //timeModelArrayList.clear();

        userModelArrayList = new LinkedList<>();
        //userModelArrayList.clear();

        timeModelArrayListForAdmin = new LinkedList<>();
        brandNewArrayList= new LinkedList<>();
        arrayListFromInsideSelectDateMethod =new LinkedList<>();
        listOfAllRecordsForUser = new LinkedList<>();

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        //WSTAW METODĘ KTÓRA BĘDZIE NASŁUCHIWAĆ ZMIANY

        //TODO 08.01.2024r SPRWADZAM CZY WYŚWIETLI MI TYLKO JEDEN ELEMENT DODANY
        authViewModel.checkMethod();


        Intent intent = getIntent();

        SharedPreferences sharedPreferences1 = getSharedPreferences("MySharedPrefForList",MODE_PRIVATE);
        jsonString2 = sharedPreferences1.getString("LIST_KEY","");

        // tak żeby czytało listę z SharedPref
      /*  if(jsonString2 !=null)
        {
            readTimeModelForDisplayToSharedPref();
        }*/
        if(intent.hasExtra("USER_ID"))
        {
            String userId = intent.getStringExtra("USER_ID");
            Log.i("userId",userId);

            //SharedPreferences - reading
            readTimeModelForDisplayToSharedPref();

            findTimeModelForDisplayToUpdateAndClearIt(userId);
            authViewModel.getTimeForUser(userId);
            Log.i("USER_ID","Invoked");

        }
        else {//(jsonString2==null)

            Log.i("Download FB", "Firebase");
            authViewModel.getUsersDataAssignedToAdmin();
            authViewModel.getUserArrayListOfUserMutableLiveData().observe(this, new Observer<List<User>>() {
                @Override
                public void onChanged(List<User> user) {
                    userModelArrayList.addAll(user);

                    for(User users: userModelArrayList)
                    {
                        assignUserToTimeModel(users.getUserId());
                    }
                }
            });
        }

        authViewModel.getTimeForUserListMutableLiveData().observe(this, new Observer<List<TimeModel>>() {
            @Override
            public void onChanged(List<TimeModel> timeModels) {

                timeModelArrayListForAdmin.addAll(timeModels);
                listOfAllRecordsForUser.addAll(timeModels);

                //2 //  ze względu na to ,że pobiera dużo list. Trzeba zrobić metodę ,która będzie porównawała listy i dodawała nowe bez dupilkatów.
                // to działa tak że pobiera dla jednego użytkownika i potem dodaje. zrób Tak żeby nie dodawało tej samej listy.
                timeModelForDisplayArrayList.addAll(summingTime((ArrayList<TimeModel>) timeModels));
                readForLogcat(timeModels);
                writeTimeModelForDisplayToSharedPref();
                setupRecyclerView();
            }
        });


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

    private void readForLogcat(List<TimeModel> timeModels) {
        for (TimeModel timeModel : timeModels) {
            Log.i("For Logcat U",timeModel.getUserName());
            Log.i("For Logcat S",timeModel.getTimeOverallInLong()+"");
            Log.i("For Logcat DId",timeModel.getDocumentId());
        }
    }

    private void readTimeModelForDisplayToSharedPref() {
        SharedPreferences sharedPreferences1 = getSharedPreferences("MySharedPrefForList",MODE_PRIVATE);
        jsonString2 = sharedPreferences1.getString("LIST_KEY","");

        Gson gson1 = new Gson();
        Type type = new TypeToken<ArrayList<TimeModelForDisplay>>(){}.getType();
        timeModelForDisplayArrayList = gson1.fromJson(jsonString2,type);
    }

    private void writeTimeModelForDisplayToSharedPref() {
        Gson gson = new Gson();
        String jsonString = gson.toJson(timeModelForDisplayArrayList);

        SharedPreferences sharedPreferences =getSharedPreferences("MySharedPrefForList",MODE_PRIVATE);

        SharedPreferences.Editor myEdit = sharedPreferences.edit();
        myEdit.putString("LIST_KEY",jsonString);
        myEdit.commit();
        myEdit.apply();
    }

    private void findTimeModelForDisplayToUpdateAndClearIt(String userId) {
        for (int j = 0; j < timeModelForDisplayArrayList.size(); j++) {
            Log.i("FIND_TIMEMODEL",timeModelForDisplayArrayList.get(j).getUserName());
            if(timeModelForDisplayArrayList.get(j).getId().equals(userId))
            {
                timeModelForDisplayArrayList.remove(j);
            }
        }
    }

    public void setDataForSelectDate(String fromDatePicker)
    {

        //1 split date for two different elements in array list
        String[] split = fromDatePicker.split("-");
        //Toast.makeText(this, "HERE", Toast.LENGTH_SHORT).show();

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

            // TEN ADAPTER JEST ZACZYTYWANY PRZY ZAWĘŻENIU DAT
            Toast.makeText(this, "Przy zawężaniu dat", Toast.LENGTH_SHORT).show();

            adapterUserForAdmin = new AdapterUserForAdmin(AdminView.this, brandNewArrayList,this, listOfAllRecordsForUser);
            binding.recyclerViewCardy.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
            binding.recyclerViewCardy.setAdapter(adapterUserForAdmin);

            adapterUserForAdmin.notifyDataSetChanged();
        }

    }

    private ArrayList<TimeModelForDisplay> summingTimeFromDatePicker(LinkedList<TimeModel> arrayListFromInsideSelectDateMethod) {
        // INVOKED FOR EACH INDIVIDUAL USER, NOT FOR ALL USER
        ArrayList<TimeModelForDisplay> timeModels = new ArrayList<>();
        long settledHours=0;
        long summedTime=0;
        int summedMoney=0;
        long leftHours=0;



        for (int i = 0,j=1; j < arrayListFromInsideSelectDateMethod.size(); i++,j++) {

            if((arrayListFromInsideSelectDateMethod.get(i).getId().equals(arrayListFromInsideSelectDateMethod.get(j).getId())))
            {
                summedMoney +=arrayListFromInsideSelectDateMethod.get(i).getWithdrawnMoney();
                Log.i("HERE CHECK","CHECK");
                summedTime += arrayListFromInsideSelectDateMethod.get(i).getTimeOverallInLong();
                if(arrayListFromInsideSelectDateMethod.get(i).getMoneyOverall()==false)
                {
                    leftHours+=arrayListFromInsideSelectDateMethod.get(i).getTimeOverallInLong();
                }
                 if(arrayListFromInsideSelectDateMethod.get(i).getMoneyOverall())
                {
                    settledHours+=arrayListFromInsideSelectDateMethod.get(i).getTimeOverallInLong();
                }

                if(j== arrayListFromInsideSelectDateMethod.size()-1)
                {

                    if(arrayListFromInsideSelectDateMethod.get(j).getMoneyOverall()==false)
                    {
                        leftHours+=arrayListFromInsideSelectDateMethod.get(j).getTimeOverallInLong();
                    }
                    if(arrayListFromInsideSelectDateMethod.get(j).getMoneyOverall()==true)
                    {
                        settledHours+=arrayListFromInsideSelectDateMethod.get(j).getTimeOverallInLong();
                    }

                    summedMoney +=arrayListFromInsideSelectDateMethod.get(j).getWithdrawnMoney();
                    Log.i("Summed Money", summedMoney+"");

                    summedTime += arrayListFromInsideSelectDateMethod.get(j).getTimeOverallInLong();
                    TimeModelForDisplay timeModel = new TimeModelForDisplay();
                    timeModel.setUserName(arrayListFromInsideSelectDateMethod.get(i).getUserName());
                    timeModel.setTimeOverallInLong(summedTime);
                    timeModel.setId(arrayListFromInsideSelectDateMethod.get(i).getId());
                    timeModel.setTimeOverallInLongLefToSettle(leftHours);
                    timeModel.setWithdrawnMoney(summedMoney);
                    timeModels.add(timeModel);
                    leftHours=0;
                    summedTime=0;
                    summedMoney=0;
                    settledHours=0;
                }
            }

            else if(!((arrayListFromInsideSelectDateMethod.get(i).getUserName().equals(arrayListFromInsideSelectDateMethod.get(j).getUserName()))))
            {
                if(j<arrayListFromInsideSelectDateMethod.size()-1) {

                    if(arrayListFromInsideSelectDateMethod.get(i).getMoneyOverall()==false)
                    {
                        leftHours+=arrayListFromInsideSelectDateMethod.get(i).getTimeOverallInLong();
                    }
                    if(arrayListFromInsideSelectDateMethod.get(i).getMoneyOverall())
                    {
                        settledHours+=arrayListFromInsideSelectDateMethod.get(i).getTimeOverallInLong();
                    }

                    summedMoney +=arrayListFromInsideSelectDateMethod.get(i).getWithdrawnMoney();
                    summedTime += arrayListFromInsideSelectDateMethod.get(i).getTimeOverallInLong();
                    TimeModelForDisplay timeModel = new TimeModelForDisplay();
                    timeModel.setUserName(arrayListFromInsideSelectDateMethod.get(i).getUserName());
                    timeModel.setId(arrayListFromInsideSelectDateMethod.get(i).getId());
                    timeModel.setTimeOverallInLong(summedTime);
                    timeModel.setWithdrawnMoney(summedMoney);
                    timeModel.setTimeOverallInLongLefToSettle(leftHours);
                    timeModels.add(timeModel);
                    summedTime = 0;
                    summedMoney=0;
                    leftHours=0;
                    settledHours=0;
                }
                else if(j==arrayListFromInsideSelectDateMethod.size()-1)
                {
                    TimeModelForDisplay [] timeModels1 = new TimeModelForDisplay[2];
                    timeModels1[0] = new TimeModelForDisplay();
                    timeModels1[1] = new TimeModelForDisplay();
                    summedTime += arrayListFromInsideSelectDateMethod.get(i).getTimeOverallInLong();
                    summedMoney +=arrayListFromInsideSelectDateMethod.get(i).getWithdrawnMoney();



                    if(arrayListFromInsideSelectDateMethod.get(i).getMoneyOverall()==false)
                    {
                        leftHours+=arrayListFromInsideSelectDateMethod.get(i).getTimeOverallInLong();
                    }
                    if(arrayListFromInsideSelectDateMethod.get(i).getMoneyOverall())
                    {
                        settledHours+=arrayListFromInsideSelectDateMethod.get(i).getTimeOverallInLong();
                    }


                    timeModels1[0].setUserName(arrayListFromInsideSelectDateMethod.get(i).getUserName());
                    timeModels1[0].setTimeOverallInLong(summedTime);
                    timeModels1[0].setWithdrawnMoney(summedMoney);
                    timeModels1[0].setTimeOverallInLongLefToSettle(leftHours);
                    timeModels1[0].setId(arrayListFromInsideSelectDateMethod.get(i).getId());
                    timeModels.add(timeModels1[0]);
                    summedTime = 0;
                    summedMoney=0;
                    leftHours=0;
                    settledHours=0;
                    summedTime += arrayListFromInsideSelectDateMethod.get(j).getTimeOverallInLong();
                    summedMoney +=arrayListFromInsideSelectDateMethod.get(j).getWithdrawnMoney();

                    if(arrayListFromInsideSelectDateMethod.get(j).getMoneyOverall()==false)
                    {
                        leftHours+=arrayListFromInsideSelectDateMethod.get(j).getTimeOverallInLong();
                    }
                    if(arrayListFromInsideSelectDateMethod.get(j).getMoneyOverall())
                    {
                        settledHours+=arrayListFromInsideSelectDateMethod.get(j).getTimeOverallInLong();
                    }
                    timeModels1[1].setUserName(arrayListFromInsideSelectDateMethod.get(j).getUserName());
                    timeModels1[1].setTimeOverallInLong(summedTime);
                    timeModels1[1].setWithdrawnMoney(summedMoney);
                    timeModels1[1].setTimeOverallInLongLefToSettle(leftHours);
                    timeModels1[1].setId(arrayListFromInsideSelectDateMethod.get(i).getId());

                 timeModels.add(timeModels1[1]);
                 summedTime=0;
                 summedMoney=0;
                 leftHours=0;
                 settledHours=0;
                }
            }
        }
        return timeModels;

    }

    private void setupRecyclerView() {
        // TEN ADAPTER JEST ZACZYTYWANY PRZY PIERWSZYM URUCHOMIENIU

        adapterUserForAdmin = new AdapterUserForAdmin(AdminView.this, timeModelForDisplayArrayList,this, listOfAllRecordsForUser);
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

    private ArrayList<TimeModelForDisplay> summingTime(ArrayList<TimeModel> timeModelArrayList) {
        // INVOKED FOR EACH INDIVIDUAL USER, NOT FOR ALL USER
        ArrayList<TimeModelForDisplay> timeModels = new ArrayList<>();

        long sumTime=0;
        double summedMoney = 0;
        long leftHours = 0;


        for (int i = 0; i < timeModelArrayList.size(); i++)
        {
        sumTime += timeModelArrayList.get(i).getTimeOverallInLong();
        summedMoney+= timeModelArrayList.get(i).getWithdrawnMoney();

        summedMoney=UserOverall.round(summedMoney,2);



        if(timeModelArrayList.get(i).getMoneyOverall()==false)
        {
            leftHours += timeModelArrayList.get(i).getTimeOverallInLong();
            /*Log.i("HERE","Should be here");
            Log.i("LeftHours", leftHours+"");*/
        }
        /*Log.i("Counter from Summing",i+"");
        Log.i("Value of time ",timeModelArrayList.get(i).getTimeOverallInLong()+"");
        Log.i("Username ",timeModelArrayList.get(i).getUserName()+"");
        Log.i("Document ID ",timeModelArrayList.get(i).getDocumentId()+"");
        Log.i("-----","----");*/
        }
        /*Log.i("Sum of Time",sumTime+"");
        Log.i("/////","/////");*/


        TimeModelForDisplay timeModel = new TimeModelForDisplay();
        timeModel.setUserName(timeModelArrayList.get(timeModelArrayList.size()-1).getUserName());
        timeModel.setId(timeModelArrayList.get(timeModelArrayList.size()-1).getId());
        timeModel.setTimeOverallInLong(sumTime);
        timeModel.setWithdrawnMoney(summedMoney);
        timeModel.setTimeOverallInLongLefToSettle(leftHours);

        timeModels.add(timeModel);
        return timeModels;
    }



    private void assignUserToTimeModel(String userId) {
        authViewModel.getTimeForUser(userId);
    }
}