package com.example.workoutapp.NutritionAdapters;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workoutapp.DAO.ConnectingMealDao;
import com.example.workoutapp.DAO.MealFoodDao;
import com.example.workoutapp.DAO.MealNameDao;
import com.example.workoutapp.MainActivity;
import com.example.workoutapp.NutritionFragments.CreateMealPresetFragment;
import com.example.workoutapp.NutritionFragments.NutritionFragment;
import com.example.workoutapp.NutritionMode;
import com.example.workoutapp.NutritionModels.FoodModel;
import com.example.workoutapp.NutritionModels.MealModel;
import com.example.workoutapp.R;

import java.util.List;
import java.util.Objects;
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
        this.outsideRecyclerView = recyclerView;
    }

    @NonNull
    @Override
    public OutsideMealAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.outside_meal_elm_card, parent, false);
        return new OutsideMealAdapter.MyViewHolder(v);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull OutsideMealAdapter.MyViewHolder holder, @SuppressLint("RecyclerView") int position) {
        if (allMealList == null || allMealList.isEmpty()) return;

        MealModel mealElm = allMealList.get(position);
        holder.mealName_TV.setText(mealElm.getMeal_name());

        setMacros(mealElm, holder);

        int meal_id = mealElm.getMeal_name_id();


        InnerFoodAdapter innerFoodAdapter = allInnerFoodAdapters.get(meal_id);
        if (innerFoodAdapter == null) {
            innerFoodAdapter = new InnerFoodAdapter(context, fragment, mealElm.getMeal_food_list(), meal_id);
            allInnerFoodAdapters.put(meal_id, innerFoodAdapter);
        } else {
            innerFoodAdapter.updateData(mealElm.getMeal_food_list(), meal_id);
        }
        holder.innerFoodRecycler.setLayoutManager(new LinearLayoutManager(context));
        holder.innerFoodRecycler.setAdapter(innerFoodAdapter);
        innerFoodAdapter.attachSwipeToDelete(holder.innerFoodRecycler, meal_id);


        holder.addFood_BTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Переход к новому фрагменту
                FragmentManager fragmentManager = fragment.getParentFragmentManager(); // Use the fragment reference to get FragmentManager
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.frameLayout, new CreateMealPresetFragment(meal_id, NutritionMode.ADD_MEAL)); // Replace with the new fragment
                fragmentTransaction.addToBackStack(null); // Add to back stack if you want to navigate back
                fragmentTransaction.commit();
            }
        });


        innerFoodAdapter.setOnMealUpdatedListener(new OnMealUpdatedListener() {
            @Override
            public void onFoodUpdated(FoodModel updated) {
                mealElm.updateFood(updated); // метод внутри MealModel
                notifyItemChanged(position);
            }

            @Override
            public void onFoodDeleted(int foodId) {
                mealElm.removeFoodById(foodId);
                notifyItemChanged(position);
            }
        });

        holder.deleteMeal_BTN.setOnClickListener(v -> delConfirmDialog(mealElm, position));

    }

    @Override
    public int getItemCount() {
        if (allMealList != null) {
            return allMealList.size();
        }
        return 0;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateOuterAdapterList(@NonNull List<MealModel> newList) {
        this.allMealList = newList.stream().map(MealModel::new).collect(Collectors.toList());
        notifyDataSetChanged();
    }

    @SuppressLint("SetTextI18n")
    private void delConfirmDialog(MealModel elm, int position) {
        Dialog dialogDeleteMeal = new Dialog(context);
        dialogDeleteMeal.setContentView(R.layout.confirm_dialog_layout);
        Objects.requireNonNull(dialogDeleteMeal.getWindow()).setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialogDeleteMeal.setCancelable(true);
        dialogDeleteMeal.setCanceledOnTouchOutside(true);

        Button deleteBtn = dialogDeleteMeal.findViewById(R.id.btnDelete);
        Button chanelBtn = dialogDeleteMeal.findViewById(R.id.btnChanel);
        TextView text1 = dialogDeleteMeal.findViewById(R.id.text1);
        TextView text2 = dialogDeleteMeal.findViewById(R.id.text2);

        deleteBtn.setText("Удалить");
        text1.setText("Удаление приема пищи");
        text2.setText("Вы действительно хотите удалить прием пищи?" + "\"" + elm.getMeal_name() +"\"");

        if (dialogDeleteMeal.getWindow() != null) {
            dialogDeleteMeal.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }

        chanelBtn.setOnClickListener(v -> dialogDeleteMeal.dismiss());

        deleteBtn.setOnClickListener(v -> {
            dialogDeleteMeal.dismiss();

            int mealId = elm.getMeal_name_id();

            // DAO
            ConnectingMealDao connectingMealDao = new ConnectingMealDao(MainActivity.getAppDataBase());
            MealFoodDao mealFoodDao = new MealFoodDao(MainActivity.getAppDataBase());
            MealNameDao mealNameDao = new MealNameDao(MainActivity.getAppDataBase());

            // Получаем все food_id, связанные с meal
            List<Long> foodIds = connectingMealDao.getFoodIdsForMeal(mealId);

            // Удаляем связи
            connectingMealDao.deleteAllConnectionsForMeal(mealId);

            // Удаляем еду по этим ID
            List<Integer> idsToDelete = foodIds.stream().map(Long::intValue).collect(Collectors.toList());
            mealFoodDao.deleteMealFoodsByIds(idsToDelete);

            // Удаляем сам приём пищи (имя)
            mealNameDao.deleteMealName(mealId);

            // Удаляем из адаптера и обновляем
            allMealList.remove(position);
            notifyItemRemoved(position);
            outsideRecyclerView.setAdapter(null);
            ((NutritionFragment) fragment).refreshAdapter();
        });

        dialogDeleteMeal.show();
    }

    public void setMacros(MealModel mealElm, MyViewHolder holder){
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


        holder.KKAL_TV.setText("Калории: " + calories);
        holder.PFC_TV.setText("Б: " + protein + " / Ж: " + fat + " / У: " + carb);
    };

    public void updateFoodInMeal(int mealId, FoodModel updatedFood) {
        for (int i = 0; i < allMealList.size(); i++) {
            MealModel meal = allMealList.get(i);
            if (meal.getMeal_name_id() == mealId) {
                meal.updateFood(updatedFood);
                notifyItemChanged(i);
                break;
            }
        }
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
