package com.example.studybest;

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

        // Screens
         tvGoRegister.setOnClickListener(v -> startActivity(new Intent(this, SignUpActivity.class)));
         tvForgot.setOnClickListener(v -> startActivity(new Intent(this, ForgotPasswordActivity.class)));
    }

    private void attemptLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email required");
            etEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password required");
            etPassword.requestFocus();
            return;
        }

        setLoading(true);

        setLoading(true);

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    setLoading(false); // ✅ ALWAYS stop loading here

                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Login success ✅", Toast.LENGTH_LONG).show();
                        // next step: go home (we will add HomeActivity soon)
                    } else {
                        String msg = (task.getException() != null)
                                ? task.getException().getMessage()
                                : "Login failed";
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