package com.trackigandchatting.specific_chat;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.WindowManager;

import com.trackigandchatting.R;

public class SpecificChatActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_specific_chat);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }
}