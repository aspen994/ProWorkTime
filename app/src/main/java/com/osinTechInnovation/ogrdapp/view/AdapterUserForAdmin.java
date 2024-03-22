package com.osinTechInnovation.ogrdapp.view;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.osinTechInnovation.ogrdapp.R;
import com.osinTechInnovation.ogrdapp.SelectPath;
import com.osinTechInnovation.ogrdapp.model.TimeModel;
import com.osinTechInnovation.ogrdapp.model.TimeModelForDisplay;
import com.osinTechInnovation.ogrdapp.utility.FormattedTime;

import java.io.Serializable;
import java.util.List;

public class AdapterUserForAdmin extends RecyclerView.Adapter<AdapterUserForAdmin.MyViewHolder>{

    private Context context;
    private FragmentActivity fragmentActivity;
    private List<TimeModelForDisplay> list;
    private View.OnClickListener onClickListener;
    private List<TimeModel> listOfAllRecordsForUser;


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


        if(holder.getAdapterPosition()%2==0) {
            holder.linearLayoutAdmin.setBackgroundColor(ContextCompat.getColor(context, R.color.blue_grey_400));
        }
        else {
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
                SelectPath selectPath = new SelectPath(context);
                selectPath.buildAlertDialog();

                selectPath.viewById.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(context, UserTimeTable.class);
                        intent.putExtra("Id",timeModel.getId());
                        intent.putExtra("listOfAllRecordsForUser",(Serializable) listOfAllRecordsForUser);

                        context.startActivity(intent);
                    }
                });

                selectPath.settleForWork.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startUserOverallActivity( timeModel.getUserName(), timeModel.getId(), listOfAllRecordsForUser);
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
        context.startActivity(i);

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
