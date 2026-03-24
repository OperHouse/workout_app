package com.example.workoutapp.Adapters.NutritionAdapters;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
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

import com.example.workoutapp.Data.ChangeElmDao;
import com.example.workoutapp.Data.DeletionQueueDao;
import com.example.workoutapp.Data.NutritionDao.ConnectingMealDao;
import com.example.workoutapp.Data.NutritionDao.MealFoodDao;
import com.example.workoutapp.Data.NutritionDao.MealNameDao;
import com.example.workoutapp.Fragments.NutritionFragments.CreateMealPresetFragment;
import com.example.workoutapp.Fragments.NutritionFragments.NutritionFragment;
import com.example.workoutapp.MainActivity;
import com.example.workoutapp.Models.NutritionModels.FoodModel;
import com.example.workoutapp.Models.NutritionModels.MealModel;
import com.example.workoutapp.R;
import com.example.workoutapp.Tools.NutritionMode;
import com.example.workoutapp.Tools.OnMealUpdatedListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class OutsideMealAdapter extends RecyclerView.Adapter<OutsideMealAdapter.MyViewHolder> {

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


        // 1. Получаем список и делаем его Null-безопасным
        List<FoodModel> foodList = mealElm.getMeal_food_list();

        // ГАРАНТИРУЕМ, ЧТО СПИСОК НЕ NULL
        if (foodList == null) {
            foodList = new ArrayList<>();
        }

        // 2. Используем Null-безопасный список
        InnerFoodAdapter innerFoodAdapter = allInnerFoodAdapters.get(meal_id);
        if (innerFoodAdapter == null) {
            // Передаем гарантированно не-null список
            innerFoodAdapter = new InnerFoodAdapter(context, fragment, foodList, meal_id);
            allInnerFoodAdapters.put(meal_id, innerFoodAdapter);
        } else {
            // Передаем гарантированно не-null список
            innerFoodAdapter.updateData(foodList, meal_id);
        }

        holder.innerFoodRecycler.setLayoutManager(new LinearLayoutManager(context));
        holder.innerFoodRecycler.setAdapter(innerFoodAdapter);
        innerFoodAdapter.attachSwipeToDelete(holder.innerFoodRecycler, meal_id);


        holder.addFood_BTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Переход к новому фрагменту
                FragmentManager fragmentManager = fragment.getParentFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                // 2. Создаем новый фрагмент
                Fragment newPresetFragment = new CreateMealPresetFragment(meal_id, NutritionMode.ADD_MEAL);

                fragmentTransaction
                        .hide(fragment) // Скрываем ТЕКУЩИЙ фрагмент, чтобы сохранить его состояние
                        .add(R.id.frameLayout, newPresetFragment, "create_meal_preset_add_food") // Добавляем новый фрагмент
                        .addToBackStack(null)  // Оставляем в стеке для возврата назад
                        .commit();
            }
        });


        innerFoodAdapter.setOnMealUpdatedListener(new OnMealUpdatedListener() {
            @Override
            public void onFoodUpdated(FoodModel updated) {
                // 1. Обновляем модель локально (в памяти адаптера)
                mealElm.updateFood(updated);
                notifyItemChanged(position);

               ChangeElmDao changeDao = new ChangeElmDao(MainActivity.getAppDataBase());
               changeDao.enqueue(mealElm.getMeal_uid(), "meal");

                // 3. Пытаемся отправить сразу
                MainActivity.getSyncManager().uploadMeal(mealElm);
            }

            @Override
            public void onFoodDeleted(int foodId, int mealID) {
                // 1. Удаляем еду из модели адаптера
                mealElm.removeFoodById(foodId);
                notifyItemChanged(position);

                // 2. Удаляем из локальной БД (SQLite) через метод фрагмента
                ((NutritionFragment) fragment).removeFoodFromMealByID(foodId, mealID);

                if (fragment instanceof NutritionFragment) {
                    ((NutritionFragment) fragment).syncDailyTotals();
                }

                ChangeElmDao changeDao = new ChangeElmDao(MainActivity.getAppDataBase());
                changeDao.enqueue(mealElm.getMeal_uid(), "meal");

                // 4. Пытаемся отправить на сервер
                MainActivity.getSyncManager().uploadMeal(mealElm);
            }
        });

        // Кнопка удаления ВСЕГО приема пищи (вызывает диалог, который мы правили ранее)
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

        Button deleteBtn = dialogDeleteMeal.findViewById(R.id.delete_confirm_D_BTN);
        Button chanelBtn = dialogDeleteMeal.findViewById(R.id.delete_cancel_D_BTN);
        TextView text1 = dialogDeleteMeal.findViewById(R.id.delete_title_D_TV);
        TextView text2 = dialogDeleteMeal.findViewById(R.id.delete_message_D_TV);

        deleteBtn.setText("Удалить");
        text1.setText("Удаление приема пищи");
        text2.setText("Вы действительно хотите удалить прием пищи: " + "\"" + elm.getMeal_name() + "\" ?");

        if (dialogDeleteMeal.getWindow() != null) {
            dialogDeleteMeal.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }

        chanelBtn.setOnClickListener(v -> dialogDeleteMeal.dismiss());

        deleteBtn.setOnClickListener(v -> {
            dialogDeleteMeal.dismiss();

            String mealUid = elm.getMeal_uid(); // Нам нужен UID для очереди
            int mealId = (int) elm.getMeal_name_id();

            // 1. СРАЗУ ЗАПИСЫВАЕМ В ОЧЕРЕДЬ НА УДАЛЕНИЕ (до локального удаления)
            if (mealUid != null && !mealUid.isEmpty()) {
                DeletionQueueDao queueDao = new DeletionQueueDao(MainActivity.getAppDataBase());
                ChangeElmDao changeElmDao = new ChangeElmDao(MainActivity.getAppDataBase());
                queueDao.enqueue(mealUid, "meal");
                changeElmDao.removeFromQueue(mealUid);
            }

            // 2. ЛОКАЛЬНОЕ УДАЛЕНИЕ (DAO)
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

            // Удаляем сам приём пищи (имя) из локальной БД
            mealNameDao.deleteMealName(mealId);

            // 3. ОБНОВЛЯЕМ ИНТЕРФЕЙС (мгновенно)
            allMealList.remove(position);
            notifyItemRemoved(position);
            // outsideRecyclerView.setAdapter(null); // Это можно убрать, если refreshAdapter делает свое дело
            ((NutritionFragment) fragment).refreshAdapter();
            ((NutritionFragment) fragment).syncDailyTotals();

            // 4. ЗАПУСКАЕМ ПРОЦЕСС ОЧИСТКИ ОЧЕРЕДИ (попытка удалить с сервера)
            // Метод сам проверит наличие интернета
            MainActivity.getSyncManager().processPendingDeletions(() -> {
                Log.d("Sync", "Все удаления завершены");
            });
        });

        dialogDeleteMeal.show();
    }

    public void setMacros(MealModel mealElm, MyViewHolder holder) {
        double totalProtein = 0;
        double totalFat = 0;
        double totalCarb = 0;
        double totalCalories = 0;


        if (mealElm.getMeal_food_list() != null) {
            for (FoodModel eat : mealElm.getMeal_food_list()) {
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


        holder.KKAL_TV.setVisibility(View.VISIBLE);
        holder.PFC_TV.setVisibility(View.VISIBLE);
        holder.KKAL_TV.setText("Калории: " + calories);
        holder.PFC_TV.setText("Б: " + protein + " / Ж: " + fat + " / У: " + carb);


    }

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
            mealName_TV = itemView.findViewById(R.id.outside_card_meal_name_TV);
            KKAL_TV = itemView.findViewById(R.id.outside_card_calories_TV);
            PFC_TV = itemView.findViewById(R.id.outside_card_protein_fat_carbs_TV);
            addFood_BTN = itemView.findViewById(R.id.outside_card_add_food_Btn);
            deleteMeal_BTN = itemView.findViewById(R.id.outside_card_delete_meal_Btn);
            innerFoodRecycler = itemView.findViewById(R.id.outside_card_inner_RV);
        }
    }


}
