package com.trackigandchatting.user_login;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import com.trackigandchatting.MainLoginActivity;
import com.trackigandchatting.R;

public class VerifyUserMobileNumber extends AppCompatActivity {

    Button sendotpbutton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_user_mobile_number);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        sendotpbutton=findViewById(R.id.sendotpbutton);

        sendotpbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(VerifyUserMobileNumber.this, OtpAuthentication.class);
                startActivity(intent);
                finish();
            }
        });
    }
}