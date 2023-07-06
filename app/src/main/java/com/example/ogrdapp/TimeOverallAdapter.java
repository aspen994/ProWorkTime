package com.example.ogrdapp;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ogrdapp.model.TimeModel;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

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

        //Hiding the LinearLayout for summing up
        holder.linearLayoutLastRecord.setVisibility(View.GONE);
        holder.date.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
        holder.hourInDay.setText(list.get(position).getTimeOverall());
        holder.beginTime.setText(list.get(position).getTimeBegin());
        holder.endTime.setText(list.get(position).getTimeEnd());
        if(position==(getItemCount()-1))
        {

            long sum = 0;
            for(TimeModel timeModel : list)
            {
                sum += timeModel.getTimeOverallInLong();
            }
            //sum += list.get(position).getTimeOverallInLong();
            //Showhing the LinearLayout for summing up
            holder.linearLayoutLastRecord.setVisibility(View.VISIBLE);
            holder.linearLayoutLastRecord.setBackgroundColor(Color.YELLOW);
            holder.dateLastRecord.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
            holder.dateLastRecord.setTextColor(Color.BLACK);
            holder.hours_last_record.setText("H");
            holder.hours_last_record.setTextColor(Color.BLACK);


            String formattedTimeToLastRecord = formattedTime(sum);


            holder.hoursInDayLastRecord.setText(formattedTimeToLastRecord);
            holder.hoursInDayLastRecord.setTextColor(Color.BLACK);

        }
    }

    private String formattedTime(long sum) {
        long seconds = sum / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;

        seconds %= 60;
        minutes %= 60;

        String formattedTime = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        return formattedTime;
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        TextView date,beginTime, endTime,hourInDay,dateLastRecord,hoursInDayLastRecord,hours_last_record;
        LinearLayout linearLayoutLastRecord;

    public  MyViewHolder (@NonNull View itemView) {
        super(itemView);

        date = itemView.findViewById(R.id.date);
        beginTime = itemView.findViewById(R.id.startTime);
        endTime = itemView.findViewById(R.id.endTime);
        hourInDay = itemView.findViewById(R.id.hoursInDay);
        dateLastRecord = itemView.findViewById(R.id.date_last_record);
        hoursInDayLastRecord = itemView.findViewById(R.id.hoursInDay_last_record);
        linearLayoutLastRecord = itemView.findViewById(R.id.linearLayout);
        hours_last_record = itemView.findViewById(R.id.hours_last_record);
    }
}

}
