package com.example.workoutapp.Models.ProfileModels;

public class DailyActivityTrackingModel {
    private int id;
    private String date; // yyyy-MM-dd
    private int trackingActivitySteps;
    private float trackingCaloriesBurned;

    public DailyActivityTrackingModel(int id, String date, int trackingActivitySteps, float trackingCaloriesBurned) {
        this.id = id;
        this.date = date;
        this.trackingActivitySteps = trackingActivitySteps;
        this.trackingCaloriesBurned = trackingCaloriesBurned;
    }

    // Конструктор без id (для вставки новой записи)
    public DailyActivityTrackingModel(String date, int trackingActivitySteps, float trackingCaloriesBurned) {
        this.date = date;
        this.trackingActivitySteps = trackingActivitySteps;
        this.trackingCaloriesBurned = trackingCaloriesBurned;
    }

    // Геттеры и сеттеры
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getTrackingActivitySteps() {
        return trackingActivitySteps;
    }

    public void setTrackingActivitySteps(int trackingActivitySteps) {
        this.trackingActivitySteps = trackingActivitySteps;
    }

    public float getTrackingCaloriesBurned() {
        return trackingCaloriesBurned;
    }

    public void setTrackingCaloriesBurned(float trackingCaloriesBurned) {
        this.trackingCaloriesBurned = trackingCaloriesBurned;
    }
}
