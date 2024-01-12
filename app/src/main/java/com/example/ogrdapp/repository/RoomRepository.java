package com.example.ogrdapp.repository;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.example.ogrdapp.dao.TimeModelDAO;
import com.example.ogrdapp.db.TimeModelDatabase;
import com.example.ogrdapp.model.TimeModel;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class RoomRepository {
    private final TimeModelDAO timeModelDAO;
    private ExecutorService executor;
    private Handler handler;

    public RoomRepository(Application application) {

        TimeModelDatabase timeModelDatabase = TimeModelDatabase.getInstance(application);
        this.timeModelDAO = timeModelDatabase.getTimeModelDao();

        // Used for Background Database Operations
        executor = Executors.newSingleThreadExecutor();

        // Used for updating the UI
        handler = new Handler(Looper.getMainLooper());
    }

    public void addTimeModel(TimeModel timeModel)
    {

        // Runnable: Executing Tasks on Separate Thread
        executor.execute(new Runnable() {
            @Override
            public void run() {
                timeModelDAO.insert(timeModel);

                Log.i("SUCCESFULLY ADDED",timeModel.toString());
            }
        });

    }

    public void deleteContact(TimeModel timeModel)
    {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                timeModelDAO.delete(timeModel);
            }
        });

    }

    public LiveData<List<TimeModel>> getAllTimeModels()
    {
        return timeModelDAO.getAllTimeModels();
    }

    public void deleteAllTimeModels()
    {
        executor.execute(()->timeModelDAO.deleteAllTimeModels());
    }
}
