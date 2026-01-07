package com.example.workoutapp.Tools;


public interface OnNavigationVisibilityListener {
    /**
     * Вызывается для установки видимости BottomNavigationView
     * @param isVisible true, если панель должна быть видна, false - если скрыта
     */
    void setBottomNavVisibility(boolean isVisible);
}
