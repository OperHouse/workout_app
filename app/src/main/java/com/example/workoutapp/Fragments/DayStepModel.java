package com.example.workoutapp.Fragments;

public class DayStepModel {
    private String dayName;
    private String date;



    private String fullDate;
    private boolean isTargetReached;
    private boolean isSelected;

    public DayStepModel(String dayName, String date, boolean isTargetReached, boolean isSelected) {
        this.dayName = dayName;
        this.date = date;
        this.isTargetReached = isTargetReached;
        this.isSelected = isSelected;
    }

    // Геттеры
    public String getDayName() { return dayName; }
    public String getDate() { return date; }
    public boolean isTargetReached() { return isTargetReached; }
    public boolean isSelected() { return isSelected; }

    // ДОБАВЬ ЭТОТ СЕТТЕР
    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public String getFullDate() {
        return fullDate;
    }

    public void setFullDate(String fullDate) {
        this.fullDate = fullDate;
    }
}