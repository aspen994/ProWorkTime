package com.osinTechInnovation.ogrdapp.repository;


import static com.osinTechInnovation.ogrdapp.view.UserOverall.round;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.osinTechInnovation.ogrdapp.App;
import com.osinTechInnovation.ogrdapp.R;
import com.osinTechInnovation.ogrdapp.UserMainActivity;
import com.osinTechInnovation.ogrdapp.dao.TimeModelDAO;
import com.osinTechInnovation.ogrdapp.db.TimeModelDatabase;
import com.osinTechInnovation.ogrdapp.model.QRModel;
import com.osinTechInnovation.ogrdapp.model.TimeModel;
import com.osinTechInnovation.ogrdapp.model.User;
import com.osinTechInnovation.ogrdapp.utility.DecodeDaysAndEntries;
import com.osinTechInnovation.ogrdapp.utility.SingleLiveEvent;

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
    private final MutableLiveData<User> getUsernameAndSurnameLiveData;
    private final MutableLiveData<User> getUsernameAndSurname2LiveData;
    private final MutableLiveData<List<TimeModel>> timeModelArrayListMutableLiveData;
    private final MutableLiveData<Boolean> ifAdminMutableLiveData;
    private final MutableLiveData<List<User>> userArrayListOfUserMutableLiveData;
    private final MutableLiveData<List<TimeModel>> timeForUserListMutableLiveData;


    private final MutableLiveData<Map<String, Object>> paycheckHoursToSettleMutableLiveData;
    private final MutableLiveData<String> emailMutableLiveData;
    private final MutableLiveData<String> adminIdMutableLiveData;
    private final MutableLiveData<List<QRModel>> qrModelMutableLiveData;
    private final MutableLiveData<List<TimeModel>> getAllTimeModelsForAdminSQLLiveData;
    private final SingleLiveEvent<Map<String, Object>> singleLiveEvent;
    private final SingleLiveEvent<Map<String, Object>> singleLiveEventStart;

    private MutableLiveData<String> getAmountEntriesSnapshotListenerLiveData;
    private MutableLiveData<Boolean> isAdminExistLiveData;
    private MutableLiveData<String> getDeviceIdLiveData;
    private MutableLiveData<Boolean> youCanCheckNowLiveData;
    private MutableLiveData<String> checkSubscriptionLiveData;
    private MutableLiveData<Boolean> valueToOpenDialogLiveData;
    private MutableLiveData<Boolean> isUserInDBLiveDataLiveData;

    private  FirebaseAuth firebaseAuth;
    private FirebaseUser fireBaseUser;
    private final CollectionReference collectionReferenceUser;
    private final CollectionReference collectionReferenceTime;
    private final CollectionReference collectionReferenceQrCode;
    private String currentUserId;
    private final List<String> arrayListForAssigningEmail;
    private final String LOGGER = "FirebaseRepository";

    private final TimeModelDAO timeModelDAO;
    private final ExecutorService executor;

    private final String ROOM_DB_LOGGER = "ROOM_DB";
    private final String FIREBASE_LOGGER = "FIREBASE_DB";
    private User user;
    public String email;
    public String orderIdField = "Nothing";
    public Observer<String> observer;
    private long sumOfTime = 0;
    private long sumOfTimeSettle = 0;
    private double sumOfPaidMoney = 0;
    private String currentUserEmail = "";


    public AuthRepository(Application application) {

        this.application = application;
        this.firebaseUserMutableLiveData = new MutableLiveData<>();
        this.userLoggedMutableLiveData = new MutableLiveData<>();
        this.getUsernameAndSurnameLiveData = new MutableLiveData<>();
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
        this.getUsernameAndSurname2LiveData = new MutableLiveData<>();
        this.isAdminExistLiveData = new MutableLiveData<>();
        this.getDeviceIdLiveData = new MutableLiveData<>();
        this.youCanCheckNowLiveData = new MutableLiveData<>();
        this.checkSubscriptionLiveData = new MutableLiveData<>();
        this.valueToOpenDialogLiveData = new MutableLiveData<>();
        this.isUserInDBLiveDataLiveData = new MutableLiveData<>();
        this.getAmountEntriesSnapshotListenerLiveData = new MutableLiveData<>();
        this.singleLiveEvent = new SingleLiveEvent<>();
        this.singleLiveEventStart = new SingleLiveEvent<>();

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
            isUserInDB(fireBaseUser.getEmail());
            firebaseUserMutableLiveData.postValue(firebaseAuth.getCurrentUser());
            currentUserId = fireBaseUser.getUid();
            adminIdMutableLiveData.postValue(currentUserId);
            getEmail();
        }

        user = new User();


    }

    public MutableLiveData<String> getGetAmountEntriesSnapshotListenerLiveData() {
        return getAmountEntriesSnapshotListenerLiveData;
    }

    public SingleLiveEvent<Map<String, Object>> getSingleLiveEventStart() {
        return singleLiveEventStart;
    }

    public SingleLiveEvent<Map<String, Object>> getSingleLiveEvent() {
        return singleLiveEvent;
    }

    public MutableLiveData<Boolean> getIsUserInDBLiveDataLiveData() {
        return isUserInDBLiveDataLiveData;
    }

    public MutableLiveData<Boolean> getValueToOpenDialogLiveData() {
        return valueToOpenDialogLiveData;
    }

    public MutableLiveData<String> getCheckSubscriptionLiveData() {
        return checkSubscriptionLiveData;
    }

    public MutableLiveData<Boolean> getYouCanCheckNowLiveData() {
        return youCanCheckNowLiveData;
    }

    public MutableLiveData<Boolean> getIsAdminExistLiveData() {
        return isAdminExistLiveData;
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

    public MutableLiveData<User> getGetUsernameAndSurnameLiveData() {
        return getUsernameAndSurnameLiveData;
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

    public void isUserInDB(String email) {
        collectionReferenceUser.whereEqualTo("email", email).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                if (!queryDocumentSnapshots.isEmpty()) {
                    isUserInDBLiveDataLiveData.postValue(true);
                } else {
                    isUserInDBLiveDataLiveData.postValue(false);
                }
            }
        });
    }

    public void updateAmountEntries(String updateAmountEntries) {
        Map<String, Object> mapa = new HashMap<>();

        mapa.put("entriesAmount", updateAmountEntries);

        collectionReferenceUser.document(fireBaseUser.getEmail()).update(mapa);
    }

    public MutableLiveData<String> getEmail() {
        collectionReferenceUser.whereEqualTo("userId", currentUserId).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (!queryDocumentSnapshots.isEmpty()) {
                    for (QueryDocumentSnapshot user : queryDocumentSnapshots) {
                        User user1 = user.toObject(User.class);
                        emailMutableLiveData.postValue(user1.getEmail());
                        email = user1.getEmail();
                    }
                }
            }
        });

        return emailMutableLiveData;
    }


    public String getUserId() {
        fireBaseUser = firebaseAuth.getCurrentUser();
        assert fireBaseUser != null;
        return fireBaseUser.getUid();
    }


    public void updateEntriesAmount(TimeModel timeModel) {
        long time1 = timeModel.getTimeAdded().toDate().getTime();

        Duration duration1 = Duration.ofMillis(time1);
        int daysSinceUnixTimeModel = (int) duration1.toDays();

        long time = new Date().getTime();

        Duration duration = Duration.ofMillis(time);
        int daysSinceUnixCurrent = (int) duration.toDays();

        DecodeDaysAndEntries decodeDaysAndEntries = new DecodeDaysAndEntries();


        collectionReferenceUser.whereEqualTo("userId", timeModel.getId()).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (!queryDocumentSnapshots.isEmpty()) {
                    for (QueryDocumentSnapshot user : queryDocumentSnapshots) {

                        User user1 = user.toObject(User.class);
                        String entriesAmount = user1.getEntriesAmount();
                        String userEmail = user1.getEmail();

                        if (daysSinceUnixTimeModel == daysSinceUnixCurrent) {

                            int amountEntriesDecoded = decodeDaysAndEntries.decodeToAmountEntries(entriesAmount);
                            int dayDecoded = decodeDaysAndEntries.decodeDays(entriesAmount);

                            amountEntriesDecoded--;

                            Map<String, Object> mapa = new HashMap<>();

                            mapa.put("entriesAmount", dayDecoded + "_" + amountEntriesDecoded);

                            collectionReferenceUser.document(userEmail).update(mapa);
                        }
                    }
                }
            }
        });


    }

    public void updateUserWithSubs(String orderId) {
        Map<String, Object> resultToSend = new HashMap<>();
        resultToSend.put("subsOrderId", orderId);
        collectionReferenceUser.document(email).update(resultToSend);
    }


    public void isSubscriptionAlreadyExist(String orderId) {
        collectionReferenceUser.whereEqualTo("subsOrderId", orderId).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (QueryDocumentSnapshot user : queryDocumentSnapshots) {
                    User user1 = user.toObject(User.class);
                    orderIdField = orderId;

                    if (!queryDocumentSnapshots.isEmpty()) {
                        checkSubscriptionLiveData.postValue(user1.getEmail());
                    } else {
                        checkSubscriptionLiveData.postValue("Null");
                    }
                }
            }
        }).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                if (orderIdField.equals("Nothing")) {

                    collectionReferenceUser.whereEqualTo("userId", currentUserId).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                            for (QueryDocumentSnapshot user : queryDocumentSnapshots) {

                                User user1 = user.toObject(User.class);
                                Map<String, Object> mapa = new HashMap<>();

                                observer = new Observer<String>() {
                                    @Override
                                    public void onChanged(String s) {

                                        if (s.equals(user1.getEmail())) {

                                            checkSubscriptionLiveData.postValue(user1.getEmail());
                                            mapa.put("subsOrderId", orderId);

                                            collectionReferenceUser.document(email).update(mapa).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {

                                                }
                                            });

                                        }
                                    }
                                };

                                if (!queryDocumentSnapshots.isEmpty()) {
                                    emailMutableLiveData.observeForever(observer);
                                }
                            }

                            emailMutableLiveData.removeObserver(observer);
                        }
                    });
                }
                orderIdField = "Nothing";
            }
        });
    }

    public SingleLiveEvent<Map<String, Object>> getAmountEntriesStart(String id) {

        collectionReferenceUser.whereEqualTo("userId", id).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                if (!queryDocumentSnapshots.isEmpty()) {
                    Map<String, Object> mapa = new HashMap<>();
                    for (QueryDocumentSnapshot user : queryDocumentSnapshots) {

                        User user1 = user.toObject(User.class);

                        String entriesAmount = user1.getEntriesAmount();
                        String email = user1.getEmail();

                        mapa.put("entriesAmount", entriesAmount);
                        mapa.put("email", email);
                    }
                    singleLiveEventStart.postValue(mapa);
                }
            }
        });
        return singleLiveEventStart;

    }


    public SingleLiveEvent<Map<String, Object>> getAmountEntries(String id) {

        collectionReferenceUser.whereEqualTo("userId", id).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                if (!queryDocumentSnapshots.isEmpty()) {
                    Map<String, Object> mapa = new HashMap<>();
                    for (QueryDocumentSnapshot user : queryDocumentSnapshots) {

                        User user1 = user.toObject(User.class);

                        String entriesAmount = user1.getEntriesAmount();
                        String email = user1.getEmail();

                        mapa.put("entriesAmount", entriesAmount);
                        mapa.put("email", email);
                    }
                    singleLiveEvent.postValue(mapa);
                }
            }
        });

        return singleLiveEvent;

    }

    public LiveData<String> getDeviceId() {
        collectionReferenceUser.whereEqualTo("userId", currentUserId).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (!value.isEmpty()) {
                    for (QueryDocumentSnapshot snapshot : value) {
                        getDeviceIdLiveData.postValue(snapshot.getString("deviceId"));
                    }
                }

            }
        });
        return getDeviceIdLiveData;
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
        });


    }


    public void getDataFirebase(Timestamp timestamp) {

        collectionReferenceTime.whereEqualTo("id", currentUserId).whereGreaterThan("timestamp", timestamp)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        if (!queryDocumentSnapshots.isEmpty()) {
                            sumOfTime = 0;
                            sumOfPaidMoney = 0;
                            sumOfTimeSettle = 0;

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
                                sumOfTime += timeModel.getTimeOverallInLong();
                                if (timeModel.getMoneyOverall()) {
                                    sumOfTimeSettle += timeModel.getTimeOverallInLong();
                                    sumOfPaidMoney += timeModel.getWithdrawnMoney();
                                }

                            }
                            timeModelArrayListMutableLiveData.postValue(timeModelArrayList);
                            saveAllCollectionToSQLite(timeModelArrayList);
                            updateDataSQLite(timeModelArrayList);
                            Map<String, Object> result = new HashMap<>();
                            result.put("hoursOverall", sumOfTime);
                            result.put("hoursToSettle", sumOfTime - sumOfTimeSettle);
                            result.put("paycheck", round(sumOfPaidMoney, 2));
                            collectionReferenceUser.document(email).update(result);
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

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
                            collectionReferenceUser.whereEqualTo("userId", userId).get().addOnSuccessListener((QuerySnapshot document) -> {
                                if (!document.isEmpty()) {
                                    TimeModel timeModel = null;
                                    sumOfTime = 0;
                                    sumOfPaidMoney = 0;
                                    sumOfTimeSettle = 0;
                                    currentUserEmail = "";
                                    for (QueryDocumentSnapshot user : document) {
                                        User user1 = user.toObject(User.class);
                                        currentUserEmail = user1.getEmail();

                                        List<TimeModel> timeModelArrayList = new ArrayList<>();
                                        for (QueryDocumentSnapshot timeModels : queryDocumentSnapshots) {

                                            timeModel = timeModels.toObject(TimeModel.class);

                                            timeModelArrayList.add(timeModel);
                                            Collections.sort(timeModelArrayList, new Comparator<TimeModel>() {
                                                @Override
                                                public int compare(TimeModel o1, TimeModel o2) {
                                                    return o1.getTimeAdded().compareTo(o2.getTimeAdded());
                                                }
                                            });
                                            sumOfTime += timeModel.getTimeOverallInLong();
                                            if (timeModel.getMoneyOverall()) {
                                                sumOfTimeSettle += timeModel.getTimeOverallInLong();
                                                sumOfPaidMoney += timeModel.getWithdrawnMoney();
                                            }
                                        }
                                        saveAllCollectionToSQLite(timeModelArrayList);

                                        updateDataSQLite(timeModelArrayList);

                                        Map<String, Object> result = new HashMap<>();
                                        result.put("hoursOverall", sumOfTime);
                                        result.put("hoursToSettle", sumOfTime - sumOfTimeSettle);
                                        result.put("paycheck", round(sumOfPaidMoney, 2));

                                        collectionReferenceUser.document(currentUserEmail).update(result);

                                    }
                                }
                            });
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
                        }
                    }
                });

        return qrModelMutableLiveData;
    }


    public LiveData<List<TimeModel>> getAllTimeModelsForAdminSQL(String userId) {

        executor.execute(new Runnable() {
            @Override
            public void run() {

                List<TimeModel> value = timeModelDAO.getAllTimeModelsList(userId);

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
                                    }

                                    saveAllCollectionToSQLite(timeModelArrayList);

                                    updateDataSQLite(timeModelArrayList);

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

                                                        if (hoursToSettle != timeModelSumForTimeOverall) {

                                                            timeModelDAO.deleteListForTimeModel(userId);
                                                            getDataFirebase(userId, new Timestamp(new Date(0)));

                                                        }
                                                    }
                                                });

                                            }
                                        }
                                    }
                                });

                            }
                        });
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

                                    }
                                    timeModelArrayListMutableLiveData.postValue(timeModelArrayList);
                                    saveAllCollectionToSQLite(timeModelArrayList);
                                    updateDataSQLite(timeModelArrayList);
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
                                                        long timeModelSumForTimeOverall = sumUp(timeModelDAO.getAllTimeModelsList(currentUserId));

                                                        if (hoursToSettle != timeModelSumForTimeOverall) {
                                                            timeModelDAO.deleteListForTimeModel(currentUserId);
                                                            getDataFirebase(new Timestamp(new Date(0)));

                                                        }
                                                    }
                                                });

                                            }
                                        }
                                    }
                                });


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
                }
            }
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

                        result.put("paycheck", user1.getPaycheck());

                        updateUserTime(result, timeModel);
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
                }

            }
        });
    }

    public void saveAllCollectionToSQLite(List<TimeModel> timeModelList) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                timeModelDAO.insertAllCollection(timeModelList);
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

            }
        });

        collectionReferenceTime.document(idDocument).set(timeModel).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(application, application.getString(R.string.fail_on_adding_data), Toast.LENGTH_SHORT).show();

            }
        });
    }

    public void deleteDataFromRoomDB(TimeModel timeModel) {
        executor.execute(() -> timeModelDAO.delete(timeModel));
    }

    public void deleteDateFromFireBase(TimeModel timeModel) {
        deleteDataFromRoomDB(timeModel);
        collectionReferenceTime.document(timeModel.getDocumentId()).delete();
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
        long hoursToSettleToSend = 0;
        double paycheckToSend = 0;

        if (!timeModel.getMoneyOverall()) {
            hoursToSettleToSend = hoursToSettle + hoursFromTimeModel;

        } else {
            hoursToSettleToSend = hoursToSettle;
            paycheckToSend = paycheck - timeModel.getWithdrawnMoney();
            resultToSend.put("paycheck", paycheckToSend);
        }

        if (hoursOverallToSend == hoursToSettleToSend) {
            resultToSend.put("paycheck", 0);
        }


        resultToSend.put("hoursOverall", hoursOverallToSend);
        resultToSend.put("hoursToSettle", hoursToSettleToSend);

        collectionReferenceUser.document(userEmail).update(resultToSend);
    }

    public void updateDataSQLite(List<TimeModel> list) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                timeModelDAO.updateList(list);
            }
        });
    }

    public void updateDataSQLite(TimeModel timeModel) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                timeModelDAO.updated(timeModel);
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

        updateDataSQLite(timeModel);
        collectionReferenceTime.document(documentID).update(result);
    }

    public void updateStatusOfPayment(String documentID, boolean isSettled, double withDrawnMoney, Timestamp timestamp, TimeModel timeModel) {
        Map<String, Object> result = new HashMap<>();
        String KEY_MONEYOVERALL = "moneyOverall";
        result.put(KEY_MONEYOVERALL, isSettled);
        String WITH_DRAWN_MONEY = "withdrawnMoney";
        result.put(WITH_DRAWN_MONEY, withDrawnMoney);
        String KEY_TIMESTAMP = "timestamp";
        result.put(KEY_TIMESTAMP, timestamp);

        timeModel.setMoneyOverall(isSettled);
        timeModel.setWithdrawnMoney(withDrawnMoney);
        timeModel.setTimestamp(timestamp);

        updateDataSQLite(timeModel);

        collectionReferenceTime.document(documentID).update(result);
    }


    public void updateStatusOfTimeForUser(String email, long settledTimeInMillis, double payCheck) {
        Map<String, Object> result = new HashMap<>();

        result.put("paycheck", payCheck);
        result.put("hoursToSettle", settledTimeInMillis);
        collectionReferenceUser.document(email).update(result);
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

                        }

                    }
                }

            }
        });

    }

    public void checkIfAdmin() {

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
                }
            }
        });


    }

    public void signInWithGoogleCredential(AuthCredential authCredential, Context context, String email) {

        Log.i("Sign in Google","Sign in Google");
        firebaseAuth.signInWithCredential(authCredential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Log.i("Sign in Google","task.isSuccessful()");
                    fireBaseUser = firebaseAuth.getCurrentUser();
                    assert fireBaseUser != null;


                    collectionReferenceUser.whereEqualTo("email", fireBaseUser.getEmail()).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            if (!queryDocumentSnapshots.isEmpty()) {
                                context.startActivity(new Intent(context, UserMainActivity.class));
                                Log.i("Sign in Google","onSuccess");
                            } else {

                                valueToOpenDialogLiveData.postValue(true);

                            }


                        }
                    });

                }else{
                    Log.i("Sign in Google","task not sucesfull");
                }
            }
        });
    }

    public void writeAdminDataToDb(Context context) {
        collectionReferenceUser.whereEqualTo("email", firebaseAuth.getCurrentUser().getEmail()).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (queryDocumentSnapshots.isEmpty()) {
                    //Aktualny dzień
                    long time = new Date().getTime();

                    Duration duration = Duration.ofMillis(time);
                    long daysSinceUnix = duration.toDays();

                    firebaseAuth = FirebaseAuth.getInstance();

                    String[] split = firebaseAuth.getCurrentUser().getDisplayName().split("\\s+");

                    currentUserId = firebaseAuth.getUid();

                    Map<String, Object> userObj = new HashMap<>();
                    userObj.put("userId", currentUserId);
                    userObj.put("foreign_key", currentUserId);
                    userObj.put("email", firebaseAuth.getCurrentUser().getEmail());
                    userObj.put("username", (split.length == 1 || split.length == 2) ? split[0] : "");
                    userObj.put("surName", split.length == 2 ? split[1] : "");
                    userObj.put("hoursOverall", 0);
                    userObj.put("hoursToSettle", 0);
                    userObj.put("paycheck", 0);
                    userObj.put("entriesAmount", daysSinceUnix + "_" + "0");
                    userObj.put("deviceId", App.android_id);

                    collectionReferenceUser.document(firebaseAuth.getCurrentUser().getEmail()).set(userObj).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            context.startActivity(new Intent(context, UserMainActivity.class));

                        }
                    });
                }
            }
        });
    }

    public void writeUserDataToDb(String foreign_email, Context context) {

        collectionReferenceUser.whereEqualTo("email", foreign_email).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (!queryDocumentSnapshots.isEmpty()) {
                    String foreign_key = "";

                    //Aktualny dzień
                    long time = new Date().getTime();
                    //long days = TimeUnit.MILLISECONDS.toDays(time);

                    Duration duration = Duration.ofMillis(time);
                    long daysSinceUnix = duration.toDays();

                    for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {
                        foreign_key = snapshot.getString("userId");
                    }


                    String[] split = firebaseAuth.getCurrentUser().getDisplayName().split("\\s+");

                    currentUserId = firebaseAuth.getUid();

                    Map<String, Object> userObj = new HashMap<>();
                    userObj.put("userId", currentUserId);
                    userObj.put("email", firebaseAuth.getCurrentUser().getEmail());
                    userObj.put("username", (split.length == 1 || split.length == 2) ? split[0] : "");
                    userObj.put("surName", split.length == 2 ? split[1] : "");
                    userObj.put("foreign_key", foreign_key);
                    userObj.put("hoursOverall", 0);
                    userObj.put("hoursToSettle", 0);
                    userObj.put("paycheck", 0);
                    userObj.put("entriesAmount", daysSinceUnix + "_" + "0");
                    userObj.put("deviceId", App.android_id);

                    collectionReferenceUser.document(firebaseAuth.getCurrentUser().getEmail()).set(userObj).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            context.startActivity(new Intent(context, UserMainActivity.class));

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });

                }
            }
        });
    }


    public void register(String email, String password, String userName_send, String surName_send, String foreign_email) {

        collectionReferenceUser.whereEqualTo("email", foreign_email).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (!value.isEmpty()) {
                    for (QueryDocumentSnapshot snapshot : value) {
                        arrayListForAssigningEmail.add(snapshot.getString("userId"));
                    }

                    if (arrayListForAssigningEmail.size() > 0) {


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
                                    //TODO Register
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
                                    userObj.put("deviceId", App.android_id);

                                    isAdminExistLiveData.postValue(true);

                                    collectionReferenceUser.document(email).set(userObj).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {

                                            arrayListForAssigningEmail.clear();
                                        }
                                    });

                                } else {
                                    Toast.makeText(application, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                    } else {
                        isAdminExistLiveData.postValue(false);
                    }

                }


            }
        });

    }


    public void register(String email, String password, String userName_send, String surName_send) {

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
                    userObj.put("entriesAmount", daysSinceUnix + "_" + "0");
                    userObj.put("deviceId", App.android_id);


                    collectionReferenceUser.document(email).set(userObj).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {

                            arrayListForAssigningEmail.clear();
                        }
                    });


                } else {
                    Toast.makeText(application, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void signIn(String email, String password) {
        firebaseAuth.signInWithEmailAndPassword(email, password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                firebaseUserMutableLiveData.postValue(firebaseAuth.getCurrentUser());
                isUserInDBLiveDataLiveData.postValue(true);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(application, application.getString(R.string.no_user), Toast.LENGTH_SHORT).show();

            }
        });
    }

    public void resetPassword(String email) {
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


    public LiveData<User> getUsernameAndSurname() {

        checkIfAdmin();
        collectionReferenceUser.whereEqualTo("userId", currentUserId).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (!queryDocumentSnapshots.isEmpty()) {
                    for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {

                        user.setUsername(snapshot.getString("username"));
                        user.setSurName(snapshot.getString("surName"));
                        user.setForeign_key(snapshot.getString("foreign_key"));
                    }
                    getUsernameAndSurnameLiveData.postValue(user);
                }
            }
        });


        return getUsernameAndSurnameLiveData;
    }

    public void writeToFile(String androidId) {

        collectionReferenceUser.whereEqualTo("userId", currentUserId).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (!queryDocumentSnapshots.isEmpty()) {
                    Map<String, Object> userObj = new HashMap<>();
                    for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {


                        userObj.put("deviceId", App.android_id);
                        email = snapshot.getString("email");
                    }


                    collectionReferenceUser.document(email).update(userObj).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {

                            youCanCheckNowLiveData.postValue(true);
                        }
                    });

                }
            }
        });
    }


    public void singOut() {
        firebaseAuth.signOut();
        userLoggedMutableLiveData.postValue(true);

    }


}
