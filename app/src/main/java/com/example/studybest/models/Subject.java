package com.example.studybest.models;

public class Subject {
    private String id;
    private String name;

    // Needed for Firestore
    public Subject() {}

    public Subject(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() { return id; }
    public String getName() { return name; }

    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
}