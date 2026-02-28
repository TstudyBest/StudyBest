package com.example.studybest;

import android.content.Intent;
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
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvForgot, tvGoRegister;
    private ProgressBar progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvForgot = findViewById(R.id.tvForgot);
        tvGoRegister = findViewById(R.id.tvGoRegister);
        progress = findViewById(R.id.progress);

        btnLogin.setOnClickListener(v -> attemptLogin());
        tvGoRegister.setOnClickListener(v -> startActivity(new Intent(this, SignUpActivity.class)));
        tvForgot.setOnClickListener(v -> startActivity(new Intent(this, ForgotPasswordActivity.class)));
    }

    @Override
    protected void onStart() {
        super.onStart();
        // if already logged in, skip straight to home
        FirebaseUser current = auth.getCurrentUser();
        if (current != null) {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        }
    }

    private void attemptLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return;
        }

        // check it looks like an actual email
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

        setLoading(true);

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    setLoading(false);

                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user == null) {
                            Toast.makeText(this, "Something went wrong, please try again", Toast.LENGTH_LONG).show();
                            return;
                        }
                        startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                        finish();
                    } else {
                        String msg = "Login failed. Please check your credentials.";
                        if (task.getException() != null) {
                            String raw = task.getException().getMessage();
                            if (raw != null && raw.contains("no user record")) {
                                msg = "No account found with this email.";
                            } else if (raw != null && raw.contains("password is invalid")) {
                                msg = "Incorrect password.";
                            }
                        }
                        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void setLoading(boolean loading) {
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!loading);
        etEmail.setEnabled(!loading);
        etPassword.setEnabled(!loading);
    }
}
