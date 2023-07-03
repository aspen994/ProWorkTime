package com.example.ogrdapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ogrdapp.model.TimeModel;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;

public class TimeOverallAdapter extends RecyclerView.Adapter<TimeOverallAdapter.MyViewHolder> {

    private Context context;
    private ArrayList<TimeModel> list;

    public TimeOverallAdapter(Context context, ArrayList<TimeModel> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public TimeOverallAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View inflate = layoutInflater.inflate(R.layout.adapter_user_time,parent,false);

        return new MyViewHolder(inflate);
    }

    @Override
    public void onBindViewHolder(@NonNull TimeOverallAdapter.MyViewHolder holder, int position) {
        holder.date.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
        holder.hourInDay.setText(list.get(position).getTimeOverall());
        holder.beginTime.setText(list.get(position).getTimeBegin());
        holder.endTime.setText(list.get(position).getTimeEnd());
        //holder.earnMoney.setText("200");
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        TextView date,beginTime, endTime,hourInDay;

    public  MyViewHolder (@NonNull View itemView) {
        super(itemView);

        date = itemView.findViewById(R.id.date);
        beginTime = itemView.findViewById(R.id.startTime);
        endTime = itemView.findViewById(R.id.endTime);
        hourInDay = itemView.findViewById(R.id.hoursInDay);
        //earnMoney = itemView.findViewById(R.id.money);
    }
}

}
