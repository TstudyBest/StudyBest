package com.example.studybest;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studybest.adapters.SubjectAdapter;
import com.example.studybest.models.Subject;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class SubjectsFragment extends Fragment {

    private RecyclerView rvSubjects;
    private SubjectAdapter adapter;
    private ArrayList<Subject> list;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_subjects, container, false);

        rvSubjects = v.findViewById(R.id.rvSubjects);
        rvSubjects.setLayoutManager(new LinearLayoutManager(getContext()));

        list = new ArrayList<>();
        adapter = new SubjectAdapter(list);
        rvSubjects.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        loadSubjects();

        return v;
    }

    private void loadSubjects() {
        String uid = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (uid == null) {
            Toast.makeText(getContext(), "Not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        // Per-user subjects path:
        db.collection("users").document(uid).collection("subjects")
                .get()
                .addOnSuccessListener(query -> {
                    list.clear();
                    for (com.google.firebase.firestore.DocumentSnapshot doc : query.getDocuments()) {
                        String id = doc.getId();
                        String name = doc.getString("name");
                        if (name != null) list.add(new Subject(id, name));
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Load failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }
}