package com.example.workoutapp.Models.ProfileModels;



public class GeneralGoalModel {

    private int general_goal_id;
    private String general_goal_Text;
    private int general_goal_workoutsWeekly;
    private int general_goal_foodTrackingWeekly;
    private String general_goal_date;
    private String general_goal_uid;

    // Конструктор для полной модели
    public GeneralGoalModel(int general_goal_id, String general_goal_Text, int general_goal_workoutsWeekly, int general_goal_foodTrackingWeekly, String general_goal_date) {
        this.general_goal_id = general_goal_id;
        this.general_goal_Text = general_goal_Text;
        this.general_goal_workoutsWeekly = general_goal_workoutsWeekly;
        this.general_goal_foodTrackingWeekly = general_goal_foodTrackingWeekly;
        this.general_goal_date = general_goal_date;
    }
    public GeneralGoalModel(int general_goal_id, String general_goal_Text, int general_goal_workoutsWeekly, int general_goal_foodTrackingWeekly, String general_goal_date, String general_goal_uid) {
        this.general_goal_id = general_goal_id;
        this.general_goal_Text = general_goal_Text;
        this.general_goal_workoutsWeekly = general_goal_workoutsWeekly;
        this.general_goal_foodTrackingWeekly = general_goal_foodTrackingWeekly;
        this.general_goal_date = general_goal_date;
        this.general_goal_uid = general_goal_uid;
    }
    public  GeneralGoalModel(){}


    // Геттеры и сеттеры
    @com.google.firebase.firestore.Exclude
    public int getGeneral_goal_id() {
        return general_goal_id;
    }

    public void setGeneral_goal_id(int general_goal_id) {
        this.general_goal_id = general_goal_id;
    }

    public String getGeneral_goal_Text() {
        return general_goal_Text;
    }

    public void setGeneral_goal_Text(String general_goal_Text) {
        this.general_goal_Text = general_goal_Text;
    }

    public int getGeneral_goal_workoutsWeekly() {
        return general_goal_workoutsWeekly;
    }

    public void setGeneral_goal_workoutsWeekly(int general_goal_workoutsWeekly) {
        this.general_goal_workoutsWeekly = general_goal_workoutsWeekly;
    }

    public int getGeneral_goal_foodTrackingWeekly() {
        return general_goal_foodTrackingWeekly;
    }

    public void setGeneral_goal_foodTrackingWeekly(int general_goal_foodTrackingWeekly) {
        this.general_goal_foodTrackingWeekly = general_goal_foodTrackingWeekly;
    }

    public String getGeneral_goal_date() {
        return general_goal_date;
    }

    public void setGeneral_goal_date(String general_goal_date) {
        this.general_goal_date = general_goal_date;
    }

    public String getGeneral_goal_uid() {
        return general_goal_uid;
    }

    public void setGeneral_goal_uid(String general_goal_uid) {
        this.general_goal_uid = general_goal_uid;
    }
}
