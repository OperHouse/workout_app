package com.example.workoutapp.Models;

public class DeletionTask {
    public String uid;
    public String type;
    public String data = "";

    public DeletionTask(String uid, String type, String data) {
        this.uid = uid;
        this.type = type;
        this.data = data;
    }
    public DeletionTask(String uid, String type) {
        this.uid = uid;
        this.type = type;
    }
}