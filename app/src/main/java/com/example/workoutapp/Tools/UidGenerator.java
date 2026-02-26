package com.example.workoutapp.Tools;

import java.util.UUID;

public class UidGenerator {

    public static String generateExUid() {
        return "EX_" + UUID.randomUUID().toString();
    }

    public static String generateFoodUid() {
        return "FOOD_" + UUID.randomUUID().toString();
    }

    public static String generateMealUid() {
        return "MEAL_" + UUID.randomUUID().toString();
    }

    public static String generateSetUid() {
        return "SET_" + UUID.randomUUID().toString();
    }
}