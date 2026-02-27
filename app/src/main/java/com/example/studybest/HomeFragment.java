package com.example.studybest;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class HomeFragment extends Fragment {

    private TextView tvSubjectsCount, tvTasksCount, tvPendingCount, tvRemindersCount;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_home, container, false);

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
        String uid = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (uid == null) {
            Toast.makeText(getContext(), "Not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1) Subjects count
        db.collection("users")
                .document(uid)
                .collection("subjects")
                .get()
                .addOnSuccessListener(q -> tvSubjectsCount.setText("Subjects: " + q.size()));

        // 2) All tasks across all subjects (collectionGroup)
        db.collectionGroup("tasks")
                .whereEqualTo("ownerUid", uid)   // we will add this field in Step 2
                .get()
                .addOnSuccessListener(q -> tvTasksCount.setText("Tasks: " + q.size()));

        // 3) Pending tasks
        db.collectionGroup("tasks")
                .whereEqualTo("ownerUid", uid)
                .whereEqualTo("done", false)
                .get()
                .addOnSuccessListener(q -> tvPendingCount.setText("Pending: " + q.size()));

        // 4) Reminders count (tasks with remindAt)
        db.collectionGroup("tasks")
                .whereEqualTo("ownerUid", uid)
                .whereGreaterThan("remindAt", 0)
                .get()
                .addOnSuccessListener(q -> tvRemindersCount.setText("Reminders: " + q.size()));
    }
}