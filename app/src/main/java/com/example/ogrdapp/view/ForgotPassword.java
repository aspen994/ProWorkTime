package com.example.ogrdapp.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import com.example.ogrdapp.R;
import com.example.ogrdapp.viewmodel.AuthViewModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPassword extends AppCompatActivity {

    private AutoCompleteTextView email;
    private Button button;

    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        email = findViewById(R.id.autoCompleteTextView3);
        button= findViewById(R.id.button2);
        authViewModel= new ViewModelProvider(this).get(AuthViewModel.class);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!TextUtils.isEmpty(email.getText().toString())) {

                    String emailToSend = email.getText().toString();
                    authViewModel.resetPassword(emailToSend);
                    startActivity(new Intent(ForgotPassword.this, MainActivity.class));

                }
                else {
                    Toast.makeText(ForgotPassword.this, getString(R.string.enter_password), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


}