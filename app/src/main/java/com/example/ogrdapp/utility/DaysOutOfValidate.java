package com.example.ogrdapp.utility;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.google.android.material.datepicker.CalendarConstraints;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

public class DaysOutOfValidate implements CalendarConstraints.DateValidator{

    int mYear, mMonth, mDayOfWeek;
    List<Calendar> blockedDatesFromDatabase;

    public DaysOutOfValidate(List<Calendar> blockedDatesFromDatabase) {
        this.blockedDatesFromDatabase = blockedDatesFromDatabase;
    }
    DaysOutOfValidate(Parcel parcel) {
        mYear = parcel.readInt();
        mMonth = parcel.readInt();
        mDayOfWeek = parcel.readInt();
    }
    @Override
    public boolean isValid(long date) {
        Calendar calendarInput = Calendar.getInstance();
        calendarInput.setTimeInMillis(date);

        for (Calendar blockedDate : blockedDatesFromDatabase) {
            if (areSameDay(calendarInput, blockedDate)) {
                return false; // If it matches one of the blocked dates, return false.
            }
        }

        return true; // If there is no match, the date is valid.
    }



    private boolean areSameDay(Calendar cal1, Calendar cal2) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");


        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeInt(mYear);
        parcel.writeInt(mMonth);
        parcel.writeInt(mDayOfWeek);
    }

    public static final Parcelable.Creator<DaysOutOfValidate> CREATOR = new Parcelable.Creator<DaysOutOfValidate>() {

        @Override
        public DaysOutOfValidate createFromParcel(Parcel parcel) {
            return new DaysOutOfValidate(parcel);
        }

        @Override
        public DaysOutOfValidate[] newArray(int size) {
            return new DaysOutOfValidate[size];
        }
    };
}
