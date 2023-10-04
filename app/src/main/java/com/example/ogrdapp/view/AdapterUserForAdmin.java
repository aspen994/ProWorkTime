package com.example.ogrdapp.view;

import static com.example.ogrdapp.view.AdminView.i;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ogrdapp.R;
import com.example.ogrdapp.model.TimeModel;
import com.example.ogrdapp.utility.FormattedTime;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AdapterUserForAdmin extends RecyclerView.Adapter<AdapterUserForAdmin.MyViewHolder>{
    private Context context;
    private ArrayList<TimeModel> list;

    public AdapterUserForAdmin(Context context, ArrayList<TimeModel> list) {
        this.context = context;
        this.list = list;
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

        Log.i("LIST SIZE ADAPTER: ", list.size()+"");


        if(holder.getAdapterPosition()%2==0)
        {
            holder.linearLayoutAdmin.setBackgroundColor(ContextCompat.getColor(context, R.color.blue_grey_400));
        }else {
            holder.linearLayoutAdmin.setBackgroundColor(ContextCompat.getColor(context,R.color.blue_grey_500));
        }

        holder.workerToAdapter.setText(list.get(position).getUserName());
        holder.workerTimeToAdapter.setText(FormattedTime.formattedTime(list.get(position).getTimeOverallInLong()));
        holder.workerMoneyEarnOverallToAdapter.setText(countingMoney(FormattedTime.formattedTimeInInt(list.get(position).getTimeOverallInLong())));
        holder.workerMoneyToWithdrawnToAdapter.setText(countingMoney(FormattedTime.formattedTimeInInt(list.get(position).getTimeOverallInLong())));

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

    public class MyViewHolder extends RecyclerView.ViewHolder{

        TextView workerToAdapter,workerTimeToAdapter,workerMoneyToWithdrawnToAdapter,workerMoneyWithdrawnToAdapter,workerMoneyEarnOverallToAdapter;
        ViewGroup  linearLayoutAdmin;

        public  MyViewHolder (@NonNull View itemView) {
            super(itemView);
            linearLayoutAdmin = itemView.findViewById(R.id.linearLayout_admin_to_adapter);
            workerToAdapter = itemView.findViewById(R.id.worker_to_adapter);
            workerTimeToAdapter = itemView.findViewById(R.id.worker_time_to_adapter);
            workerMoneyToWithdrawnToAdapter= itemView.findViewById(R.id.worker_money_toWithdrawn_to_adapter);
            workerMoneyWithdrawnToAdapter = itemView.findViewById(R.id.worker_money_withdrawn_to_adapter);
            workerMoneyEarnOverallToAdapter = itemView.findViewById(R.id.worker_money_earn_overall_to_adapter);

        }
    }
}
