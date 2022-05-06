package com.analysisgroup.streamingapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import com.analysisgroup.streamingapp.LiveVideoBroadcaster.LiveVideoBroadcasterActivity;
import com.analysisgroup.streamingapp.MainFragments.HomeFragment;
import com.analysisgroup.streamingapp.MainFragments.LiveFragment;
import com.analysisgroup.streamingapp.MainFragments.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity implements NavigationBarView.OnItemSelectedListener {

    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;

    public static final String RTMP_BASE_URL = "rtmp://20.124.2.54/LiveApp/999831198613297070837254";
    public static final String HLS_BASE_URL = "http://20.124.2.54:5080/LiveApp/streams/999831198613297070837254.m3u8?token=undefined&subscriberId=undefined&subscriberCode=undefined";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        BottomNavigationView bottomNavigationView = findViewById(R.id.nav_view_main);
        bottomNavigationView.setOnItemSelectedListener(this);
        bottomNavigationView.setItemIconTintList(null);

        if(savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container_main, new HomeFragment()).commit();
        }
    }

    @Override
    protected void onStart() {
        checkingLogin();
        super.onStart();
    }

    private void checkingLogin() {
        if(firebaseUser == null) {
            // If you are not logged in as administrator
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
            Toast.makeText(this, "Necesitas iniciar sesion", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.home:
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container_main, new HomeFragment()).commit();
                break;
            case R.id.live:
                startActivity(new Intent(MainActivity.this, LiveVideoBroadcasterActivity.class));
                /*getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container_main, new LiveFragment()).commit();*/
                break;
            case R.id.profile:
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container_main, new ProfileFragment()).commit();
                break;
            default:
                Toast.makeText(MainActivity.this, "An error has occurred...", Toast.LENGTH_SHORT).show();
        }

        return true;
    }
}