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
import androidx.lifecycle.ViewModelProvider;

import com.example.ogrdapp.R;
import com.example.ogrdapp.UserMainActivity;
import com.example.ogrdapp.viewmodel.AuthViewModel;
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

    //MVVM PATERN
    private AuthViewModel viewModel;






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //MVVM PATTERN
        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);


        // TODO 25/07/23 - Switch to mvvm pattern
        //Intaliizng auth
        /*firebaseAuth = FirebaseAuth.getInstance();

        if(firebaseAuth.getCurrentUser()!=null)
        {
            startActivity(new Intent(MainActivity.this, UserMainActivity.class));
            finish();
        }*/

        if(viewModel.getCurrentUser()!=null)
        {
            startActivity(new Intent(MainActivity.this, UserMainActivity.class));
            Log.i("WORKING !!!!!!!!","IT's working properly");
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
                startActivity(new Intent(MainActivity.this, ForgotPassword.class));
            }
        });


        textViewRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, RegisterActivity.class));
            }
        });
      /*  loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //startActivity(new Intent(MainActivity.this,UserMainActivity.class));
                String emailToLogin = email.getText().toString();
                String passwordToLogin = password.getText().toString();
                if(!TextUtils.isEmpty(email.getText().toString())&&!TextUtils.isEmpty(password.getText().toString())) {
                    viewModel.getFirebaseAuth().signInWithEmailAndPassword(emailToLogin,passwordToLogin).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
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
*/
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //startActivity(new Intent(MainActivity.this,UserMainActivity.class));
                String emailToLogin = email.getText().toString();
                String passwordToLogin = password.getText().toString();

                // MVVM PATTERN
                if(!TextUtils.isEmpty(email.getText().toString())&&!TextUtils.isEmpty(password.getText().toString())) {
                    viewModel.signIn(emailToLogin,passwordToLogin);

                    startActivity(new Intent(MainActivity.this,UserMainActivity.class));
                    finish();
                }
                else {
                    Toast.makeText(MainActivity.this, "Wprowadź użytkownika i hasła", Toast.LENGTH_SHORT).show();
                }
            }
        });



    }
}