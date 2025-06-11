package com.example.workoutapp.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workoutapp.NutritionModels.EatModel;
import com.example.workoutapp.NutritionModels.PresetMealModel;
import com.example.workoutapp.OnPresetMealLongClickListener;
import com.example.workoutapp.R;

import java.util.List;

public class PresetMealAdapter extends RecyclerView.Adapter<PresetMealAdapter.PresetViewHolder> {

    private final Context context;
    private final List<PresetMealModel> presetMeals;

    private final OnPresetMealLongClickListener longClickListener;

    public PresetMealAdapter(Context context, List<PresetMealModel> presetMeals, OnPresetMealLongClickListener longClickListener) {
        this.context = context;
        this.presetMeals = presetMeals;
        this.longClickListener = longClickListener;
    }

    @NonNull
    @Override
    public PresetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.preset_meal_item_card, parent, false);
        return new PresetViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull PresetViewHolder holder, int position) {
        PresetMealModel preset = presetMeals.get(position);

        holder.namePresetMeal.setText(preset.getPresetMealName());

        double totalProtein = 0;
        double totalFat = 0;
        double totalCarb = 0;
        double totalCalories = 0;

        for (EatModel eat : preset.getPresetMealEat()) {
            totalProtein += eat.getProtein();
            totalFat += eat.getFat();
            totalCarb += eat.getCarb();
            totalCalories += eat.getCalories();
        }

        @SuppressLint("DefaultLocale") String protein = String.format("%.1f", totalProtein);
        @SuppressLint("DefaultLocale") String fat = String.format("%.1f", totalFat);
        @SuppressLint("DefaultLocale") String carb = String.format("%.1f", totalCarb);
        @SuppressLint("DefaultLocale") String calories = String.format("%.0f", totalCalories);

        holder.pfcText.setText("Б: " + protein + " / Ж: " + fat + " / У: " + carb);
        holder.eatCalories.setText(calories + " ккал");

        holder.itemView.setOnLongClickListener(v -> {
            longClickListener.onPresetMealLongClick(preset);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return presetMeals.size();
    }

    public static class PresetViewHolder extends RecyclerView.ViewHolder {

        TextView namePresetMeal, pfcText, eatCalories;
        LinearLayout linearLayoutMain;

        public PresetViewHolder(@NonNull View itemView) {
            super(itemView);
            namePresetMeal = itemView.findViewById(R.id.namePresetMeal);
            pfcText = itemView.findViewById(R.id.pfcText);
            eatCalories = itemView.findViewById(R.id.eatCalories);
            linearLayoutMain = itemView.findViewById(R.id.linearLayoutMain);
        }
    }
}
