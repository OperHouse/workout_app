package com.example.workoutapp.Models.WorkoutModels;

public class BaseExModel {
    private long base_ex_id;
    private String exName;
    private String exType;
    private String bodyType;
    private boolean isPressed = false;

    public BaseExModel(long id, String exName, String exType, String bodyType, boolean isPressed) {
        this.base_ex_id = id;
        this.exName = exName;
        this.exType = exType;
        this.bodyType = bodyType;
        this.isPressed = isPressed;
    }

    public BaseExModel(long id, String exName, String exType, String bodyType) {
        this.base_ex_id = id;
        this.exName = exName;
        this.exType = exType;
        this.bodyType = bodyType;
    }

    public BaseExModel(BaseExModel other) {
        this.base_ex_id = other.base_ex_id;
        this.exName = other.exName;
        this.exType = other.exType;
        this.bodyType = other.bodyType;
        this.isPressed = other.isPressed;
    }

    public BaseExModel() {

    }

    public long getBase_ex_id() {
        return base_ex_id;
    }

    public void setBase_ex_id(long base_ex_id) {
        this.base_ex_id = base_ex_id;
    }

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
