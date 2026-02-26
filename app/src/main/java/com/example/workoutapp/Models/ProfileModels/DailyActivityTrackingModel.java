package com.example.workoutapp.Models.ProfileModels;

public class DailyActivityTrackingModel {
    private int daily_activity_tracking_id;
    private String daily_activity_tracking_uid;
    private String daily_activity_tracking_date;
    private int daily_activity_tracking_steps;
    private float daily_activity_tracking_caloriesBurned;

    public DailyActivityTrackingModel(int daily_activity_tracking_id, String daily_activity_tracking_date, int daily_activity_tracking_steps, float daily_activity_tracking_caloriesBurned) {
        this.daily_activity_tracking_id = daily_activity_tracking_id;
        this.daily_activity_tracking_date = daily_activity_tracking_date;
        this.daily_activity_tracking_steps = daily_activity_tracking_steps;
        this.daily_activity_tracking_caloriesBurned = daily_activity_tracking_caloriesBurned;
    }
    public DailyActivityTrackingModel(int daily_activity_tracking_id, String daily_activity_tracking_date, int daily_activity_tracking_steps, float daily_activity_tracking_caloriesBurned, String daily_activity_tracking_uid) {
        this.daily_activity_tracking_id = daily_activity_tracking_id;
        this.daily_activity_tracking_uid = daily_activity_tracking_uid;
        this.daily_activity_tracking_date = daily_activity_tracking_date;
        this.daily_activity_tracking_steps = daily_activity_tracking_steps;
        this.daily_activity_tracking_caloriesBurned = daily_activity_tracking_caloriesBurned;
    }
    public  DailyActivityTrackingModel(){}


    // Геттеры и сеттеры
    @com.google.firebase.firestore.Exclude
    public int getDaily_activity_tracking_id() {
        return daily_activity_tracking_id;
    }

    public void setDaily_activity_tracking_id(int daily_activity_tracking_id) {
        this.daily_activity_tracking_id = daily_activity_tracking_id;
    }

    public String getDaily_activity_tracking_date() {
        return daily_activity_tracking_date;
    }

    public void setDaily_activity_tracking_date(String daily_activity_tracking_date) {
        this.daily_activity_tracking_date = daily_activity_tracking_date;
    }

    public int getDaily_activity_tracking_steps() {
        return daily_activity_tracking_steps;
    }

    public void setDaily_activity_tracking_steps(int daily_activity_tracking_steps) {
        this.daily_activity_tracking_steps = daily_activity_tracking_steps;
    }

    public float getDaily_activity_tracking_caloriesBurned() {
        return daily_activity_tracking_caloriesBurned;
    }

    public void setDaily_activity_tracking_caloriesBurned(float daily_activity_tracking_caloriesBurned) {
        this.daily_activity_tracking_caloriesBurned = daily_activity_tracking_caloriesBurned;
    }

    public String getDaily_activity_tracking_uid() {
        return daily_activity_tracking_uid;
    }

    public void setDaily_activity_tracking_uid(String daily_activity_tracking_uid) {
        this.daily_activity_tracking_uid = daily_activity_tracking_uid;
    }
}
