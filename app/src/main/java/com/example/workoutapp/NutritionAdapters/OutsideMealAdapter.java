package com.example.workoutapp.NutritionAdapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workoutapp.NutritionModels.FoodModel;
import com.example.workoutapp.NutritionModels.MealModel;
import com.example.workoutapp.R;

import java.util.List;
import java.util.stream.Collectors;

public class OutsideMealAdapter extends RecyclerView.Adapter<OutsideMealAdapter.MyViewHolder>{

    private RecyclerView outsideRecyclerView;
    private List<MealModel> allMealList;
    private Fragment fragment;
    private final Context context;

    private final SparseArray<InnerFoodAdapter> allInnerFoodAdapters = new SparseArray<>();

    public OutsideMealAdapter(@NonNull Fragment fragment, RecyclerView recyclerView) {
        this.context = fragment.requireContext();
        this.fragment = fragment;
        this.outsideRecyclerView = recyclerView; // сохраняем ссылку
    }

    @NonNull
    @Override
    public OutsideMealAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.outside_meal_elm_card, parent, false);
        return new OutsideMealAdapter.MyViewHolder(v);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull OutsideMealAdapter.MyViewHolder holder, int position) {
        if (allMealList == null || allMealList.isEmpty()) return;

        MealModel mealElm = allMealList.get(position);
        holder.mealName_TV.setText(mealElm.getMeal_name());

        double totalProtein = 0;
        double totalFat = 0;
        double totalCarb = 0;
        double totalCalories = 0;

        for (FoodModel eat : mealElm.getMeal_food_list()) {
            totalProtein += eat.getProtein();
            totalFat += eat.getFat();
            totalCarb += eat.getCarb();
            totalCalories += eat.getCalories();
        }

        @SuppressLint("DefaultLocale") String protein = String.format("%.1f", totalProtein);
        @SuppressLint("DefaultLocale") String fat = String.format("%.1f", totalFat);
        @SuppressLint("DefaultLocale") String carb = String.format("%.1f", totalCarb);
        @SuppressLint("DefaultLocale") String calories = String.format("%.0f", totalCalories);


        holder.KKAL_TV.setText("Калории: " +calories);
        holder.PFC_TV.setText("Б: " + protein + " / Ж: " + fat + " / У: " + carb);

        int meal_id = mealElm.getMeal_name_id();


        InnerFoodAdapter innerFoodAdapter = allInnerFoodAdapters.get(meal_id);
        if (innerFoodAdapter == null) {
            innerFoodAdapter = new InnerFoodAdapter(mealElm.getMeal_food_list(), meal_id);
            allInnerFoodAdapters.put(meal_id, innerFoodAdapter);
        } else {
            innerFoodAdapter.updateData(mealElm.getMeal_food_list(), meal_id);
        }
        holder.innerFoodRecycler.setLayoutManager(new LinearLayoutManager(context));
        holder.innerFoodRecycler.setAdapter(innerFoodAdapter);

    }

    @Override
    public int getItemCount() {
        if (allMealList != null) {
            return allMealList.size();
        }
        return 0;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateOuterAdapterList(List<MealModel> newList) {
        this.allMealList = newList.stream().map(MealModel::new).collect(Collectors.toList());
        notifyDataSetChanged();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView PFC_TV;
        TextView KKAL_TV;
        TextView mealName_TV;
        Button addFood_BTN;
        Button deleteMeal_BTN;

        RecyclerView innerFoodRecycler;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            mealName_TV = itemView.findViewById(R.id.mealName_TV);
            KKAL_TV = itemView.findViewById(R.id.kkal_TV);
            PFC_TV = itemView.findViewById(R.id.PFC);
            addFood_BTN = itemView.findViewById(R.id.addFoodBtn);
            deleteMeal_BTN = itemView.findViewById(R.id.deleteMeal);
            innerFoodRecycler = itemView.findViewById(R.id.innerFoodRecycle);
        }
    }
}
