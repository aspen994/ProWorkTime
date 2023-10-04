package com.example.ogrdapp.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ogrdapp.model.TimeModel;
import com.example.ogrdapp.model.User;
import com.example.ogrdapp.repository.AuthRepository;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class AuthViewModel extends AndroidViewModel {

    private AuthRepository authRepository;
    private MutableLiveData<FirebaseUser> userData;
    private MutableLiveData<Boolean> loggedStatus;
    private MutableLiveData<TimeModel> timeModelMutableLiveData;
    private MutableLiveData<List<TimeModel>> timeModelListMutableLiveData;
    private MutableLiveData<Boolean> ifAdminMutableLiveData;
    private MutableLiveData<List<User>> timeModelArrayListOfUserMutableLiveData;
    private MutableLiveData<List<TimeModel>> timeForUserListMutableLiveData;


    public AuthViewModel(@NonNull Application application) {
        super(application);
        authRepository = new AuthRepository(application);
        userData = authRepository.getFirebaseUserMutableLiveData();
        timeModelMutableLiveData = authRepository.getFirebaseTimeModel();
        timeModelListMutableLiveData=authRepository.getTimeModelArrayListMutableLiveData();
        loggedStatus = authRepository.getUserLoggedMutableLiveData();
        ifAdminMutableLiveData = authRepository.getIfAdminMutableLiveData();
        timeModelArrayListOfUserMutableLiveData = authRepository.getUserArrayListOfUserMutableLiveData();
        timeForUserListMutableLiveData = authRepository.getTimeForUserListMutableLiveData();
    }

    public MutableLiveData<List<TimeModel>> getTimeForUserListMutableLiveData() {
        return timeForUserListMutableLiveData;
    }

    public MutableLiveData<List<User>> getTimeModelArrayListOfUserMutableLiveData() {
        return timeModelArrayListOfUserMutableLiveData;
    }

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

    public MutableLiveData<List<TimeModel>> getTimeModelListMutableLiveData() {
        return timeModelListMutableLiveData;
    }

    public MutableLiveData<Boolean> getIfAdminMutableLiveData() {
        return ifAdminMutableLiveData;
    }

    public void getTimeForUser(String userId)
    {
        authRepository.getTimeForUser(userId);
    }
    public void getUsersDataAssignedToAdmin()
    {
        authRepository.getUsersDataAssignedToAdmin();
    }

    public void saveTimeModelToFirebase(TimeModel timeModel)
    {
        authRepository.saveDataToFireBase(timeModel);
    }
    public TimeModel getUsernameAndSurname()
    {
        return authRepository.getUsernameAndSurname();
    }
    public void registerUser(String email, String password,String userName_send,String surName_send,String foreign_email)
    {
        authRepository.register(email, password,userName_send,surName_send,foreign_email);
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
