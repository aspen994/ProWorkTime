package com.example.ogrdapp.view;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.ogrdapp.R;
import com.example.ogrdapp.viewmodel.AuthViewModel;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {


    private Button buttonRegister;
    private AutoCompleteTextView email,userName,surName,emailOfAdmin;
    private TextInputEditText password;
    private RadioGroup radioGroup;
    private RadioButton radioButtonAdministrator,radioButtonUser;

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
        emailOfAdmin = findViewById(R.id.autoCompleteTextView_email_of_administrator);
        radioGroup = findViewById(R.id.radio_group);
        radioButtonAdministrator = findViewById(R.id.radio_admin);
        radioButtonUser = findViewById(R.id.radio_user);

        buttonRegister = findViewById(R.id.button_rejestruj);

        emailOfAdmin.setVisibility(View.VISIBLE);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                    if(checkedId==radioButtonAdministrator.getId())
                    {
                        emailOfAdmin.setVisibility(View.INVISIBLE);
                    }
                    else{
                        emailOfAdmin.setVisibility(View.VISIBLE);
                    }
            }
        });

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
                String email_send = email.getText().toString().toLowerCase();
                String password_send = password.getText().toString();
                String userName_send = userName.getText().toString();
                String surName_send = surName.getText().toString();
                String adminEmail = emailOfAdmin.getText().toString();

                if(!TextUtils.isEmpty(email.getText().toString())&&
                        !TextUtils.isEmpty(password.getText().toString())&&
                        !TextUtils.isEmpty(userName.getText().toString())&&
                        !TextUtils.isEmpty(surName.getText().toString())&&
                        ((!TextUtils.isEmpty(emailOfAdmin.getText().toString())
                        && emailOfAdmin.getVisibility()==View.VISIBLE)||
                        (TextUtils.isEmpty(emailOfAdmin.getText().toString())
                        && emailOfAdmin.getVisibility() == View.INVISIBLE))
                        &&radioGroup.getCheckedRadioButtonId()!=-1
                  ) {
                    authViewModel.registerUser(email_send,password_send,userName_send,surName_send,adminEmail);
                    }
                    else {
                        Toast.makeText(RegisterActivity.this, getString(R.string.complete_the_fields), Toast.LENGTH_SHORT).show();
                    }

                }
        });

    }
}