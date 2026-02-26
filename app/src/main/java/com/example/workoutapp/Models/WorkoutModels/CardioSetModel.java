package com.example.workoutapp.Models.WorkoutModels;

public class CardioSetModel {

    private long cardio_set_id;
    private double cardio_set_temp;
    private int cardio_set_time;
    private double cardio_set_distance;
    private String cardio_set_state;
    private String cardio_set_uid;
    private int cardio_set_order;

    public CardioSetModel() { }

    public CardioSetModel(long id, double cardio_set_temp, int cardio_set_time, double cardio_set_distance, String cardio_set_state, int cardio_set_order) {
        this.cardio_set_id = id;
        this.cardio_set_temp = cardio_set_temp;
        this.cardio_set_time = cardio_set_time;
        this.cardio_set_distance = cardio_set_distance;
        this.cardio_set_state = cardio_set_state;
        this.cardio_set_order = cardio_set_order;
    }
    public CardioSetModel(long id, double cardio_set_temp, int cardio_set_time, double cardio_set_distance, String cardio_set_state, int cardio_set_order, String cardio_set_uid) {
        this.cardio_set_id = id;
        this.cardio_set_temp = cardio_set_temp;
        this.cardio_set_time = cardio_set_time;
        this.cardio_set_distance = cardio_set_distance;
        this.cardio_set_state = cardio_set_state;
        this.cardio_set_order = cardio_set_order;
        this.cardio_set_uid = cardio_set_uid;
    }

    public CardioSetModel(CardioSetModel other) {
        this.cardio_set_id = other.cardio_set_id;
        this.cardio_set_temp = other.cardio_set_temp;
        this.cardio_set_time = other.cardio_set_time;
        this.cardio_set_distance = other.cardio_set_distance;
        this.cardio_set_state = other.cardio_set_state;
        this.cardio_set_order = other.cardio_set_order;
    }


    @com.google.firebase.firestore.Exclude
    public long getCardio_set_id() {
        return cardio_set_id;
    }

    public void setCardio_set_id(long cardio_set_id) {
        this.cardio_set_id = cardio_set_id;
    }

    public double getCardio_set_temp() {
        return cardio_set_temp;
    }

    public void setCardio_set_temp(double cardio_set_temp) {
        this.cardio_set_temp = cardio_set_temp;
    }

    public int getCardio_set_time() {
        return cardio_set_time;
    }

    public void setCardio_set_time(int cardio_set_time) {
        this.cardio_set_time = cardio_set_time;
    }

    public double getCardio_set_distance() {
        return cardio_set_distance;
    }

    public void setCardio_set_distance(double cardio_set_distance) {
        this.cardio_set_distance = cardio_set_distance;
    }

    public String getCardio_set_state() {
        return cardio_set_state;
    }

    public void setCardio_set_state(String cardio_set_state) {
        this.cardio_set_state = cardio_set_state;
    }

    public int getCardio_set_order() {
        return cardio_set_order;
    }

    public void setCardio_set_order(int cardio_set_order) {
        this.cardio_set_order = cardio_set_order;
    }

    public String getCardio_set_uid() {
        return cardio_set_uid;
    }

    public void setCardio_set_uid(String cardio_set_uid) {
        this.cardio_set_uid = cardio_set_uid;
    }
}
