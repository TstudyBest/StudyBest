package com.example.studybest;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import android.app.AlertDialog;
import android.widget.EditText;

import com.google.firebase.firestore.DocumentReference;

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

        adapter.setOnSubjectLongClickListener(subject -> confirmDelete(subject));

        adapter.setOnSubjectClickListener(subject -> showEditSubjectDialog(subject));

         db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        View fab = v.findViewById(R.id.fabAddSubject);
        fab.setOnClickListener(view -> showAddSubjectDialog());


        loadSubjects();

        return v;
    }



    private void loadSubjects() {
        String uid = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (uid == null) {
            Toast.makeText(getContext(), "Not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("users")
                .document(uid)
                .collection("subjects")
                .addSnapshotListener((query, error) -> {

                    if (error != null) {
                        Toast.makeText(getContext(), "Listen failed: " + error.getMessage(), Toast.LENGTH_LONG).show();
                        return;
                    }

                    if (query == null) return;

                    list.clear();

                    for (com.google.firebase.firestore.DocumentSnapshot doc : query.getDocuments()) {
                        String id = doc.getId();
                        String name = doc.getString("name");
                        if (name != null) {
                            list.add(new Subject(id, name));
                        }
                    }

                    adapter.notifyDataSetChanged();
                });
    }

    private void confirmDelete(Subject subject) {
        String uid = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (uid == null) return;

        new android.app.AlertDialog.Builder(getContext())
                .setTitle("Delete Subject")
                .setMessage("Delete \"" + subject.getName() + "\"?")
                .setPositiveButton("Delete", (d, w) -> {
                    db.collection("users")
                            .document(uid)
                            .collection("subjects")
                            .document(subject.getId())
                            .delete()
                            .addOnSuccessListener(v -> Toast.makeText(getContext(), "Deleted", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(getContext(), "Delete failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showEditSubjectDialog(Subject subject) {
        String uid = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (uid == null) {
            Toast.makeText(getContext(), "Not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        final android.widget.EditText input = new android.widget.EditText(getContext());
        input.setText(subject.getName());
        input.setSelection(input.getText().length()); // cursor at end

        new android.app.AlertDialog.Builder(getContext())
                .setTitle("Edit Subject")
                .setView(input)
                .setPositiveButton("Update", (dialog, which) -> {
                    String newName = input.getText().toString().trim();
                    if (newName.isEmpty()) {
                        Toast.makeText(getContext(), "Name cannot be empty", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    db.collection("users")
                            .document(uid)
                            .collection("subjects")
                            .document(subject.getId())
                            .update("name", newName)
                            .addOnSuccessListener(v -> Toast.makeText(getContext(), "Updated", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(getContext(), "Update failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showAddSubjectDialog() {
        String uid = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (uid == null) {
            Toast.makeText(getContext(), "Not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        final EditText input = new EditText(getContext());
        input.setHint("e.g., Physics");

        new AlertDialog.Builder(getContext())
                .setTitle("Add Subject")
                .setView(input)
                .setPositiveButton("Add", (dialog, which) -> {
                    String name = input.getText().toString().trim();
                    if (name.isEmpty()) {
                        Toast.makeText(getContext(), "Subject name required", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Save to: users/{uid}/subjects/{autoId}
                    DocumentReference ref = db.collection("users")
                            .document(uid)
                            .collection("subjects")
                            .document(); // auto-id

                    ref.set(new Subject(ref.getId(), name))
                            .addOnSuccessListener(v -> Toast.makeText(getContext(), "Added", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(getContext(), "Add failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

}