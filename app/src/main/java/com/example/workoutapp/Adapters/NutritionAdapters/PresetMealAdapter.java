package com.example.workoutapp.Adapters.NutritionAdapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workoutapp.Data.NutritionDao.ConnectingMealDao;
import com.example.workoutapp.Data.NutritionDao.MealFoodDao;
import com.example.workoutapp.Data.NutritionDao.MealNameDao;
import com.example.workoutapp.Fragments.NutritionFragments.SelectionMealPresetsFragment;
import com.example.workoutapp.MainActivity;
import com.example.workoutapp.Models.NutritionModels.FoodModel;
import com.example.workoutapp.Models.NutritionModels.MealModel;
import com.example.workoutapp.R;
import com.example.workoutapp.Tools.OnPresetMealLongClickListener;
import com.example.workoutapp.Tools.OnPresetMealSelectedListener;
import com.example.workoutapp.Tools.UidGenerator;

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
    private final OnPresetMealSelectedListener selectionListener;

    private List<MealModel> presetMeals = new ArrayList<>();
    public List<MealModel> filteredList = new ArrayList<>();
    private String currentFilter = "";


    public PresetMealAdapter(Fragment fragment, Context context, OnPresetMealLongClickListener longClickListener, OnPresetMealSelectedListener selectionListener) {
        this.fragment = fragment;
        this.context = context;
        this.longClickListener = longClickListener;
        this.selectionListener = selectionListener;
    }

    @NonNull
    @Override
    public PresetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.eat_elm_card, parent, false);
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

            if (preset.getMeal_food_list() != null) {
                for (FoodModel eat : preset.getMeal_food_list()) {
                    totalProtein += eat.getProtein();
                    totalFat += eat.getFat();
                    totalCarb += eat.getCarb();
                    totalCalories += eat.getCalories();
                }
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
                        formattedDate = currentDate.format(
                                DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    }

                    preset.setMealData(formattedDate);
                    String presetName = preset.getMeal_name();

                    boolean exists =
                            mealNameDao.checkIfMealExist(
                                    presetName,
                                    formattedDate);

                    if (!exists) {

                        this.mealFoodDao =
                                new MealFoodDao(MainActivity.getAppDataBase());
                        this.connectingMealDao =
                                new ConnectingMealDao(MainActivity.getAppDataBase());
                        String mealUid = UidGenerator.generateMealUid();

                        // 1️⃣ Создаём meal локально
                        mealNameDao.insertMealName(
                                preset.getMeal_name(),
                                formattedDate,
                                mealUid);

                        int meal_Id =
                                (int) mealNameDao.getLastInsertedMealNameId();

                        // 2️⃣ Копируем еду
                        List<Long> insertedEatIds =
                                new ArrayList<>();

                        for (FoodModel eat :
                                preset.getMeal_food_list()) {

                            long id =
                                    mealFoodDao.addSingleFood(eat);

                            insertedEatIds.add(id);
                        }

                        connectingMealDao.connecting(
                                meal_Id,
                                insertedEatIds);

                        // =========================================
                        // 🔥 СИНХРОНИЗАЦИЯ
                        // =========================================



                        // ВАЖНО: сохранить uid в локальной таблице!
                        mealNameDao.setMealUid(meal_Id, mealUid);

                        MealModel meal =
                                new MealModel(
                                        meal_Id,
                                        preset.getMeal_name(),
                                        formattedDate,
                                        preset.getMeal_food_list(),
                                        mealUid
                                );

                        meal.setDeleted(false);
                        meal.setVersion(1);

                        MainActivity.getSyncManager().uploadMeal(meal);

                        // =========================================

                        selectionListener.onPresetMealSelected();
                        fragment.getParentFragmentManager()
                                .popBackStack();

                    } else {

                        Toast.makeText(context,
                                "Такой приём пищи уже добавлен!",
                                Toast.LENGTH_SHORT).show();
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
        notifyDataSetChanged();
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
        ConstraintLayout constraintLayout;

        public PresetViewHolder(@NonNull View itemView) {
            super(itemView);
            namePresetMeal = itemView.findViewById(R.id.foodName_D_TV);
            pfcText = itemView.findViewById(R.id.protFatCarb_D_TV);
            eatCalories = itemView.findViewById(R.id.eatCalories);
            constraintLayout = itemView.findViewById(R.id.eat_elm_card_CL);
        }
    }
}
