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

import com.example.workoutapp.DAO.ConnectingMealPresetDao;
import com.example.workoutapp.MainActivity;
import com.example.workoutapp.NutritionModels.EatModel;
import com.example.workoutapp.NutritionModels.PresetMealModel;
import com.example.workoutapp.OnPresetMealLongClickListener;
import com.example.workoutapp.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class PresetMealAdapter extends RecyclerView.Adapter<PresetMealAdapter.PresetViewHolder> {

    private final Context context;
    private final ConnectingMealPresetDao connectingMealPresetDao;
    private final OnPresetMealLongClickListener longClickListener;
    private List<PresetMealModel> presetMeals = new ArrayList<>();
    public List<PresetMealModel> filteredList = new ArrayList<>();
    private String currentFilter = "";


    public PresetMealAdapter(Context context, OnPresetMealLongClickListener longClickListener) {
        this.context = context;
        this.longClickListener = longClickListener;
        this.connectingMealPresetDao = new ConnectingMealPresetDao(MainActivity.getAppDataBase());
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
        if (!presetMeals.isEmpty() || !filteredList.isEmpty()) {
            PresetMealModel preset;

            if(!Objects.equals(currentFilter, "")){
                preset = filteredList.get(position);
            }else{
                preset = presetMeals.get(position);
            }


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
    }

    @Override
    public int getItemCount() {
        if (currentFilter.isEmpty() && !presetMeals.isEmpty()) {
            return presetMeals.size();
        }else if(!currentFilter.isEmpty() && !filteredList.isEmpty()){
            return filteredList.size();
        }else {return 0;}

    }

    @SuppressLint("NotifyDataSetChanged")
    public void updatePresetMealsList(List<PresetMealModel> presetModelList) {
        this.presetMeals = presetModelList.stream().map(PresetMealModel::new).collect(Collectors.toList());
    }

    public void setFilteredList(String text){
        currentFilter = text;
        filteredList.clear();
        for (PresetMealModel elm:presetMeals) {
            if(elm.getPresetMealName().toLowerCase().contains(currentFilter)){
                filteredList.add(elm);
            }
        }
        notifyDataSetChanged();
    };

    public List<PresetMealModel> getList(){
        return presetMeals;
    }
    public void changeFilterText(String text){
        currentFilter = text;
    }
    public void removePresetElm(PresetMealModel presetElmToRemove){
        presetMeals.remove(presetElmToRemove);
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
