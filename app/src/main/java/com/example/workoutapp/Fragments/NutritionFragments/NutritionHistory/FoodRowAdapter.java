package com.example.workoutapp.Fragments.NutritionFragments.NutritionHistory;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workoutapp.Models.NutritionModels.FoodModel;
import com.example.workoutapp.R;

import java.util.List;
import java.util.Locale;

public class FoodRowAdapter extends RecyclerView.Adapter<FoodRowAdapter.ViewHolder> {

    private final List<FoodModel> foodList;

    public FoodRowAdapter(List<FoodModel> foodList) {
        this.foodList = foodList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_food_detail_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FoodModel food = foodList.get(position);

        holder.tvName.setText(food.getFood_name());
        holder.tvKcal.setText(String.format(Locale.getDefault(), "%.0f ккал", food.getCalories()));

        String macros = String.format(Locale.getDefault(), "Б: %.0fг · Ж: %.0fг · У: %.0fг",
                food.getProtein(), food.getFat(), food.getCarb());
        holder.tvMacros.setText(macros);
    }

    @Override
    public int getItemCount() {
        return foodList != null ? foodList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvMacros, tvKcal;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_food_row_name);
            tvMacros = itemView.findViewById(R.id.tv_food_row_macros);
            tvKcal = itemView.findViewById(R.id.tv_food_row_kcal);
        }
    }
}
