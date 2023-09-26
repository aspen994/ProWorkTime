package com.example.ogrdapp.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.ogrdapp.model.TimeModel;
import com.example.ogrdapp.repository.AuthRepository;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class AuthViewModel extends AndroidViewModel {

    private AuthRepository authRepository;
    private MutableLiveData<FirebaseUser> userData;
    private MutableLiveData<Boolean> loggedStatus;
    private MutableLiveData<TimeModel> timeModelMutableLiveData;
    private MutableLiveData<List<TimeModel>> timeModelListMutableLiveData;

    public MutableLiveData<FirebaseUser> getUserData() {
        return userData;
    }

    public MutableLiveData<Boolean> getLoggedStatus() {
        return loggedStatus;
    }

    public MutableLiveData<TimeModel> getUsernameAndSurnameMB()
    {
        return timeModelMutableLiveData;
    }

    public AuthViewModel(@NonNull Application application) {
        super(application);
        authRepository = new AuthRepository(application);
        userData = authRepository.getFirebaseUserMutableLiveData();
        timeModelMutableLiveData = authRepository.getFirebaseTimeModel();
        timeModelListMutableLiveData=authRepository.getTimeModelArrayListMutableLiveData();
        loggedStatus = authRepository.getUserLoggedMutableLiveData();
    }

    public MutableLiveData<List<TimeModel>> getTimeModelListMutableLiveData() {
        return timeModelListMutableLiveData;
    }

    public void saveTimeModelToFirebase(TimeModel timeModel)
    {
        authRepository.saveDataToFireBase(timeModel);
    }
    public TimeModel getUsernameAndSurname()
    {
        return authRepository.getUsernameAndSurname();
    }
    public void registerUser(String email, String password,String userName_send,String surName_send)
    {
        authRepository.register(email, password,userName_send,surName_send);
    }

    public void signIn(String email, String password)
    {
        authRepository.signIn(email,password);
    }

    public void resetPassword(String email)
    {
        authRepository.resetPassword(email);
    }

    public void getData()
    {
        authRepository.getData();
    }
    public void signOut()
    {
        authRepository.singOut();
    }

    public String getUserId()
    {
        return authRepository.getUserId();
    }


}
