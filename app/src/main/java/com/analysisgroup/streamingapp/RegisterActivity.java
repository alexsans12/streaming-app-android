package com.analysisgroup.streamingapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    EditText Username, FirstName, LastName, Birthday, Email, Password;
    Button btnRegistrar;

    FirebaseAuth auth;

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Created action bar
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setTitle("Crear una cuenta"); // set the title.
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
        datePicker.setTitleText("Selecciona la fecha");
        MaterialDatePicker<Long> datePickerBuild = datePicker.build();

        Birthday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                datePickerBuild.show(getSupportFragmentManager(), "DATE_PICKER");
            }
        });

        datePickerBuild.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener<Long>() {
            @Override
            public void onPositiveButtonClick(Long selection) {
                Date date = new Date(datePickerBuild.getHeaderText());
                @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("d'/'M'/'yyyy");
                String sDate = dateFormat.format(date);
                Birthday.setText(sDate);
            }
        });

        Date date = new Date();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("d'/'M'/'yyyy");
        String sDate = "dd/mm/yyyy";

        Birthday.setText(sDate);

        // Click event button
        btnRegistrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = Username.getText().toString();
                String firstName = FirstName.getText().toString();
                String lastName = LastName.getText().toString();
                String birthday = Birthday.getText().toString();
                String email = Email.getText().toString();
                String password = Password.getText().toString();

                boolean isDate = false;

                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/mm/yyyy");
                simpleDateFormat.setLenient(false);

                try {
                    simpleDateFormat.parse(birthday);
                    isDate = true;
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                if(email.equals("") || password.equals("") || !isDate || lastName.equals("") || firstName.equals("") || username.equals("")) {
                    Toast.makeText(RegisterActivity.this, "All fields must be filled", Toast.LENGTH_SHORT).show();
                } else {
                    //Validation
                    if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        Email.setError("Invalid email...");
                        Email.setFocusable(true);
                    }
                    else if(Password.length() < 6) {
                        Password.setError("Password must contain at least six characters");
                        Password.setFocusable(true);
                    }
                    else {
                        registerAccount(email, password);
                    }
                }
            }
        });

        progressDialog = new ProgressDialog(RegisterActivity.this);
        progressDialog.setMessage("Registering, please wait...");
        progressDialog.setCancelable(false);

    }

    private void registerAccount(String email, String password) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
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

                            HashMap<Object, Object> Users = new HashMap<>();
                            Users.put("UID", UID);
                            Users.put("Username", username);
                            Users.put("Email", email2);
                            Users.put("FirstName", firstName);
                            Users.put("LastName", lastName);
                            Users.put("Birthday", birthday);
                            Users.put("image", "");

                            // Initializing firebaseDatabase
                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            DatabaseReference reference = database.getReference("DATABASE USERS");
                            reference.child(UID).setValue(Users);

                            startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                            Toast.makeText(RegisterActivity.this, "Successful registration", Toast.LENGTH_SHORT).show();
                            RegisterActivity.this.finish();
                        }
                        else {
                            progressDialog.dismiss();
                            Toast.makeText(RegisterActivity.this, "An error has occurred", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(RegisterActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}