package com.example.workoutapp.Models.WorkoutModels;

public class CardioSetModel {



    private long cardio_set_id;
    private double temp;
    private int time;
    private double distance;


    private String state;
    private int order;

    public CardioSetModel(long id, double temp, int time, double distance, String state, int order) {
        this.cardio_set_id = id;
        this.temp = temp;
        this.time = time;
        this.distance = distance;
        this.state = state;
        this.order = order;
    }

    public CardioSetModel(CardioSetModel other) {
        this.cardio_set_id = other.cardio_set_id;
        this.temp = other.temp;
        this.time = other.time;
        this.distance = other.distance;
        this.state = other.state;
        this.order = other.order;
    }



    public long getCardio_set_id() {
        return cardio_set_id;
    }

    public void setCardio_set_id(long cardio_set_id) {
        this.cardio_set_id = cardio_set_id;
    }

    public double getTemp() {
        return temp;
    }

    public void setTemp(double temp) {
        this.temp = temp;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
