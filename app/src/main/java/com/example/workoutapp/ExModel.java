package com.example.workoutapp;

import java.io.Serializable;

public class ExModel implements Serializable {

    String exName;
    String exType;
    String bodyType;
    boolean isPressed = false;

    public boolean getIsPressed() {
        return isPressed;
    }

    public void setIsPressed(boolean pressed) {
        isPressed = pressed;
    }


    public String getExName() {
        return exName;
    }
    public void setExName(String exName) {
        this.exName = exName;
    }
    public String getExType() {
        return exType;
    }
    public void setExType(String exType) {
        this.exType = exType;
    }
    public String getBodyType() {
        return bodyType;
    }
    public void setBodyType(String bodyType) {
        this.bodyType = bodyType;
    }







}
