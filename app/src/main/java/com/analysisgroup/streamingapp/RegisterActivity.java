package com.analysisgroup.streamingapp;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    EditText Username, FirstName, LastName, Birthday, Email, Password;
    Button btnRegistrar;

    FirebaseAuth auth;
    ProgressDialog progressDialog;
    Calendar calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Created action bar
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setTitle(R.string.registerBarTitle); // set the title.
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        // Initializing firebase
        auth = FirebaseAuth.getInstance();

        Username = findViewById(R.id.username);
        FirstName = findViewById(R.id.names);
        LastName = findViewById(R.id.lastNames);
        Birthday = findViewById(R.id.birthday);
        Email = findViewById(R.id.email);
        Password = findViewById(R.id.password);

        btnRegistrar = findViewById(R.id.btnRegister);

        MaterialDatePicker.Builder<Long> datePicker = MaterialDatePicker.Builder.datePicker();
        datePicker.setTheme(R.style.ThemeOverlay_App_DatePicker);
        datePicker.setTitleText(R.string.birthday);
        MaterialDatePicker<Long> datePickerBuild = datePicker.build();

        Birthday.setOnClickListener(view -> datePickerBuild.show(getSupportFragmentManager(), "DATE_PICKER"));

        datePickerBuild.addOnPositiveButtonClickListener(selection -> {
            calendar = Calendar.getInstance();
            calendar.setTimeInMillis(selection);
            SimpleDateFormat dateFormat = new SimpleDateFormat("d'/'M'/'yyyy");
            String sDate = dateFormat.format(calendar.getTime());
            Birthday.setText(sDate);
        });

        // Click event button
        btnRegistrar.setOnClickListener(view -> {
            String username = Username.getText().toString();
            String firstName = FirstName.getText().toString();
            String lastName = LastName.getText().toString();
            String birthday = Birthday.getText().toString();
            String email = Email.getText().toString();
            String password = Password.getText().toString();

            if(email.equals("") || password.equals("") || birthday.equals("") || lastName.equals("") || firstName.equals("") || username.equals("")) {
                Toast.makeText(RegisterActivity.this, R.string.errorEmptyField, Toast.LENGTH_SHORT).show();
            } else {
                //Validation
                if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    Email.setError(getString(R.string.errorInvalidEmail));
                    Email.setFocusable(true);
                }
                else if(Password.length() < 6) {
                    Password.setError(getString(R.string.errorInvalidPassword));
                    Password.setFocusable(true);
                }
                else {
                    registerAccount(email, password);
                }
            }
        });

        progressDialog = new ProgressDialog(RegisterActivity.this);
        progressDialog.setMessage(getString(R.string.registerMessage));
        progressDialog.setCancelable(false);

    }

    private void registerAccount(String email, String password) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    //If the administrator was created correctly
                    if(task.isSuccessful()) {
                        progressDialog.dismiss();
                        FirebaseUser user = auth.getCurrentUser();
                        assert user != null; //Verify that admin is not null

                        //Convert admin data to string
                        String UID = user.getUid();
                        String username = Username.getText().toString();
                        String email2 = Email.getText().toString();
                        String firstName = FirstName.getText().toString();
                        String lastName = LastName.getText().toString();
                        String birthday = Birthday.getText().toString();
                        String key = String.valueOf(Calendar.getInstance().getTimeInMillis());

                        HashMap<Object, Object> Users = new HashMap<>();
                        Users.put("UID", UID);
                        Users.put("Username", username);
                        Users.put("Email", email2);
                        Users.put("FirstName", firstName);
                        Users.put("LastName", lastName);
                        Users.put("Birthday", birthday);
                        Users.put("SecretKey", key);
                        Users.put("Image", "");

                        // Initializing firebaseDatabase
                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                        DatabaseReference reference = database.getReference("DATABASE USERS");
                        reference.child(UID).setValue(Users);

                        startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                        Toast.makeText(RegisterActivity.this, R.string.registerSuccess, Toast.LENGTH_SHORT).show();
                        RegisterActivity.this.finish();
                    }
                    else {
                        progressDialog.dismiss();
                        Toast.makeText(RegisterActivity.this, R.string.errorServer, Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(RegisterActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}