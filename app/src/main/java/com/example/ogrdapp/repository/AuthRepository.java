package com.example.ogrdapp.repository;

import android.app.Application;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;

import com.example.ogrdapp.R;
import com.example.ogrdapp.model.QRModel;
import com.example.ogrdapp.model.TimeModel;
import com.example.ogrdapp.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class AuthRepository {
    private Application application;
    private MutableLiveData<FirebaseUser> firebaseUserMutableLiveData;
    private MutableLiveData<Boolean> userLoggedMutableLiveData;
    private MutableLiveData<TimeModel> firebaseTimeModel;
    private MutableLiveData<List<TimeModel>> timeModelArrayListMutableLiveData;
    private MutableLiveData<Boolean> ifAdminMutableLiveData;
    private MutableLiveData<List<User>> userArrayListOfUserMutableLiveData;
    private MutableLiveData<List<TimeModel>> timeForUserListMutableLiveData;
    private MutableLiveData<List<TimeModel>> timeSelectedForUserListMutableLiveData;
    private MutableLiveData<Map<String, Object>> paycheckHoursToSettleMutableLiveData;
    private MutableLiveData<String> emailMutableLiveData;
    private MutableLiveData<String> adminIdMutableLiveData;
    private MutableLiveData<LinkedList<QRModel>> qrModelMutableLiveData;



    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db;
    private CollectionReference collectionReference;
    private FirebaseUser fireBaseUser;
    private CollectionReference collectionReferenceTime;
    private CollectionReference collectionReferenceQrCode;

    private final String collectionReferenceQrCodeString = "QRCode";
    private String currentUserId;

    private List<String> arrayListForAssigningEmail;
    private List<String> arrayListIfAdmin;
    boolean ifAdmin = false;
    private final String KEY_TIME_BEGIN = "timeBegin";
    private final String KEY_TIME_END = "timeEnd";
    private final String KEY_TIME_OVERALL = "timeOverall";
    private final String KEY_TIME_OVERALL_IN_LONG = "timeOverallInLong";
    private final String KEY_PAYCHECK = "paycheck";
    private final String KEY_MONEYOVERALL = "moneyOverall";
    private final String WITH_DRAWN_MONEY = "withdrawnMoney";
    private final String LOGER = "FirebaseRepository";

    public AuthRepository(Application application) {

        this.application = application;
        this.firebaseUserMutableLiveData = new MutableLiveData<>();
        this.userLoggedMutableLiveData = new MutableLiveData<>();
        this.firebaseTimeModel = new MutableLiveData<>();
        this.timeModelArrayListMutableLiveData = new MutableLiveData<>();
        this.arrayListForAssigningEmail = new ArrayList<>();
        this.arrayListIfAdmin= new ArrayList<>();
        this.ifAdminMutableLiveData = new MutableLiveData<>();
        this.userArrayListOfUserMutableLiveData = new MutableLiveData<>();
        this.timeForUserListMutableLiveData = new MutableLiveData<>();
        this.timeSelectedForUserListMutableLiveData = new MutableLiveData<>();
        this.paycheckHoursToSettleMutableLiveData = new MutableLiveData<>();
        this.emailMutableLiveData = new MutableLiveData<>();
        this.adminIdMutableLiveData = new MutableLiveData<>();
        this.qrModelMutableLiveData = new MutableLiveData<>();

        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        collectionReference = db.collection("Users");
        collectionReferenceTime = db.collection("Time");
        collectionReferenceQrCode = db.collection("QRCode");

        if(firebaseAuth.getCurrentUser()!=null)
        {
            fireBaseUser = firebaseAuth.getCurrentUser();
            firebaseUserMutableLiveData.postValue(firebaseAuth.getCurrentUser());
            currentUserId = fireBaseUser.getUid();
            adminIdMutableLiveData.postValue(currentUserId);
        }

    }

    public String getUserId()
    {
        fireBaseUser = firebaseAuth.getCurrentUser();
        assert  fireBaseUser !=null;
        return  fireBaseUser.getUid();
    }

    public MutableLiveData<LinkedList<QRModel>> getQrModelMutableLiveData() {
        return qrModelMutableLiveData;
    }

    public MutableLiveData<List<TimeModel>> getTimeForUserListMutableLiveData() {
        return timeForUserListMutableLiveData;
    }

    public MutableLiveData<List<User>> getUserArrayListOfUserMutableLiveData() {
        return userArrayListOfUserMutableLiveData;
    }

    public MutableLiveData<Boolean> getIfAdminMutableLiveData() {
        return ifAdminMutableLiveData;
    }

    public MutableLiveData<List<TimeModel>> getTimeModelArrayListMutableLiveData() {
        return timeModelArrayListMutableLiveData;
    }

    public MutableLiveData<FirebaseUser> getFirebaseUserMutableLiveData() {
        return firebaseUserMutableLiveData;
    }

    public MutableLiveData<Boolean> getUserLoggedMutableLiveData() {
        return userLoggedMutableLiveData;
    }

    public MutableLiveData<TimeModel> getFirebaseTimeModel() {
        return firebaseTimeModel;
    }

    public MutableLiveData<Map<String, Object>> getPaycheckHoursToSettleMutableLiveData() {
        return paycheckHoursToSettleMutableLiveData;
    }

    public FirebaseAuth getFirebaseAuth()
    {
        return firebaseAuth;
    }
    public FirebaseUser getCurrentUser() {
        return firebaseAuth.getCurrentUser();
    }

    public MutableLiveData<List<TimeModel>> getTimeSelectedForUserListMutableLiveData() {
        return timeSelectedForUserListMutableLiveData;
    }

    public MutableLiveData<String> getEmailMutableLiveData() {
        return emailMutableLiveData;
    }

    public MutableLiveData<String> getAdminIdMutableLiveData() {
        return adminIdMutableLiveData;
    }

    //FOR QRCODE
    public void setNewQrCode(Map<String,Object> qrCodeMap)
    {
        collectionReferenceQrCode.document().set(qrCodeMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
            Log.i("setNewQrCode","Added Succesfully");
            LinkedList<QRModel> qrModelLinkedList = new LinkedList<>();

                QRModel qrModel = new QRModel();
                qrModel.setDelay((int)qrCodeMap.get("delay"));
                qrModel.setAdminId((String)qrCodeMap.get("idAdmin"));
                qrModel.setQRCode((String) qrCodeMap.get("QRCode"));

                qrModelLinkedList.add(qrModel);

                qrModelMutableLiveData.postValue(qrModelLinkedList);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
            Log.i("Fail setNewCodeQR",e.getMessage().toString());
            }
        });
    }

    //getTimeSelectedForUserListMutableLiveData - mutable live data for this below.
    public void getSelectedTimeForUser(String userId, String dateRange)
    {

        //decoderForDate(dateRange);

        collectionReferenceTime.whereEqualTo("id",userId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if(!queryDocumentSnapshots.isEmpty())
                        {
                            List<TimeModel> timeModelArrayList = new ArrayList<>();
                            for(QueryDocumentSnapshot timeModels: queryDocumentSnapshots)
                            {

                                TimeModel timeModel = timeModels.toObject(TimeModel.class);


                                timeModelArrayList.add(timeModel);
                                Collections.sort(timeModelArrayList, new Comparator<TimeModel>() {
                                    @Override
                                    public int compare(TimeModel o1, TimeModel o2) {
                                        return o1.getTimeAdded().compareTo(o2.getTimeAdded());
                                    }
                                });

                            }
                            timeSelectedForUserListMutableLiveData.setValue(timeModelArrayList);

                            Log.i(LOGER,"getSelectedTimeForUser");
                        }
                    }
                });
    }



    public void getTimeForUser(String userId)
    {

        collectionReferenceTime.whereEqualTo("id", userId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        if(!queryDocumentSnapshots.isEmpty())
                        {
                            List<TimeModel> timeModelArrayList = new ArrayList<>();
                            for(QueryDocumentSnapshot timeModels: queryDocumentSnapshots)
                            {

                                TimeModel timeModel = timeModels.toObject(TimeModel.class);


                                timeModelArrayList.add(timeModel);
                                Collections.sort(timeModelArrayList, new Comparator<TimeModel>() {
                                    @Override
                                    public int compare(TimeModel o1, TimeModel o2) {
                                        return o1.getTimeAdded().compareTo(o2.getTimeAdded());
                                    }
                                });

                            }
                            timeForUserListMutableLiveData.setValue(timeModelArrayList);
                        }

                        Log.i(LOGER,"getTimeForUser");

                    }
                });
    }

    public void getDataQRCode(String adminId)
    {

        collectionReferenceQrCode.whereEqualTo("idAdmin", adminId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        if(!queryDocumentSnapshots.isEmpty())
                        {

                            LinkedList<QRModel> qrModelLink = new LinkedList<>();

                            for(QueryDocumentSnapshot qrModels: queryDocumentSnapshots)
                            {

                                QRModel qrModel = qrModels.toObject(QRModel.class);


                                qrModelLink.add(qrModel);

                            }
                            qrModelMutableLiveData.postValue(qrModelLink);
                        }

                    }
                });
    }
    public void getData()
    {

        collectionReferenceTime.whereEqualTo("id", currentUserId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        if(!queryDocumentSnapshots.isEmpty())
                        {

                            List<TimeModel> timeModelArrayList = new ArrayList<>();
                            for(QueryDocumentSnapshot timeModels: queryDocumentSnapshots)
                            {

                                TimeModel timeModel = timeModels.toObject(TimeModel.class);


                                timeModelArrayList.add(timeModel);
                                Collections.sort(timeModelArrayList, new Comparator<TimeModel>() {
                                    @Override
                                    public int compare(TimeModel o1, TimeModel o2) {
                                        return o1.getTimeAdded().compareTo(o2.getTimeAdded());
                                    }
                                });

                            }
                            timeModelArrayListMutableLiveData.postValue(timeModelArrayList);
                        }
                        Log.i(LOGER,"getData");
                    }
                });
    }

    // I tutaj
    public void updatedDataHoursToFirebaseUser(TimeModel timeModel)
    {
        collectionReference.whereEqualTo("userId",timeModel.getId()).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if(!queryDocumentSnapshots.isEmpty())
                {
                    for(QueryDocumentSnapshot user: queryDocumentSnapshots)
                    {
                        User user1 = user.toObject(User.class);

                        Map<String, Object> result = null;

                        result = new HashMap<>();

                        result.put("userEmail", user1.getEmail());
                        //to z danych
                        result.put("timeOverallFromTimeModel", timeModel.getTimeOverallInLong());
                        //to z usera
                        result.put("hoursOverall",user1.getHoursOverall());

                        result.put("hoursToSettle",user1.getHoursToSettle());

                        Log.i(LOGER,"updatedDataHoursToFirebaseUser");

                        updateUserTime(result);
                    }
                }
            }
        });
    }

    // Nie jest to używane można usunąć.
    public void getPaycheckAndHoursToSettleLong(String userId)
    {
        collectionReference.whereEqualTo("userId",userId).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if(!value.isEmpty())
                {
                    for(QueryDocumentSnapshot snapshot:value)
                    {
                        Long hoursToSettle = snapshot.getLong("hoursToSettle");
                        long paycheck = (long)snapshot.get("paycheck");


                        Map<String, Object> result = new HashMap<>();

                        result.put("paycheck",paycheck);
                        result.put("hoursToSettle", hoursToSettle);

                        Log.i(LOGER,"getPaycheckAndHoursToSettleLong");

                        paycheckHoursToSettleMutableLiveData.postValue(result);

                    }

                }
            }
        });
    }
    public void getDataToUpdatePayCheck(String currentUserId)
    {

        collectionReference.whereEqualTo("userId",currentUserId).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if(!queryDocumentSnapshots.isEmpty())
                {
                    for(QueryDocumentSnapshot snapshot:queryDocumentSnapshots)
                    {
                        Long hoursToSettle = snapshot.getLong("hoursToSettle");
                        double paycheck =  snapshot.getDouble("paycheck");
                        String email = snapshot.getString("email");

                        Map<String, Object> result = new HashMap<>();

                        result.put("paycheck",paycheck);
                        result.put("hoursToSettle", hoursToSettle);
                        result.put("email",email);

                        Log.i(LOGER,"getDataToUpdatePayCheck");

                        paycheckHoursToSettleMutableLiveData.postValue(result);

                    }

                }
            }
        });
    }
    public void saveDataToFireBase(TimeModel timeModel)
    {
        String idDocument = collectionReferenceTime.document().getId();
        timeModel.setDocumentId(idDocument);
        String id = timeModel.getId();

        timeModel.getTimeOverallInLong();


        collectionReferenceTime.document(idDocument).set(timeModel).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(application, "Fail on adding data", Toast.LENGTH_SHORT).show();
                Log.i(LOGER,"saveDataToFireBase");
            }
        });
    }
    public void deleteDateFromFireBase(String documentID)
    {
        collectionReferenceTime.document(documentID).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.i("Firebase","deleteDateFromFireBase");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }

    //TUTAJ
    private void updateUserTime(Map<String, Object>result)
    {
        String userEmail =(String)result.get("userEmail");
        //to z usera
        long hoursOverall = (long)result.get("hoursOverall");
        long hoursToSettle = (long)result.get("hoursToSettle");
        long hoursFromTimeModel = (long)result.get("timeOverallFromTimeModel");
        //to z modelu
        long hoursOverallToSend = hoursOverall + hoursFromTimeModel;
        long hoursToSettleToSend = hoursToSettle + hoursFromTimeModel;
        Map<String,Object> resultToSend = new HashMap<>();

        resultToSend.put("hoursOverall",hoursOverallToSend);
        resultToSend.put("hoursToSettle",hoursToSettleToSend);
        Log.i(LOGER,"updateUserTime");
        collectionReference.document(userEmail).update(resultToSend).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {


            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }

    public void updateDataToFirebase(String documentID,String beginTime,String endTime,String overall,long timeInLong)
    {
        Map<String,Object> result = new HashMap<>();
        result.put(KEY_TIME_BEGIN,beginTime);
        result.put(KEY_TIME_END,endTime);
        result.put(KEY_TIME_OVERALL,overall);
        result.put(KEY_TIME_OVERALL_IN_LONG,timeInLong);

        Log.i("Firebase","updateDataToFirebase");

        collectionReferenceTime.document(documentID).update(result).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
            }
        });

    }

    public void updateStatusOfPayment(String documentID,boolean isSettled,double withDrawnMoney)
    {
        Map<String,Object> result = new HashMap<>();
        result.put(KEY_MONEYOVERALL,isSettled);
        result.put(WITH_DRAWN_MONEY,withDrawnMoney);

        Log.i(LOGER,"updateStatusOfPayment");

        collectionReferenceTime.document(documentID).update(result);
    }


    public void updateStatusOfTimeForUser(String email, long settledTimeInMillis, double payCheck)
    {
                            Map<String, Object> result = new HashMap<>();

                            result.put("paycheck",payCheck);
                            result.put("hoursToSettle", settledTimeInMillis);
                            Log.i("Firebase","updateStatusOfTimeForUser");
                        collectionReference.document(email).update(result).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                            }
                        });
    }

    // METODA Stworzona 22.12.2023r.
    public void getUserForeignKey()
    {
        //collectionReference.whereEqualTo("userId").get()
    }


    public void getUserDataAssignedToAdmin(String workerId)
    {
        Log.i(LOGER,"getUsersDataAssignedToAdmin 2 method");
        collectionReference.whereEqualTo("foreign_key",currentUserId).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if(!queryDocumentSnapshots.isEmpty())
                {
                    List<User> userArrayList = new ArrayList<>();

                    for(QueryDocumentSnapshot snapshot:queryDocumentSnapshots)
                    {
                        if(currentUserId.equals(snapshot.getString("foreign_key")))
                        {
                            ifAdminMutableLiveData.postValue(true);

                            User user = snapshot.toObject(User.class);

                            userArrayList.add(user);


                            userArrayListOfUserMutableLiveData.postValue(userArrayList);
                        }

                    }

                }
            }
        });
    }

    //  Było add snapshotListener zmieniam na add on SuccesListenr
    public void getUsersDataAssignedToAdmin()
    {
        Log.i(LOGER,"getUsersDataAssignedToAdmin");
        collectionReference.whereEqualTo("foreign_key",currentUserId).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if(!queryDocumentSnapshots.isEmpty())
                {
                    List<User> userArrayList = new ArrayList<>();

                    for(QueryDocumentSnapshot snapshot:queryDocumentSnapshots)
                    {
                        if(currentUserId.equals(snapshot.getString("foreign_key")))
                        {
                            ifAdminMutableLiveData.postValue(true);

                            User user = snapshot.toObject(User.class);

                            userArrayList.add(user);


                            userArrayListOfUserMutableLiveData.postValue(userArrayList);
                        }

                    }

                }
            }
        });
      /*  collectionReference.whereEqualTo("foreign_key",currentUserId).addOnSuccessListener(new OnSuccessListener<>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if(!value.isEmpty())
                {
                    List<User> userArrayList = new ArrayList<>();

                    for(QueryDocumentSnapshot snapshot:value)
                    {
                        if(currentUserId.equals(snapshot.getString("foreign_key")))
                        {
                            ifAdminMutableLiveData.postValue(true);

                            User user = snapshot.toObject(User.class);

                            userArrayList.add(user);


                            userArrayListOfUserMutableLiveData.postValue(userArrayList);
                        }

                    }

                }

            }
        });*/
       /* collectionReference.whereEqualTo("foreign_key",currentUserId).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if(!value.isEmpty())
                {
                    List<User> userArrayList = new ArrayList<>();

                    for(QueryDocumentSnapshot snapshot:value)
                    {
                        if(currentUserId.equals(snapshot.getString("foreign_key")))
                        {
                            ifAdminMutableLiveData.postValue(true);

                            User user = snapshot.toObject(User.class);

                            userArrayList.add(user);


                            userArrayListOfUserMutableLiveData.postValue(userArrayList);
                        }

                    }

                }

            }
        });*/



    }
    public boolean checkIfAdmin ()
    {

        Log.i(LOGER,"checkIfAdmin");

        collectionReference.whereEqualTo("foreign_key",currentUserId).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {

                    if(!value.isEmpty())
                    {
                        for(QueryDocumentSnapshot snapshot:value)
                        {
                            if(currentUserId.equals(snapshot.getString("foreign_key")))
                              {
                                  ifAdminMutableLiveData.postValue(true);

                              }
                              else{

                                  ifAdminMutableLiveData.postValue(false);
                              }
                        }
                    }
            }
        });

        return ifAdmin;
    }



    public void register (String email, String password,String userName_send,String surName_send,String foreign_email)
    {

        Log.i(LOGER,"register");

        collectionReference.whereEqualTo("email",foreign_email).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if(!value.isEmpty())
                {
                    for(QueryDocumentSnapshot snapshot: value)
                    {
                        arrayListForAssigningEmail.add(snapshot.getString("userId"));
                    }
                }
            }
        });

        firebaseAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful())
                {
                    firebaseUserMutableLiveData.postValue(firebaseAuth.getCurrentUser());

                    fireBaseUser = firebaseAuth.getCurrentUser();
                    assert  fireBaseUser!=null;
                    final String currentUserId = fireBaseUser.getUid();

                    // Create a userMap so we can create user in the User Collection in FireStore
                    Map<String, Object> userObj = new HashMap<>();
                    userObj.put("userId",currentUserId);
                    userObj.put("email",email);
                    userObj.put("username",userName_send);
                    userObj.put("surName",surName_send);
                    userObj.put("foreign_key", arrayListForAssigningEmail.get(0));
                    userObj.put("hoursOverall",0);
                    userObj.put("hoursToSettle",0);
                    userObj.put("paycheck",0);

                    Log.i("Hello five","five");

                    collectionReference.document(email).set(userObj).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {

                            arrayListForAssigningEmail.clear();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });

                }
                else{
                    Toast.makeText(application, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void signIn(String email, String password)
    {

        Log.i(LOGER,"signIn");

        firebaseAuth.signInWithEmailAndPassword(email,password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                firebaseUserMutableLiveData.postValue(firebaseAuth.getCurrentUser());

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(application, application.getString(R.string.no_user), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void resetPassword(String email)
    {
        Log.i(LOGER,"resetPassword");

        firebaseAuth.sendPasswordResetEmail(email).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(application, application.getString(R.string.reset_meesage), Toast.LENGTH_SHORT).show();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(application, application.getString(R.string.wrong_email), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public TimeModel getUsernameAndSurname()
    {
        Log.i(LOGER,"getUsernameAndSurname");

        checkIfAdmin();
        TimeModel timeModel = new TimeModel();
        collectionReference.whereEqualTo("userId",currentUserId).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if(!value.isEmpty())
                {
                    for(QueryDocumentSnapshot snapshot: value)
                    {
                        timeModel.setUserName(snapshot.getString("username"));
                        timeModel.setUserSurname(snapshot.getString("surName"));
                        timeModel.setId(snapshot.getString("foreign_key"));
                    }

                    firebaseTimeModel.postValue(timeModel);
                }
                else {

                    Toast.makeText(application, error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });


        return timeModel;
    }



    public void singOut(){
        firebaseAuth.signOut();
        userLoggedMutableLiveData.postValue(true);
        Log.i(LOGER,"singOut");
    }

}
