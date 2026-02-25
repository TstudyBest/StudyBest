package com.example.studybest;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private EditText etEmail;
    private Button btnSend;
    private TextView tvBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        auth = FirebaseAuth.getInstance();

        etEmail = findViewById(R.id.etEmail);
        btnSend = findViewById(R.id.btnSend);
        tvBack = findViewById(R.id.tvBack);

        btnSend.setOnClickListener(v -> sendReset());
        tvBack.setOnClickListener(v -> finish());
    }

    private void sendReset() {
        String email = etEmail.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email required");
            etEmail.requestFocus();
            return;
        }

        auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Reset email sent ✅", Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        Toast.makeText(this,
                                task.getException() != null ? task.getException().getMessage() : "Failed",
                                Toast.LENGTH_LONG).show();
                    }
                });
    }
}