package com.example.workoutapp.Models.ProfileModels;

public class WeightHistoryModel {


    private long weight_history_id;
    private String weight_history_uid;
    private String weight_history_measurementDate;
    private float weight_history_value;

    // Конструктор
    public WeightHistoryModel(long weight_history_id, String weight_history_uid, String weight_history_measurementDate, float weight_history_value) {
        this.weight_history_id = weight_history_id;
        this.weight_history_uid = weight_history_uid;
        this.weight_history_measurementDate = weight_history_measurementDate;
        this.weight_history_value = weight_history_value;
    }

    public WeightHistoryModel() {
    }

    @com.google.firebase.firestore.Exclude
    public long getWeight_history_id() {
        return weight_history_id;
    }

    public void setWeight_history_id(long weight_history_id) {
        this.weight_history_id = weight_history_id;
    }

    public String getWeight_history_measurementDate() {
        return weight_history_measurementDate;
    }

    public void setWeight_history_measurementDate(String weight_history_measurementDate) {
        this.weight_history_measurementDate = weight_history_measurementDate;
    }

    public float getWeight_history_value() {
        return weight_history_value;
    }

    public void setWeight_history_value(float weight_history_value) {
        this.weight_history_value = weight_history_value;
    }

    public String getWeight_history_uid() { return weight_history_uid; }
    public void setWeight_history_uid(String weight_history_uid) { this.weight_history_uid = weight_history_uid; }
}
