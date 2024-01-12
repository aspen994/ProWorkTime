package com.example.ogrdapp.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.ogrdapp.model.TimeModel;
import com.example.ogrdapp.repository.RoomRepository;

import java.util.List;

public class RoomViewModel extends AndroidViewModel {
    private final RoomRepository roomRepository;

    public RoomViewModel(@NonNull Application application) {
        super(application);
        this.roomRepository = new RoomRepository(application);
    }

    public LiveData<List<TimeModel>> getAllContacts()
    {
        return roomRepository.getAllTimeModels();
    }

    public void deleteAllCollection()
    {
        roomRepository.deleteAllTimeModels();
    }

    public void addTimeModel(TimeModel timeModel)
    {
        roomRepository.addTimeModel(timeModel);
    }

    public void deleteTimeModel(TimeModel timeModel)
    {
        roomRepository.deleteContact(timeModel);
    }


}
