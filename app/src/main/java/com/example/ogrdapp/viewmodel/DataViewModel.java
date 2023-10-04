package com.example.ogrdapp.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.ogrdapp.repository.DataRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;

public class DataViewModel extends AndroidViewModel {

    public int rate_money=0;
    public MutableLiveData<Integer> rateMoneyMutableLiveData;

    public DataViewModel(@NonNull Application application) {
        super(application);
        rateMoneyMutableLiveData = new MutableLiveData<>();
    }

}
