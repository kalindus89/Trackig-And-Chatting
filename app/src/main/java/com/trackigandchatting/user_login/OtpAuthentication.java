package com.trackigandchatting.user_login;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import com.trackigandchatting.R;

public class OtpAuthentication extends AppCompatActivity {

    Button verifyOtpButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_authentication);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        verifyOtpButton=findViewById(R.id.verifyOtpButton);

        verifyOtpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(OtpAuthentication.this, CreateProfile.class);
                startActivity(intent);
                finish();
            }
        });
    }
}