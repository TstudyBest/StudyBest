package com.example.studybest;

import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.widget.EditText;
import android.widget.Toast;

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

        adapter.setOnTaskCheckedListener((task, isChecked) -> {

            String uid = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
            if (uid == null) return;

            db.collection("users")
                    .document(uid)
                    .collection("subjects")
                    .document(subjectId)
                    .collection("tasks")
                    .document(task.getId())
                    .update("done", isChecked);
        });

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


                        Long remindAtLong = doc.getLong("remindAt");
                        long remindAt = (remindAtLong != null) ? remindAtLong : 0L;

                        list.add(new Task(
                                id,
                                title == null ? "" : title,
                                done != null && done,
                                remindAt
                        ));



                    }
                    adapter.notifyDataSetChanged();
                });
    }


    private void showAddTaskDialog() {
        EditText input = new EditText(this);
        input.setHint("e.g., Do tutorial 3");

        String[] options = {"No reminder", "Remind in 1 minute", "Remind in 1 hour"};

        new android.app.AlertDialog.Builder(this)
                .setTitle("Add Task")
                .setView(input)
                .setSingleChoiceItems(options, 0, null)
                .setPositiveButton("Add", (d, w) -> {
                    String taskTitle = input.getText().toString().trim();
                    if (taskTitle.isEmpty()) {
                        Toast.makeText(this, "Task title required", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String uid = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
                    if (uid == null) return;

                    // Which reminder option selected?
                    android.app.AlertDialog dialog = (android.app.AlertDialog) d;
                    int selected = dialog.getListView().getCheckedItemPosition();

                    long remindAt = 0L;
                    if (selected == 1) remindAt = System.currentTimeMillis() + 60_000L;        // 1 min
                    if (selected == 2) remindAt = System.currentTimeMillis() + 60 * 60_000L;   // 1 hour

                    // ✅ Make final copies for lambdas
                    final long finalRemindAt = remindAt;
                    final String finalTaskTitle = taskTitle;
                    final String finalSubjectName = subjectName;

                    // Firestore path: users/{uid}/subjects/{subjectId}/tasks/{taskId}
                    com.google.firebase.firestore.DocumentReference ref =
                            db.collection("users")
                                    .document(uid)
                                    .collection("subjects")
                                    .document(subjectId)
                                    .collection("tasks")
                                    .document(); // auto-id

                    java.util.HashMap<String, Object> data = new java.util.HashMap<>();
                    data.put("title", finalTaskTitle);
                    data.put("done", false);
                    if (finalRemindAt > 0) data.put("remindAt", finalRemindAt);

                    ref.set(data)
                            .addOnSuccessListener(v -> {
                                Toast.makeText(this, "Added", Toast.LENGTH_SHORT).show();

                                // Schedule local notification if selected
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
                                    Toast.makeText(this, "Add failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
                            );
                })
                .setNegativeButton("Cancel", null)
                .show();
    }



}