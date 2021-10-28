package com.trackigandchatting.user_login;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.WindowManager;

import com.trackigandchatting.R;

public class CreateProfile extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_profile);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

    }
}