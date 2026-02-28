package com.example.studybest;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.widget.EditText;

import com.example.studybest.adapters.TaskAdapter;
import com.example.studybest.models.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;

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

        tvTitle = findViewById(R.id.tvSubjectTitle);
        rv = findViewById(R.id.rvTasks);
        fab = findViewById(R.id.fabAddTask);

        subjectId = getIntent().getStringExtra("subjectId");
        subjectName = getIntent().getStringExtra("subjectName");

        // if subjectId is missing, this screen can't work at all
        if (TextUtils.isEmpty(subjectId)) {
            Toast.makeText(this, "Error: subject not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        tvTitle.setText(subjectName != null ? subjectName : "Tasks");

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Please log in first", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TaskAdapter(list);
        rv.setAdapter(adapter);

        adapter.setOnTaskCheckedListener((task, isChecked) -> {
            if (task == null || task.getId() == null) return;

            String uid = user.getUid();
            if (uid == null || uid.isEmpty()) return;

            db.collection("users")
                    .document(uid)
                    .collection("subjects")
                    .document(subjectId)
                    .collection("tasks")
                    .document(task.getId())
                    .update("done", isChecked)
                    .addOnFailureListener(e -> Toast.makeText(this, "Could not update task", Toast.LENGTH_SHORT).show());
        });

        fab.setOnClickListener(v -> showAddTaskDialog());

        listenTasks();
    }

    private void listenTasks() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null || TextUtils.isEmpty(subjectId)) {
            finish();
            return;
        }

        String uid = user.getUid();
        if (uid == null || uid.isEmpty()) {
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
                        Long remindAtLong = doc.getLong("remindAt");
                        long remindAt = remindAtLong != null ? remindAtLong : 0L;

                        list.add(new Task(
                                id,
                                title != null ? title : "",
                                done != null && done,
                                remindAt
                        ));
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    private void showAddTaskDialog() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        String uid = user.getUid();
        if (uid == null || uid.isEmpty()) return;

        EditText input = new EditText(this);
        input.setHint("e.g., Complete tutorial 3");

        String[] options = {"No reminder", "Remind in 1 minute", "Remind in 1 hour"};

        new AlertDialog.Builder(this)
                .setTitle("Add Task")
                .setView(input)
                .setSingleChoiceItems(options, 0, null)
                .setPositiveButton("Add", (d, w) -> {
                    String taskTitle = input.getText().toString().trim();

                    if (TextUtils.isEmpty(taskTitle)) {
                        Toast.makeText(this, "Task title is required", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (taskTitle.length() > 200) {
                        Toast.makeText(this, "Task title is too long", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    AlertDialog dialog = (AlertDialog) d;
                    int selected = dialog.getListView().getCheckedItemPosition();

                    long remindAt = 0L;
                    if (selected == 1) remindAt = System.currentTimeMillis() + 60_000L;
                    if (selected == 2) remindAt = System.currentTimeMillis() + 60 * 60_000L;

                    final long finalRemindAt = remindAt;
                    final String finalTaskTitle = taskTitle;
                    final String finalSubjectName = subjectName;

                    com.google.firebase.firestore.DocumentReference ref =
                            db.collection("users")
                                    .document(uid)
                                    .collection("subjects")
                                    .document(subjectId)
                                    .collection("tasks")
                                    .document();

                    HashMap<String, Object> data = new HashMap<>();
                    data.put("title", finalTaskTitle);
                    data.put("done", false);
                    data.put("ownerUid", uid);
                    if (finalRemindAt > 0) data.put("remindAt", finalRemindAt);

                    ref.set(data)
                            .addOnSuccessListener(v -> {
                                Toast.makeText(this, "Task added", Toast.LENGTH_SHORT).show();

                                if (finalRemindAt > 0) {
                                    ReminderScheduler.schedule(
                                            this,
                                            finalRemindAt,
                                            "StudyBest: " + (finalSubjectName != null ? finalSubjectName : "Task"),
                                            finalTaskTitle
                                    );
                                }
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Failed to add task", Toast.LENGTH_LONG).show()
                            );
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
