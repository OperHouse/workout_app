package com.example.workoutapp.Models.ProfileModels;

public class UserProfileModel {


    private long userId;
    private String userName;
    private float userHeight;
    private int userAge;

    // Конструктор
    public UserProfileModel(long userId, String userName, float userHeight, int userAge) {
        this.userId = userId;
        this.userName = userName;
        this.userHeight = userHeight;
        this.userAge = userAge;
    }

    public UserProfileModel() {
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public float getUserHeight() {
        return userHeight;
    }

    public void setUserHeight(float userHeight) {
        this.userHeight = userHeight;
    }

    public int getUserAge() {
        return userAge;
    }

    public void setUserAge(int userAge) {
        this.userAge = userAge;
    }
}
