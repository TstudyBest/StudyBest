package com.example.studybest;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class HomeFragment extends Fragment {

    private TextView tvSubjectsCount, tvTasksCount, tvPendingCount, tvRemindersCount, tvWelcome;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_home, container, false);

        tvWelcome = v.findViewById(R.id.tvWelcome);
        tvSubjectsCount = v.findViewById(R.id.tvSubjectsCount);
        tvTasksCount = v.findViewById(R.id.tvTasksCount);
        tvPendingCount = v.findViewById(R.id.tvPendingCount);
        tvRemindersCount = v.findViewById(R.id.tvRemindersCount);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        loadCounts();

        return v;
    }

    private void loadCounts() {
        FirebaseUser user = auth.getCurrentUser();

        // fail fast if not logged in
        if (user == null) {
            redirectToLogin();
            return;
        }

        String uid = user.getUid();

        if (uid == null || uid.isEmpty()) {
            redirectToLogin();
            return;
        }

        // show welcome with email
        String email = user.getEmail();
        if (email != null && !email.isEmpty()) {
            tvWelcome.setText("Welcome back, " + email.split("@")[0] + "!");
        } else {
            tvWelcome.setText("Welcome back!");
        }

        // subjects count
        db.collection("users")
                .document(uid)
                .collection("subjects")
                .get()
                .addOnSuccessListener(q -> {
                    if (isAdded()) {
                        tvSubjectsCount.setText(String.valueOf(q.size()));
                    }
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) tvSubjectsCount.setText("0");
                });

        // all tasks
        db.collectionGroup("tasks")
                .whereEqualTo("ownerUid", uid)
                .get()
                .addOnSuccessListener(q -> {
                    if (isAdded()) tvTasksCount.setText(String.valueOf(q.size()));
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) tvTasksCount.setText("0");
                });

        // pending tasks
        db.collectionGroup("tasks")
                .whereEqualTo("ownerUid", uid)
                .whereEqualTo("done", false)
                .get()
                .addOnSuccessListener(q -> {
                    if (isAdded()) tvPendingCount.setText(String.valueOf(q.size()));
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) tvPendingCount.setText("0");
                });

        // tasks with reminders
        db.collectionGroup("tasks")
                .whereEqualTo("ownerUid", uid)
                .whereGreaterThan("remindAt", 0)
                .get()
                .addOnSuccessListener(q -> {
                    if (isAdded()) tvRemindersCount.setText(String.valueOf(q.size()));
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) tvRemindersCount.setText("0");
                });
    }

    private void redirectToLogin() {
        if (isAdded() && getActivity() != null) {
            startActivity(new Intent(getActivity(), LoginActivity.class));
            getActivity().finish();
        }
    }
}
