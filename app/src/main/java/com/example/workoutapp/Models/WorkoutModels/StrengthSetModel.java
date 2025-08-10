package com.example.workoutapp.Models.WorkoutModels;

public class StrengthSetModel {

    private long strength_set_id;



    private double weight;
    private int rep;
    private String state;
    private int order;

    public StrengthSetModel(long id, double weight, int rep, String state, int order) {
        this.strength_set_id = id;
        this.weight = weight;
        this.rep = rep;
        this.state = state;
        this.order = order;
    }

    public StrengthSetModel(StrengthSetModel other) {
        this.strength_set_id = other.strength_set_id;
        this.weight = other.weight;
        this.rep = other.rep;
        this.state = other.state;
        this.order = other.order;
    }


    public long getStrength_set_id() {
        return strength_set_id;
    }

    public void setStrength_set_id(long strength_set_id) {
        this.strength_set_id = strength_set_id;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public int getRep() {
        return rep;
    }

    public void setRep(int rep) {
        this.rep = rep;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }
}
