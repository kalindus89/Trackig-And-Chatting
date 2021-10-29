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
import android.widget.Toast;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.hbb20.CountryCodePicker;
import com.trackigandchatting.MainActivity;
import com.trackigandchatting.MainLoginActivity;
import com.trackigandchatting.R;

import java.util.concurrent.TimeUnit;

public class VerifyUserMobileNumber extends AppCompatActivity {

    EditText getphonenumber;
    Button sendotpbutton;
    CountryCodePicker countrycodepicker;
    ProgressBar progressbarofmain;
    String countryCode, phoneNumber;
    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    FirebaseAuth firebaseAuth;
    String codeSent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_user_mobile_number);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        countrycodepicker = findViewById(R.id.countrycodepicker);
        getphonenumber = findViewById(R.id.getphonenumber);
        sendotpbutton = findViewById(R.id.sendotpbutton);
        progressbarofmain = findViewById(R.id.progressbarofmain);

        firebaseAuth = FirebaseAuth.getInstance();

        countryCode = countrycodepicker.getSelectedCountryCodeWithPlus();

        countrycodepicker.setOnCountryChangeListener(new CountryCodePicker.OnCountryChangeListener() {
            @Override
            public void onCountrySelected() {
                countryCode = countrycodepicker.getSelectedCountryCodeWithPlus();
            }
        });

      /*  sendotpbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(VerifyUserMobileNumber.this, OtpAuthentication.class);
                startActivity(intent);
                finish();
            }
        });*/

        sendotpbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String number = getphonenumber.getText().toString().trim();

                if (number.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Please enter your number", Toast.LENGTH_SHORT).show();
                } else if (number.length() < 9) {
                    Toast.makeText(getApplicationContext(), "Please enter correct number", Toast.LENGTH_SHORT).show();
                }else{

                    progressbarofmain.setVisibility(View.VISIBLE);
                 //   sendotpbutton.setEnabled(false);

                    phoneNumber=countryCode+number;// +,country code and number enter

                    Toast.makeText(getApplicationContext(), "Everything ok "+phoneNumber, Toast.LENGTH_SHORT).show();

                    PhoneAuthOptions options= PhoneAuthOptions
                            .newBuilder(firebaseAuth)
                            .setPhoneNumber(phoneNumber)
                            .setTimeout(60L, TimeUnit.SECONDS)
                            .setActivity(VerifyUserMobileNumber.this)
                            .setCallbacks(mCallbacks)
                            .build();

                    PhoneAuthProvider.verifyPhoneNumber(options);

                }
            }
        });

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                // how to automatically fetch code here budihail
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {

            }

            @Override
            public void onCodeSent(@NonNull String code, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(code, forceResendingToken);

                Toast.makeText(getApplicationContext(), "OTP is sent", Toast.LENGTH_SHORT).show();
                progressbarofmain.setVisibility(View.INVISIBLE);
                sendotpbutton.setEnabled(true);
                codeSent=code;

                Intent intent = new Intent(VerifyUserMobileNumber.this, OtpAuthentication.class);
                intent.putExtra("otp",codeSent);
                startActivity(intent);
                finish();
            }
        };
    }

}