package com.trackigandchatting.user_login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.trackigandchatting.R;

public class OtpAuthentication extends AppCompatActivity {

    Button verifyOtpButton;
    String enterOtp,optReceived;
    ProgressBar progressbarofotpauth;
    EditText getotp;
    TextView changenumber;
    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_authentication);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        optReceived = getIntent().getStringExtra("otp");

        verifyOtpButton=findViewById(R.id.verifyOtpButton);
        changenumber = findViewById(R.id.changenumber);
        getotp = findViewById(R.id.getotp);
        progressbarofotpauth = findViewById(R.id.progressbarofotpauth);

        firebaseAuth = FirebaseAuth.getInstance();

        changenumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(OtpAuthentication.this, VerifyUserMobileNumber.class);
                startActivity(intent);
                finish();
            }
        });

        verifyOtpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                enterOtp = getotp.getText().toString().trim();
                if (enterOtp.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Enter your OTP", Toast.LENGTH_SHORT).show();
                } else {
                    progressbarofotpauth.setVisibility(View.VISIBLE);
                    verifyOtpButton.setEnabled(false);

                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(optReceived, enterOtp);
                    signInWithPhoneAuthCredential(credential);
                }
            }
        });
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {

        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {


                if (task.isSuccessful()) {

                    progressbarofotpauth.setVisibility(View.INVISIBLE);
                    verifyOtpButton.setEnabled(true);

                    Toast.makeText(getApplicationContext(), "Login Success", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(OtpAuthentication.this, CreateProfile.class);
                    startActivity(intent);
                    finish();

                } else {
                    progressbarofotpauth.setVisibility(View.INVISIBLE);
                    verifyOtpButton.setEnabled(true);

                    if (task.getException() instanceof FirebaseAuthInvalidCredentialsException)

                        Toast.makeText(getApplicationContext(), "Login Failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}