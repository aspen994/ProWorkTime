package com.osinTechInnovation.ogrdapp.viewmodel;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.google.firebase.auth.AuthCredential;
import com.osinTechInnovation.ogrdapp.model.QRModel;
import com.osinTechInnovation.ogrdapp.model.TimeModel;
import com.osinTechInnovation.ogrdapp.model.User;
import com.osinTechInnovation.ogrdapp.repository.AuthRepository;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseUser;
import com.osinTechInnovation.ogrdapp.utility.DecodeDaysAndEntries;
import com.osinTechInnovation.ogrdapp.utility.SingleLiveEvent;

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

    private MutableLiveData<String> checkSubscribtion;
    private MutableLiveData<Boolean> valueToOpenDialog;
    private MutableLiveData<Boolean> isUserInDB;
    private MutableLiveData<Integer> entriesLiveData;
    private MutableLiveData<String> getAmountEntriesLiveData2;
    private SingleLiveEvent<Map<String, Object>> singleLiveEvent;
    private SingleLiveEvent<Map<String, Object>> singleLiveEventStart;




    public AuthViewModel(@NonNull Application application) {
        super(application);
        authRepository = new AuthRepository(application);
        userData = authRepository.getFirebaseUserMutableLiveData();
        timeModelMutableLiveData = authRepository.getGetUsernameAndSurnameLiveData();
        loggedStatus = authRepository.getUserLoggedMutableLiveData();
        ifAdminMutableLiveData = authRepository.getIfAdminMutableLiveData();
        userArrayListOfUserMutableLiveData = authRepository.getUserArrayListOfUserMutableLiveData();
        timeForUserListMutableLiveData = authRepository.getTimeForUserListMutableLiveData();
        paycheckHoursToSettleMutableLiveData = authRepository.getPaycheckHoursToSettleMutableLiveData();
        emailMutableLiveData = authRepository.getEmailMutableLiveData();
        adminIdMutableLiveData =authRepository.getAdminIdMutableLiveData();
        timeModelListMutableLiveData=authRepository.getTimeModelArrayListMutableLiveData();
        getAllTimeModelsForAdminSQLLiveData = authRepository.getGetAllTimeModelsForAdminSQLLiveData();
        checkSubscribtion = authRepository.getCheckSubscriptionLiveData();
        valueToOpenDialog = authRepository.getValueToOpenDialogLiveData();
        isUserInDB = authRepository.getIsUserInDBLiveDataLiveData();
        entriesLiveData = new MutableLiveData<>();
        this.getAmountEntriesLiveData2 = authRepository.getGetAmountEntriesSnapshotListenerLiveData();
        this.singleLiveEvent = authRepository.getSingleLiveEvent();
        this.singleLiveEventStart = authRepository.getSingleLiveEventStart();
    }

    public SingleLiveEvent<Map<String, Object>> getSingleLiveEventStart() {
        return singleLiveEventStart;
    }

    public SingleLiveEvent<Map<String, Object>> getSingleLiveEvent() {
        return singleLiveEvent;
    }

    public MutableLiveData<String> getGetAmountEntriesLiveData2() {
        return getAmountEntriesLiveData2;
    }

    public MutableLiveData<Integer> getEntriesLiveData() {
        return entriesLiveData;
    }

    public MutableLiveData<Boolean> getIsUserInDB() {
        return isUserInDB;
    }

    public MutableLiveData<Boolean> getValueToOpenDialog() {
        return valueToOpenDialog;
    }

    public SingleLiveEvent<Map<String, Object>>getAmountEntriesStart(String id){
        return authRepository.getAmountEntriesStart(id);
    }

    public MutableLiveData<String> getGetEmailMutableLiveData(){
        return authRepository.getEmail();
    }

    public MutableLiveData<String> getCheckSubscribtion() {
        return checkSubscribtion;
    }

    public void isSubscriptionAlreadyExist(String orderId)
    {
         authRepository.isSubscriptionAlreadyExist(orderId);
    }

    public void signInWithGoogleCredential(AuthCredential authCredential, Context context,String email){
        authRepository.signInWithGoogleCredential(authCredential,context,email);
    }


    public void setTheSubsForUser(String orderId){
        authRepository.updateUserWithSubs(orderId);
    }


    public MutableLiveData<Boolean> getYouCanCheckNow(){
        return authRepository.getYouCanCheckNowLiveData();
    }

    public MutableLiveData<Boolean> getIsAdminExist(){
        return authRepository.getIsAdminExistLiveData();
    }

    public LiveData<String> getDeviceId() {
        return authRepository.getDeviceId();
    }

    public SingleLiveEvent<Map<String, Object>> getAmountEntries(String id){
        return authRepository.getAmountEntries(id);
    }

    public void updateAmountEntries(String updateAmountEntries) {
        authRepository.updateAmountEntries(updateAmountEntries);
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

    public void registerUser(String email, String password,String userName_send,String surName_send)
    {
        authRepository.register(email, password,userName_send,surName_send);
    }

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


    // TODo 16.08.2024 dlaczego tutaj a nie w repozytorium
    public void updateEntriesAmount(TimeModel timeModel) {

/*        long time1 = timeModel.getTimeAdded().toDate().getTime();

        Duration duration1 = Duration.ofMillis(time1);
        int daysSinceUnixTimeModel = (int) duration1.toDays();


        long time = new Date().getTime();

        Duration duration = Duration.ofMillis(time);
        int daysSinceUnixCurrent = (int) duration.toDays();

        DecodeDaysAndEntries decodeDaysAndEntries = new DecodeDaysAndEntries();*/

        authRepository.updateEntriesAmount(timeModel);

     /*   observer = new Observer<Map<String, Object>>() {
            @Override
            public void onChanged(Map<String, Object> stringObjectMap) {
                String s = (String) stringObjectMap.get("entriesAmount");
                String email = (String)stringObjectMap.get("email");

                Log.i("daysSinceUnixTimeModel",daysSinceUnixTimeModel+"");
                Log.i("daysSinceUnixCurrent",daysSinceUnixCurrent+"");



                if(daysSinceUnixTimeModel==daysSinceUnixCurrent) {

                    int amountEntriesDecoded = decodeDaysAndEntries.decodeToAmountEntries(s);
                    int dayDecoded = decodeDaysAndEntries.decodeDays(s);

                    amountEntriesDecoded--;


                }
            }
        };*/

        //TOOD 19.08.2024
       // authRepository.getAmountEntries(timeModel.getId()).observeForever(observer);
    }

     public void writeToFile(String androidId) {
        authRepository.writeToFile(androidId);
    }



    @Override
    protected void onCleared() {
        //TODO 19.08.2024
        //authRepository.getGetAmountEntriesLiveData().removeObserver(observer);
        super.onCleared();
    }

    public void writeUserDataToDb(String emailAdmin,Context context) {
        authRepository.writeUserDataToDb(emailAdmin,context);
    }

    public void writeAdminDataToDb(Context context) {
        authRepository.writeAdminDataToDb(context);
    }

    public MutableLiveData<Integer> doesTheUserStillHaveAnyEntries() {

        Log.i("doesTheUser", "doestTheUser");

        final String[] amountEntriesWithDays = {""};

        DecodeDaysAndEntries decodeDaysAndEntries = new DecodeDaysAndEntries();

        /*getAmountEntries(getUserId()).observe(UserMainActivity.this, new Observer<Map<String, Object>>() {
            @Override
            public void onChanged(Map<String, Object> stringObjectMap) {
                amountEntriesWithDays[0] = (String) stringObjectMap.get("entriesAmount");


                Log.i("Tu paczz",decodeDaysAndEntries.decodeToAmountEntries(amountEntriesWithDays[0])+"");

                //Log.i("doesTheUser inside", isInvoked+"");
                //if (!isInvoked) {

                *//*    switch (decodeDaysAndEntries.decodeToAmountEntries(amountEntriesWithDays[0])) {
                        case 0:
                            Toast.makeText(UserMainActivity.this, "Masz jeszcze 7 możliwości zapisania czasu", Toast.LENGTH_SHORT).show();
                            authViewModel.getEntriesLiveData().postValue(0);
                            break;
                        case 1:
                            Toast.makeText(UserMainActivity.this, "Masz jeszcze 6 możliwości zapisania czasu", Toast.LENGTH_SHORT).show();
                            authViewModel.getEntriesLiveData().postValue(1);
                            break;
                        case 2:
                            Toast.makeText(UserMainActivity.this, "Masz jeszcze 5 możliwości zapisania czasu", Toast.LENGTH_SHORT).show();
                            authViewModel.getEntriesLiveData().postValue(2);
                            break;
                        case 3:
                            Toast.makeText(UserMainActivity.this, "Masz jeszcze 4 możliwości zapisania czasu", Toast.LENGTH_SHORT).show();
                            authViewModel.getEntriesLiveData().postValue(3);
                            break;
                        case 4:
                            Toast.makeText(UserMainActivity.this, "Masz jeszcze 3 możliwości zapisania czasu", Toast.LENGTH_SHORT).show();
                            authViewModel.getEntriesLiveData().postValue(4);
                            break;
                        case 5:
                            Toast.makeText(UserMainActivity.this, "Masz jeszcze 2 możliwości zapisania czasu", Toast.LENGTH_SHORT).show();
                            authViewModel.getEntriesLiveData().postValue(5);
                            break;
                        case 6:
                            Toast.makeText(UserMainActivity.this, "Masz jeszcze 1 możliwości zapisania czasu", Toast.LENGTH_SHORT).show();
                            authViewModel.getEntriesLiveData().postValue(6);
                            break;
                        case 7:
                            Toast.makeText(UserMainActivity.this, "Masz jeszcze 0 możliwości zapisania czasu", Toast.LENGTH_SHORT).show();
                            authViewModel.getEntriesLiveData().postValue(7);
                            break;

                    }*//*

                    if (decodeDaysAndEntries.decodeToAmountEntries(amountEntriesWithDays[0]) < 7) {
                        //authViewModel.getEntriesLiveData().postValue(true);
                    } else {
                        //authViewModel.getEntriesLiveData().postValue(false);
                    }
             //       isInvoked = true;
                }

           // }
        });*/

        //authViewModel.getAmountEntries().re

        return getEntriesLiveData();
    }
}
