package com.osinTechInnovation.ogrdapp;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.osinTechInnovation.ogrdapp.model.TimeModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

public class TimeOverallAdapter extends RecyclerView.Adapter<TimeOverallAdapter.MyViewHolder> {

    private Context context;
    private ArrayList<TimeModel> list;
    long timeOverallInLong = 0;


    public TimeOverallAdapter(Context context, ArrayList<TimeModel> list) {
        this.context = context;
        this.list = list;


        Collections.sort(list, new Comparator<TimeModel>() {
            @Override
            public int compare(TimeModel o1, TimeModel o2) {
                return o1.getTimeAdded().compareTo(o2.getTimeAdded());
            }
        });
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


        //Hiding the LinearLayout for last record of the day
        holder.linearLayoutLastRecord.setVisibility(View.VISIBLE);

        //Setting highlights for last record on Color Green
        holder.linearLayoutLastRecord.setBackgroundColor(Color.GREEN);
        holder.dateLastRecord.setTextColor(Color.BLACK);
        holder.hoursInDayLastRecord.setTextColor(Color.BLACK);
        holder.hours_last_record.setTextColor(Color.BLACK);

        //Hiding the LinearLayout for last record of the month
        holder.linearLayoutSummingRecord.setVisibility(View.GONE);

        //Getting date from getTimeAdded
        Date date = list.get(position).getTimeAdded().toDate();
        String dateFormatted = formattedDate(date);
        holder.dateLastRecord.setText(dateFormatted);

        holder.hours_last_record.setText(R.string.hours_shortcut);
        holder.noteSettled.setText(list.get(position).getMoneyOverall()?context.getString(R.string.billed):"");

        //Setting date
        holder.date.setText(formattedDate(date));

        //Setting lasting time of the current record in String
        holder.hourInDay.setText(list.get(position).getTimeOverall());
        holder.beginTime.setText(list.get(position).getTimeBegin());
        holder.endTime.setText(list.get(position).getTimeEnd());

        // Geting variable string for loop in 87 line and loop 104
        String dateLastRecordDate = holder.dateLastRecord.getText().toString();

        // Hiding LinearLayout for the Last record
        for (int i = 0; i < position; i++) {

            Date date1 = list.get(i).getTimeAdded().toDate();
            String s = formattedDate(date1);

            // Checking if the last record with date is qual to date of the holder.
            // Before in line 71 it is set for the first time for linear layout last record
            // This hiding when it repeats
            if(dateLastRecordDate.equals(s))
            {
                holder.linearLayoutLastRecord.setVisibility(View.GONE);

            }

        }


        if(View.VISIBLE==holder.linearLayoutLastRecord.getVisibility()) {
            for (int i = 0; i < list.size() ; i++) {
                if (dateLastRecordDate.equals(formattedDate(list.get(i).getTimeAdded().toDate()))) {
                    timeOverallInLong += list.get(i).getTimeOverallInLong();
                    holder.hoursInDayLastRecord.setText(formattedTime(timeOverallInLong));
                }
            }
            //This can broke a code WATCHOUT !!
            holder.dateLastRecord.setText(formattedDateWithNameDay(list.get(position).getTimeAdded().toDate()));
            timeOverallInLong=0;
        }

        // Setting for the last position of the arrayList, a summing Linear Layout
        // When will be more months could be a problem
        if(holder.getAdapterPosition()==(list.size()-1))
        {
            holder.linearLayoutSummingRecord.setVisibility(View.VISIBLE);
            holder.linearLayoutSummingRecord.setBackgroundColor(Color.YELLOW);
            holder.hoursInMonthSumming.setTextColor(Color.BLACK);
            holder.hoursSumming.setTextColor(Color.BLACK);
            holder.dateSumming.setTextColor(Color.BLACK);

            holder.dateSumming.setText(formatDateWithMonth(list.get(holder.getAdapterPosition()).getTimeAdded().toDate()));
            holder.hoursSumming.setText(R.string.hours_shortcut);
            long suma = 0;
            for (int i = 0; i < list.size(); i++) {
                suma += list.get(i).getTimeOverallInLong();
            }
            //textView.setText((suma/3600000)*moneyMultiplier+" zł");
            holder.hoursInMonthSumming.setText(formattedTime(suma));
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

    private String formatDateWithMonth(Date date)
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat("LLLL yyyy", context.getResources().getConfiguration().locale);
        return dateFormat.format(date);
    }

    private String formattedDate(Date date)
    {

        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yy",context.getResources().getConfiguration().locale);

        return format.format(date);
    }

    private String formattedDateWithNameDay(Date date)
    {

        SimpleDateFormat format = new SimpleDateFormat("EEEE dd/MM/yy",context.getResources().getConfiguration().locale);

        return format.format(date);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        TextView date,beginTime, endTime,hourInDay,dateLastRecord,hoursInDayLastRecord,hours_last_record,dateSumming,hoursInMonthSumming,hoursSumming, noteSettled;
        LinearLayout linearLayoutLastRecord,linearLayoutSummingRecord;

    public  MyViewHolder (@NonNull View itemView) {
        super(itemView);

        // for each time
        date = itemView.findViewById(R.id.date);
        beginTime = itemView.findViewById(R.id.startTime);
        endTime = itemView.findViewById(R.id.endTime);
        hourInDay = itemView.findViewById(R.id.hoursInDay);
        noteSettled = itemView.findViewById(R.id.note_settled);

        // sum for each day
        linearLayoutLastRecord = itemView.findViewById(R.id.linearLayout);
        dateLastRecord = itemView.findViewById(R.id.date_last_record);
        hoursInDayLastRecord = itemView.findViewById(R.id.hoursInDay_last_record);
        hours_last_record = itemView.findViewById(R.id.hours_last_record);

        // sum for month
        linearLayoutSummingRecord = itemView.findViewById(R.id.summinglinearLayout);
        dateSumming = itemView.findViewById(R.id.date_summing);
        hoursInMonthSumming = itemView.findViewById(R.id.hoursInDay_summing);
        hoursSumming = itemView.findViewById(R.id.hours_summing);

    }
}

}
