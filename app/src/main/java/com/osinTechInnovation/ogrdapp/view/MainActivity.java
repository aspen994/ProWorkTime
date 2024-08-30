package com.osinTechInnovation.ogrdapp.view;

import static android.os.AsyncTask.THREAD_POOL_EXECUTOR;

import android.app.Dialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
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
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.credentials.ClearCredentialStateRequest;
import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.CustomCredential;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.PasswordCredential;
import androidx.credentials.PublicKeyCredential;
import androidx.credentials.exceptions.GetCredentialException;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.osinTechInnovation.ogrdapp.R;
import com.osinTechInnovation.ogrdapp.UserMainActivity;
import com.osinTechInnovation.ogrdapp.viewmodel.AuthViewModel;

import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;


public class MainActivity extends AppCompatActivity {

    private Button loginBtn,singInGoogleBtn;
    private TextView textViewRegister,textViewZresetuj;
    private AutoCompleteTextView email, password;
    private AuthViewModel authViewModel;
    private Dialog dialog,dialogForUser;
    private  Button btnDialogAdmin, btnDialogUser,btnRegister;
    private AutoCompleteTextView actvFillAdminEmail;
    private GoogleSignInClient googleSignInClient;


    private final ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            Log.i("result",result.getResultCode()+"");
            if(result.getResultCode() == RESULT_OK){
                Task<GoogleSignInAccount> accountTask = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                Log.i("result after if",result.getResultCode()+"");
                try{
                    GoogleSignInAccount signInAccount = accountTask.getResult(ApiException.class);
                    AuthCredential  authCredential = GoogleAuthProvider.getCredential(signInAccount.getIdToken(),null);
                    authViewModel.signInWithGoogleCredential(authCredential,MainActivity.this,signInAccount.getEmail());

                } catch (ApiException e) {
                    Log.i("ApiException",e.getMessage());
                    throw new RuntimeException(e);
                }
            }

        }
    });



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
           // Log.i("Credential Data",credentialData.getDisplayName());

        });

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
                finish();
            }
        });

        //TODO 08.08.2024
        authViewModel.getIsUserInDB().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if(aBoolean){
                    startActivity(new Intent(MainActivity.this,UserMainActivity.class));
                    finish();
                }
            }
        });

        //TODO 08.08.2024
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String emailToLogin = email.getText().toString();
                String passwordToLogin = password.getText().toString();
                if(!TextUtils.isEmpty(email.getText().toString())&&!TextUtils.isEmpty(password.getText().toString())) {
                    authViewModel.signIn(emailToLogin,passwordToLogin);
                }
            }
        });

        singInGoogleBtn.setOnClickListener(view -> {
            // FOR NEXT IMPROVEMENTS - CREDENTIAL MANAGER
            /*if (Build.VERSION.SDK_INT > Build.VERSION_CODES.TIRAMISU) {
                googleSignInWithCredentialManager();
            }else{
                googleSignIn();
            }*/
            googleSignIn();

        });
    }

    private void googleSignIn() {

        GoogleSignInOptions options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.serverClientId))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(MainActivity.this,options);
        Log.i("Method google Sign IN","Method");
        Intent intent = googleSignInClient.getSignInIntent();
        activityResultLauncher.launch(intent);
    }

    public void googleSignInWithCredentialManager(){
        CancellationSignal cancellationSignal = null;
        CredentialManager credentialManager = CredentialManager.create(this);

        GetGoogleIdOption getGoogleIdOption = new GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(getString(R.string.serverClientId))
                .build();

        GetCredentialRequest request  = new GetCredentialRequest.Builder()
                .addCredentialOption(getGoogleIdOption)
                .build();


        credentialManager.getCredentialAsync(this, request, null,
                THREAD_POOL_EXECUTOR,
                new CredentialManagerCallback<GetCredentialResponse, androidx.credentials.exceptions.GetCredentialException>() {

                    @Override
                    public void onResult(GetCredentialResponse getCredentialResponse) {
                        handleSignIn(getCredentialResponse);
                    }

                    @Override
                    public void onError(@NonNull GetCredentialException e) {
                        Log.i("Google Login Problem",e.getMessage());
                      //  credentialManager.clearCredentialState(new ClearCredentialStateRequest(),null);
                    }
                });

    }

    public void handleSignIn(GetCredentialResponse result) {
        // Handle the successfully returned credential.
        Credential credential = result.getCredential();

        if(credential.getType().equals(GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL)){
            GoogleIdTokenCredential credentialData = GoogleIdTokenCredential.createFrom(credential.getData());
            AuthCredential authCredential = GoogleAuthProvider.getCredential(credentialData.getIdToken(),null);
            Log.i("CREDENTIAL DATA",credentialData.getId());
            authViewModel.signInWithGoogleCredential(authCredential,MainActivity.this,credentialData.getId());
        }

        if (credential instanceof PublicKeyCredential) {
            String responseJson = ((PublicKeyCredential) credential).getAuthenticationResponseJson();
            Log.e("Check the Tag", "ResponseJson");
            // Share responseJson i.e. a GetCredentialResponse on your server to validate and authenticate
        } else if (credential instanceof PasswordCredential) {
            String username = ((PasswordCredential) credential).getId();
            String password = ((PasswordCredential) credential).getPassword();
            Log.e("Check the Tag", "PasswordCredential");
            // Use id and password to send to your server to validate and authenticate
        } /*else if (credential instanceof CustomCredential) {
            if (ExampleCustomCredential.TYPE.equals(credential.getType())) {
                try {
                    ExampleCustomCredential customCred = ExampleCustomCredential.createFrom(customCredential.getData());
                    // Extract the required credentials and complete the
                    // authentication as per the federated sign in or any external
                    // sign in library flow
                } catch (ExampleCustomCredential.ExampleCustomCredentialParsingException e) {
                    // Unlikely to happen. If it does, you likely need to update the
                    // dependency version of your external sign-in library.
                    Log.e("Check the Tag", "Failed to parse an ExampleCustomCredential", e);
                }
            } else {
                // Catch any unrecognized custom credential type here.
                Log.e("Check the Tag", "Unexpected type of credential");
            }
        }*/ else {
            // Catch any unrecognized credential type here.
            Log.e("Check the Tag", "Unexpected type of credential");
        }

    }
}

