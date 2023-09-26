package com.example.ogrdapp.repository;

import android.app.Application;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;

import com.example.ogrdapp.R;
import com.example.ogrdapp.UserMainActivity;
import com.example.ogrdapp.model.TimeModel;
import com.example.ogrdapp.view.ForgotPassword;
import com.example.ogrdapp.view.MainActivity;
import com.example.ogrdapp.view.RegisterActivity;
import com.example.ogrdapp.viewmodel.AuthViewModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
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
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db;
    private CollectionReference collectionReference;
    private FirebaseUser fireBaseUser;
    private CollectionReference collectionReferenceTime;
    private String currentUserId;
    public AuthRepository(Application application) {
        this.application = application;
        this.firebaseUserMutableLiveData = new MutableLiveData<>();
        this.userLoggedMutableLiveData = new MutableLiveData<>();
        this.firebaseTimeModel = new MutableLiveData<>();
        this.timeModelArrayListMutableLiveData = new MutableLiveData<>();

        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        collectionReference = db.collection("Users");
        collectionReferenceTime = db.collection("Time");

        fireBaseUser = firebaseAuth.getCurrentUser();
        assert  fireBaseUser!=null;
        currentUserId = fireBaseUser.getUid();

        if(firebaseAuth.getCurrentUser()!=null)
        {
            firebaseUserMutableLiveData.postValue(firebaseAuth.getCurrentUser());
            Log.i("NOT NULL","NOT NULL");
        }
    }

    public String getUserId()
    {
        fireBaseUser = firebaseAuth.getCurrentUser();
        assert  fireBaseUser !=null;
        return  fireBaseUser.getUid();
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

    public void register (String email, String password,String userName_send,String surName_send)
    {
        firebaseAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful())
                {
                    firebaseUserMutableLiveData.postValue(firebaseAuth.getCurrentUser());

                  /*  fireBaseUser = firebaseAuth.getCurrentUser();
                    assert  fireBaseUser!=null;
                    final String currentUserId = fireBaseUser.getUid();*/

                    // Create a userMap so we can create user in the User Collection in FireStore
                    Map<String, String> userObj = new HashMap<>();
                    userObj.put("userId",currentUserId);
                    userObj.put("email",email);
                    userObj.put("username",userName_send);
                    userObj.put("surName",surName_send);

                    collectionReference.document(email).set(userObj).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(application.getApplicationContext(), application.getString(R.string.registry_sucesfully), Toast.LENGTH_SHORT).show();
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
                Toast.makeText(application, "NIE MA TAKIE UŻYTKOWNIKA BYCZQ", Toast.LENGTH_SHORT).show();
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
        TimeModel timeModel = new TimeModel();
        collectionReference.whereEqualTo("userId",getCurrentUser().getUid()).addSnapshotListener(new EventListener<QuerySnapshot>() {
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
