package com.example.workoutapp.Models.ProfileModels;

public class DailyFoodTrackingModel {
    private long trackingFoodId;
    private String dailyFoodTrackingDate;
    private double trackingCalories;
    private double trackingProtein;
    private double trackingFat;
    private double trackingCarb;

    // Конструктор
    public DailyFoodTrackingModel(long trackingFoodId, String dailyFoodTrackingDate, double trackingCalories, double trackingProtein, double trackingFat, double trackingCarb) {
        this.trackingFoodId = trackingFoodId;
        this.dailyFoodTrackingDate = dailyFoodTrackingDate;
        this.trackingCalories = trackingCalories;
        this.trackingProtein = trackingProtein;
        this.trackingFat = trackingFat;
        this.trackingCarb = trackingCarb;

    }

    public double getTrackingProtein() {
        return trackingProtein;
    }

    public void setTrackingProtein(double trackingProtein) {
        this.trackingProtein = trackingProtein;
    }

    public long getTrackingFoodId() {
        return trackingFoodId;
    }

    public void setTrackingFoodId(long trackingFoodId) {
        this.trackingFoodId = trackingFoodId;
    }

    public String getDailyFoodTrackingDate() {
        return dailyFoodTrackingDate;
    }

    public void setDailyFoodTrackingDate(String dailyFoodTrackingDate) {
        this.dailyFoodTrackingDate = dailyFoodTrackingDate;
    }

    public double getTrackingCalories() {
        return trackingCalories;
    }

    public void setTrackingCalories(double trackingCalories) {
        this.trackingCalories = trackingCalories;
    }

    public double getTrackingFat() {
        return trackingFat;
    }

    public void setTrackingFat(double trackingFat) {
        this.trackingFat = trackingFat;
    }

    public double getTrackingCarb() {
        return trackingCarb;
    }

    public void setTrackingCarb(double trackingCarb) {
        this.trackingCarb = trackingCarb;
    }

}
