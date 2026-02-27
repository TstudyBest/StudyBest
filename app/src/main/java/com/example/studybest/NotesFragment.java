package com.example.studybest;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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

import com.example.studybest.adapters.NoteAdapter;
import com.example.studybest.models.Note;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class NotesFragment extends Fragment {

    private RecyclerView rv;
    private EditText etSearch;
    private FloatingActionButton fab;

    private final ArrayList<Note> all = new ArrayList<>();
    private final ArrayList<Note> shown = new ArrayList<>();
    private NoteAdapter adapter;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_notes, container, false);

        rv = v.findViewById(R.id.rvNotes);
        etSearch = v.findViewById(R.id.etNotesSearch);
        fab = v.findViewById(R.id.fabAddNote);

        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new NoteAdapter(shown);
        rv.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        adapter.setOnNoteClickListener(this::showEditNoteDialog);
        adapter.setOnNoteLongClickListener(this::confirmDeleteNote);

        fab.setOnClickListener(view -> showAddNoteDialog());

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { applyFilter(s.toString()); }
            @Override public void afterTextChanged(Editable s) {}
        });

        listenNotes();
        return v;
    }

    private void listenNotes() {
        String uid = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (uid == null) return;

        db.collection("users").document(uid).collection("notes")
                .addSnapshotListener((q, e) -> {
                    if (e != null || q == null) return;

                    all.clear();
                    for (var doc : q.getDocuments()) {
                        String id = doc.getId();
                        String title = doc.getString("title");
                        String content = doc.getString("content");
                        all.add(new Note(id, title == null ? "" : title, content == null ? "" : content));
                    }
                    applyFilter(etSearch.getText().toString());
                });
    }

    private void applyFilter(String text) {
        String t = text == null ? "" : text.toLowerCase().trim();
        shown.clear();

        for (Note n : all) {
            String hay = (n.getTitle() + " " + n.getContent()).toLowerCase();
            if (t.isEmpty() || hay.contains(t)) shown.add(n);
        }
        adapter.notifyDataSetChanged();
    }

    private void showAddNoteDialog() {
        String uid = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (uid == null) return;

        View form = LayoutInflater.from(getContext()).inflate(R.layout.dialog_note, null);
        EditText etTitle = form.findViewById(R.id.etNoteTitle);
        EditText etContent = form.findViewById(R.id.etNoteContent);

        new AlertDialog.Builder(getContext())
                .setTitle("Add Note")
                .setView(form)
                .setPositiveButton("Save", (d, w) -> {
                    String title = etTitle.getText().toString().trim();
                    String content = etContent.getText().toString().trim();
                    if (title.isEmpty() && content.isEmpty()) {
                        Toast.makeText(getContext(), "Write something", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    var ref = db.collection("users").document(uid).collection("notes").document();
                    java.util.HashMap<String, Object> data = new java.util.HashMap<>();
                    data.put("title", title.isEmpty() ? "Untitled" : title);
                    data.put("content", content);
                    data.put("createdAt", com.google.firebase.firestore.FieldValue.serverTimestamp());
                    ref.set(data);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showEditNoteDialog(Note note) {
        String uid = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (uid == null) return;

        View form = LayoutInflater.from(getContext()).inflate(R.layout.dialog_note, null);
        EditText etTitle = form.findViewById(R.id.etNoteTitle);
        EditText etContent = form.findViewById(R.id.etNoteContent);

        etTitle.setText(note.getTitle());
        etContent.setText(note.getContent());

        new AlertDialog.Builder(getContext())
                .setTitle("Edit Note")
                .setView(form)
                .setPositiveButton("Update", (d, w) -> {
                    String title = etTitle.getText().toString().trim();
                    String content = etContent.getText().toString().trim();

                    db.collection("users").document(uid).collection("notes")
                            .document(note.getId())
                            .update("title", title.isEmpty() ? "Untitled" : title,
                                    "content", content);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void confirmDeleteNote(Note note) {
        String uid = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (uid == null) return;

        new AlertDialog.Builder(getContext())
                .setTitle("Delete Note")
                .setMessage("Delete \"" + note.getTitle() + "\"?")
                .setPositiveButton("Delete", (d, w) -> {
                    db.collection("users").document(uid).collection("notes")
                            .document(note.getId())
                            .delete();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}