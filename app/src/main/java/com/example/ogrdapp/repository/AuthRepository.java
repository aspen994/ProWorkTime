package com.example.ogrdapp.repository;

import android.app.Application;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.ogrdapp.R;
import com.example.ogrdapp.dao.TimeModelDAO;
import com.example.ogrdapp.db.TimeModelDatabase;
import com.example.ogrdapp.model.QRModel;
import com.example.ogrdapp.model.TimeModel;
import com.example.ogrdapp.model.User;
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
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AuthRepository {
    private Application application;
    private MutableLiveData<FirebaseUser> firebaseUserMutableLiveData;
    private MutableLiveData<Boolean> userLoggedMutableLiveData;
    private MutableLiveData<TimeModel> getUsernameAndSurname;
    private MutableLiveData<List<TimeModel>> timeModelArrayListMutableLiveData;
    private MutableLiveData<Boolean> ifAdminMutableLiveData;
    private MutableLiveData<List<User>> userArrayListOfUserMutableLiveData;
    private MutableLiveData<List<TimeModel>> timeForUserListMutableLiveData;
    private MutableLiveData<List<TimeModel>> timeSelectedForUserListMutableLiveData;
    private MutableLiveData<Map<String, Object>> paycheckHoursToSettleMutableLiveData;
    private MutableLiveData<String> emailMutableLiveData;
    private MutableLiveData<String> adminIdMutableLiveData;
    private MutableLiveData<List<QRModel>> qrModelMutableLiveData;
    private MutableLiveData<List<TimeModel>> timeModelMutableLiveData;
    private MutableLiveData<List<TimeModel>> getAllTimeModelsForAdminSQLLiveData;
    private MutableLiveData<Integer> getIntegerHelps;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db;
    private FirebaseUser fireBaseUser;
    private CollectionReference collectionReferenceUser;
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
    private final String KEY_TIMESTAMP = "timestamp";
    private final String LOGER = "FirebaseRepository";

    private final TimeModelDAO timeModelDAO;
    private ExecutorService executor;

    private String ROOM_DB_LOGGER = "ROOM_DB";
    private String FIREBASE_LOGGER = "FIREBASE_DB";


    public AuthRepository(Application application) {

        this.application = application;
        this.firebaseUserMutableLiveData = new MutableLiveData<>();
        this.userLoggedMutableLiveData = new MutableLiveData<>();
        this.getUsernameAndSurname = new MutableLiveData<>();
        this.timeModelArrayListMutableLiveData = new MutableLiveData<>();
        this.arrayListForAssigningEmail = new ArrayList<>();
        this.arrayListIfAdmin = new ArrayList<>();
        this.ifAdminMutableLiveData = new MutableLiveData<>();
        this.userArrayListOfUserMutableLiveData = new MutableLiveData<>();
        this.timeForUserListMutableLiveData = new MutableLiveData<>();
        this.timeSelectedForUserListMutableLiveData = new MutableLiveData<>();
        this.paycheckHoursToSettleMutableLiveData = new MutableLiveData<>();
        this.emailMutableLiveData = new MutableLiveData<>();
        this.adminIdMutableLiveData = new MutableLiveData<>();
        this.qrModelMutableLiveData = new MutableLiveData<>();
        this.getAllTimeModelsForAdminSQLLiveData = new MutableLiveData<>();
        this.getIntegerHelps = new MutableLiveData<>();


        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

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

    public String getUserId() {
        fireBaseUser = firebaseAuth.getCurrentUser();
        assert fireBaseUser != null;
        return fireBaseUser.getUid();
    }

    //getAllTimeModelsForAdminSQLLiveData


    public MutableLiveData<Integer> getGetIntegerHelps() {
        return getIntegerHelps;
    }

    public MutableLiveData<List<TimeModel>> getGetAllTimeModelsForAdminSQLLiveData() {
        return getAllTimeModelsForAdminSQLLiveData;
    }



    public MutableLiveData<List<TimeModel>> getTimeModelArrayListMutableLiveData() {
        return timeModelArrayListMutableLiveData;
    }

    public LiveData<List<TimeModel>> getGetAllTimeModelsForAdminSQLLiveData(String userId) {
        return timeModelDAO.getAllTimeModels(userId);
    }

    public MutableLiveData<List<QRModel>> getQrModelMutableLiveData() {
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


    public MutableLiveData<FirebaseUser> getFirebaseUserMutableLiveData() {
        return firebaseUserMutableLiveData;
    }

    public MutableLiveData<Boolean> getUserLoggedMutableLiveData() {
        return userLoggedMutableLiveData;
    }

    public MutableLiveData<TimeModel> getGetUsernameAndSurname() {
        return getUsernameAndSurname;
    }

    public MutableLiveData<Map<String, Object>> getPaycheckHoursToSettleMutableLiveData() {
        return paycheckHoursToSettleMutableLiveData;
    }

    public FirebaseAuth getFirebaseAuth() {
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
    public void setNewQrCode(Map<String, Object> qrCodeMap) {
        collectionReferenceQrCode.document().set(qrCodeMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                //Log.i(LOGER, "Added Succesfully");
                LinkedList<QRModel> qrModelLinkedList = new LinkedList<>();

                QRModel qrModel = new QRModel();
                qrModel.setDelay((int) qrCodeMap.get("delay"));
                qrModel.setAdminId((String) qrCodeMap.get("idAdmin"));
                qrModel.setQRCode((String) qrCodeMap.get("QRCode"));

                qrModelLinkedList.add(qrModel);

                qrModelMutableLiveData.postValue(qrModelLinkedList);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.i("Fail setNewCodeQR", e.getMessage().toString());
            }
        });
    }

    //getTimeSelectedForUserListMutableLiveData - mutable live data for this below.
    public void getSelectedTimeForUser(String userId, String dateRange) {

        //decoderForDate(dateRange);

        collectionReferenceTime.whereEqualTo("id", userId)
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
                            timeSelectedForUserListMutableLiveData.setValue(timeModelArrayList);

                            //Log.i(LOGER, "getSelectedTimeForUser");
                        }
                    }
                });
    }

    public void listenChangeForUser() {
    /*    collectionReferenceTime.document(nazwaDokumentu).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                value
            }
        })*/
    }


    // TODO 170124
    public LiveData<List<TimeModel>> getAllTimeModelsForAdminSQL(String userId)
    {
        Log.i("STRING UserID",userId);
        executor.execute(new Runnable() {
            @Override
            public void run() {
                Log.i("ITERACJA POCZĄTEK","Iteracja Początek");
                List<TimeModel> value = timeModelDAO.getAllTimeModelsList(userId);
                Log.i("value list: ",value.size()+"");
                Log.i("ForAdminSQL user:", userId);

                //TODO 250124 Tutaj daj dwie metody
                getTimeForUserNewMethod(userId,countMethod(value));
                getAllTimeModelsForAdminSQLLiveData.postValue(value);

                readerList(value);
                Log.i(ROOM_DB_LOGGER,"ROOM_DB"+" "+"getAllTimeModelsForAdminSQL");

                Log.i("ITERACJA KONIEC","Iteracja KONIEC");
            }


        });

        return timeModelDAO.getAllTimeModels(userId);

    }

    public void readerList(List<TimeModel> value) {
        for (TimeModel timeModel:value) {
            Log.i("ForAdminSQL value name:",timeModel.getUserName()==null?"null USERNAME":timeModel.getUserName());
            Log.i("ForAdminSQL value timeB",timeModel.getTimeBegin()==null?"null TIMEBEGIn":timeModel.getTimeBegin());
            Log.i("ForAdminSQL timeE:",timeModel.getTimeEnd()==null?"null":timeModel.getTimeEnd());
            Log.i("ForAdminSQL timestamp:",timeModel.getTimestamp().toDate()==null?"null":timeModel.getTimestamp().toDate()+"");
            Log.i("ForAdminSQL documentId:",timeModel.getDocumentId()==null?"null":timeModel.getDocumentId()+"");
            Log.i("---------","----------");
        }
    }

    public void getTimeForUserNewMethod(String userId, Timestamp timestamp) {

        collectionReferenceTime.whereEqualTo("id", userId).whereGreaterThan("timestamp",timestamp)
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
                                Log.i("getTimeForUser Id",timeModel.getDocumentId());
                                Log.i("getTimeForUser timeStam",timestamp.toDate()+"");
                            }
                            saveAllCollectionToSQLite(timeModelArrayList);
                            updateDataSQLite(timeModelArrayList);
                            Log.i(FIREBASE_LOGGER," "+"getTimeForUserNewMethod");
                        }



                    }
                });

    }

    // TODO 170124
    public void getTimeForUserNewMethod(String userId) {

        collectionReferenceTime.whereEqualTo("id", userId)
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

                                //Log.i("getTimeForUser UserName", timeModel.getUserName());
                                //Log.i("getTimeForUser TimeAded", timeModel.getTimeAdded().toString());
                                //Log.i("getTimeForUser Id",timeModel.getDocumentId());

                            }
                            timeForUserListMutableLiveData.setValue(timeModelArrayList);
                            Log.i(FIREBASE_LOGGER," "+"getTimeForUserNewMethod2");
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

                            List<QRModel> qrModelLink = new LinkedList<>();

                            for (QueryDocumentSnapshot qrModels : queryDocumentSnapshots) {

                                QRModel qrModel = qrModels.toObject(QRModel.class);


                                qrModelLink.add(qrModel);

                            }
                            qrModelMutableLiveData.postValue(qrModelLink);
                            Log.i(FIREBASE_LOGGER," "+"getDataQRCode");
                        }

                    }
                });

        return qrModelMutableLiveData;
    }

    //TODO 250124
    public  void getTimeForUserNewMethod()
    {
        collectionReferenceUser.whereEqualTo("userId",currentUserId).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (!queryDocumentSnapshots.isEmpty()) {

                    for (QueryDocumentSnapshot users : queryDocumentSnapshots) {
                        User user = users.toObject(User.class);
                        //Log.i("GetTimeForUser",user.getHoursOverall()+"");
                        user.getHoursToSettle();
                    }
                }
            }
        });

    }
    public LiveData<List<TimeModel>> getAllTimeModelsForUserSQL()
    {
/*
        executor.execute(new Runnable() {
            @Override
            public void run() {
                List<TimeModel> value = timeModelDAO.getAllTimeModelsList(currentUserId);
                //TODO 250124 Tutaj daj dwie metody
                // Ta metoda do ściagania hoursOverall dla Users
                readLogcat(value);
                Log.i(ROOM_DB_LOGGER,ROOM_DB_LOGGER+" "+"getAllTimeModelsForUserSQL");
                getDataFirebase(countMethod(value));
            }
        });*/
        executor.execute(new Runnable() {
            @Override
            public void run() {
                List<TimeModel> value = timeModelDAO.getAllTimeModelsList(currentUserId);
                //TODO 250124 Tutaj daj dwie metody
                // Ta metoda do ściagania hoursOverall dla Users
                //readLogcat(value);
                Log.i(ROOM_DB_LOGGER,ROOM_DB_LOGGER+" "+"getAllTimeModelsForUserSQL");
                getDataFirebase(countMethod(value));

        //TODO 250124 nowa metoda

                List<TimeModel> value2 = timeModelDAO.getAllTimeModelsList(currentUserId);
        collectionReferenceUser.whereEqualTo("userId",currentUserId).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (!queryDocumentSnapshots.isEmpty()) {
                    for (QueryDocumentSnapshot users : queryDocumentSnapshots) {
                        User user = users.toObject(User.class);
                        long hoursToSettle = user.getHoursOverall();
                        long timeModelSumForTimeOverall = sumUp(value2);
                        Log.i("FROM USER VALUE",hoursToSettle+"");
                        Log.i("FROM TimeModel VALUE",timeModelSumForTimeOverall+"");

                        if(hoursToSettle!=timeModelSumForTimeOverall) {
                            //Log.i("Second", "The sum control is the Difrent");
                            executor.execute(new Runnable() {
                                @Override
                                public void run() {
                                    timeModelDAO.deleteAllTimeModels();
                                    getDataFirebase(new Timestamp(new Date(0)));
                                    Log.i("Deleting and Adding","delete and add");
                                }
                            });
                        }
                        else {
                            //Log.i("First","The sum control is the same");
                        }

                    }
                }
            }
        });

            }
        });


        return timeModelDAO.getAllTimeModels(currentUserId);
    }

    private long sumUp(List<TimeModel> value) {
        long toReturn=0;
        for(TimeModel timeModel:value)
        {
            toReturn+=timeModel.getTimeOverallInLong();
        }

        return toReturn;
    }

    private void readLogcat(List<TimeModel> value) {
        long sum=0;
        for(TimeModel timeModel: value)
        {
            sum+=timeModel.getTimeOverallInLong();
            //Log.i("TimeModel",timeModel.getDocumentId());
            //Log.i("TimeModel time",timeModel.getTimeOverallInLong()+"");
        }

        //Log.i("SZYMON SPÓJRZ",sum+"");
    }

    public Timestamp countMethod(List<TimeModel> timeModels) {
        long max = 0;
        if(!timeModels.isEmpty()) {
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

    public void deleteAllTimeModels()
    {
        executor.execute(()->timeModelDAO.deleteAllTimeModels());
      //  Log.i("DELETE ALL","OVER HERE MAN");
    }

     public void getDataFirebase(Timestamp timestamp) {



       // Log.i("getDataFirebase","invoked");
         // TODO 170124 - Properly working downloadData
         collectionReferenceTime.whereEqualTo("id", currentUserId).whereGreaterThan("timestamp",timestamp)
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
                                Log.i("PRINT5: ", timeModel.getTimeAdded().toDate()+"");
                                Log.i("------", "--------");


                            }
                            timeModelArrayListMutableLiveData.postValue(timeModelArrayList);
                            saveAllCollectionToSQLite(timeModelArrayList);
                            updateDataSQLite(timeModelArrayList);
                            Log.i(FIREBASE_LOGGER, " "+"getDataFirebase");
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
                      @Override
                      public void onFailure(@NonNull Exception e) {
                          Log.i("Fail getData",e.getMessage());
                      }
                  });

    }

    // I tutaj
    public void updatedDataHoursToFirebaseUser(TimeModel timeModel) {
        collectionReferenceUser.whereEqualTo("userId", timeModel.getId()).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (!queryDocumentSnapshots.isEmpty()) {
                    for (QueryDocumentSnapshot user : queryDocumentSnapshots) {
                        User user1 = user.toObject(User.class);

                        Map<String, Object> result = null;

                        result = new HashMap<>();

                        result.put("userEmail", user1.getEmail());
                        //to z danych
                        result.put("timeOverallFromTimeModel", timeModel.getTimeOverallInLong());
                        //to z usera
                        result.put("hoursOverall", user1.getHoursOverall());

                        result.put("hoursToSettle", user1.getHoursToSettle());

                        Log.i("2 UU userName",user1.getUsername()+"");
                        Log.i("2 UU hoursOverall",user1.getHoursOverall()+"");
                        Log.i("2 UTM timeOverall ",timeModel.getTimeOverallInLong()+"");

                        Log.i(FIREBASE_LOGGER, " "+"updatedDataHoursToFirebaseUser");

                        updateUserTime(result);
                    }
                }


            }
        });
    }

    // Nie jest to używane można usunąć.
    public void getPaycheckAndHoursToSettleLong(String userId) {
        collectionReferenceUser.whereEqualTo("userId", userId).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (!value.isEmpty()) {
                    for (QueryDocumentSnapshot snapshot : value) {
                        Long hoursToSettle = snapshot.getLong("hoursToSettle");
                        long paycheck = (long) snapshot.get("paycheck");


                        Map<String, Object> result = new HashMap<>();

                        result.put("paycheck", paycheck);
                        result.put("hoursToSettle", hoursToSettle);

                        Log.i(LOGER, "getPaycheckAndHoursToSettleLong");

                        paycheckHoursToSettleMutableLiveData.postValue(result);

                    }
                    Log.i(FIREBASE_LOGGER, " "+"getPaycheckAndHoursToSettleLong");
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
                        double paycheck = snapshot.getDouble("paycheck");
                        String email = snapshot.getString("email");

                        Map<String, Object> result = new HashMap<>();

                        result.put("paycheck", paycheck);
                        result.put("hoursToSettle", hoursToSettle);
                        result.put("email", email);


                        paycheckHoursToSettleMutableLiveData.postValue(result);

                    }
                    Log.i(FIREBASE_LOGGER, " "+"getDataToUpdatePayCheck");
                }

            }
        });
    }

    public void saveAllCollectionToSQLite(List<TimeModel>timeModelList)
    {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                timeModelDAO.insertAllCollection(timeModelList);
            }
        });
        Log.i(ROOM_DB_LOGGER, ROOM_DB_LOGGER+" "+"saveAllCollectionToSQLite");

    }

    public void saveDataToFireBase(TimeModel timeModel) {
        String idDocument = collectionReferenceTime.document().getId();
        timeModel.setDocumentId(idDocument);
        String id = timeModel.getId();

        timeModel.getTimeOverallInLong();

        executor.execute(new Runnable() {
            @Override
            public void run() {
                timeModelDAO.insert(timeModel);

                Log.i("SUCCESFULLY ADDED", timeModel.toString());
                Log.i(ROOM_DB_LOGGER, ROOM_DB_LOGGER+" "+"saveAllCollectionToSQLite");
            }
        });

        collectionReferenceTime.document(idDocument).set(timeModel).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.i("Succes on adding",timeModel.toString());
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(application, "Fail on adding data", Toast.LENGTH_SHORT).show();
                Log.i(FIREBASE_LOGGER, " "+"saveDataToFireBase");
            }
        });
    }

    public void checkMethod()
    {
        Query limit = collectionReferenceTime.orderBy("timeAdded", Query.Direction.DESCENDING).limit(1);

        limit.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful())
                {
                    QuerySnapshot document = task.getResult();

                    for (QueryDocumentSnapshot timeModel: document)
                    {
                        TimeModel timeModel1 = timeModel.toObject(TimeModel.class);
                        Log.i("TIME MODEL checkMethod", timeModel1.getTimeEnd());
                    }

                    Log.i(FIREBASE_LOGGER, " "+"checkMethod");
                }

            }
        });
     /*   collectionReferenceTime.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if(!value.isEmpty())
                {
                    for(QueryDocumentSnapshot snapshot:value)
                    {
                        TimeModel timeModel = snapshot.toObject(TimeModel.class);
                        Log.i("ONE ADDED ROW",timeModel.getTimeEnd());
                    }
                }
            }
        });*/

/*
        collectionReferenceTime.whereEqualTo("id", "userId")
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

                            }
                            timeForUserListMutableLiveData.setValue(timeModelArrayList);
                        }

                        Log.i(LOGER, "getTimeForUser");

                    }
                });*/
    }
    public void deleteDataFromRoomDB(TimeModel timeModel){
        executor.execute(()->timeModelDAO.delete(timeModel));
    }

    public void deleteLastRecordsMETHODTODELTE()
    {

        collectionReferenceTime
                .whereEqualTo("id",currentUserId)
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if(!queryDocumentSnapshots.isEmpty())
                        {
                            for(QueryDocumentSnapshot snapshot:queryDocumentSnapshots)
                            {
                                    TimeModel timeModel= snapshot.toObject(TimeModel.class);
                                    collectionReferenceTime.document(timeModel.getDocumentId()).delete();
//                                    Log.i("timeModel to Delte",timeModel.getTimestamp().toDate()+"");
  //                                  Log.i("TIME STAMP DElte",new Timestamp(new Date(1706095680000L)).toDate()+"");

                            }

                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i("SZYMON KURWA",e.getMessage());
                    }
                });
    }
    public void deleteDateFromFireBase(TimeModel timeModel)
    {
        deleteDataFromRoomDB(timeModel);
      /*  Log.i("4 UU username",timeModel.getUserName());
        Log.i("4 UU time begin",timeModel.getTimeBegin());
        Log.i("4 UU time end",timeModel.getTimeEnd());
        Log.i("4 UU time overall",timeModel.getTimeOverallInLong()+"");*/
        collectionReferenceTime.document(timeModel.getDocumentId()).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.i(FIREBASE_LOGGER, " "+"deleteDateFromFireBase");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
            Log.i(FIREBASE_LOGGER + "FAIl", e.getMessage());
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

        Log.i("3 UU hoursOverallToSend",hoursOverallToSend+"");
        //Log.i(LOGER,"updateUserTime");
        collectionReferenceUser.document(userEmail).update(resultToSend).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {

                Log.i(FIREBASE_LOGGER, " "+"updateUserTime");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }

    public void updateDataSQLite(List<TimeModel> list)
    {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                timeModelDAO.updateList(list);
            }
        });
    }

    public void updateDataSQLite(TimeModel timeModel)
    {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                timeModelDAO.updated(timeModel);
            }
        });
    }

    public void updateDataToFirebase(String documentID,String beginTime,String endTime,String overall,long timeInLong,TimeModel timeModel)
    {

        Timestamp timestamp = new Timestamp(new Date());
        Map<String,Object> result = new HashMap<>();
        result.put(KEY_TIME_BEGIN,beginTime);
        result.put(KEY_TIME_END,endTime);
        result.put(KEY_TIME_OVERALL,overall);
        result.put(KEY_TIME_OVERALL_IN_LONG,timeInLong);
        //TODO 230124
        result.put("timestamp",timestamp);

        //TODO 230124
        timeModel.setTimeBegin(beginTime);
        timeModel.setTimeEnd(endTime);
        timeModel.setTimeOverall(overall);
        timeModel.setTimeOverallInLong(timeInLong);
        timeModel.setTimestamp(timestamp);
        Log.i("TimeModel updated: " ,timeModel.toString());

        Log.i(FIREBASE_LOGGER, " "+"updateDataToFirebase");

        Log.i("3’ UTM username",timeModel.getUserName());
        Log.i("3’ UTM username",timeModel.getTimeBegin());
        Log.i("3’ UTM username",timeModel.getTimeEnd());
        Log.i("3’ UTM username",timeModel.getTimeOverallInLong()+"");

        //TODO 230124
        updateDataSQLite(timeModel);
        collectionReferenceTime.document(documentID).update(result).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
            }
        });

    }

    public void updateStatusOfPayment(String documentID,boolean isSettled,double withDrawnMoney,Timestamp timestamp)
    {
        Map<String,Object> result = new HashMap<>();
        result.put(KEY_MONEYOVERALL,isSettled);
        result.put(WITH_DRAWN_MONEY,withDrawnMoney);
        result.put(KEY_TIMESTAMP,timestamp);

        Log.i(LOGER,"updateStatusOfPayment");

        collectionReferenceTime.document(documentID).update(result);

        Log.i(FIREBASE_LOGGER, " "+"updateStatusOfPayment");
    }


    public void updateStatusOfTimeForUser(String email, long settledTimeInMillis, double payCheck)
    {
                            Map<String, Object> result = new HashMap<>();

                            result.put("paycheck",payCheck);
                            result.put("hoursToSettle", settledTimeInMillis);
                            Log.i("Firebase","updateStatusOfTimeForUser");
                        collectionReferenceUser.document(email).update(result).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Log.i(FIREBASE_LOGGER, " "+"updateStatusOfTimeForUser");
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

        collectionReferenceUser.whereEqualTo("foreign_key",currentUserId).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
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
                    Log.i(FIREBASE_LOGGER, " "+"getUserDataAssignedToAdmin");
                }
            }
        });
    }

    //  Było add snapshotListener zmieniam na add on SuccesListenr
    //TODO 260124
    public void getUsersDataAssignedToAdmin()
    {
        collectionReferenceUser.whereEqualTo("foreign_key",currentUserId).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
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

                            Log.i(FIREBASE_LOGGER, " "+"getUsersDataAssignedToAdmin");
                            Log.i("User name:",user.getUsername());
                            Log.i("User hoursOverall:",user.getHoursOverall()+"");
                            Log.i("User hoursToSettle: ",user.getHoursToSettle()+"");
                            Log.i("-----","-----");

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

        collectionReferenceUser.whereEqualTo("foreign_key",currentUserId).addSnapshotListener(new EventListener<QuerySnapshot>() {
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
                        Log.i(FIREBASE_LOGGER, " "+"checkIfAdmin");
                    }


            }
        });

        return ifAdmin;
    }



    public void register (String email, String password,String userName_send,String surName_send,String foreign_email)
    {

        collectionReferenceUser.whereEqualTo("email",foreign_email).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if(!value.isEmpty())
                {
                    for(QueryDocumentSnapshot snapshot: value)
                    {
                        arrayListForAssigningEmail.add(snapshot.getString("userId"));
                    }
                    Log.i(FIREBASE_LOGGER, " "+"register");
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
                Log.i(FIREBASE_LOGGER, " "+"signIn");
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
                Log.i(FIREBASE_LOGGER, " "+"resetPassword");

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(application, application.getString(R.string.wrong_email), Toast.LENGTH_SHORT).show();
            }
        });
    }

    //TODO 290124
    public LiveData<TimeModel> getUsernameAndSurname()
    {
        checkIfAdmin();
        TimeModel timeModel = new TimeModel();
        collectionReferenceUser.whereEqualTo("userId",currentUserId).addSnapshotListener(new EventListener<QuerySnapshot>() {
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

                    getUsernameAndSurname.postValue(timeModel);
                    Log.i(FIREBASE_LOGGER, " "+"getUsernameAndSurname");
                }
                else {

                    Toast.makeText(application, error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        return getUsernameAndSurname;
    }



    public void singOut(){
        firebaseAuth.signOut();
        userLoggedMutableLiveData.postValue(true);
        Log.i(FIREBASE_LOGGER, " "+"singOut");
    }

}
