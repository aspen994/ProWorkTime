package com.example.ogrdapp.repository;

import android.app.Application;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.example.ogrdapp.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class DataRepository {
    private Application application;
    private MutableLiveData<CollectionReference> firebaseUserDataMutableLiveData;
    private FirebaseFirestore db;
    private CollectionReference collectionReference;
    private FirebaseAuth firebaseAuth;
    private String collectionPath;

    public DataRepository(Application application) {
        this.application = application;
        firebaseUserDataMutableLiveData = new MutableLiveData<>();
        db=FirebaseFirestore.getInstance();
        this.collectionPath =collectionPath;
        collectionReference = db.collection("Users");
        firebaseAuth =FirebaseAuth.getInstance();

    }

    public FirebaseAuth getFirebaseAuth() {
        return firebaseAuth;
    }

    public MutableLiveData<CollectionReference> getFirebaseUserDataMutableLiveData() {
        return firebaseUserDataMutableLiveData;
    }

    public String getCollectionPath() {
        return collectionPath;
    }

    public void setCollectionPath(String collectionPath) {
        this.collectionPath = collectionPath;
    }

    public void addUserToDB(String email, String username, String userSurname)
    {



        // Create a userMap so we can create user in the User Collection in FireStore
        Map<String, String> userObj = new HashMap<>();
        userObj.put("userId","s");
        userObj.put("email",email);
        userObj.put("username",username);
        userObj.put("surName",userSurname);

        collectionReference.document(email).set(userObj).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                firebaseUserDataMutableLiveData.postValue(collectionReference.document(collectionPath).getParent());
                Toast.makeText(application, application.getString( R.string.registry_sucesfully), Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(application, application.getString(R.string.fail_registry), Toast.LENGTH_SHORT).show();
            }
        });

    }
}
