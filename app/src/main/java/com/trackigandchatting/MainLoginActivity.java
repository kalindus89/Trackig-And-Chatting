package com.trackigandchatting;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.trackigandchatting.user_login.VerifyUserMobileNumber;

public class MainLoginActivity extends AppCompatActivity {


    Button intro_signIn;
    TextView sign_in;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_login);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        intro_signIn=findViewById(R.id.intro_signIn);
        sign_in=findViewById(R.id.sign_in);

        intro_signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainLoginActivity.this, VerifyUserMobileNumber.class);
                startActivity(intent);
                MainLoginActivity.this.finish();
            }
        });

        sign_in.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainLoginActivity.this, VerifyUserMobileNumber.class);
                startActivity(intent);
                MainLoginActivity.this.finish();
            }
        });
    }
}