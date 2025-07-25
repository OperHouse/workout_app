package com.example.workoutapp.NutritionAdapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workoutapp.NutritionModels.FoodModel;
import com.example.workoutapp.R;

import java.util.List;
import java.util.stream.Collectors;

public class InnerFoodAdapter  extends RecyclerView.Adapter<InnerFoodAdapter.InnerViewHolder>{

    private List<FoodModel> innerAdapterFoodList;
    private int meal_id;

    public InnerFoodAdapter(List<FoodModel> innerAdapterFoodList, int meal_id) {
        this.innerAdapterFoodList = innerAdapterFoodList.stream().map(FoodModel::new).collect(Collectors.toList());
        this.meal_id = meal_id;
    }

    @NonNull
    @Override
    public InnerFoodAdapter.InnerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.inside_eat_elm_card, parent, false);
        return new InnerFoodAdapter.InnerViewHolder(v);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull InnerFoodAdapter.InnerViewHolder holder, int position) {
        FoodModel foodElm = innerAdapterFoodList.get(position);

        holder.foodName.setText(foodElm.getFood_name());
        holder.foodAmount.setText(foodElm.getAmount() + " " + foodElm.getMeasurement_type());
        @SuppressLint("DefaultLocale") String calories = String.format("%.0f", foodElm.getCalories());
        holder.foodCalories.setText(calories);



    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateData(List<FoodModel> updateFoodList, int meal_id) {
        this.meal_id = meal_id;
        this.innerAdapterFoodList = updateFoodList.stream().map(FoodModel::new).collect(Collectors.toList());
    }

    @Override
    public int getItemCount() {
        return innerAdapterFoodList.size();
    }

    public class InnerViewHolder extends RecyclerView.ViewHolder {

        TextView foodName, foodAmount, foodCalories;

        public InnerViewHolder(@NonNull View itemView) {
            super(itemView);
            foodName = itemView.findViewById(R.id.nameEat);
            foodAmount = itemView.findViewById(R.id.amountEat);
            foodCalories = itemView.findViewById(R.id.eatCalories);
        }
    }
}
