package com.pepin_prestini.elim.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Adrien on 16/02/2018.
 */

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // Start home activity
        startActivity(new Intent(SplashActivity.this, MainActivity.class));
        // close splash activity
        finish();
    }
}
