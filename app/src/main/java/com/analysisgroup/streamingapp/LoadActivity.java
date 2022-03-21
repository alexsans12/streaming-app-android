package com.analysisgroup.streamingapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;

public class LoadActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_load);

        final int DURATION = 2000;

        new Handler().postDelayed(() -> {
            // Code that runs
            Intent intent = new Intent(LoadActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }, DURATION);
    }
}