package com.osinTechInnovation.ogrdapp.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.osinTechInnovation.ogrdapp.converter.TimestampConverter;
import com.osinTechInnovation.ogrdapp.dao.TimeModelDAO;
import com.osinTechInnovation.ogrdapp.model.TimeModel;

@Database(entities = {TimeModel.class}, version = 1)
@TypeConverters({TimestampConverter.class})
public abstract class TimeModelDatabase extends RoomDatabase {
    public abstract TimeModelDAO getTimeModelDao();

    private static TimeModelDatabase dbInstance;

    public static synchronized TimeModelDatabase getInstance(Context context){
        if(dbInstance==null)
        {
            dbInstance = Room.databaseBuilder(
                    context.getApplicationContext(),
                    TimeModelDatabase.class,
                    "timeModel_db").
                    fallbackToDestructiveMigration()
                    .build();

        }

        return dbInstance;
    }


}
