package com.example.ogrdapp.view;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.ViewModelProvider;

import com.example.ogrdapp.R;
import com.example.ogrdapp.UserMainActivity;
import com.example.ogrdapp.viewmodel.UserMainActivityViewModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
public class MainActivity extends AppCompatActivity {

    private Button loginBtn;
    private TextView textViewRegister,textViewZresetuj;
    private AutoCompleteTextView email, password;

    private FirebaseAuth firebaseAuth;


    //Firebase Connection
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private CollectionReference collectionReference = db.collection("Users");




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        setContentView(R.layout.activity_main);

        //Intaliizng auth
        firebaseAuth = FirebaseAuth.getInstance();

        // TODO 31.05.2023
        if(firebaseAuth.getCurrentUser()!=null)
        {
            startActivity(new Intent(MainActivity.this,UserMainActivity.class));
            finish();
        }

        textViewRegister = findViewById(R.id.textViewRegister);
        loginBtn = findViewById(R.id.button_login);
        textViewZresetuj = findViewById(R.id.textViewZresetuj);

        email = findViewById(R.id.autoCompleteTextView_email_main);
        password = findViewById(R.id.autoCompleteTextView_password_main);



        textViewZresetuj.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,ForgotPassword.class));
            }
        });


        textViewRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, RegisterActivity.class));
            }
        });

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //startActivity(new Intent(MainActivity.this,UserMainActivity.class));
                String emailToLogin = email.getText().toString();
                String passwordToLogin = password.getText().toString();
                if(!TextUtils.isEmpty(email.getText().toString())&&!TextUtils.isEmpty(password.getText().toString())) {
                    firebaseAuth.signInWithEmailAndPassword(emailToLogin,passwordToLogin).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {
                            startActivity(new Intent(MainActivity.this,UserMainActivity.class));
                            finish();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(MainActivity.this, "nie ma takiego użytkownika", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                else {
                    Toast.makeText(MainActivity.this, "Wprowadź użytkownika i hasła", Toast.LENGTH_SHORT).show();
                }
            }
        });



    }
}