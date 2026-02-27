package com.example.workoutapp.Tools;

import java.util.UUID;

public class UidGenerator {

    public static String generateBaseExUid() {
        return "EX_" + UUID.randomUUID().toString();
    }
    public static String generateWorkoutExUid() {
        return "WORK_" + UUID.randomUUID().toString();
    }
    public static String generateWeightUid() {
        return "WEIGHT_" + UUID.randomUUID().toString();
    }
    public static String generatePresetWorkoutUid() {
        return "PW_" + UUID.randomUUID().toString();
    }
    public static String generateActivityGoalUid() {
        return "AG_" + UUID.randomUUID().toString();
    }
    public static String generateFoodGoalUid() {
        return "FG_" + UUID.randomUUID().toString();
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