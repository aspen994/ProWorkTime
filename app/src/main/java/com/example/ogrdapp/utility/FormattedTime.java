package com.example.ogrdapp.utility;

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

}
