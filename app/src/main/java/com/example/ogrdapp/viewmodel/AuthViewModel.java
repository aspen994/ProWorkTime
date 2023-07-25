package com.example.ogrdapp.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.ogrdapp.repository.AuthRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AuthViewModel extends AndroidViewModel {

    private MutableLiveData<FirebaseUser> firebaseUserMutableLiveData;
    private FirebaseUser currentUser;
    private AuthRepository repository;

    public MutableLiveData<FirebaseUser> getFirebaseUserMutableLiveData() {
        return firebaseUserMutableLiveData;
    }

    public FirebaseUser getCurrentUser() {
        return currentUser;
    }

    public FirebaseAuth getFirebaseAuth()
    {
        return repository.getFirebaseAuth();
    }

    public AuthViewModel(@NonNull Application application) {
        super(application);
        repository = new AuthRepository(application);
        currentUser = repository.getCurrentUser();
        firebaseUserMutableLiveData = repository.getFirebaseUserMutableLiveData();
        Log.i("AuthViewModel","Invoked construcotr form AuthViewModel");
    }

    public void singUp (String email, String password)
    {
        repository.signUp(email,password);
    }

    public void signIn(String email, String password)
    {
        repository.signIn(email,password);

    }

    public void signOut()
    {
        repository.singOut();
    }

}
