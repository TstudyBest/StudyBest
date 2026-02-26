package com.example.studybest;

import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.widget.EditText;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studybest.adapters.TaskAdapter;
import com.example.studybest.models.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class SubjectTasksActivity extends AppCompatActivity {


    private RecyclerView rv;
    private FloatingActionButton fab;
    private TextView tvTitle;

    private final ArrayList<Task> list = new ArrayList<>();
    private TaskAdapter adapter;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private String subjectId;
    private String subjectName;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subject_tasks);
        /// //////

        tvTitle = findViewById(R.id.tvSubjectTitle);
        rv = findViewById(R.id.rvTasks);
        fab = findViewById(R.id.fabAddTask);

        subjectId = getIntent().getStringExtra("subjectId");
        subjectName = getIntent().getStringExtra("subjectName");

        tvTitle.setText(subjectName != null ? subjectName : "Tasks");

        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TaskAdapter(list);
        rv.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        fab.setOnClickListener(v -> showAddTaskDialog());

        listenTasks();




        /// ////////

        String subjectName = getIntent().getStringExtra("subjectName");

        TextView tv = findViewById(R.id.tvSubjectTitle);
        tv.setText(subjectName != null ? subjectName : "Tasks");
    }


    private void listenTasks() {
        String uid = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (uid == null || subjectId == null) {
            finish();
            return;
        }

        db.collection("users")
                .document(uid)
                .collection("subjects")
                .document(subjectId)
                .collection("tasks")
                .addSnapshotListener((query, error) -> {
                    if (error != null || query == null) return;

                    list.clear();
                    for (com.google.firebase.firestore.DocumentSnapshot doc : query.getDocuments()) {
                        String id = doc.getId();
                        String title = doc.getString("title");
                        Boolean done = doc.getBoolean("done");
                        list.add(new Task(id, title == null ? "" : title, done != null && done));
                    }
                    adapter.notifyDataSetChanged();
                });
    }


    private void showAddTaskDialog() {
        EditText input = new EditText(this);
        input.setHint("e.g., Do tutorial 3");

        new AlertDialog.Builder(this)
                .setTitle("Add Task")
                .setView(input)
                .setPositiveButton("Add", (d, w) -> {
                    String text = input.getText().toString().trim();
                    if (text.isEmpty()) return;

                    String uid = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
                    if (uid == null) return;

                    var ref = db.collection("users")
                            .document(uid)
                            .collection("subjects")
                            .document(subjectId)
                            .collection("tasks")
                            .document(); // auto-id

                    ref.set(new Task(ref.getId(), text, false));
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

}