package com.example.ogrdapp.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.ogrdapp.model.QRModel;
import com.example.ogrdapp.model.TimeModel;
import com.example.ogrdapp.model.User;
import com.example.ogrdapp.repository.AuthRepository;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;
import java.util.Map;

public class AuthViewModel extends AndroidViewModel {

    private AuthRepository authRepository;
    private MutableLiveData<FirebaseUser> userData;
    private MutableLiveData<Boolean> loggedStatus;
    private MutableLiveData<TimeModel> timeModelMutableLiveData;
    private MutableLiveData<Boolean> ifAdminMutableLiveData;
    private MutableLiveData<List<User>> userArrayListOfUserMutableLiveData;
    private MutableLiveData<List<TimeModel>> timeForUserListMutableLiveData;
    private MutableLiveData<Map<String, Object>> paycheckHoursToSettleMutableLiveData;
    private MutableLiveData<String> emailMutableLiveData;
    private MutableLiveData<String> adminIdMutableLiveData;
    private MutableLiveData<List<TimeModel>> timeModelListMutableLiveData;

    private MutableLiveData<List<TimeModel>> getAllTimeModelsForAdminSQLLiveData;
    private MutableLiveData<Integer> getIntegerHelps;




    public AuthViewModel(@NonNull Application application) {
        super(application);
        authRepository = new AuthRepository(application);
        userData = authRepository.getFirebaseUserMutableLiveData();
        timeModelMutableLiveData = authRepository.getGetUsernameAndSurname();
        loggedStatus = authRepository.getUserLoggedMutableLiveData();
        ifAdminMutableLiveData = authRepository.getIfAdminMutableLiveData();
        userArrayListOfUserMutableLiveData = authRepository.getUserArrayListOfUserMutableLiveData();
        timeForUserListMutableLiveData = authRepository.getTimeForUserListMutableLiveData();
        paycheckHoursToSettleMutableLiveData = authRepository.getPaycheckHoursToSettleMutableLiveData();
        emailMutableLiveData = authRepository.getEmailMutableLiveData();
        adminIdMutableLiveData =authRepository.getAdminIdMutableLiveData();
        timeModelListMutableLiveData=authRepository.getTimeModelArrayListMutableLiveData();
        getAllTimeModelsForAdminSQLLiveData = authRepository.getGetAllTimeModelsForAdminSQLLiveData();
        this.getIntegerHelps = authRepository.getGetIntegerHelps();
    }

    public MutableLiveData<Integer> getGetIntegerHelps() {
        return getIntegerHelps;
    }

    public MutableLiveData<List<TimeModel>> getGetAllTimeModelsForAdminSQLLiveData() {
        return getAllTimeModelsForAdminSQLLiveData;
    }



    public MutableLiveData<List<TimeModel>> getTimeModelListMutableLiveData() {
        return timeModelListMutableLiveData;
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

    public MutableLiveData<Boolean> getIfAdminMutableLiveData() {
        return ifAdminMutableLiveData;
    }

    public void getTimeForUser(String userId)
    {
        authRepository.getTimeForUserNewMethod(userId);
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

    public void saveAllCollectionToSQLite(List<TimeModel>timeModelList)
    {
        authRepository.saveAllCollectionToSQLite(timeModelList);
    }

    // zwróć liveData
    public LiveData<TimeModel> getUsernameAndSurname()
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


    public void deleteDateFromFireBase(TimeModel timeModel)
    {
        authRepository.deleteDateFromFireBase(timeModel);
    }
    public void updateDataToFirebase(String documentID,String beginTime,String endTime,String overall,long timeInLong,TimeModel timeModel)
    {
        authRepository.updateDataToFirebase(documentID,beginTime,endTime,overall,timeInLong,timeModel);
    }

    public void updateStatusOfSettled(String documentID, boolean isSettled, double withDrawnMoney, Timestamp timestamp)
    {
        authRepository.updateStatusOfPayment(documentID,isSettled,withDrawnMoney,timestamp);
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



    public LiveData<List<TimeModel>> getAllTimeModelsForUserSQL()
    {
        return authRepository.getAllTimeModelsForUserSQL();
    }

    public LiveData<List<TimeModel>> getAllTimeModelsForAdminSQL(String userId)
    {
        return authRepository.getAllTimeModelsForAdminSQL(userId);
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
        //TODO 170124
       // authRepository.getDataFirebase();
    }
    public void signOut()
    {
        authRepository.singOut();
    }

    public String getUserId()
    {
        return authRepository.getUserId();
    }

    public LiveData<List<QRModel>> getDataQRCode(String adminId)
    {
        return authRepository.getDataQRCode(adminId);
    }

    public void checkMethod(){
        authRepository.checkMethod();
    }

    public void deleteLastRecordsMETHODTODELTE()
    {
       // authRepository.deleteLastRecordsMETHODTODELTE();
        authRepository.deleteAllTimeModels();
    }




}
