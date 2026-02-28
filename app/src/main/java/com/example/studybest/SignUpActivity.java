package com.example.studybest;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class SignUpActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private EditText etEmail, etPassword, etConfirmPassword;
    private Button btnCreate;
    private TextView tvBackToLogin;
    private ProgressBar progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        auth = FirebaseAuth.getInstance();

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnCreate = findViewById(R.id.btnCreate);
        tvBackToLogin = findViewById(R.id.tvBackToLogin);
        progress = findViewById(R.id.progress);

        btnCreate.setOnClickListener(v -> createAccount());
        tvBackToLogin.setOnClickListener(v -> finish());
    }

    private void createAccount() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();
        String confirm = etConfirmPassword.getText().toString();

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Please enter a valid email address");
            etEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            etPassword.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(confirm)) {
            etConfirmPassword.setError("Please confirm your password");
            etConfirmPassword.requestFocus();
            return;
        }

        if (!password.equals(confirm)) {
            etConfirmPassword.setError("Passwords do not match");
            etConfirmPassword.requestFocus();
            return;
        }

        setLoading(true);

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    setLoading(false);
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Account created successfully!", Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        String msg = "Sign up failed. Please try again.";
                        if (task.getException() != null && task.getException().getMessage() != null) {
                            if (task.getException().getMessage().contains("already in use")) {
                                msg = "This email is already registered.";
                            }
                        }
                        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void setLoading(boolean loading) {
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnCreate.setEnabled(!loading);
        etEmail.setEnabled(!loading);
        etPassword.setEnabled(!loading);
        etConfirmPassword.setEnabled(!loading);
    }
}
