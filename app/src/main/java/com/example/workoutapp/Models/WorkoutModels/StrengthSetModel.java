package com.example.workoutapp.Models.WorkoutModels;

public class StrengthSetModel {

    private long strength_set_id;
    private double strength_set_weight;
    private int strength_set_rep;
    private String strength_set_state;
    private String strength_set_uid;
    private int strength_set_order;

    public StrengthSetModel() { }

    public StrengthSetModel(long id, double strength_set_weight, int strength_set_rep, String strength_set_state, int strength_set_order) {
        this.strength_set_id = id;
        this.strength_set_weight = strength_set_weight;
        this.strength_set_rep = strength_set_rep;
        this.strength_set_state = strength_set_state;
        this.strength_set_order = strength_set_order;
    }
    public StrengthSetModel(long id, double strength_set_weight, int strength_set_rep, String strength_set_state, int strength_set_order, String strength_set_uid) {
        this.strength_set_id = id;
        this.strength_set_weight = strength_set_weight;
        this.strength_set_rep = strength_set_rep;
        this.strength_set_state = strength_set_state;
        this.strength_set_order = strength_set_order;
        this.strength_set_uid = strength_set_uid;
    }

    public StrengthSetModel(StrengthSetModel other) {
        this.strength_set_id = other.strength_set_id;
        this.strength_set_weight = other.strength_set_weight;
        this.strength_set_rep = other.strength_set_rep;
        this.strength_set_state = other.strength_set_state;
        this.strength_set_order = other.strength_set_order;
    }

    @com.google.firebase.firestore.Exclude
    public long getStrength_set_id() {
        return strength_set_id;
    }

    public void setStrength_set_id(long strength_set_id) {
        this.strength_set_id = strength_set_id;
    }

    public double getStrength_set_weight() {
        return strength_set_weight;
    }

    public void setStrength_set_weight(double strength_set_weight) {
        this.strength_set_weight = strength_set_weight;
    }

    public int getStrength_set_rep() {
        return strength_set_rep;
    }

    public void setStrength_set_rep(int strength_set_rep) {
        this.strength_set_rep = strength_set_rep;
    }

    public String getStrength_set_state() {
        return strength_set_state;
    }

    public void setStrength_set_state(String strength_set_state) {
        this.strength_set_state = strength_set_state;
    }

    public int getStrength_set_order() {
        return strength_set_order;
    }

    public void setStrength_set_order(int strength_set_order) {
        this.strength_set_order = strength_set_order;
    }

    public String getStrength_set_uid() {
        return strength_set_uid;
    }

    public void setStrength_set_uid(String strength_set_uid) {
        this.strength_set_uid = strength_set_uid;
    }
}
