package com.example.studybest;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class HomeActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        auth = FirebaseAuth.getInstance();

        // if somehow user is not logged in, send them back
        if (auth.getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        askNotificationPermissionIfNeeded();
        createNotificationChannel();

        bottomNav = findViewById(R.id.bottomNav);

        loadFragment(new HomeFragment());
        bottomNav.setSelectedItemId(R.id.nav_home);

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selected = null;

            int id = item.getItemId();
            if (id == R.id.nav_home) {
                selected = new HomeFragment();
            } else if (id == R.id.nav_subjects) {
                selected = new SubjectsFragment();
            } else if (id == R.id.nav_notes) {
                selected = new NotesFragment();
            } else if (id == R.id.nav_notifications) {
                selected = new NotificationsFragment();
            } else if (id == R.id.nav_signout) {
                showSignOutDialog();
                return true;
            }

            return loadFragment(selected);
        });
    }

    // shows a confirm dialog before signing out
    private void showSignOutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Sign Out")
                .setMessage("Are you sure you want to sign out?")
                .setPositiveButton("Sign Out", (d, w) -> {
                    auth.signOut();
                    Intent i = new Intent(this, LoginActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void askNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        1001
                );
            }
        }
    }

    private boolean loadFragment(Fragment fragment) {
        if (fragment == null) return false;

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();

        return true;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            android.app.NotificationChannel channel =
                    new android.app.NotificationChannel(
                            "studybest_reminders",
                            "StudyBest Reminders",
                            android.app.NotificationManager.IMPORTANCE_HIGH
                    );
            channel.setDescription("Task reminder notifications");

            android.app.NotificationManager nm = getSystemService(android.app.NotificationManager.class);
            if (nm != null) {
                nm.createNotificationChannel(channel);
            }
        }
    }
}
