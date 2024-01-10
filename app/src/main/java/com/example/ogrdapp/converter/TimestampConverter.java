package com.example.ogrdapp.converter;

import androidx.room.ProvidedTypeConverter;
import androidx.room.TypeConverter;

import com.google.firebase.Timestamp;

import java.util.Date;

@ProvidedTypeConverter
public class TimestampConverter {

    @TypeConverter
    public static Timestamp timestamp(Long dateLong){
        return dateLong == null ? null: new Timestamp(new Date(dateLong));
    }

    @TypeConverter
    public static Long fromDate(Timestamp date){
        return date == null ? null : date.toDate().getTime();

    }

}
