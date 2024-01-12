package com.example.ogrdapp.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.ogrdapp.model.TimeModel;

import java.util.List;

@Dao
public interface TimeModelDAO {

    //@Insert
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert (TimeModel timeModel);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertAllCollection(List<TimeModel>timeModelList);

    @Delete
    void delete(TimeModel timeModel);

    @Query("SELECT * FROM TimeModel_table")
    LiveData<List<TimeModel>> getAllTimeModels();

    @Query("delete from TimeModel_table ")
    void deleteAllTimeModels();
}
