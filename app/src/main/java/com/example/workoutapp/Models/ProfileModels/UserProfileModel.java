package com.example.workoutapp.Models.ProfileModels;

public class UserProfileModel {

    private long userId;
    private String userName;
    private float userHeight;
    private int userAge;
    private String userImagePath; // Поле для хранения пути к изображению

    // Полный конструктор (включая путь к фото)
    public UserProfileModel(long userId, String userName, float userHeight, int userAge, String userImagePath) {
        this.userId = userId;
        this.userName = userName;
        this.userHeight = userHeight;
        this.userAge = userAge;
        this.userImagePath = userImagePath;
    }

    // Конструктор без фото (для удобства, если фото еще нет)
    public UserProfileModel(long userId, String userName, float userHeight, int userAge) {
        this.userId = userId;
        this.userName = userName;
        this.userHeight = userHeight;
        this.userAge = userAge;
    }

    public UserProfileModel() {
    }

    // Геттеры и сеттеры
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

    public String getUserImagePath() {
        return userImagePath;
    }

    public void setUserImagePath(String userImagePath) {
        this.userImagePath = userImagePath;
    }
}