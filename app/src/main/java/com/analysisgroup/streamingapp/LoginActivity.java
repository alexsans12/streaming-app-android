package com.analysisgroup.streamingapp;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    EditText Email, Password;
    Button btnLogin;
    TextView btnRegister;

    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Created action bar
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setTitle(R.string.login); // set the title
        actionBar.setDisplayShowHomeEnabled(true);

        Email = findViewById(R.id.email);
        Password = findViewById(R.id.password);
        btnLogin = findViewById(R.id.login);
        btnRegister = findViewById(R.id.register);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        progressDialog = new ProgressDialog(LoginActivity.this);
        progressDialog.setMessage(getString(R.string.loginMessage));
        progressDialog.setCancelable(false);

        btnLogin.setOnClickListener(view -> {
            String email = Email.getText().toString();
            String password = Password.getText().toString();

            if(email.equals("") || password.equals("")) {
                Toast.makeText(LoginActivity.this, R.string.errorEmptyField, Toast.LENGTH_SHORT).show();
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
                    signInUser(email, password);
                }
            }
        });

        btnRegister.setOnClickListener(view -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onStart() {
        checkingLogin();
        super.onStart();
    }

    private void checkingLogin() {
        if(firebaseUser != null) {
            // If you are logged in as administrator
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
            Toast.makeText(this, R.string.loginSuccess, Toast.LENGTH_SHORT).show();
        }
    }

    private void signInUser(String email, String password) {
        progressDialog.show();
        progressDialog.setCancelable(false);

        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(LoginActivity.this, task -> {
                    if(task.isSuccessful()) {
                        progressDialog.dismiss();
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class)
                                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        assert user != null;
                        Toast.makeText(LoginActivity.this, getString(R.string.welcome) + user.getEmail(), Toast.LENGTH_SHORT).show();

                        startActivity(intent);
                        finish();
                    } else {
                        progressDialog.dismiss();
                        invalidUser();
                    }
                }).addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    invalidUser();
                });
    }

    private void invalidUser() {
        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
        builder.setCancelable(false);
        builder.setTitle(R.string.errorServer);
        builder.setMessage(R.string.loginMessageError)
                .setPositiveButton("Okay", (dialogInterface, i) -> dialogInterface.dismiss()).show();
    }
}