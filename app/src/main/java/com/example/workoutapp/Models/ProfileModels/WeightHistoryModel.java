package com.example.workoutapp.Models.ProfileModels;

public class WeightHistoryModel {


    private long weightId;
    private String measurementDate;
    private float weightValue;

    // Конструктор
    public WeightHistoryModel(long weightId, String measurementDate, float weightValue) {
        this.weightId = weightId;
        this.measurementDate = measurementDate;
        this.weightValue = weightValue;
    }

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
}
