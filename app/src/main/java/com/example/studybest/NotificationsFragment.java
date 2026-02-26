package com.example.studybest;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studybest.adapters.AlertsAdapter;
import com.example.studybest.models.ReminderItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class NotificationsFragment extends Fragment {

    private RecyclerView rv;
    private AlertsAdapter adapter;
    private final ArrayList<ReminderItem> list = new ArrayList<>();

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_notifications, container, false);

        rv = v.findViewById(R.id.rvAlerts);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new AlertsAdapter(list);
        rv.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        loadReminders();

        return v;
    }

    private void loadReminders() {
        String uid = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (uid == null) return;

        // We first load subjects, then for each subject load tasks where remindAt exists
        db.collection("users").document(uid).collection("subjects")
                .get()
                .addOnSuccessListener(subjects -> {
                    list.clear();

                    for (var subjectDoc : subjects.getDocuments()) {
                        String subjectName = subjectDoc.getString("name");
                        String subjectId = subjectDoc.getId();

                        db.collection("users").document(uid)
                                .collection("subjects").document(subjectId)
                                .collection("tasks")
                                .whereGreaterThan("remindAt", 0)
                                .get()
                                .addOnSuccessListener(tasks -> {
                                    for (var taskDoc : tasks.getDocuments()) {
                                        String title = taskDoc.getString("title");
                                        Long remindAt = taskDoc.getLong("remindAt");
                                        if (title != null && remindAt != null && subjectName != null) {
                                            list.add(new ReminderItem(subjectName, title, remindAt));
                                        }
                                    }
                                    adapter.notifyDataSetChanged();
                                });
                    }
                });
    }
}