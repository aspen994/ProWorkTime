package com.example.ogrdapp.view;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.ogrdapp.R;
import com.example.ogrdapp.repository.DataRepository;
import com.example.ogrdapp.viewmodel.AuthViewModel;
import com.example.ogrdapp.viewmodel.DataViewModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {


    private Button buttonRegister;
    private AutoCompleteTextView email,userName,surName;
    private TextInputEditText password;

    // MVVM
    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        email = findViewById(R.id.autoCompleteTextView_email);
        password = findViewById(R.id.etPassword);
        userName = findViewById(R.id.username);
        surName = findViewById(R.id.user_surname);

        buttonRegister = findViewById(R.id.button_rejestruj);

        //Intaliizng auth


        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        authViewModel.getUserData().observe(this, new Observer<FirebaseUser>() {
            @Override
            public void onChanged(FirebaseUser firebaseUser) {
                startActivity(new Intent(RegisterActivity.this, MainActivity.class));
            }
        });

        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email_send = email.getText().toString();
                String password_send = password.getText().toString();
                String userName_send = userName.getText().toString();
                String surName_send = surName.getText().toString();

                if(!TextUtils.isEmpty(email.getText().toString())&&
                        !TextUtils.isEmpty(password.getText().toString())&&
                        !TextUtils.isEmpty(userName.getText().toString())&&
                        !TextUtils.isEmpty(surName.getText().toString())) {

                    authViewModel.registerUser(email_send,password_send,userName_send,surName_send);
                }

            }
        });

    }
}