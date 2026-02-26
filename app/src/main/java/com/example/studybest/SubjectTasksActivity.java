package com.example.studybest;

import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class SubjectTasksActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subject_tasks);

        String subjectName = getIntent().getStringExtra("subjectName");

        TextView tv = findViewById(R.id.tvSubjectTitle);
        tv.setText(subjectName != null ? subjectName : "Tasks");
    }
}