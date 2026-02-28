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

public class ForgotPasswordActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private EditText etEmail;
    private Button btnSend;
    private TextView tvBack;
    private ProgressBar progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        auth = FirebaseAuth.getInstance();

        etEmail = findViewById(R.id.etEmail);
        btnSend = findViewById(R.id.btnSend);
        tvBack = findViewById(R.id.tvBack);
        progress = findViewById(R.id.progress);

        btnSend.setOnClickListener(v -> sendReset());
        tvBack.setOnClickListener(v -> finish());
    }

    private void sendReset() {
        String email = etEmail.getText().toString().trim();

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

        setLoading(true);

        auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    setLoading(false);
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Reset link sent! Check your inbox.", Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Could not send reset email. Please try again.", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void setLoading(boolean loading) {
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnSend.setEnabled(!loading);
        etEmail.setEnabled(!loading);
    }
}
