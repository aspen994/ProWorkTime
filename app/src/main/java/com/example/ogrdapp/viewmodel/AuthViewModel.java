package com.example.ogrdapp.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.ogrdapp.model.QRModel;
import com.example.ogrdapp.model.TimeModel;
import com.example.ogrdapp.model.User;
import com.example.ogrdapp.repository.AuthRepository;
import com.google.firebase.auth.FirebaseUser;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class AuthViewModel extends AndroidViewModel {

    private AuthRepository authRepository;
    private MutableLiveData<FirebaseUser> userData;
    private MutableLiveData<Boolean> loggedStatus;
    private MutableLiveData<TimeModel> timeModelMutableLiveData;
    private MutableLiveData<List<TimeModel>> timeModelListMutableLiveData;
    private MutableLiveData<Boolean> ifAdminMutableLiveData;
    private MutableLiveData<List<User>> userArrayListOfUserMutableLiveData;
    private MutableLiveData<List<TimeModel>> timeForUserListMutableLiveData;
    private MutableLiveData<Map<String, Object>> paycheckHoursToSettleMutableLiveData;
    private MutableLiveData<String> emailMutableLiveData;
    private MutableLiveData<String> adminIdMutableLiveData;
    private MutableLiveData<LinkedList<QRModel>>getQrModelMutableLiveData;



    public AuthViewModel(@NonNull Application application) {
        super(application);
        authRepository = new AuthRepository(application);
        userData = authRepository.getFirebaseUserMutableLiveData();
        timeModelMutableLiveData = authRepository.getFirebaseTimeModel();
        timeModelListMutableLiveData=authRepository.getTimeModelArrayListMutableLiveData();
        loggedStatus = authRepository.getUserLoggedMutableLiveData();
        ifAdminMutableLiveData = authRepository.getIfAdminMutableLiveData();
        userArrayListOfUserMutableLiveData = authRepository.getUserArrayListOfUserMutableLiveData();
        timeForUserListMutableLiveData = authRepository.getTimeForUserListMutableLiveData();
        paycheckHoursToSettleMutableLiveData = authRepository.getPaycheckHoursToSettleMutableLiveData();
        emailMutableLiveData = authRepository.getEmailMutableLiveData();
        adminIdMutableLiveData =authRepository.getAdminIdMutableLiveData();
        getQrModelMutableLiveData= authRepository.getQrModelMutableLiveData();

    }

    public MutableLiveData<LinkedList<QRModel>> getGetQrModelMutableLiveData() {
        return getQrModelMutableLiveData;
    }

    public MutableLiveData<String> getAdminIdMutableLiveData() {
        return adminIdMutableLiveData;
    }

    public MutableLiveData<String> getEmailMutableLiveData() {
        return emailMutableLiveData;
    }

    public MutableLiveData<Map<String, Object>> getPaycheckHoursToSettleMutableLiveData() {
        return paycheckHoursToSettleMutableLiveData;
    }

    public MutableLiveData<List<TimeModel>> getTimeForUserListMutableLiveData() {
        return timeForUserListMutableLiveData;
    }


    public MutableLiveData<List<User>> getUserArrayListOfUserMutableLiveData() {
        return userArrayListOfUserMutableLiveData;
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

    public void setNewQrCode(Map<String,Object> qrCodeMap)
    {
        authRepository.setNewQrCode(qrCodeMap);
    }

    public void updatedDataHoursToFirebaseUser(TimeModel timeModel)
    {
        authRepository.updatedDataHoursToFirebaseUser(timeModel);
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

    public void getSelectedTimeForUser(String userId, String dateRange)
    {
        authRepository.getSelectedTimeForUser(userId,dateRange);
    }


    public void deleteDateFromFireBase(String documentId)
    {
        authRepository.deleteDateFromFireBase(documentId);
    }
    public void updateDataToFirebase(String documentID,String beginTime,String endTime,String overall,long timeInLong)
    {
        authRepository.updateDataToFirebase(documentID,beginTime,endTime,overall,timeInLong);
    }

    public void updateStatusOfSettled(String documentID, boolean isSettled,double withDrawnMoney )
    {
        authRepository.updateStatusOfPayment(documentID,isSettled,withDrawnMoney);
    }

    public void updateStatusOfTimeForUser(String userId,long settletedTimeInMillis, double payCheck )
    {
        authRepository.updateStatusOfTimeForUser(userId,settletedTimeInMillis,payCheck);
    }

    public void getPaycheckAndHoursToSettleLong(String userId)
    {
        authRepository.getPaycheckAndHoursToSettleLong(userId);
    }

    public void getDataToUpdatePayCheck(String userId)
    {
        authRepository.getDataToUpdatePayCheck(userId);
    }

  /*  public void getAllIdDocumentFromTimeModel()
    {
        authRepository.getAllIdDocumentFromTimeModel();
    }
*/
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

    public void getDataQRCode(String adminId)
    {
        authRepository.getDataQRCode(adminId);
    }


}
