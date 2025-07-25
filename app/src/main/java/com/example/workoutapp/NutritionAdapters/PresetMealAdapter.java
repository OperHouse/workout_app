package com.example.workoutapp.NutritionAdapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workoutapp.DAO.ConnectingMealDao;
import com.example.workoutapp.DAO.MealFoodDao;
import com.example.workoutapp.DAO.MealNameDao;
import com.example.workoutapp.MainActivity;
import com.example.workoutapp.NutritionFragments.SelectionMealPresetsFragment;
import com.example.workoutapp.NutritionModels.FoodModel;
import com.example.workoutapp.NutritionModels.MealModel;
import com.example.workoutapp.OnPresetMealLongClickListener;
import com.example.workoutapp.R;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class PresetMealAdapter extends RecyclerView.Adapter<PresetMealAdapter.PresetViewHolder> {

    private final Context context;

    private final OnPresetMealLongClickListener longClickListener;
    private final Fragment fragment;
    private MealNameDao mealNameDao;
    private ConnectingMealDao connectingMealDao;
    private MealFoodDao mealFoodDao;

    private List<MealModel> presetMeals = new ArrayList<>();
    public List<MealModel> filteredList = new ArrayList<>();
    private String currentFilter = "";


    public PresetMealAdapter(Fragment fragment, Context context, OnPresetMealLongClickListener longClickListener) {
        this.fragment = fragment;
        this.context = context;
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
        if (!presetMeals.isEmpty() || !filteredList.isEmpty()) {
            MealModel preset;

            if(!Objects.equals(currentFilter, "")){
                preset = filteredList.get(position);
            }else{
                preset = presetMeals.get(position);
            }


            holder.namePresetMeal.setText(preset.getMeal_name());

            double totalProtein = 0;
            double totalFat = 0;
            double totalCarb = 0;
            double totalCalories = 0;

            for (FoodModel eat : preset.getMeal_food_list()) {
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

            holder.itemView.setOnClickListener(v -> {
                if (fragment instanceof SelectionMealPresetsFragment) {
                    this.mealNameDao = new MealNameDao(MainActivity.getAppDataBase());
                    String formattedDate = "";
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        LocalDate currentDate = LocalDate.now();
                        formattedDate = currentDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
                    }

                    preset.setMealData(formattedDate); // Устанавливаем дату в модель
                    String presetName = preset.getMeal_name();

                    boolean exists = mealNameDao.checkIfMealExist(presetName,formattedDate);

                    if (!exists) {
                        // Сохраняем объект
                        this.mealFoodDao = new MealFoodDao(MainActivity.getAppDataBase());
                        this.connectingMealDao = new ConnectingMealDao(MainActivity.getAppDataBase());

                        mealNameDao.insertMealName(preset.getMeal_name(), formattedDate);
                        int meal_Id = (int) mealNameDao.getLastInsertedMealNameId();
                        // Добавляем еду и сохраняем полученные IDs
                        List<Long> insertedEatIds = new ArrayList<>();
                        for (FoodModel eat : preset.getMeal_food_list()) {
                            long id = mealFoodDao.addSingleFood(eat); // новый метод
                            insertedEatIds.add(id);
                        }

// Связываем mealId с этими eatId
                        connectingMealDao.connecting(meal_Id, insertedEatIds);

                        mealNameDao.logAllMealNames();
                        mealFoodDao.logAllMealFoods();
                        connectingMealDao.logAllConnections();

                        fragment.getParentFragmentManager().popBackStack();
                    } else {
                        Toast.makeText(context, "Такой приём пищи уже добавлен!", Toast.LENGTH_SHORT).show();
                    }
                }
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
    public void updatePresetMealsList(List<MealModel> presetModelList) {
        this.presetMeals = presetModelList.stream().map(MealModel::new).collect(Collectors.toList());
    }

    public void setFilteredList(String text){
        currentFilter = text;
        filteredList.clear();
        for (MealModel elm:presetMeals) {
            if(elm.getMeal_name().toLowerCase().contains(currentFilter)){
                filteredList.add(elm);
            }
        }
        notifyDataSetChanged();
    };

    public List<MealModel> getList(){
        return presetMeals;
    }
    public void changeFilterText(String text){
        currentFilter = text;
    }
    public void removePresetElm(MealModel presetElmToRemove){
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
