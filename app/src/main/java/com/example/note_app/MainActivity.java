package com.example.note_app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.safetynet.SafetyNet;
import com.google.android.gms.safetynet.SafetyNetApi;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class MainActivity extends AppCompatActivity {

    private EditText mloginemail,mloginpassword;
    private RelativeLayout mlogin,mgotosignup;
    private  TextView mgotoforgotpassword;

    private FirebaseAuth firebaseAuth;

    ProgressBar mprogressbarofmainactivity;

    private static final String SAFETY_NET_API_KEY = "6Lfx-0smAAAAALabQz4_OzjiFxNyLhPvfDRgJmvG";

    private GoogleApiClient mGoogleApiClient;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().hide();

        mloginemail=findViewById(R.id.loginemail);
        mloginpassword=findViewById(R.id.loginpassword);
        mlogin=findViewById(R.id.login);
        mgotoforgotpassword=findViewById(R.id.gotoforgotpassword);
        mgotosignup=findViewById(R.id.gotosignup);
        mprogressbarofmainactivity=findViewById(R.id.progressbarofmainactivity);

        firebaseAuth=FirebaseAuth.getInstance();
        FirebaseUser firebaseUser=firebaseAuth.getCurrentUser();

        if(firebaseUser!=null)
        {
            finish();
            startActivity(new Intent(MainActivity.this,notesactivity.class));
        }

        mgotosignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,signup.class));
            }
        });

        mgotoforgotpassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,forgotpassword.class));
            }
        });

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(SafetyNet.API)
                .build();
        mlogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mail = mloginemail.getText().toString().trim();
                String password = mloginpassword.getText().toString().trim();
                //
                if (mail.isEmpty() || password.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "All Fields Are Required", Toast.LENGTH_SHORT).show();
                } else {
                    // Perform reCAPTCHA verification
                    SafetyNet.getClient(MainActivity.this).verifyWithRecaptcha(SAFETY_NET_API_KEY)
                            .addOnCompleteListener(new OnCompleteListener<SafetyNetApi.RecaptchaTokenResponse>() {
                                @Override
                                public void onComplete(@NonNull Task<SafetyNetApi.RecaptchaTokenResponse> task) {
                                    if (task.isSuccessful()) {
                                        SafetyNetApi.RecaptchaTokenResponse response = task.getResult();
                                        if (response != null && !response.getTokenResult().isEmpty()) {
                                            // reCAPTCHA verification successful, continue with login
                                            String token = response.getTokenResult();
                                            // Perform Firebase login authentication
                                            firebaseAuth.signInWithEmailAndPassword(mail, password)
                                                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<AuthResult> task) {
                                                            if (task.isSuccessful()) {
                                                                checkmailverfication();
                                                            } else {
                                                                Toast.makeText(getApplicationContext(), "Failed to login", Toast.LENGTH_SHORT).show();
                                                                mprogressbarofmainactivity.setVisibility(View.INVISIBLE);
                                                            }
                                                        }
                                                    });
                                        } else {
                                            // reCAPTCHA verification failed, show an error message
                                            Toast.makeText(getApplicationContext(), "reCAPTCHA verification failed", Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        // Error occurred during reCAPTCHA verification, show an error message
                                        Exception exception = task.getException();
                                        if (exception instanceof ApiException) {
                                            ApiException apiException = (ApiException) exception;
                                            int statusCode = apiException.getStatusCode();
                                            Toast.makeText(getApplicationContext(), "Error: " + CommonStatusCodes.getStatusCodeString(statusCode), Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(getApplicationContext(), "Error: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }
                            });
                }
            }

        });

    }


    private void checkmailverfication()
    {
        FirebaseUser firebaseUser=firebaseAuth.getCurrentUser();

        if(firebaseUser.isEmailVerified()==true)
        {
            Toast.makeText(getApplicationContext(),"Logged In",Toast.LENGTH_SHORT).show();
            finish();
            startActivity(new Intent(MainActivity.this,notesactivity.class));
        }
        else
        {
            mprogressbarofmainactivity.setVisibility(View.INVISIBLE);
            Toast.makeText(getApplicationContext(),"Verify your mail first",Toast.LENGTH_SHORT).show();
            firebaseAuth.signOut();
        }
    }

}