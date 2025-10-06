package com.example.workoutapp;

public class ProfileItem {
    public static final int TYPE_GROUP = 1;
    public static final int TYPE_CHILD = 2;

    public int type;
    public String title;
    public boolean isExpanded; // Только для группы
    public boolean isVisible;  // Для дочернего элемента
    public int groupIndex;     // Индекс родительской группы в общем списке dataList

    // Конструктор для группы
    public ProfileItem(String title, boolean isExpanded) {
        this.type = TYPE_GROUP;
        this.title = title;
        this.isExpanded = isExpanded;
        this.isVisible = true; // Группа всегда видима
        this.groupIndex = -1;
    }

    // Конструктор для дочернего элемента
    public ProfileItem(String title, int groupIndex) {
        this.type = TYPE_CHILD;
        this.title = title;
        this.isExpanded = false;
        this.isVisible = false; // Дочерний элемент изначально скрыт
        this.groupIndex = groupIndex;
    }
}
