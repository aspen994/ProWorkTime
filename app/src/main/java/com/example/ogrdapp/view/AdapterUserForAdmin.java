package com.example.ogrdapp.view;

import static com.example.ogrdapp.view.AdminView.i;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ogrdapp.R;
import com.example.ogrdapp.SelectPath;
import com.example.ogrdapp.SettleForWork;
import com.example.ogrdapp.model.TimeModel;
import com.example.ogrdapp.model.TimeModelForDisplay;
import com.example.ogrdapp.model.User;
import com.example.ogrdapp.utility.FormattedTime;
import com.example.ogrdapp.viewmodel.AuthViewModel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AdapterUserForAdmin extends RecyclerView.Adapter<AdapterUserForAdmin.MyViewHolder>{

    private Context context;
    private FragmentActivity fragmentActivity;
    private List<TimeModelForDisplay> list;
    private View.OnClickListener onClickListener;
    private AuthViewModel authViewModel;
    List<TimeModel> listOfAllRecordsForUser;
//    List<User> userModelArrayList;


    public AdapterUserForAdmin(Context context, List<TimeModelForDisplay> list,
                               FragmentActivity fragmentActivity,List<TimeModel> listOfAllRecordsForUser) {
        this.context = context;
        this.list = list;
        this.fragmentActivity = fragmentActivity;
        this.listOfAllRecordsForUser = listOfAllRecordsForUser;
    }

    @NonNull
    @Override
    public AdapterUserForAdmin.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View inflate = layoutInflater.inflate(R.layout.adapter_user_for_admin,parent,false);

        return new AdapterUserForAdmin.MyViewHolder(inflate);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterUserForAdmin.MyViewHolder holder, int position) {

        TimeModelForDisplay timeModel = list.get(position);

        if(holder.getAdapterPosition()%2==0)
        {
            holder.linearLayoutAdmin.setBackgroundColor(ContextCompat.getColor(context, R.color.blue_grey_400));
        }else {
            holder.linearLayoutAdmin.setBackgroundColor(ContextCompat.getColor(context,R.color.blue_grey_500));
        }

        long settledHours = list.get(position).getTimeOverallInLong() - list.get(position).getTimeOverallInLongLefToSettle();

        holder.workerToAdapter.setText("Pracownik: "+list.get(position).getUserName());
        holder.workerTimeToAdapter.setText("Przepracowane godziny: " + FormattedTime.formattedTime(list.get(position).getTimeOverallInLong()));
        holder.hoursToSettleToAdapter.setText("Godziny do rozliczenia: " + FormattedTime.formattedTime(list.get(position).getTimeOverallInLongLefToSettle()));
        holder.settledHours.setText("Rozliczone godziny: " + FormattedTime.formattedTime(settledHours));
        holder.workerMoneyToWithdrawnToAdapter.setText(context.getString(R.string.money_paid) + list.get(position).getWithdrawnMoney() + " zł");


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(context, timeModel.getUserName(), Toast.LENGTH_SHORT).show();
                SelectPath selectPath = new SelectPath(context);
                //startUserOverallActivity(context,fragmentActivity,timeModel.getUserName(),timeModel.getId(),authViewModel,listOfAllRecordsForUser);
                //SettleForWork settleForWork  = new SettleForWork(context,fragmentActivity,timeModel.getUserName(),timeModel.getId(),authViewModel,listOfAllRecordsForUser);

                selectPath.buildAlertDialog();

                selectPath.viewById.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // TUTAJ zamienń jak w settleForWork
                        /*Intent intent = new Intent(context, UserTimeTable.class);
                        intent.putExtra("Id",timeModel.getId());
                        context.startActivity(intent);*/
                        Toast.makeText(context, timeModel.getUserName()+"here", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(context, UserTimeTable.class);
                        intent.putExtra("Id",timeModel.getId());
                        intent.putExtra("listOfAllRecordsForUser",(Serializable) listOfAllRecordsForUser);

                        context.startActivity(intent);
                    }
                });

                selectPath.settleForWork.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                      //  settleForWork.buildAlertDialog();
                        startUserOverallActivity( timeModel.getUserName(), timeModel.getId(), listOfAllRecordsForUser);
                        //selectPath.dismissBuildAlertDialog();
                    }
                });
            }
        });
    }

    private void startUserOverallActivity(String userName, String id, List<TimeModel> listOfAllRecordsForUser) {


        Intent i = new Intent(context, UserOverall.class);
        i.putExtra("List",(Serializable) listOfAllRecordsForUser);
        i.putExtra("UserName",userName);
        i.putExtra("Id",id);
        //i.putExtra("fragmentActivity",(Serializable) fragmentActivity);
        context.startActivity(i);

    }

    private double giveTheUserPaycheck(String id, List<User> userModelArrayList) {
        double paycheck =0;
        for(User user: userModelArrayList)
        {
            if(id.equals(user.getUserId()))
            {
                paycheck = user.getPaycheck();
                Log.i("PayCheck", user.getPaycheck()+"");
            }
        }

        return paycheck;
    }

    private String countingMoney(int timeOverallInLong) {
        int i1 = i * timeOverallInLong;
        return i1+" zł";
    }


    public Set<String> getUniqueElements()
    {
        List<String> list1 = new ArrayList<>();
        Set<String> unique = new HashSet<String>(list1);
        for (int i = 0; i < list.size(); i++) {
            list1.add(list.get(i).getUserName());
        }
        unique.addAll(list1);


        return unique;
    }

    private long getTimeOverall() {
        long sum = 0;
        for (int i = 0; i < list.size(); i++) {

            sum+=list.get(i).getTimeOverallInLong();
        }

        return sum;
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
    public void setOnClickListener(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }
    public interface onClickListener {
        void onClick(int position, TimeModel timeModel);
    }


    public class MyViewHolder extends RecyclerView.ViewHolder{

        TextView workerToAdapter,workerTimeToAdapter,workerMoneyToWithdrawnToAdapter, hoursToSettleToAdapter,settledHours;
        ViewGroup  linearLayoutAdmin;

        public  MyViewHolder (@NonNull View itemView) {
            super(itemView);
            linearLayoutAdmin = itemView.findViewById(R.id.linearLayout_admin_to_adapter);
            workerToAdapter = itemView.findViewById(R.id.worker_to_adapter);
            workerTimeToAdapter = itemView.findViewById(R.id.worker_time_to_adapter);
            workerMoneyToWithdrawnToAdapter= itemView.findViewById(R.id.worker_money_toWithdrawn_to_adapter);
            hoursToSettleToAdapter = itemView.findViewById(R.id.hours_to_settle);
            settledHours = itemView.findViewById(R.id.settled_hours);
        }
    }
}
