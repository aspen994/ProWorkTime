package com.osinTechInnovation.ogrdapp.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.osinTechInnovation.ogrdapp.model.TimeModel;

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

    @Update
    void updated(TimeModel timeModel);
    @Update(onConflict = OnConflictStrategy.IGNORE)
    void updateList(List<TimeModel>list);

    @Query("SELECT * FROM TimeModel_table WHERE id =:currentUser")
    LiveData<List<TimeModel>> getAllTimeModels(String currentUser);

    @Query("SELECT * FROM TimeModel_table WHERE id =:currentUser")
    List<TimeModel> getAllTimeModelsList(String currentUser);


    @Query("delete from TimeModel_table WHERE id=:userId")
    void deleteListForTimeModel(String userId);
}
