package com.osinTechInnovation.ogrdapp.repository;



import android.app.Application;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import com.osinTechInnovation.ogrdapp.R;
import com.osinTechInnovation.ogrdapp.dao.TimeModelDAO;
import com.osinTechInnovation.ogrdapp.db.TimeModelDatabase;
import com.osinTechInnovation.ogrdapp.model.QRModel;
import com.osinTechInnovation.ogrdapp.model.TimeModel;
import com.osinTechInnovation.ogrdapp.model.User;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AuthRepository {
    private final Application application;
    private final MutableLiveData<FirebaseUser> firebaseUserMutableLiveData;
    private final MutableLiveData<Boolean> userLoggedMutableLiveData;
    private final MutableLiveData<User> getUsernameAndSurname;
    private final MutableLiveData<User> getUsernameAndSurname2;
    private final MutableLiveData<List<TimeModel>> timeModelArrayListMutableLiveData;
    private final MutableLiveData<Boolean> ifAdminMutableLiveData;
    private final MutableLiveData<List<User>> userArrayListOfUserMutableLiveData;
    private final MutableLiveData<List<TimeModel>> timeForUserListMutableLiveData;

    private final MutableLiveData<Map<String, Object>> paycheckHoursToSettleMutableLiveData;
    private final MutableLiveData<String> emailMutableLiveData;
    private final MutableLiveData<String> adminIdMutableLiveData;
    private final MutableLiveData<List<QRModel>> qrModelMutableLiveData;
    private final MutableLiveData<QRModel> qrModelMutableLiveData2;
    private final MutableLiveData<List<TimeModel>> getAllTimeModelsForAdminSQLLiveData;

    //TODO 29.04.2024 r - Podmieniłem
    private  MutableLiveData<Map<String,Object>> getAmountEntriesLiveData;
    private  MutableLiveData<Boolean> isAdminExist;

    //private MutableLiveData<String> getAmountEntriesLiveData;

    private final FirebaseAuth firebaseAuth;
    private FirebaseUser fireBaseUser;
    private final CollectionReference collectionReferenceUser;
    private final CollectionReference collectionReferenceTime;
    private final CollectionReference collectionReferenceQrCode;
    private String currentUserId;
    private final List<String> arrayListForAssigningEmail;
    boolean ifAdmin = false;
    private final String LOGER = "FirebaseRepository";

    private final TimeModelDAO timeModelDAO;
    private final ExecutorService executor;

    private final String ROOM_DB_LOGGER = "ROOM_DB";
    private final String FIREBASE_LOGGER = "FIREBASE_DB";


    public AuthRepository(Application application) {

        this.application = application;
        this.firebaseUserMutableLiveData = new MutableLiveData<>();
        this.userLoggedMutableLiveData = new MutableLiveData<>();
        this.getUsernameAndSurname = new MutableLiveData<>();
        this.timeModelArrayListMutableLiveData = new MutableLiveData<>();
        this.arrayListForAssigningEmail = new ArrayList<>();
        this.ifAdminMutableLiveData = new MutableLiveData<>();
        this.userArrayListOfUserMutableLiveData = new MutableLiveData<>();
        this.timeForUserListMutableLiveData = new MutableLiveData<>();
        this.paycheckHoursToSettleMutableLiveData = new MutableLiveData<>();
        this.emailMutableLiveData = new MutableLiveData<>();
        this.adminIdMutableLiveData = new MutableLiveData<>();
        this.qrModelMutableLiveData = new MutableLiveData<>();
        this.getAllTimeModelsForAdminSQLLiveData = new MutableLiveData<>();
        this.qrModelMutableLiveData2 = new MutableLiveData<>();
        this.getUsernameAndSurname2= new MutableLiveData<>();
        this.getAmountEntriesLiveData = new MutableLiveData<>();
        this.isAdminExist = new MutableLiveData<>();



        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        collectionReferenceUser = db.collection("Users");
        collectionReferenceTime = db.collection("Time");
        collectionReferenceQrCode = db.collection("QRCode");

        TimeModelDatabase timeModelDatabase = TimeModelDatabase.getInstance(application);
        this.timeModelDAO = timeModelDatabase.getTimeModelDao();

        // Used for Background Database Operations
        executor = Executors.newSingleThreadExecutor();

        if (firebaseAuth.getCurrentUser() != null) {
            fireBaseUser = firebaseAuth.getCurrentUser();
            firebaseUserMutableLiveData.postValue(firebaseAuth.getCurrentUser());
            currentUserId = fireBaseUser.getUid();
            adminIdMutableLiveData.postValue(currentUserId);
        }

    }

    public MutableLiveData<Boolean> getIsAdminExist() {
        return isAdminExist;
    }

    public MutableLiveData<Map<String, Object>> getGetAmountEntriesLiveData() {
        return getAmountEntriesLiveData;
    }
 /*   public MutableLiveData<String> getGetAmountEntriesLiveData() {
        return getAmountEntriesLiveData;
    }*/

   /* public void setGetAmountEntriesLiveData(MutableLiveData<Map<String, Object>> getAmountEntriesLiveData) {
        this.getAmountEntriesLiveData = getAmountEntriesLiveData;
    }*/

    public void updateAmountEntries(String updateAmountEntries){
        Map<String,Object> mapa = new HashMap<>();

        mapa.put("entriesAmount",updateAmountEntries);

        Log.i("entriesAmount",updateAmountEntries);

        collectionReferenceUser.document(fireBaseUser.getEmail()).update(mapa).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {

            }
        });
    }

    public String getUserId() {
        fireBaseUser = firebaseAuth.getCurrentUser();
        assert fireBaseUser != null;
        return fireBaseUser.getUid();
    }

    public MutableLiveData<List<TimeModel>> getGetAllTimeModelsForAdminSQLLiveData() {
        return getAllTimeModelsForAdminSQLLiveData;
    }


    public MutableLiveData<List<TimeModel>> getTimeModelArrayListMutableLiveData() {
        return timeModelArrayListMutableLiveData;
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


    public MutableLiveData<FirebaseUser> getFirebaseUserMutableLiveData() {
        return firebaseUserMutableLiveData;
    }

    public MutableLiveData<Boolean> getUserLoggedMutableLiveData() {
        return userLoggedMutableLiveData;
    }

    public MutableLiveData<User> getGetUsernameAndSurname() {
        return getUsernameAndSurname;
    }

    public MutableLiveData<User> getGetUsernameAndSurname2() {
        return getUsernameAndSurname2;
    }

    public MutableLiveData<Map<String, Object>> getPaycheckHoursToSettleMutableLiveData() {
        return paycheckHoursToSettleMutableLiveData;
    }


    public MutableLiveData<String> getEmailMutableLiveData() {
        return emailMutableLiveData;
    }

    public MutableLiveData<String> getAdminIdMutableLiveData() {
        return adminIdMutableLiveData;
    }

    public void updateEntriesAmount(String updatedtedValue,String email)
    {
        Map<String,Object> mapa = new HashMap<>();

        mapa.put("entriesAmount",updatedtedValue);

        Log.i("entriesAmount",updatedtedValue);

        collectionReferenceUser.document(email).update(mapa);
    }

    //TODO 29.04.2024 r  - podmieniłem
 /*   public MutableLiveData<String> getAmountEntries(String id){
        System.gc();
        getAmountEntriesLiveData = new MutableLiveData<>();
        collectionReferenceUser.whereEqualTo("userId",id).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                if (!queryDocumentSnapshots.isEmpty()) {

                    for (QueryDocumentSnapshot user : queryDocumentSnapshots) {

                        User user1 = user.toObject(User.class);

                        String entriesAmount = user1.getEntriesAmount();
                        Log.i("Tutaj Hel",entriesAmount);

                        getAmountEntriesLiveData.setValue(entriesAmount);

                    }

                    Log.i(FIREBASE_LOGGER, " " + "getDataFirebase");
                }
            }
        });

        return getAmountEntriesLiveData;

    }*/

    public MutableLiveData<Map<String,Object>> getAmountEntries(String id){
        System.gc();
        getAmountEntriesLiveData = new MutableLiveData<>();
        collectionReferenceUser.whereEqualTo("userId",id).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                if (!queryDocumentSnapshots.isEmpty()) {

                    for (QueryDocumentSnapshot user : queryDocumentSnapshots) {

                        User user1 = user.toObject(User.class);
                        Map<String,Object> mapa = new HashMap<>();


                        String entriesAmount = user1.getEntriesAmount();
                        String email = user1.getEmail();

                        mapa.put("entriesAmount",entriesAmount);
                        mapa.put("email",email);


                        getAmountEntriesLiveData.setValue(mapa);

                    }

                    Log.i(FIREBASE_LOGGER, " " + "getDataFirebase");
                }
            }
        });

        return getAmountEntriesLiveData;

    }

    //FOR QRCODE
    public void setNewQrCode(Map<String, Object> qrCodeMap) {
        collectionReferenceQrCode.document().set(qrCodeMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                LinkedList<QRModel> qrModelLinkedList = new LinkedList<>();

                QRModel qrModel = new QRModel();
                qrModel.setDelay((int) qrCodeMap.get("delay"));
                qrModel.setAdminId((String) qrCodeMap.get("idAdmin"));
                qrModel.setQRCode((String) qrCodeMap.get("QRCode"));

                qrModelLinkedList.add(qrModel);

                qrModelMutableLiveData.postValue(qrModelLinkedList);
            }
        }).addOnFailureListener((Exception e) ->
                Log.i("Fail setNewCodeQR", Optional.ofNullable(e.getMessage())+""));

    }


    public void getDataFirebase(Timestamp timestamp) {

        collectionReferenceTime.whereEqualTo("id", currentUserId).whereGreaterThan("timestamp", timestamp)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        if (!queryDocumentSnapshots.isEmpty()) {

                            List<TimeModel> timeModelArrayList = new ArrayList<>();
                            for (QueryDocumentSnapshot timeModels : queryDocumentSnapshots) {

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
                            saveAllCollectionToSQLite(timeModelArrayList);
                            updateDataSQLite(timeModelArrayList);
                            Log.i(FIREBASE_LOGGER, " " + "getDataFirebase");
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i("Fail getData", Optional.ofNullable(e.getMessage())+"");
                    }
                });

    }

    public void getDataFirebase(String userId, Timestamp timestamp) {

        collectionReferenceTime.whereEqualTo("id", userId).whereGreaterThan("timestamp", timestamp)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        if (!queryDocumentSnapshots.isEmpty()) {
                            List<TimeModel> timeModelArrayList = new ArrayList<>();
                            for (QueryDocumentSnapshot timeModels : queryDocumentSnapshots) {

                                TimeModel timeModel = timeModels.toObject(TimeModel.class);

                                timeModelArrayList.add(timeModel);
                                Collections.sort(timeModelArrayList, new Comparator<TimeModel>() {
                                    @Override
                                    public int compare(TimeModel o1, TimeModel o2) {
                                        return o1.getTimeAdded().compareTo(o2.getTimeAdded());
                                    }
                                });
                                Log.i("getTimeForUser UserName", timeModel.getUserName());
                                Log.i("getTimeForUser TimeAded", timeModel.getTimeAdded().toString());
                                Log.i("getTimeForUser Id", timeModel.getDocumentId());
                                Log.i("getTimeForUser timeStam", timeModel.getTimestamp().toDate() + "");
                            }

                            saveAllCollectionToSQLite(timeModelArrayList);
                            Log.i("SAVING DATA", "SAVING DATA");


                            updateDataSQLite(timeModelArrayList);
                            Log.i("UPDATING DATA", "UPDATING DATA");


                            Log.i(FIREBASE_LOGGER, " " + "getTimeForUserNewMethod");
                        }

                    }
                });

    }


    public LiveData<List<QRModel>> getDataQRCode(String adminId) {


        collectionReferenceQrCode.whereEqualTo("idAdmin", adminId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {


                        if (!queryDocumentSnapshots.isEmpty()) {
                            List<QRModel> qrModelLink = new ArrayList<>();
                            for (QueryDocumentSnapshot qrModels : queryDocumentSnapshots) {

                                QRModel qrModel = qrModels.toObject(QRModel.class);

                                qrModelLink.add(qrModel);
                            }
                            qrModelMutableLiveData.postValue(qrModelLink);
                            for(QRModel qrModel: qrModelLink)
                            {
                                Log.i("INSIDE QRMODE",qrModel.getDelay()+" ");
                            }
                            Log.i(FIREBASE_LOGGER, " " + "getDataQRCode");
                        }

                    }
                });

        return qrModelMutableLiveData;
    }

    public LiveData<QRModel> getDataQRCode2(String adminId) {


        collectionReferenceQrCode.whereEqualTo("idAdmin", adminId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        if (!queryDocumentSnapshots.isEmpty()) {

                            for (QueryDocumentSnapshot qrModels : queryDocumentSnapshots) {

                                QRModel qrModel = qrModels.toObject(QRModel.class);

                                qrModelMutableLiveData2.postValue(qrModel);
                                Log.i(FIREBASE_LOGGER, " " + "getDataQRCode");
                            }



                        }

                    }
                });

        return qrModelMutableLiveData2;
    }

    public LiveData<List<TimeModel>> getAllTimeModelsForAdminSQL(String userId) {

        Log.i("STRING UserID", userId);
        executor.execute(new Runnable() {
            @Override
            public void run() {
                Log.i("ITERACJA POCZĄTEK", "Iteracja Początek");
                List<TimeModel> value = timeModelDAO.getAllTimeModelsList(userId);
                Log.i("value list: ", value.size() + "");
                Log.i("ForAdminSQL user:", userId);


                ///
                collectionReferenceTime.whereEqualTo("id", userId).whereGreaterThan("timestamp", countMethod(value))
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                                if (!queryDocumentSnapshots.isEmpty()) {
                                    List<TimeModel> timeModelArrayList = new ArrayList<>();
                                    for (QueryDocumentSnapshot timeModels : queryDocumentSnapshots) {

                                        TimeModel timeModel = timeModels.toObject(TimeModel.class);

                                        timeModelArrayList.add(timeModel);
                                        Collections.sort(timeModelArrayList, new Comparator<TimeModel>() {
                                            @Override
                                            public int compare(TimeModel o1, TimeModel o2) {
                                                return o1.getTimeAdded().compareTo(o2.getTimeAdded());
                                            }
                                        });
                                        Log.i("getTimeForUser UserName", timeModel.getUserName());
                                        Log.i("getTimeForUser TimeAded", timeModel.getTimeAdded().toString());
                                        Log.i("getTimeForUser Id", timeModel.getDocumentId());
                                        Log.i("getTimeForUser timeStam", timeModel.getTimestamp().toDate() + "");
                                    }

                                    saveAllCollectionToSQLite(timeModelArrayList);
                                    Log.i("SAVING DATA", "SAVING DATA");


                                    updateDataSQLite(timeModelArrayList);
                                    Log.i("UPDATING DATA", "UPDATING DATA");


                                    Log.i(FIREBASE_LOGGER, " " + "getTimeForUserNewMethod");
                                }

                                    collectionReferenceUser.whereEqualTo("userId", userId).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                        @Override
                                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                            if (!queryDocumentSnapshots.isEmpty()) {
                                                for (QueryDocumentSnapshot users : queryDocumentSnapshots) {
                                                    executor.execute(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            User user = users.toObject(User.class);
                                                            long hoursToSettle = user.getHoursOverall();
                                                            long timeModelSumForTimeOverall = sumUp(timeModelDAO.getAllTimeModelsList(userId));

                                                            Log.i("SZY userId: ", userId);
                                                            Log.i("FROM USER VALUE", hoursToSettle + "");
                                                            Log.i("FROM TimeModel VALUE", timeModelSumForTimeOverall + "");

                                                            if (hoursToSettle != timeModelSumForTimeOverall) {
                                                                Log.i("ROZBIEŻNOŚĆ", "<<<<<<<<<");
                                                                Log.i("USER VALUE", hoursToSettle + "");
                                                                Log.i("TimeModel VALUE", timeModelSumForTimeOverall + "");
                                                                Log.i("ROZBIEŻNOŚĆ", ">>>>>>>>>>");
                                                                Log.i("Deleting and Adding A", userId);
                                                                timeModelDAO.deleteListForTimeModel(userId);
                                                                getDataFirebase(userId, new Timestamp(new Date(0)));

                                                            }
                                                        }
                                                    });

                                                }
                                            }
                                        }
                                    });

                                //}

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.i("NOTHING NEW", "-----");
                            }
                        });

                ////
                //List<TimeModel> value2 = timeModelDAO.getAllTimeModelsList(userId);

                //readerList(value2);
                Log.i(ROOM_DB_LOGGER, "ROOM_DB" + " " + "getAllTimeModelsForAdminSQL");

            }
        });

        return timeModelDAO.getAllTimeModels(userId);
    }


    public LiveData<List<TimeModel>> getAllTimeModelsForUserSQL() {

        executor.execute(new Runnable() {
            @Override
            public void run() {
                List<TimeModel> value = timeModelDAO.getAllTimeModelsList(currentUserId);
                // Ta metoda do ściagania hoursOverall dla Users
                //readLogcat(value);
                Log.i(ROOM_DB_LOGGER, ROOM_DB_LOGGER + " " + "getAllTimeModelsForUserSQL");

                collectionReferenceTime.whereEqualTo("id", currentUserId).whereGreaterThan("timestamp", countMethod(value))
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                                if (!queryDocumentSnapshots.isEmpty()) {

                                    List<TimeModel> timeModelArrayList = new ArrayList<>();
                                    for (QueryDocumentSnapshot timeModels : queryDocumentSnapshots) {

                                        TimeModel timeModel = timeModels.toObject(TimeModel.class);

                                        timeModelArrayList.add(timeModel);
                                        Collections.sort(timeModelArrayList, new Comparator<TimeModel>() {
                                            @Override
                                            public int compare(TimeModel o1, TimeModel o2) {
                                                return o1.getTimeAdded().compareTo(o2.getTimeAdded());
                                            }
                                        });

                                        Log.i("PRINT1: ", timeModel.getUserName());
                                        Log.i("PRINT2: ", timeModel.getTimeBegin());
                                        Log.i("PRINT3: ", timeModel.getTimeEnd());
                                        Log.i("PRINT4: ", timeModel.getTimeOverall());
                                        Log.i("PRINT5: ", timeModel.getTimeAdded().toDate() + "");
                                        Log.i("------", "--------");


                                    }
                                    timeModelArrayListMutableLiveData.postValue(timeModelArrayList);
                                    saveAllCollectionToSQLite(timeModelArrayList);
                                    updateDataSQLite(timeModelArrayList);
                                    Log.i(FIREBASE_LOGGER, " " + "getDataFirebase");

                                }


                                collectionReferenceUser.whereEqualTo("userId", currentUserId).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                    @Override
                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                        if (!queryDocumentSnapshots.isEmpty()) {
                                            for (QueryDocumentSnapshot users : queryDocumentSnapshots) {
                                                executor.execute(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        User user = users.toObject(User.class);
                                                        long hoursToSettle = user.getHoursOverall();
                                                        //long timeModelSumForTimeOverall = sumUp(timeModelDAO.getAllTimeModelsList(currentUserId));
                                                        long timeModelSumForTimeOverall = sumUp(timeModelDAO.getAllTimeModelsList(currentUserId));


                                                        Log.i("FROM USER VALUE", hoursToSettle + "");
                                                        Log.i("FROM TimeModel VALUE", timeModelSumForTimeOverall + "");

                                                        if (hoursToSettle != timeModelSumForTimeOverall) {
                                                            Log.i("ROZBIEŻNOŚĆ", "<<<<<<<<<");
                                                            Log.i("USER VALUE", hoursToSettle + "");
                                                            Log.i("TimeModel VALUE", timeModelSumForTimeOverall + "");
                                                            Log.i("ROZBIEŻNOŚĆ", ">>>>>>>>>>");

                                                            timeModelDAO.deleteListForTimeModel(currentUserId);
                                                            Log.i("Deleting and Adding U", "delete and add");
                                                            getDataFirebase(new Timestamp(new Date(0)));

                                                        }
                                                    }
                                                });

                                            }
                                        }
                                    }
                                });


                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.i("Fail getData", Optional.ofNullable(e.getMessage())+"");

                            }
                        });

            }
        });

        return timeModelDAO.getAllTimeModels(currentUserId);
    }

    private long sumUp(List<TimeModel> value) {
        long toReturn = 0;
        for (TimeModel timeModel : value) {
            toReturn += timeModel.getTimeOverallInLong();
        }

        return toReturn;
    }

    public Timestamp countMethod(List<TimeModel> timeModels) {
        long max = 0;
        if (!timeModels.isEmpty()) {
            for (TimeModel timeModel : timeModels) {
                if (timeModel.getTimestamp().toDate().getTime() >= max) {
                    max = timeModel.getTimestamp().toDate().getTime();
                    Log.i("Inside loop", max + "");
                }
            }
            Log.i("USERNAME VALUE MAX", timeModels.get(0).getUserName());
            Log.i("the value of max", max + "");
            Log.i("What is the last date", new Date(max) + "");
            Log.i("---------", "-----");
        }

        return new Timestamp(new Date(max));
    }


    public void updatedDataHoursToFirebaseUser(TimeModel timeModel) {
        collectionReferenceUser.whereEqualTo("userId", timeModel.getId()).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (!queryDocumentSnapshots.isEmpty()) {
                    for (QueryDocumentSnapshot user : queryDocumentSnapshots) {
                        User user1 = user.toObject(User.class);

                        Map<String, Object> result;

                        result = new HashMap<>();

                        result.put("userEmail", user1.getEmail());
                        //to z danych
                        result.put("timeOverallFromTimeModel", timeModel.getTimeOverallInLong());
                        //to z usera
                        result.put("hoursOverall", user1.getHoursOverall());

                        result.put("hoursToSettle", user1.getHoursToSettle());

                        result.put("paycheck",user1.getPaycheck());




              /*          Log.i("2 UU userName", user1.getUsername() + "");
                        Log.i("2 UU hoursOverall", user1.getHoursOverall() + "");
                        Log.i("2 UTM timeOverall ", timeModel.getTimeOverallInLong() + "");*/

                        Log.i(FIREBASE_LOGGER, " " + "updatedDataHoursToFirebaseUser");

                        updateUserTime(result,timeModel);
                    }
                }


            }
        });
    }

    public void getDataToUpdatePayCheck(String currentUserId) {
        collectionReferenceUser.whereEqualTo("userId", currentUserId).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (!queryDocumentSnapshots.isEmpty()) {
                    for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {
                        Long hoursToSettle = snapshot.getLong("hoursToSettle");
                        Double paycheck = snapshot.getDouble("paycheck");
                        String email = snapshot.getString("email");

                        Map<String, Object> result = new HashMap<>();

                        result.put("paycheck", paycheck);
                        result.put("hoursToSettle", hoursToSettle);
                        result.put("email", email);

                        paycheckHoursToSettleMutableLiveData.postValue(result);

                    }
                    Log.i(FIREBASE_LOGGER, " " + "getDataToUpdatePayCheck");
                }

            }
        });
    }

    public void saveAllCollectionToSQLite(List<TimeModel> timeModelList) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                timeModelDAO.insertAllCollection(timeModelList);
                Log.i(ROOM_DB_LOGGER, ROOM_DB_LOGGER + " " + "saveAllCollectionToSQLite");
            }
        });


    }

    public void saveDataToFireBase(TimeModel timeModel) {
        String idDocument = collectionReferenceTime.document().getId();
        timeModel.setDocumentId(idDocument);


        executor.execute(new Runnable() {
            @Override
            public void run() {
                timeModelDAO.insert(timeModel);

                Log.i("SUCCESFULLY ADDED", timeModel.toString());
                Log.i(ROOM_DB_LOGGER, ROOM_DB_LOGGER + " " + "saveAllCollectionToSQLite");
            }
        });

        collectionReferenceTime.document(idDocument).set(timeModel).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.i("Succes on adding", timeModel.toString());
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(application, "Fail on adding data", Toast.LENGTH_SHORT).show();
                Log.i(FIREBASE_LOGGER, " " + "saveDataToFireBase");
            }
        });
    }

    public void deleteDataFromRoomDB(TimeModel timeModel) {
        executor.execute(() -> timeModelDAO.delete(timeModel));
    }

    public void deleteDateFromFireBase(TimeModel timeModel) {
        deleteDataFromRoomDB(timeModel);
        Log.i("4 UU username", timeModel.getUserName());
        Log.i("4 UU time begin", timeModel.getTimeBegin());
        Log.i("4 UU time end", timeModel.getTimeEnd());
        Log.i("4 UU time overall", timeModel.getTimeOverallInLong() + "");
        collectionReferenceTime.document(timeModel.getDocumentId()).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.i(FIREBASE_LOGGER, " " + "deleteDateFromFireBase");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.i(FIREBASE_LOGGER + "FAIl", Optional.ofNullable(e.getMessage())+"");
            }
        });
    }

    private void updateUserTime(Map<String, Object> result, TimeModel timeModel) {
        Map<String, Object> resultToSend = new HashMap<>();


        String userEmail = (String) result.get("userEmail");
        //to z usera
        long hoursOverall = (long) result.get("hoursOverall");
        long hoursToSettle = (long) result.get("hoursToSettle");
        long hoursFromTimeModel = (long) result.get("timeOverallFromTimeModel");
        double paycheck = (double) result.get("paycheck");
        //to z modelu

        long hoursOverallToSend = hoursOverall + hoursFromTimeModel;
        long hoursToSettleToSend=0;
        double paycheckToSend =0;

        if(!timeModel.getMoneyOverall())
        {
             hoursToSettleToSend = hoursToSettle + hoursFromTimeModel;

        }
        else {
             hoursToSettleToSend = hoursToSettle;
            paycheckToSend = paycheck -timeModel.getWithdrawnMoney();
            resultToSend.put("paycheck",paycheckToSend);
        }

        if(hoursOverallToSend==hoursToSettleToSend)
        {
            resultToSend.put("paycheck",0);
        }


        resultToSend.put("hoursOverall", hoursOverallToSend);
        resultToSend.put("hoursToSettle", hoursToSettleToSend);

        Log.i("3 UU hoursOverallToSend", hoursOverallToSend + "");
        collectionReferenceUser.document(userEmail).update(resultToSend).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {

                Log.i(FIREBASE_LOGGER, " " + "updateUserTime");
            }
        });
    }

    public void updateDataSQLite(List<TimeModel> list) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                timeModelDAO.updateList(list);
                Log.i(ROOM_DB_LOGGER, "Updated List");
            }
        });
    }

    public void updateDataSQLite(TimeModel timeModel) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                timeModelDAO.updated(timeModel);
                Log.i(ROOM_DB_LOGGER, "Updated TimeModel");
            }
        });
    }

    public void updateDataToFirebase(String documentID, String beginTime, String endTime, String overall, long timeInLong, TimeModel timeModel) {

        Timestamp timestamp = new Timestamp(new Date());
        Map<String, Object> result = new HashMap<>();
        String KEY_TIME_BEGIN = "timeBegin";
        result.put(KEY_TIME_BEGIN, beginTime);
        String KEY_TIME_END = "timeEnd";
        result.put(KEY_TIME_END, endTime);
        String KEY_TIME_OVERALL = "timeOverall";
        result.put(KEY_TIME_OVERALL, overall);
        String KEY_TIME_OVERALL_IN_LONG = "timeOverallInLong";
        result.put(KEY_TIME_OVERALL_IN_LONG, timeInLong);
        result.put("timestamp", timestamp);

        timeModel.setTimeBegin(beginTime);
        timeModel.setTimeEnd(endTime);
        timeModel.setTimeOverall(overall);
        timeModel.setTimeOverallInLong(timeInLong);
        timeModel.setTimestamp(timestamp);
        Log.i("TimeModel updated: ", timeModel.toString());

        Log.i(FIREBASE_LOGGER, " " + "updateDataToFirebase");

        Log.i("3’ UTM username", timeModel.getUserName());
        Log.i("3’ UTM username", timeModel.getTimeBegin());
        Log.i("3’ UTM username", timeModel.getTimeEnd());
        Log.i("3’ UTM username", timeModel.getTimeOverallInLong() + "");


        updateDataSQLite(timeModel);
        collectionReferenceTime.document(documentID).update(result).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
            }
        });

    }

    public void updateStatusOfPayment(String documentID, boolean isSettled, double withDrawnMoney, Timestamp timestamp, TimeModel timeModel) {
        Map<String, Object> result = new HashMap<>();
        String KEY_MONEYOVERALL = "moneyOverall";
        result.put(KEY_MONEYOVERALL, isSettled);
        String WITH_DRAWN_MONEY = "withdrawnMoney";
        result.put(WITH_DRAWN_MONEY, withDrawnMoney);
        String KEY_TIMESTAMP = "timestamp";
        result.put(KEY_TIMESTAMP, timestamp);

        Log.i(LOGER, "updateStatusOfPayment");

        timeModel.setMoneyOverall(isSettled);
        timeModel.setWithdrawnMoney(withDrawnMoney);
        timeModel.setTimestamp(timestamp);

        updateDataSQLite(timeModel);

        collectionReferenceTime.document(documentID).update(result);

        Log.i(FIREBASE_LOGGER, " " + "updateStatusOfPayment");
    }


    public void updateStatusOfTimeForUser(String email, long settledTimeInMillis, double payCheck) {
        Map<String, Object> result = new HashMap<>();

        result.put("paycheck", payCheck);
        result.put("hoursToSettle", settledTimeInMillis);
        Log.i("Firebase", "updateStatusOfTimeForUser");
        collectionReferenceUser.document(email).update(result).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.i(FIREBASE_LOGGER, " " + "updateStatusOfTimeForUser");
            }
        });
    }

    public void getUsersDataAssignedToAdmin() {
        collectionReferenceUser.whereEqualTo("foreign_key", currentUserId).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (!queryDocumentSnapshots.isEmpty()) {
                    List<User> userArrayList = new ArrayList<>();

                    for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {
                        if (currentUserId.equals(snapshot.getString("foreign_key"))) {
                            ifAdminMutableLiveData.postValue(true);

                            User user = snapshot.toObject(User.class);

                            userArrayList.add(user);


                            userArrayListOfUserMutableLiveData.postValue(userArrayList);

                            Log.i(FIREBASE_LOGGER, " " + "getUsersDataAssignedToAdmin");
                            Log.i("User name:", user.getUsername());
                            Log.i("User hoursOverall:", user.getHoursOverall() + "");
                            Log.i("User hoursToSettle: ", user.getHoursToSettle() + "");
                            Log.i("-----", "-----");

                        }

                    }
                }

            }
        });

    }

    public boolean checkIfAdmin() {

        collectionReferenceUser.whereEqualTo("foreign_key", currentUserId).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                if (!queryDocumentSnapshots.isEmpty()) {
                    for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {
                        if (currentUserId.equals(snapshot.getString("foreign_key"))) {
                            ifAdminMutableLiveData.postValue(true);

                        } else {

                            ifAdminMutableLiveData.postValue(false);
                        }
                    }
                    Log.i(FIREBASE_LOGGER, " " + "checkIfAdmin");
                }
            }
        });

        return ifAdmin;
    }

    public void register(String email, String password, String userName_send, String surName_send, String foreign_email) {

        collectionReferenceUser.whereEqualTo("email", foreign_email).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (!value.isEmpty()) {
                    for (QueryDocumentSnapshot snapshot : value) {
                        arrayListForAssigningEmail.add(snapshot.getString("userId"));
                    }

                    Log.i(FIREBASE_LOGGER, " " + "register");


        if(arrayListForAssigningEmail.size()>0) {

            firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {

                        firebaseUserMutableLiveData.postValue(firebaseAuth.getCurrentUser());

                        fireBaseUser = firebaseAuth.getCurrentUser();
                        assert fireBaseUser != null;
                        final String currentUserId = fireBaseUser.getUid();

                        //Aktualny dzień
                        long time = new Date().getTime();
                        //long days = TimeUnit.MILLISECONDS.toDays(time);

                        Duration duration = Duration.ofMillis(time);
                        long daysSinceUnix = duration.toDays();


                        // Create a userMap so we can create user in the User Collection in FireStore
                        Map<String, Object> userObj = new HashMap<>();
                        userObj.put("userId", currentUserId);
                        userObj.put("email", email);
                        userObj.put("username", userName_send);
                        userObj.put("surName", surName_send);
                        userObj.put("foreign_key", arrayListForAssigningEmail.get(0));
                        userObj.put("hoursOverall", 0);
                        userObj.put("hoursToSettle", 0);
                        userObj.put("paycheck", 0);
                        userObj.put("entriesAmount", daysSinceUnix + "_" + "0");

                        isAdminExist.postValue(true);

                        Log.i("isAdmin05","true");

                        Log.i("Hello five", "five");

                        collectionReferenceUser.document(email).set(userObj).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {

                                arrayListForAssigningEmail.clear();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                            }
                        });


                    } else {
                        Toast.makeText(application, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }
        else
        {
            isAdminExist.postValue(false);
            Log.i("isAdmin05","false");
        }

                }


            }
        });

    }


    public void register(String email, String password, String userName_send, String surName_send/*, String foreign_email*/) {

    /*    collectionReferenceUser.whereEqualTo("email", foreign_email).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (!value.isEmpty()) {
                    for (QueryDocumentSnapshot snapshot : value) {
                        arrayListForAssigningEmail.add(snapshot.getString("userId"));
                    }
                    Log.i(FIREBASE_LOGGER, " " + "register");
                }


            }
        });*/

        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    firebaseUserMutableLiveData.postValue(firebaseAuth.getCurrentUser());

                    fireBaseUser = firebaseAuth.getCurrentUser();
                    assert fireBaseUser != null;
                    final String currentUserId = fireBaseUser.getUid();

                    //Aktualny dzień
                    long time = new Date().getTime();
                    //long days = TimeUnit.MILLISECONDS.toDays(time);

                    Duration duration = Duration.ofMillis(time);
                    long daysSinceUnix = duration.toDays();

                    // Create a userMap so we can create user in the User Collection in FireStore
                    Map<String, Object> userObj = new HashMap<>();
                    userObj.put("userId", currentUserId);
                    userObj.put("email", email);
                    userObj.put("username", userName_send);
                    userObj.put("surName", surName_send);
                    userObj.put("foreign_key", currentUserId /*arrayListForAssigningEmail.get(0)*/);
                    userObj.put("hoursOverall", 0);
                    userObj.put("hoursToSettle", 0);
                    userObj.put("paycheck", 0);
                    userObj.put("entriesAmount", daysSinceUnix+"_"+"0");


                    collectionReferenceUser.document(email).set(userObj).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {

                            arrayListForAssigningEmail.clear();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });


                } else {
                    Toast.makeText(application, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void signIn(String email, String password) {

        Log.i(LOGER, "signIn");

        firebaseAuth.signInWithEmailAndPassword(email, password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                firebaseUserMutableLiveData.postValue(firebaseAuth.getCurrentUser());
                Log.i(FIREBASE_LOGGER, " " + "signIn");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(application, application.getString(R.string.no_user), Toast.LENGTH_SHORT).show();

            }
        });
    }

    public void resetPassword(String email) {
        Log.i(LOGER, "resetPassword");

        firebaseAuth.sendPasswordResetEmail(email).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(application, application.getString(R.string.reset_meesage), Toast.LENGTH_SHORT).show();
                Log.i(FIREBASE_LOGGER, " " + "resetPassword");

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(application, application.getString(R.string.wrong_email), Toast.LENGTH_SHORT).show();
            }
        });
    }



    public LiveData<User> getUsernameAndSurname() {
        checkIfAdmin();

        User user = new User();

        collectionReferenceUser.whereEqualTo("userId", currentUserId).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (!queryDocumentSnapshots.isEmpty()) {
                    for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {

                        user.setUsername(snapshot.getString("username"));
                        user.setSurName(snapshot.getString("surName"));
                        user.setForeign_key(snapshot.getString("foreign_key"));
                    }
                    getUsernameAndSurname.postValue(user);
                    Log.i(FIREBASE_LOGGER, " " + "getUsernameAndSurname");
                }
            }
        });


        return getUsernameAndSurname;
    }


    public void singOut() {
        firebaseAuth.signOut();
        userLoggedMutableLiveData.postValue(true);
        Log.i(FIREBASE_LOGGER, " " + "singOut");
    }

}
