package com.osinTechInnovation.ogrdapp.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.google.protobuf.ByteString;
import com.osinTechInnovation.ogrdapp.model.QRModel;
import com.osinTechInnovation.ogrdapp.model.TimeModel;
import com.osinTechInnovation.ogrdapp.model.User;
import com.osinTechInnovation.ogrdapp.repository.AuthRepository;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseUser;
import com.osinTechInnovation.ogrdapp.utility.DecodeDaysAndEntries;

import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class AuthViewModel extends AndroidViewModel {

    private AuthRepository authRepository;
    private MutableLiveData<FirebaseUser> userData;
    private MutableLiveData<Boolean> loggedStatus;
    private MutableLiveData<User> timeModelMutableLiveData;
    private MutableLiveData<Boolean> ifAdminMutableLiveData;
    private MutableLiveData<List<User>> userArrayListOfUserMutableLiveData;
    private MutableLiveData<List<TimeModel>> timeForUserListMutableLiveData;
    private MutableLiveData<Map<String, Object>> paycheckHoursToSettleMutableLiveData;
    private MutableLiveData<String> emailMutableLiveData;
    private MutableLiveData<String> adminIdMutableLiveData;
    private MutableLiveData<List<TimeModel>> timeModelListMutableLiveData;

    private MutableLiveData<List<TimeModel>> getAllTimeModelsForAdminSQLLiveData;
    private MutableLiveData<Integer> getIntegerHelps;
    private Observer observer;




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
    }


    public MutableLiveData<String> getAdminIdMutableLiveData() {
        return adminIdMutableLiveData;
    }

    public MutableLiveData<Map<String, Object>> getPaycheckHoursToSettleMutableLiveData() {
        return paycheckHoursToSettleMutableLiveData;
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

    public MutableLiveData<User> getUsernameAndSurnameMB()

    {
        return timeModelMutableLiveData;
    }

    public MutableLiveData<Boolean> getIfAdminMutableLiveData() {
        return ifAdminMutableLiveData;
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

    public LiveData<User> getUsernameAndSurname()
    {
        return authRepository.getUsernameAndSurname();
    }

   /* public LiveData<User> getUsernameAndSurname2(LifecycleOwner lifecycleOwner)
    {
        return authRepository.getUsernameAndSurname2(lifecycleOwner);
    }
*/

    public void registerUser(String email, String password,String userName_send,String surName_send,String foreign_email)
    {
        authRepository.register(email, password,userName_send,surName_send,foreign_email);
    }


    public void deleteDateFromFireBase(TimeModel timeModel)
    {
        authRepository.deleteDateFromFireBase(timeModel);
    }
    public void updateDataToFirebase(String documentID,String beginTime,String endTime,String overall,long timeInLong,TimeModel timeModel)
    {
        authRepository.updateDataToFirebase(documentID,beginTime,endTime,overall,timeInLong,timeModel);
    }

    public void updateStatusOfSettled(String documentID, boolean isSettled, double withDrawnMoney, Timestamp timestamp,TimeModel timeModel)
    {
        authRepository.updateStatusOfPayment(documentID,isSettled,withDrawnMoney,timestamp,timeModel);
    }

    public void updateStatusOfTimeForUser(String userId,long settletedTimeInMillis, double payCheck )
    {
        authRepository.updateStatusOfTimeForUser(userId,settletedTimeInMillis,payCheck);
    }


    public void getDataToUpdatePayCheck(String userId)
    {
        authRepository.getDataToUpdatePayCheck(userId);
    }

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
    public LiveData<QRModel> getDataQrCode2(String admin)
    {
        return authRepository.getDataQRCode2(admin);
    }

    public void updateEntriesAmount(TimeModel timeModel) {

        long time1 = timeModel.getTimeAdded().toDate().getTime();

        Duration duration1 = Duration.ofMillis(time1);
        int daysSinceUnixTimeModel = (int) duration1.toDays();


        long time = new Date().getTime();

        Duration duration = Duration.ofMillis(time);
        int daysSinceUnixCurrent = (int) duration.toDays();

        DecodeDaysAndEntries decodeDaysAndEntries = new DecodeDaysAndEntries();

        observer = new Observer<Map<String, Object>>() {
            @Override
            public void onChanged(Map<String, Object> stringObjectMap) {
                String s = (String) stringObjectMap.get("entriesAmount");
                String email = (String)stringObjectMap.get("email");

                if(daysSinceUnixTimeModel==daysSinceUnixCurrent) {

                    int amountEntriesDecoded = decodeDaysAndEntries.decodeToAmountEntries(s);
                    int dayDecoded = decodeDaysAndEntries.decodeDays(s);

                    amountEntriesDecoded--;

                    authRepository.updateEntriesAmount(dayDecoded+"_"+amountEntriesDecoded,email);
                }
            }
        };

         authRepository.getAmountEntries(timeModel.getId()).observeForever(observer);
    }

    @Override
    protected void onCleared() {
        authRepository.getGetAmountEntriesLiveData().removeObserver(observer);
        super.onCleared();
    }
}
