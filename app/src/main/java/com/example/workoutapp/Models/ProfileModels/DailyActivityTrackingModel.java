package com.example.workoutapp.Models.ProfileModels;

public class DailyActivityTrackingModel {


    private long trackingActivityId;
    private String trackingActivityDate;
    private int trackingActivitySteps;
    private double dailyActivityTrackingCalories;

    // Конструктор
    public DailyActivityTrackingModel(long trackingActivityId, String trackingActivityDate, int trackingActivitySteps, double dailyActivityTrackingCalories) {
        this.trackingActivityId = trackingActivityId;
        this.trackingActivityDate = trackingActivityDate;
        this.trackingActivitySteps = trackingActivitySteps;
        this.dailyActivityTrackingCalories = dailyActivityTrackingCalories;
    }

    public long getTrackingActivityId() {
        return trackingActivityId;
    }

    public void setTrackingActivityId(long trackingActivityId) {
        this.trackingActivityId = trackingActivityId;
    }

    public String getTrackingActivityDate() {
        return trackingActivityDate;
    }

    public void setTrackingActivityDate(String trackingActivityDate) {
        this.trackingActivityDate = trackingActivityDate;
    }

    public int getTrackingActivitySteps() {
        return trackingActivitySteps;
    }

    public void setTrackingActivitySteps(int trackingActivitySteps) {
        this.trackingActivitySteps = trackingActivitySteps;
    }

    public double getDailyActivityTrackingCalories() {
        return dailyActivityTrackingCalories;
    }

    public void setDailyActivityTrackingCalories(double dailyActivityTrackingCalories) {
        this.dailyActivityTrackingCalories = dailyActivityTrackingCalories;
    }
}
