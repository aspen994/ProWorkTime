package com.osinTechInnovation.ogrdapp.view;

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

import com.osinTechInnovation.ogrdapp.R;
import com.osinTechInnovation.ogrdapp.viewmodel.AuthViewModel;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {

    private Button buttonRegister;
    private AutoCompleteTextView email,userName,surName,emailOfAdmin;
    private TextInputEditText password;


    private RadioGroup radioGroup;
    private RadioButton radioButtonAdministrator,radioButtonUser;


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

        getSupportActionBar().hide();

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


        authViewModel.getIsAdminExist().observe(this,(isAdminExist)->{
            if(isAdminExist.equals(false)){
                Toast.makeText(this, getString(R.string.there_is_no_such_admin), Toast.LENGTH_SHORT).show();
            }else{

            }
            //Log.i("MyTag0506",isAdminExist+"");
        });

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
                String adminEmail = emailOfAdmin.getText().toString().toLowerCase();


                if(
                        !TextUtils.isEmpty(email.getText().toString())&&
                        !TextUtils.isEmpty(password.getText().toString())&&
                        !TextUtils.isEmpty(userName.getText().toString())&&
                        !TextUtils.isEmpty(surName.getText().toString())
                )


                {
                    // DLA USERA
                    if(
                            ((!TextUtils.isEmpty(emailOfAdmin.getText().toString().toLowerCase())
                            && emailOfAdmin.getVisibility()==View.VISIBLE))
                            && radioButtonUser.isChecked()
                    ){

                        authViewModel.registerUser(email_send,password_send,userName_send,surName_send,adminEmail);
                    }
                    // DLA ADMINA
                    else if(
                            ((TextUtils.isEmpty(emailOfAdmin.getText().toString().toLowerCase())
                                    && emailOfAdmin.getVisibility()==View.INVISIBLE))
                                    && radioButtonAdministrator.isChecked()
                    ){
                        authViewModel.registerUser(email_send,password_send,userName_send,surName_send);
                    }
                    else {
                        Toast.makeText(RegisterActivity.this,getString(R.string.are_you_admin_or_user), Toast.LENGTH_LONG).show();
                    }

                    }
                    else {
                    Toast.makeText(RegisterActivity.this, getString(R.string.complete_the_fields), Toast.LENGTH_SHORT).show();
                    }

                }
        });

    }
}