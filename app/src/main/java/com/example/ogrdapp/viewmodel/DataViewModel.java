package com.example.ogrdapp.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.ogrdapp.repository.DataRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;

public class DataViewModel extends AndroidViewModel {

    private DataRepository dataRepository;
    private MutableLiveData<CollectionReference> firebaseUserDataMutableLiveData;
    private String collectionPath="Users";
    private FirebaseAuth firebaseAuth;

    public DataViewModel(@NonNull Application application) {
        super(application);
        dataRepository = new DataRepository(application);
        firebaseUserDataMutableLiveData = dataRepository.getFirebaseUserDataMutableLiveData();
        firebaseAuth = dataRepository.getFirebaseAuth();
    }

    public MutableLiveData<CollectionReference> getFirebaseUserDataMutableLiveData() {
        return firebaseUserDataMutableLiveData;
    }

    public FirebaseAuth getFirebaseAuth() {
        return firebaseAuth;
    }

    public void setCollectionPath(String collectionPath)
    {
        dataRepository.setCollectionPath(collectionPath);
    }

    public void saveUserData(String email, String username, String userSurname)
    {
        dataRepository.addUserToDB(email,username,userSurname);
    }


}
