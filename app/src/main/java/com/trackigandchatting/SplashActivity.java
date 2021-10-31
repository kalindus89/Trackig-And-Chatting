package com.trackigandchatting;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.trackigandchatting.main_chat_activities.MainActivity;
import com.trackigandchatting.user_login.VerifyUserMobileNumber;


public class SplashActivity extends AppCompatActivity {

    private static int SPLASH_TIMER = 2500;
    ImageView logo;
    TextView appName;
    Animation uptodown,downtoup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);


        logo = findViewById(R.id.logo);
        appName = findViewById(R.id.appName);

        logo.setAnimation(AnimationUtils.loadAnimation(this,R.anim.uptodown));
        appName.setAnimation(AnimationUtils.loadAnimation(this,R.anim.downtoup));

        logo.animate().translationY(-1400).setDuration(700).setStartDelay(1780);
        appName.animate().translationY(1400).setDuration(700).setStartDelay(1780);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (FirebaseAuth.getInstance().getCurrentUser() != null) {


                   Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {

                    Intent intent = new Intent(SplashActivity.this, VerifyUserMobileNumber.class);
                    startActivity(intent);
                    SplashActivity.this.finish();
                }

            }
        }, SPLASH_TIMER);

    }
}