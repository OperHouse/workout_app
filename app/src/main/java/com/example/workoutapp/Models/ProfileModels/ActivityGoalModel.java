package com.example.workoutapp.Models.ProfileModels;

public class ActivityGoalModel {
    private int activity_goal_id;
    private String activity_goal_date;
    private String activity_goal_uid;
    private int activity_goal_steps;
    private int activity_goal_caloriesToBurn;

    public ActivityGoalModel(int activity_goal_id, String activity_goal_date, int activity_goal_steps, int activity_goal_caloriesToBurn) {
        this.activity_goal_id = activity_goal_id;
        this.activity_goal_date = activity_goal_date;
        this.activity_goal_steps = activity_goal_steps;
        this.activity_goal_caloriesToBurn = activity_goal_caloriesToBurn;
    }
    public ActivityGoalModel(int activity_goal_id, String activity_goal_date, int activity_goal_steps, int activity_goal_caloriesToBurn, String activity_goal_uid) {
        this.activity_goal_id = activity_goal_id;
        this.activity_goal_date = activity_goal_date;
        this.activity_goal_steps = activity_goal_steps;
        this.activity_goal_caloriesToBurn = activity_goal_caloriesToBurn;
        this.activity_goal_uid = activity_goal_uid;
    }

    public ActivityGoalModel() {
    }

    // Getters
    @com.google.firebase.firestore.Exclude
    public int getActivity_goal_id() {
        return activity_goal_id;
    }

    public String getActivity_goal_date() {
        return activity_goal_date;
    }

    public int getActivity_goal_steps() {
        return activity_goal_steps;
    }

    public int getActivity_goal_caloriesToBurn() {
        return activity_goal_caloriesToBurn;
    }

    // Setters
    public void setActivity_goal_id(int activity_goal_id) {
        this.activity_goal_id = activity_goal_id;
    }

    public void setActivity_goal_date(String activity_goal_date) {
        this.activity_goal_date = activity_goal_date;
    }

    public void setActivity_goal_steps(int activity_goal_steps) {
        this.activity_goal_steps = activity_goal_steps;
    }

    public void setActivity_goal_caloriesToBurn(int activity_goal_caloriesToBurn) {
        this.activity_goal_caloriesToBurn = activity_goal_caloriesToBurn;
    }

    public String getActivity_goal_uid() {
        return activity_goal_uid;
    }

    public void setActivity_goal_uid(String activity_goal_uid) {
        this.activity_goal_uid = activity_goal_uid;
    }
}
