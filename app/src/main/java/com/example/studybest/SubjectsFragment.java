package com.example.studybest;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studybest.adapters.SubjectAdapter;
import com.example.studybest.models.Subject;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class SubjectsFragment extends Fragment {

    private RecyclerView rvSubjects;
    private SubjectAdapter adapter;
    private ArrayList<Subject> list;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private android.widget.EditText etSubjectSearch;
    private android.widget.Spinner spSort;

    private final ArrayList<Subject> allSubjects = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_subjects, container, false);

        etSubjectSearch = v.findViewById(R.id.etSubjectSearch);
        spSort = v.findViewById(R.id.spSort);

        android.widget.ArrayAdapter<String> sortAdapter =
                new android.widget.ArrayAdapter<>(
                        getContext(),
                        android.R.layout.simple_spinner_dropdown_item,
                        getResources().getStringArray(R.array.subject_sort_options)
                );
        spSort.setAdapter(sortAdapter);

        rvSubjects = v.findViewById(R.id.rvSubjects);
        rvSubjects.setLayoutManager(new LinearLayoutManager(getContext()));

        list = new ArrayList<>();
        adapter = new SubjectAdapter(list);
        rvSubjects.setAdapter(adapter);

        adapter.setOnSubjectClickListener(subject -> openSubject(subject));
        adapter.setOnSubjectLongClickListener(subject -> showSubjectMenu(subject));

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        View fab = v.findViewById(R.id.fabAddSubject);
        fab.setOnClickListener(view -> showAddSubjectDialog());

        loadSubjects();

        etSubjectSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { applySubjectFilter(); }
            @Override public void afterTextChanged(android.text.Editable s) {}
        });

        spSort.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                applySubjectFilter();
            }
            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        return v;
    }

    private String getUid() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return null;
        String uid = user.getUid();
        return (uid != null && !uid.isEmpty()) ? uid : null;
    }

    private void loadSubjects() {
        String uid = getUid();
        if (uid == null) {
            if (isAdded()) Toast.makeText(getContext(), "Please log in to view subjects", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("users")
                .document(uid)
                .collection("subjects")
                .addSnapshotListener((query, error) -> {

                    if (!isAdded()) return;

                    if (error != null) {
                        Toast.makeText(getContext(), "Failed to load subjects", Toast.LENGTH_LONG).show();
                        return;
                    }

                    if (query == null) return;

                    allSubjects.clear();

                    for (com.google.firebase.firestore.DocumentSnapshot doc : query.getDocuments()) {
                        String id = doc.getId();
                        String name = doc.getString("name");
                        // only add if name is valid
                        if (name != null && !name.trim().isEmpty()) {
                            allSubjects.add(new Subject(id, name.trim()));
                        }
                    }

                    applySubjectFilter();
                });
    }

    private void confirmDelete(Subject subject) {
        if (subject == null || subject.getId() == null) return;

        String uid = getUid();
        if (uid == null) return;

        new android.app.AlertDialog.Builder(getContext())
                .setTitle("Delete Subject")
                .setMessage("Delete \"" + subject.getName() + "\"? This cannot be undone.")
                .setPositiveButton("Delete", (d, w) -> {
                    db.collection("users")
                            .document(uid)
                            .collection("subjects")
                            .document(subject.getId())
                            .delete()
                            .addOnSuccessListener(unused -> {
                                if (isAdded()) Toast.makeText(getContext(), "Subject deleted", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                if (isAdded()) Toast.makeText(getContext(), "Delete failed", Toast.LENGTH_LONG).show();
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showEditSubjectDialog(Subject subject) {
        if (subject == null || subject.getId() == null) return;

        String uid = getUid();
        if (uid == null) {
            if (isAdded()) Toast.makeText(getContext(), "Please log in", Toast.LENGTH_SHORT).show();
            return;
        }

        final android.widget.EditText input = new android.widget.EditText(getContext());
        input.setText(subject.getName());
        input.setSelection(input.getText().length());

        new android.app.AlertDialog.Builder(getContext())
                .setTitle("Edit Subject")
                .setView(input)
                .setPositiveButton("Update", (dialog, which) -> {
                    String newName = input.getText().toString().trim();

                    if (TextUtils.isEmpty(newName)) {
                        Toast.makeText(getContext(), "Subject name cannot be empty", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (newName.length() > 100) {
                        Toast.makeText(getContext(), "Subject name is too long", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    db.collection("users")
                            .document(uid)
                            .collection("subjects")
                            .document(subject.getId())
                            .update("name", newName)
                            .addOnSuccessListener(v -> {
                                if (isAdded()) Toast.makeText(getContext(), "Updated", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                if (isAdded()) Toast.makeText(getContext(), "Update failed", Toast.LENGTH_LONG).show();
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void openSubject(Subject subject) {
        if (subject == null || subject.getId() == null || subject.getName() == null) {
            Toast.makeText(getContext(), "Unable to open this subject", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent i = new Intent(getContext(), SubjectTasksActivity.class);
        i.putExtra("subjectId", subject.getId());
        i.putExtra("subjectName", subject.getName());
        startActivity(i);
    }

    private void showSubjectMenu(Subject subject) {
        if (subject == null) return;

        String[] options = {"Edit", "Delete"};
        new android.app.AlertDialog.Builder(getContext())
                .setTitle(subject.getName())
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        showEditSubjectDialog(subject);
                    } else {
                        confirmDelete(subject);
                    }
                })
                .show();
    }

    private void showAddSubjectDialog() {
        String uid = getUid();
        if (uid == null) {
            if (isAdded()) Toast.makeText(getContext(), "Please log in", Toast.LENGTH_SHORT).show();
            return;
        }

        final EditText input = new EditText(getContext());
        input.setHint("e.g., Mathematics");

        new AlertDialog.Builder(getContext())
                .setTitle("Add Subject")
                .setView(input)
                .setPositiveButton("Add", (dialog, which) -> {
                    String name = input.getText().toString().trim();

                    if (TextUtils.isEmpty(name)) {
                        Toast.makeText(getContext(), "Subject name is required", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (name.length() > 100) {
                        Toast.makeText(getContext(), "Subject name is too long", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    DocumentReference ref = db.collection("users")
                            .document(uid)
                            .collection("subjects")
                            .document();

                    ref.set(new Subject(ref.getId(), name))
                            .addOnSuccessListener(v -> {
                                if (isAdded()) Toast.makeText(getContext(), "Subject added", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                if (isAdded()) Toast.makeText(getContext(), "Failed to add subject", Toast.LENGTH_LONG).show();
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void applySubjectFilter() {
        if (!isAdded()) return;

        String search = etSubjectSearch.getText().toString().toLowerCase().trim();
        int sortPos = spSort.getSelectedItemPosition();

        list.clear();

        for (Subject s : allSubjects) {
            String name = s.getName() != null ? s.getName().toLowerCase() : "";
            if (search.isEmpty() || name.contains(search)) {
                list.add(s);
            }
        }

        java.util.Collections.sort(list, (a, b) -> {
            String an = a.getName() == null ? "" : a.getName();
            String bn = b.getName() == null ? "" : b.getName();
            return sortPos == 0 ? an.compareToIgnoreCase(bn) : bn.compareToIgnoreCase(an);
        });

        adapter.notifyDataSetChanged();
    }
}
