package com.example.ogrdapp.view;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.ogrdapp.R;
import com.example.ogrdapp.UserMainActivity;
import com.example.ogrdapp.viewmodel.AuthViewModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    // Widgets
    private Button loginBtn;
    private TextView textViewRegister,textViewZresetuj;
    private EditText editText;
    private AutoCompleteTextView email, password;

    private AuthViewModel authViewModel;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        setContentView(R.layout.activity_main);

        getSupportActionBar().hide();

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        //! ! ! ! ! ! ! ! ! ! ! !-FOR NEXT IMPROVEMNET LEAV IT ! ! ! ! ! ! ! ! ! ! ! !
        /*spinner = findViewById(R.id.spinner_language);
        textViewSelectLanguage= findViewById(R.id.textView);*/
        textViewRegister = findViewById(R.id.textViewRegister);
        loginBtn = findViewById(R.id.button_login);
        textViewZresetuj = findViewById(R.id.textViewZresetuj);

        email = findViewById(R.id.autoCompleteTextView_email_main);
        password = findViewById(R.id.autoCompleteTextView_password_main);



        //! ! ! ! ! ! ! ! ! ! ! !-FOR NEXT IMPROVEMENT LEAVE IT ! ! ! ! ! ! ! ! ! ! ! !

        /*ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,R.array.language, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setPrompt("Wybierz język");
        spinner.setAdapter(adapter);*/

        //! ! ! ! ! ! ! ! ! ! ! !-FOR NEXT IMPROVEMENT LEAVE IT ! ! ! ! ! ! ! ! ! ! ! !
        /*textViewSelectLanguage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spinner.performClick();
            }
        });

        //! ! ! ! ! ! ! ! ! ! ! !-FOR NEXT IMPROVEMENT LEAVE IT ! ! ! ! ! ! ! ! ! ! ! !
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedLanguage = parent.getItemAtPosition(position).toString();

                if(selectedLanguage.equals("polski"))
                {
                    setLocal(MainActivity.this,"pl");
                    finish();
                    startActivity(getIntent());
                }
                else if(selectedLanguage.equals("українська"))
                {
                    setLocal(MainActivity.this,"uk");
                    finish();
                    startActivity(getIntent());

                } else if (selectedLanguage.equals("english")) {
                    setLocal(MainActivity.this,"en");
                    finish();
                    startActivity(getIntent());
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });*/



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

        authViewModel.getUserData().observe(this, new Observer<FirebaseUser>() {
            @Override
            public void onChanged(FirebaseUser firebaseUser) {
                startActivity(new Intent(MainActivity.this,UserMainActivity.class));
            }
        });

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //startActivity(new Intent(MainActivity.this,UserMainActivity.class));

                String emailToLogin = email.getText().toString();
                String passwordToLogin = password.getText().toString();
                if(!TextUtils.isEmpty(email.getText().toString())&&!TextUtils.isEmpty(password.getText().toString())) {
                    authViewModel.signIn(emailToLogin,passwordToLogin);
                }

            }
        });
    }

    //! ! ! ! ! ! ! ! ! ! ! !-FOR NEXT IMPROVEMNET LEAV IT ! ! ! ! ! ! ! ! ! ! ! !
/*    private void setLocal(Activity activity, String langCode) {
        Locale locale = new Locale(langCode);
        locale.setDefault(locale);
        Resources resources = activity.getResources();
        Configuration configuration = resources.getConfiguration();
        configuration.setLocale(locale);
        resources.updateConfiguration(configuration,resources.getDisplayMetrics());
    }*/
}