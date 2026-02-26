package com.example.studybest.models;

public class ReminderItem {
    private String subjectName;
    private String taskTitle;
    private long remindAt;

    public ReminderItem() {}

    public ReminderItem(String subjectName, String taskTitle, long remindAt) {
        this.subjectName = subjectName;
        this.taskTitle = taskTitle;
        this.remindAt = remindAt;
    }

    public String getSubjectName() { return subjectName; }
    public String getTaskTitle() { return taskTitle; }
    public long getRemindAt() { return remindAt; }
}