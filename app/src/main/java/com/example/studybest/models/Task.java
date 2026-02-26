package com.example.studybest.models;

public class Task {

    private String id;
    private String title;
    private boolean done;
    private long remindAt; // ✅ NEW

    public Task() { }

    public Task(String id, String title, boolean done) {
        this.id = id;
        this.title = title;
        this.done = done;
        this.remindAt = 0L;
    }

    public Task(String id, String title, boolean done, long remindAt) { // ✅ NEW
        this.id = id;
        this.title = title;
        this.done = done;
        this.remindAt = remindAt;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public boolean isDone() { return done; }
    public long getRemindAt() { return remindAt; } // ✅ NEW

    public void setId(String id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setDone(boolean done) { this.done = done; }
    public void setRemindAt(long remindAt) { this.remindAt = remindAt; } // ✅ NEW
}