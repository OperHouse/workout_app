package com.example.workoutapp.Models.ProfileModels;

public class ProfileItemModel {
    public static final int TYPE_GROUP = 1;
    public static final int TYPE_CHILD = 2;

    public int type;
    public String title;
    public boolean isExpanded; // Только для группы
    public boolean isAnimating = false;
    public boolean isVisible;  // Для дочернего элемента
    public int groupIndex;     // Индекс родительской группы в dataList

    // Конструктор для группы
    public ProfileItemModel(String title, boolean isExpanded) {
        this.type = TYPE_GROUP;
        this.title = title;
        this.isExpanded = isExpanded;
        this.isVisible = true;
        this.groupIndex = -1;
    }

    // Конструктор для дочернего элемента
    public ProfileItemModel(String title, int groupIndex) {
        this.type = TYPE_CHILD;
        this.title = title;
        this.isExpanded = false;
        this.isVisible = false;
        this.groupIndex = groupIndex;
    }
}