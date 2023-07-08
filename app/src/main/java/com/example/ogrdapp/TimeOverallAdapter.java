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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class TimeOverallAdapter extends RecyclerView.Adapter<TimeOverallAdapter.MyViewHolder> {

    private Context context;
    private ArrayList<TimeModel> list;
    int positionOfTheNextDay =0;
    int previousPosition = 0;

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
        holder.linearLayoutSummingRecord.setVisibility(View.GONE);
        //Getting date from getTimeAdded
        Date date = list.get(position).getTimeAdded().toDate();

        holder.date.setText(formattedDate(date));
        holder.hourInDay.setText(list.get(position).getTimeOverall());
        holder.beginTime.setText(list.get(position).getTimeBegin());
        holder.endTime.setText(list.get(position).getTimeEnd());

        list.get(position).getTimeAdded().toDate().getDay();




        // Comparing days
        if(list.size()>=2&& position<list.size()-1) {

            // Comparing work with this format
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
            Date first = list.get(position).getTimeAdded().toDate();
            Date second = list.get((position+1)).getTimeAdded().toDate();



            // 1 Comparing days, if the next day is different than current day, make summing
            if(!(simpleDateFormat.format(first).equals(simpleDateFormat.format(second))))
            {

                // Assigning the position of the next day.
                positionOfTheNextDay = position + 1;
                Log.i("Position !!",position+"");

                //Showhing the LinearLayout for last record of the day
                holder.linearLayoutLastRecord.setVisibility(View.VISIBLE);
                holder.linearLayoutLastRecord.setBackgroundColor(Color.GREEN);

                Date date1 = list.get(position).getTimeAdded().toDate();
                holder.dateLastRecord.setText(formattedDateWithNameDay(date1));

                holder.dateLastRecord.setTextColor(Color.WHITE);
                holder.hours_last_record.setText("H");
                holder.hours_last_record.setTextColor(Color.WHITE);


                // summing time in the last day
                long sum = 0;
                for(int i = previousPosition;i<=position;i++)
                {
                    sum += list.get(i).getTimeOverallInLong();
                }

                previousPosition= positionOfTheNextDay-previousPosition;

                String formattedTimeToLastRecord = formattedTime(sum);


                holder.hoursInDayLastRecord.setText(formattedTimeToLastRecord);
                holder.hoursInDayLastRecord.setTextColor(Color.WHITE);

            }
        }

        //Month overall
        if(position==(getItemCount()-1))
        {

            // Summing for all the record in ArrayList
            long sum = 0;
            for(TimeModel timeModel : list)
            {
                sum += timeModel.getTimeOverallInLong();
            }

            holder.linearLayoutSummingRecord.setVisibility(View.VISIBLE);
            holder.linearLayoutSummingRecord.setBackgroundColor(Color.YELLOW);


            // Formatting date to "LIPIEC 2023" and seting to last record
            SimpleDateFormat dateFormat = new SimpleDateFormat("LLLL yyyy", Locale.getDefault());
            Date date1 = list.get(position).getTimeAdded().toDate();


            holder.dateSumming.setText(dateFormat.format(date1));
            holder.dateSumming.setTextColor(Color.BLACK);
            holder.hoursSumming.setText("H");
            holder.hoursSumming.setTextColor(Color.BLACK);


            String formattedTimeToLastRecord = formattedTime(sum);


            holder.hoursInMonthSumming.setText(formattedTimeToLastRecord);
            holder.hoursInMonthSumming.setTextColor(Color.BLACK);

            // Checking if list size is more than 1 cous I need make a comparssion.
            // Checking in if(position==(getItemCount()-1)) cous I need to make comparssion in the last record
            if(list.size()>1) {

                //summing for the day
                // Work with this format
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
                //Daj warunkowość
                Date first = list.get(position).getTimeAdded().toDate();
                Date second = list.get((position - 1)).getTimeAdded().toDate();


                if ((simpleDateFormat.format(first).equals(simpleDateFormat.format(second)))) {


                    holder.linearLayoutLastRecord.setBackgroundColor(Color.GREEN);
                    holder.linearLayoutLastRecord.setVisibility(View.VISIBLE);

                    Date date2 = list.get(position).getTimeAdded().toDate();
                    holder.dateLastRecord.setText(formattedDateWithNameDay(date1));

                    holder.dateLastRecord.setTextColor(Color.WHITE);
                    holder.hours_last_record.setText("H");
                    holder.hours_last_record.setTextColor(Color.WHITE);

                }

                // summing for the current day
                long sum2 = 0;
                for (int i = positionOfTheNextDay; i < list.size(); i++) {
                    sum2 += list.get(i).getTimeOverallInLong();

                }

                String formattedTimeToLastRecord2 = formattedTime(sum2);


                holder.hoursInDayLastRecord.setText(formattedTimeToLastRecord2);
                holder.hoursInDayLastRecord.setTextColor(Color.WHITE);
            }
            else {
                holder.linearLayoutLastRecord.setBackgroundColor(Color.GREEN);
                holder.linearLayoutLastRecord.setVisibility(View.VISIBLE);

                Date date2 = list.get(position).getTimeAdded().toDate();
                holder.dateLastRecord.setText(formattedDateWithNameDay(date1));

                holder.dateLastRecord.setTextColor(Color.WHITE);
                holder.hours_last_record.setText("H");
                holder.hours_last_record.setTextColor(Color.WHITE);

                long sum3 = 0;
                for (int i = positionOfTheNextDay; i < list.size(); i++) {
                    sum3 += list.get(i).getTimeOverallInLong();

                }

                String formattedTimeToLastRecord2 = formattedTime(sum3);


                holder.hoursInDayLastRecord.setText(formattedTimeToLastRecord2);
                holder.hoursInDayLastRecord.setTextColor(Color.WHITE);

            }

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

    private String formattedDate(Date date)
    {

        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yy",Locale.getDefault());

        return format.format(date);
    }

    private String formattedDateWithNameDay(Date date)
    {

        SimpleDateFormat format = new SimpleDateFormat("EEEE dd/MM/yy",Locale.getDefault());

        return format.format(date);
    }
    @Override
    public int getItemCount() {
        return list.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        TextView date,beginTime, endTime,hourInDay,dateLastRecord,hoursInDayLastRecord,hours_last_record,dateSumming,hoursInMonthSumming,hoursSumming;
        LinearLayout linearLayoutLastRecord,linearLayoutSummingRecord;

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

        linearLayoutSummingRecord = itemView.findViewById(R.id.summinglinearLayout);
        dateSumming = itemView.findViewById(R.id.date_summing);
        hoursInMonthSumming = itemView.findViewById(R.id.hoursInDay_summing);
        hoursSumming = itemView.findViewById(R.id.hours_summing);

    }
}

}
