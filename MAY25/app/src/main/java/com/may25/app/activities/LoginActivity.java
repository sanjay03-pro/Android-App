package com.may25.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.may25.app.R;
import com.may25.app.models.User;

import java.util.HashMap;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword, etName;
    private Button btnLogin, btnRegister;
    private ProgressBar progressBar;
    private TextView tvToggle;
    private FirebaseAuth mAuth;
    private DatabaseReference dbRef;
    private boolean isLoginMode = true;

    // Only these 2 emails are allowed to use the app
    private static final String USER1_EMAIL = "user1@may25.com";
    private static final String USER2_EMAIL = "user2@may25.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Enable offline caching so chat/data doesn't appear to "disappear"
        // when reopening the app with a slow or briefly unavailable connection.
        // Must be called only once, before any other database reference is used.
        try {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        } catch (Exception ignored) {
            // Already enabled (e.g. app resumed without a fresh process) — safe to ignore
        }

        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference();

        // If already logged in, go to main
        if (mAuth.getCurrentUser() != null) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        etEmail    = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        etName     = findViewById(R.id.et_name);
        btnLogin   = findViewById(R.id.btn_login);
        progressBar = findViewById(R.id.progress_bar);
        tvToggle   = findViewById(R.id.tv_toggle);

        btnLogin.setOnClickListener(v -> {
            String email    = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String name     = etName.getText().toString().trim();

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(this, "Fill in email and password", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!email.equals(USER1_EMAIL) && !email.equals(USER2_EMAIL)) {
                Toast.makeText(this, "Access denied. This app is private.", Toast.LENGTH_LONG).show();
                return;
            }

            progressBar.setVisibility(View.VISIBLE);

            if (isLoginMode) {
                loginUser(email, password);
            } else {
                if (TextUtils.isEmpty(name)) {
                    Toast.makeText(this, "Enter your name", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    return;
                }
                registerUser(email, password, name);
            }
        });

        tvToggle.setOnClickListener(v -> toggleMode());
    }

    private void toggleMode() {
        isLoginMode = !isLoginMode;
        etName.setVisibility(isLoginMode ? View.GONE : View.VISIBLE);
        btnLogin.setText(isLoginMode ? "Login" : "Register");
        tvToggle.setText(isLoginMode ? "Don't have an account? Register" : "Already have an account? Login");
    }

    private void loginUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        startActivity(new Intent(this, MainActivity.class));
                        finish();
                    } else {
                        Toast.makeText(this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void registerUser(String email, String password, String name) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        HashMap<String, Object> userMap = new HashMap<>();
                        userMap.put("uid", user.getUid());
                        userMap.put("name", name);
                        userMap.put("email", email);
                        userMap.put("online", true);
                        dbRef.child("users").child(user.getUid()).setValue(userMap);

                        startActivity(new Intent(this, MainActivity.class));
                        finish();
                    } else {
                        Toast.makeText(this, "Register failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}
