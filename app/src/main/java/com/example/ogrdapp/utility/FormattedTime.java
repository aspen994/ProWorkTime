package com.example.ogrdapp.utility;

import android.util.Log;

public class FormattedTime {

    public static String formattedTime(long sum) {
        long seconds = sum / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;

        seconds %= 60;
        minutes %= 60;

        String formattedTime = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        return formattedTime;
    }

    public static int formattedTimeInInt(long sum) {
        long seconds = sum / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;

        return (int)hours;
    }


    public static double formattedTimeInDoubleToSave(long sum) {

        double seconds = sum / 1000;
        double minutes = seconds / 60;
        double hours = minutes / 60;

        //Log.i("FormattedDate",hours+"");

     return  hours;

    }

    public static int formattedTimeInIntToPay(long sum) {

        long seconds = sum / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;

       // Log.i("Minutes",minutes%60+"");
        //Log.i("ITS INOVKED","ITS INOVKED");

       if(minutes%60>=31)
        {
            return (int) hours+1;
        }
        else {
            return (int) hours;
        }

    }




    public static String formattedTimeInHoursAndMinutes(int sum) {
        long hours = sum / 60;
        long minutes = sum%60;

        String formattedTime = String.format("%02d:%02d", hours, minutes);
        return formattedTime;
    }

}
