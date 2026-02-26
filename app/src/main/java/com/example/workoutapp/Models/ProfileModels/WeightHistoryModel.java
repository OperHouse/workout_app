package com.example.workoutapp.Models.ProfileModels;

public class WeightHistoryModel {


    private long weightId;
    private String weightUid;
    private String measurementDate;
    private float weightValue;

    // Конструктор
    public WeightHistoryModel(long weightId, String weightUid, String measurementDate, float weightValue) {
        this.weightId = weightId;
        this.weightUid = weightUid;
        this.measurementDate = measurementDate;
        this.weightValue = weightValue;
    }

    public WeightHistoryModel() {
    }

    @com.google.firebase.firestore.Exclude
    public long getWeightId() {
        return weightId;
    }

    public void setWeightId(long weightId) {
        this.weightId = weightId;
    }

    public String getMeasurementDate() {
        return measurementDate;
    }

    public void setMeasurementDate(String measurementDate) {
        this.measurementDate = measurementDate;
    }

    public float getWeightValue() {
        return weightValue;
    }

    public void setWeightValue(float weightValue) {
        this.weightValue = weightValue;
    }

    public String getWeightUid() { return weightUid; }
    public void setWeightUid(String weightUid) { this.weightUid = weightUid; }
}
