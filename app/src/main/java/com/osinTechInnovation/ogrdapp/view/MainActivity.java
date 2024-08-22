package com.osinTechInnovation.ogrdapp.view;

import android.app.Dialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.osinTechInnovation.ogrdapp.R;
import com.osinTechInnovation.ogrdapp.UserMainActivity;
import com.osinTechInnovation.ogrdapp.viewmodel.AuthViewModel;

public class MainActivity extends AppCompatActivity {

    private Button loginBtn,singInGoogleBtn;
    private TextView textViewRegister,textViewZresetuj;
    private AutoCompleteTextView email, password;

    private AuthViewModel authViewModel;

    public GoogleSignInClient googleSignInClient;

    public BeginSignInRequest signInRequest;
    private Dialog dialog,dialogForUser;
    private  Button btnDialogAdmin, btnDialogUser,btnRegister;
    private AutoCompleteTextView actvFillAdminEmail;





    private final    ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if(result.getResultCode() == RESULT_OK){
                Task<GoogleSignInAccount> accountTask = GoogleSignIn.getSignedInAccountFromIntent(result.getData());


                try{
                    GoogleSignInAccount signInAccount = accountTask.getResult(ApiException.class);
                    AuthCredential authCredential = GoogleAuthProvider.getCredential(signInAccount.getIdToken(),null);
                    authViewModel.signInWithGoogleCredential(authCredential,MainActivity.this,signInAccount.getEmail());

                } catch (ApiException e) {
                    throw new RuntimeException(e);
                }
            }

        }
    });


    @RequiresApi(api = Build.VERSION_CODES.P)
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
        textViewZresetuj = findViewById(R.id.btn_view_zresetuj);

        email = findViewById(R.id.autoCompleteTextView_email_main);
        password = findViewById(R.id.autoCompleteTextView_password_main);
        singInGoogleBtn = findViewById(R.id.googleSignIn);


        dialog = new Dialog(MainActivity.this);
        dialog.setContentView(R.layout.dialog_chose_user_admin);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.custom_dialog_bg));
        dialog.setCancelable(false);

        btnDialogAdmin = dialog.findViewById(R.id.btn_chose_admin);
        btnDialogUser = dialog.findViewById(R.id.btn_chose_user);

        dialogForUser = new Dialog(MainActivity.this);
        dialogForUser.setContentView(R.layout.dialog_admin);
        dialogForUser.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        dialogForUser.getWindow().setBackgroundDrawable(getDrawable(R.drawable.custom_dialog_bg));

        actvFillAdminEmail = dialogForUser.findViewById(R.id.actv_fill_admin_email);
        btnRegister = dialogForUser.findViewById(R.id.btn_register);

        btnRegister.setOnClickListener((View v)->{
            if(!actvFillAdminEmail.getText().toString().isEmpty()&& Patterns.EMAIL_ADDRESS.matcher(actvFillAdminEmail.getText().toString()).matches()){
                authViewModel.writeUserDataToDb(actvFillAdminEmail.getText().toString(),this);
            }
            else {
                //TODO Edit this
                Toast.makeText(this, getString(R.string.wrong_format), Toast.LENGTH_SHORT).show();
            }
        });

        btnDialogAdmin.setOnClickListener((View v)->{
            authViewModel.writeAdminDataToDb(this);
        });




        signInRequest = BeginSignInRequest.builder()
                .setGoogleIdTokenRequestOptions(BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                        .setSupported(true)
                        // Your server's client ID, not your Android client ID.
                        .setServerClientId(getString(R.string.serverClientId))
                        // Only show accounts previously used to sign in.
                        .setFilterByAuthorizedAccounts(true)
                        .build())
                .build();

        authViewModel.getValueToOpenDialog().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if(aBoolean){
                    dialog.show();
                }
            }
        });

        btnDialogUser.setOnClickListener((View v)->{
            dialog.hide();
            dialogForUser.show();

        });

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
             /*   startActivity(new Intent(MainActivity.this,UserMainActivity.class));
                finish();*/
            }
        });

        //TODO 08.08.2024
        authViewModel.getIsUserInDB().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if(aBoolean){
                    Log.i("Here baby",true+"");
                    startActivity(new Intent(MainActivity.this,UserMainActivity.class));
                    finish();
                }
                else {
                    //Log.i("Here baby", "Not in DB yet");
                    googleSignInClient.signOut();
                }
            }
        });

        //TODO 08.08.2024
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //startActivity(new Intent(MainActivity.this,UserMainActivity.class));

                String emailToLogin = email.getText().toString();
                String passwordToLogin = password.getText().toString();
                if(!TextUtils.isEmpty(email.getText().toString())&&!TextUtils.isEmpty(password.getText().toString())) {
                    authViewModel.signIn(emailToLogin,passwordToLogin);
                    //startActivity(new Intent(MainActivity.this,UserMainActivity.class));
                }

            }
        });

        GoogleSignInOptions options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.serverClientId))
                        .requestEmail()
                                .build();

        googleSignInClient = GoogleSignIn.getClient(MainActivity.this,options);




        singInGoogleBtn.setOnClickListener((view -> {

            Intent intent = googleSignInClient.getSignInIntent();
            activityResultLauncher.launch(intent);

            //TODO 25.07.2024
            //googleSignInClient.signOut();



            /*CredentialManager credentialManager = CredentialManager.create(getApplicationContext());

            String rawNonce = UUID.randomUUID().toString();
            byte[] bytes = rawNonce.getBytes();
            StringBuilder hashedNonce = new StringBuilder("");
            try {
                MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
                byte[] digest = messageDigest.digest(bytes);

                for (int i = 0; i < digest.length; i++) {
                    //hashedNonce.append(Integer.toString((digest[i] & 0xff) + 0x100, 16).substring(1));
                    hashedNonce.append(String.format("%02X ", digest[i]));
                }
            } catch (NoSuchAlgorithmException e) {
                Log.i("NoSuchAlgorithm", e.getMessage());
            }



            GetGoogleIdOption getGoogleIdOption = new GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(getString(R.string.serverClientId))
                    .setNonce(hashedNonce.toString())
                    .build();

            GetCredentialRequest getCredentialRequest = new GetCredentialRequest.Builder()
                    .addCredentialOption(getGoogleIdOption)
                    .build();


            Executor executor = Executors.newSingleThreadExecutor();

            CancellationSignal cancellationSignal = null;


            credentialManager.getCredentialAsync(
                    MainActivity.this,
                    getCredentialRequest,
                    cancellationSignal,
                    getApplicationContext().getMainExecutor(),
                    new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                        @Override
                        public void onResult(GetCredentialResponse result) {
                            handleSignIn(result);
                        }

                        @Override
                        public void onError(GetCredentialException e) {
                            Log.i("Error Google", e.getMessage());
                        }
                    }
                    );*/




        }));
    }


  /*  public void handleSignIn(GetCredentialResponse result) {
        // Handle the successfully returned credential.


        Credential credential = result.getCredential();


        result.toString();

        if (credential instanceof PublicKeyCredential) {
            String responseJson = ((PublicKeyCredential) credential).getAuthenticationResponseJson();

            Log.i("responseJson",responseJson);
            // Share responseJson i.e. a GetCredentialResponse on your server to validate and authenticate
        } else if (credential instanceof PasswordCredential) {
            String username = ((PasswordCredential) credential).getId();
            String password = ((PasswordCredential) credential).getPassword();
            Log.i("Username", username);
            Log.i("password", password);
            // Use id and password to send to your server to validate and authenticate
        }

    }*/



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

