package com.commodots.umcaaconnecta.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

import com.commodots.umcaaconnecta.R;
import com.commodots.umcaaconnecta.utils.Helper;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        final Helper helper = new Helper(this);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                SharedPreferences prefs = getSharedPreferences("agreePrivacy", MODE_PRIVATE);
                String name = prefs.getString("isAllow", "notallow");//"No name defined" is the default value.

                if (name.equals("allow")) {
                    startActivity(new Intent(SplashActivity.this, helper.getLoggedInUser() != null ? MainActivity.class : SignInActivity.class));
                    finish();
                } else {
                    Intent intent = new Intent(SplashActivity.this, PrivacyAndPolicyActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        }, 1500);
    }
}
