package com.analysisgroup.streamingapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import com.analysisgroup.streamingapp.MainFragments.HomeFragment;
import com.analysisgroup.streamingapp.MainFragments.LiveFragment;
import com.analysisgroup.streamingapp.MainFragments.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity implements NavigationBarView.OnItemSelectedListener {

    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        BottomNavigationView bottomNavigationView = findViewById(R.id.nav_view_main);
        bottomNavigationView.setBackground(null);
        bottomNavigationView.setOnItemSelectedListener(this);
        bottomNavigationView.setItemIconTintList(null);

        FloatingActionButton floatingActionButton = findViewById(R.id.init_stream);

        floatingActionButton.setOnClickListener(click -> getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container_main, new LiveFragment()).commit());

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

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.home:
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container_main, new HomeFragment()).commit();
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