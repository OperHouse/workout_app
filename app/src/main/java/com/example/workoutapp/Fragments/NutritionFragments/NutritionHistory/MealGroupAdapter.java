package com.example.workoutapp.Fragments.NutritionFragments.NutritionHistory;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workoutapp.Models.NutritionModels.FoodModel;
import com.example.workoutapp.R;

import java.util.List;
import java.util.Locale;

public class MealGroupAdapter extends RecyclerView.Adapter<MealGroupAdapter.ViewHolder> {

    private final List<MealWithFoods> mealGroups;

    public MealGroupAdapter(List<MealWithFoods> mealGroups) {
        this.mealGroups = mealGroups;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_meal_group, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MealWithFoods group = mealGroups.get(position);
        holder.tvMealName.setText(group.getMealName().getMeal_name());

        double mCals = 0, mProt = 0, mFat = 0, mCarb = 0;

        for (FoodModel f : group.getFoods()) {
            mCals += f.getCalories();
            mProt += f.getProtein();
            mFat += f.getFat();
            mCarb += f.getCarb();
        }

        holder.tvMealCalories.setText(String.format(Locale.getDefault(), "%.0f ккал", mCals));

        // Установка БЖУ для группы
        holder.tvMealProt.setText(String.format(Locale.getDefault(), "Белки\n%.0f г", mProt));
        holder.tvMealFat.setText(String.format(Locale.getDefault(), "Жиры\n%.0f г", mFat));
        holder.tvMealCarb.setText(String.format(Locale.getDefault(), "Углеводы\n%.0f г", mCarb));

        // Настройка вложенного списка
        holder.rvFoods.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext()));
        FoodRowAdapter foodAdapter = new FoodRowAdapter(group.getFoods());
        holder.rvFoods.setAdapter(foodAdapter);
    }

    @Override
    public int getItemCount() {
        return mealGroups != null ? mealGroups.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMealName, tvMealCalories, tvMealProt, tvMealFat, tvMealCarb;
        RecyclerView rvFoods;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMealName = itemView.findViewById(R.id.tv_meal_group_name);
            tvMealCalories = itemView.findViewById(R.id.tv_meal_group_calories);
            tvMealProt = itemView.findViewById(R.id.tv_meal_group_prot);
            tvMealFat = itemView.findViewById(R.id.tv_meal_group_fat);
            tvMealCarb = itemView.findViewById(R.id.tv_meal_group_carb);
            rvFoods = itemView.findViewById(R.id.rv_foods_inside_meal);
        }
    }
}