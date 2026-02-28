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
import com.google.firebase.auth.FirebaseUser;
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
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        String uid = user.getUid();
        if (uid == null || uid.isEmpty()) return;

        db.collection("users").document(uid).collection("subjects")
                .get()
                .addOnSuccessListener(subjects -> {
                    if (!isAdded()) return;

                    list.clear();

                    for (var subjectDoc : subjects.getDocuments()) {
                        String subjectName = subjectDoc.getString("name");
                        String subjectId = subjectDoc.getId();

                        // skip if subject data is missing
                        if (subjectName == null || subjectId == null) continue;

                        db.collection("users").document(uid)
                                .collection("subjects").document(subjectId)
                                .collection("tasks")
                                .whereGreaterThan("remindAt", 0)
                                .get()
                                .addOnSuccessListener(tasks -> {
                                    if (!isAdded()) return;

                                    for (var taskDoc : tasks.getDocuments()) {
                                        String title = taskDoc.getString("title");
                                        Long remindAt = taskDoc.getLong("remindAt");

                                        // only add if all fields are present
                                        if (title != null && !title.isEmpty() && remindAt != null && remindAt > 0) {
                                            list.add(new ReminderItem(subjectName, title, remindAt));
                                        }
                                    }
                                    adapter.notifyDataSetChanged();
                                })
                                .addOnFailureListener(e -> {
                                    // silently skip subjects that fail to load tasks
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    // don't crash if subjects fail to load
                });
    }
}
