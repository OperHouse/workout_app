package com.example.workoutapp.Models.WorkoutModels;

public class BaseExModel {
    private long base_ex_id;
    private String base_ex_name;
    private String base_ex_type;
    private String base_ex_bodyType;
    private String base_ex_uid;
    private boolean isPressed = false;

    public BaseExModel(long id, String base_ex_name, String base_ex_type, String base_ex_bodyType, boolean isPressed) {
        this.base_ex_id = id;
        this.base_ex_name = base_ex_name;
        this.base_ex_type = base_ex_type;
        this.base_ex_bodyType = base_ex_bodyType;
        this.isPressed = isPressed;
    }

    public BaseExModel(long id, String base_ex_name, String base_ex_type, String base_ex_bodyType,String base_ex_uid) {
        this.base_ex_id = id;
        this.base_ex_name = base_ex_name;
        this.base_ex_type = base_ex_type;
        this.base_ex_bodyType = base_ex_bodyType;
        this.base_ex_uid = base_ex_uid;
    }

    public BaseExModel(long id, String base_ex_name, String base_ex_type, String base_ex_bodyType) {
        this.base_ex_id = id;
        this.base_ex_name = base_ex_name;
        this.base_ex_type = base_ex_type;
        this.base_ex_bodyType = base_ex_bodyType;
    }

    public BaseExModel(BaseExModel other) {
        this.base_ex_id = other.base_ex_id;
        this.base_ex_name = other.base_ex_name;
        this.base_ex_type = other.base_ex_type;
        this.base_ex_bodyType = other.base_ex_bodyType;
        this.isPressed = other.isPressed;
    }
    public BaseExModel(BaseExModel other, boolean state) {
        this.base_ex_id = other.base_ex_id;
        this.base_ex_name = other.base_ex_name;
        this.base_ex_type = other.base_ex_type;
        this.base_ex_bodyType = other.base_ex_bodyType;
        this.isPressed = state;
    }

    public BaseExModel() {

    }

    @com.google.firebase.firestore.Exclude
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


    public String getBase_ex_name() {
        return base_ex_name;
    }
    public void setBase_ex_name(String base_ex_name) {
        this.base_ex_name = base_ex_name;
    }
    public String getBase_ex_type() {
        return base_ex_type;
    }
    public void setBase_ex_type(String base_ex_type) {
        this.base_ex_type = base_ex_type;
    }
    public String getBase_ex_bodyType() {
        return base_ex_bodyType;
    }
    public void setBase_ex_bodyType(String base_ex_bodyType) {
        this.base_ex_bodyType = base_ex_bodyType;
    }


    public String getBase_ex_uid() {
        return base_ex_uid;
    }

    public void setBase_ex_uid(String base_ex_uid) {
        this.base_ex_uid = base_ex_uid;
    }
}
