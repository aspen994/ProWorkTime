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

import com.example.ogrdapp.R;
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



    //Firebase Authentication
    // To register we need only FirebaseAuth.
    private FirebaseAuth firebaseAuth;
    private FirebaseUser fireBaseUser;

    //Firebase Connection
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private CollectionReference collectionReference = db.collection("Users");

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
        firebaseAuth = FirebaseAuth.getInstance();


        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!TextUtils.isEmpty(email.getText().toString())&&
                   !TextUtils.isEmpty(password.getText().toString())&&
                   !TextUtils.isEmpty(userName.getText().toString())&&
                   !TextUtils.isEmpty(surName.getText().toString())
                    ) // dodaj !Textuti dla nowego pola.
                {
                    String email_send = email.getText().toString();
                    String password_send = password.getText().toString();
                    String userName_send = userName.getText().toString();
                    String surName_send = surName.getText().toString();

                    firebaseAuth.createUserWithEmailAndPassword(email_send,password_send).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful())
                            {
                                fireBaseUser = firebaseAuth.getCurrentUser();
                                assert  fireBaseUser!=null;
                                final String currentUserId = fireBaseUser.getUid();

                                // Create a userMap so we can create user in the User Collection in FireStore
                                Map<String, String> userObj = new HashMap<>();
                                userObj.put("userId",currentUserId);
                                userObj.put("email",email_send);
                                userObj.put("username",userName_send);
                                userObj.put("surName",surName_send);

                                collectionReference.document(email_send).set(userObj).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Toast.makeText(RegisterActivity.this, getString(R.string.registry_sucesfully), Toast.LENGTH_SHORT).show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(RegisterActivity.this, getString(R.string.fail_registry), Toast.LENGTH_SHORT).show();
                                    }
                                });

                            }
                        }
                    }).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {
                            startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(RegisterActivity.this, getString(R.string.fail_registry), Toast.LENGTH_SHORT).show();
                        }
                    });
                }

            }
        });
    }
}