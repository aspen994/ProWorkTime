package com.example.ogrdapp.repository;

import android.app.Application;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;

import com.example.ogrdapp.R;
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
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db;
    private CollectionReference collectionReference;
    private FirebaseUser fireBaseUser;
    private CollectionReference collectionReferenceTime;
    private String currentUserId;

    private List<String> arrayListForAssigningEmail;
    private List<String> arrayListIfAdmin;

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
        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        collectionReference = db.collection("Users");
        collectionReferenceTime = db.collection("Time");

        /*fireBaseUser = firebaseAuth.getCurrentUser();
        assert  fireBaseUser!=null;
        currentUserId = fireBaseUser.getUid();*/
        if(firebaseAuth.getCurrentUser()!=null)
        {
            fireBaseUser = firebaseAuth.getCurrentUser();
            firebaseUserMutableLiveData.postValue(firebaseAuth.getCurrentUser());
            Log.i("NOT NULL","NOT NULL");
            currentUserId = fireBaseUser.getUid();
        }

    }

    public String getUserId()
    {
        fireBaseUser = firebaseAuth.getCurrentUser();
        assert  fireBaseUser !=null;
        return  fireBaseUser.getUid();
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

    public FirebaseAuth getFirebaseAuth()
    {
        return firebaseAuth;
    }
    public FirebaseUser getCurrentUser() {
        return firebaseAuth.getCurrentUser();
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
                                //TODO 06.07.2023 - Sorting the ArrayList to show time in proper order

                                timeModelArrayList.add(timeModel);
                                Collections.sort(timeModelArrayList, new Comparator<TimeModel>() {
                                    @Override
                                    public int compare(TimeModel o1, TimeModel o2) {
                                        return o1.getTimeAdded().compareTo(o2.getTimeAdded());
                                    }
                                });

                            }
                            Log.i("THREE TIMES","not importent");
                            timeForUserListMutableLiveData.setValue(timeModelArrayList);

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
                                //TODO 06.07.2023 - Sorting the ArrayList to show time in proper order

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

                    }
                });
    }
    public void saveDataToFireBase(TimeModel timeModel)
    {
        collectionReferenceTime.add(timeModel).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                  Toast.makeText(application, "Fail on adding data", Toast.LENGTH_SHORT).show();
            }
        });


    }

    boolean ifAdmin = false;
    //Jeśli userId jest równy foreignkey- wtedy pokaż panel admina


    public void getUsersDataAssignedToAdmin()
    {
        collectionReference.whereEqualTo("foreign_key",currentUserId).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                Log.i("CURRENT USER ID: ",currentUserId+"");
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
                            Log.i("Inside: ","getUsersDataAssignedToAdmin");

                            userArrayListOfUserMutableLiveData.postValue(userArrayList);
                        }

                    }

                }

            }
        });


    }
    public boolean checkIfAdmin ()
    {
        collectionReference.whereEqualTo("foreign_key",currentUserId).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                Log.i("CURRENT USER ID: ",currentUserId+"");
                    if(!value.isEmpty())
                    {
                        for(QueryDocumentSnapshot snapshot:value)
                        {
                            if(currentUserId.equals(snapshot.getString("foreign_key")))
                              {
                                  ifAdminMutableLiveData.postValue(true);

                              }
                              else{
                                  Log.i("JESTEŚ UŻYTKOWNIKIEM ","JESTEŚ UŻYTKOWNIKIEM");
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
                    Map<String, String> userObj = new HashMap<>();
                    userObj.put("userId",currentUserId);
                    userObj.put("email",email);
                    userObj.put("username",userName_send);
                    userObj.put("surName",surName_send);
                    userObj.put("foreign_key", arrayListForAssigningEmail.get(0));

                    collectionReference.document(email).set(userObj).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(application.getApplicationContext(), application.getString(R.string.registry_sucesfully), Toast.LENGTH_SHORT).show();
                            arrayListForAssigningEmail.clear();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(application.getApplicationContext(), application.getString(R.string.fail_registry), Toast.LENGTH_SHORT).show();
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
    }

}
