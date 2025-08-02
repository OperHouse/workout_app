package com.example.workoutapp.Adapters.NutritionAdapters;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workoutapp.Data.NutritionDao.ConnectingMealDao;
import com.example.workoutapp.Data.NutritionDao.MealFoodDao;
import com.example.workoutapp.MainActivity;
import com.example.workoutapp.Tools.OnMealUpdatedListener;
import com.example.workoutapp.Fragments.NutritionFragments.NutritionFragment;
import com.example.workoutapp.Models.NutritionModels.FoodModel;
import com.example.workoutapp.R;
import com.example.workoutapp.Tools.NutritionCircleView;

import org.jetbrains.annotations.Contract;

import java.util.List;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class InnerFoodAdapter  extends RecyclerView.Adapter<InnerFoodAdapter.InnerViewHolder>{

    private List<FoodModel> innerAdapterFoodList;

    private final MealFoodDao mealFoodDao;
    private final ConnectingMealDao connectingMealDao;
    private final Context context;
    private final Fragment fragment;
    private int meal_id;
    private OnMealUpdatedListener listener;
    private boolean isAmountDropdownManuallyShown = false;

    public InnerFoodAdapter(Context context, Fragment fragment, List<FoodModel> innerAdapterFoodList, int meal_id) {
        this.innerAdapterFoodList = innerAdapterFoodList.stream().map(FoodModel::new).collect(Collectors.toList());
        this.meal_id = meal_id;

        this.fragment = fragment;
        this.context = context;
        this.mealFoodDao = new MealFoodDao(MainActivity.getAppDataBase());
        this.connectingMealDao = new ConnectingMealDao(MainActivity.getAppDataBase());
    }

    public void setOnMealUpdatedListener(OnMealUpdatedListener listener) {
        this.listener = listener;
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
        holder.itemView.setOnClickListener(v -> showEditFoodDialog(foodElm, position));


    }

    private void showEditFoodDialog(FoodModel foodElm, int position) {
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.amount_dialog);
        Objects.requireNonNull(dialog.getWindow()).setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        // UI
        TextView title = dialog.findViewById(R.id.textView1);
        TextView amountLabel = dialog.findViewById(R.id.textView2);
        AutoCompleteTextView autoComplete = dialog.findViewById(R.id.autoCompleteAmount);
        EditText editText = dialog.findViewById(R.id.editTextAmount);
        Button updateBtn = dialog.findViewById(R.id.createWorkBtn);
        ImageButton closeBtn = dialog.findViewById(R.id.imageButtonBack1);

        TextView proteinTV = dialog.findViewById(R.id.textViewProtein);
        TextView fatTV = dialog.findViewById(R.id.textViewFat);
        TextView carbTV = dialog.findViewById(R.id.textViewCarb);
        TextView caloriesTV = dialog.findViewById(R.id.textViewCalories);
        NutritionCircleView circle = dialog.findViewById(R.id.NutritionCircleView);

        title.setText("Изменить: " + foodElm.getFood_name());
        String type = foodElm.getMeasurement_type().toLowerCase();
        amountLabel.setText("Количество пищи в (" + type + ")");

        // UI логика в зависимости от типа измерения
        if (type.contains("г") || type.contains("мл")) {
            editText.setVisibility(View.VISIBLE);
            autoComplete.setVisibility(View.GONE);
            editText.setText(String.valueOf(foodElm.getAmount()));
        } else {
            editText.setVisibility(View.GONE);
            autoComplete.setVisibility(View.VISIBLE);

            // Настроим автодополнение
            List<String> options = new java.util.ArrayList<>();
            for (int i = 1; i <= 50; i++) options.add(i + " " + type);

            ArrayAdapter<String> adapter = createStyledAdapter(context, options);

            // Используем универсальный метод для настройки autoComplete
            setupAutoComplete(
                    autoComplete,
                    adapter,
                    null,
                    () -> isAmountDropdownManuallyShown,
                    value -> isAmountDropdownManuallyShown = value,
                    foodElm,
                    proteinTV,
                    fatTV,
                    carbTV,
                    caloriesTV,
                    circle
            );
            autoComplete.setText(options.get(0), false);
        }

        // Макросы
        updateMacrosUI(foodElm, proteinTV, fatTV, carbTV, caloriesTV, circle);

        editText.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                int newAmount = parseIntOrZero(s.toString().trim());
                FoodModel updated = scaleFood(foodElm, newAmount);
                updateMacrosUI(updated, proteinTV, fatTV, carbTV, caloriesTV, circle);
            }
            @Override public void afterTextChanged(android.text.Editable s) {}
        });

        updateBtn.setText("Обновить");
        updateBtn.setOnClickListener(v -> {
            int newAmount;

            if (editText.getVisibility() == View.VISIBLE) {
                newAmount = parseIntOrZero(editText.getText().toString().trim());
            } else {
                try {
                    newAmount = Integer.parseInt(autoComplete.getText().toString().split(" ")[0]);
                } catch (Exception e) {
                    Toast.makeText(context, "Неверный ввод", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            if (newAmount <= 0) {
                Toast.makeText(context, "Введите корректное количество", Toast.LENGTH_SHORT).show();
                return;
            }

            // Обновляем еду
            FoodModel updatedModel = scaleFood(foodElm, newAmount);
            updatedModel.setFood_id(foodElm.getFood_id());

            // Обновляем в БД
            mealFoodDao.updateMealFood(updatedModel);

            // Обновляем адаптер
            innerAdapterFoodList.set(position, updatedModel);
            notifyItemChanged(position);

            if (listener != null) {
                listener.onFoodUpdated(updatedModel);
            }
            if (fragment instanceof NutritionFragment) {
                ((NutritionFragment) fragment).updateFoodInMeal(meal_id, updatedModel);
            }
            Toast.makeText(context, "Изменено", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        closeBtn.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
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

    public void attachSwipeToDelete(RecyclerView recyclerView, int meal_id) {
        recyclerView.setItemAnimator(null);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getBindingAdapterPosition();


                if (position >= 0 && position < innerAdapterFoodList.size()) {
                    FoodModel foodToDelete = innerAdapterFoodList.get(position);

                    // Удаление из таблицы еды
                    mealFoodDao.deleteMealFoodById(foodToDelete.getFood_id());

                    // Удаление связи из ConnectingMealTable
                    connectingMealDao.deleteConnection(meal_id, foodToDelete.getFood_id());

                    // Удаление из адаптера
                    innerAdapterFoodList.remove(position);
                    notifyItemRemoved(position);

                    Toast.makeText(context, "Еда удалена", Toast.LENGTH_SHORT).show();

                    if (listener != null) {
                        listener.onFoodDeleted(foodToDelete.getFood_id());
                    }

                    ((NutritionFragment) fragment).removeFoodFromMeal();


                    connectingMealDao.logAllConnections();
                    mealFoodDao.logAllMealFoods();
                }
            }
        });

        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    private FoodModel scaleFood(FoodModel original, int newAmount) {
        double multiplier = newAmount / (double) original.getAmount();
        return new FoodModel(
                original.getFood_id(),
                original.getFood_name(),
                original.getProtein() * multiplier,
                original.getFat() * multiplier,
                original.getCarb() * multiplier,
                original.getCalories() * multiplier,
                newAmount,
                original.getMeasurement_type()
        );
    }

    private int parseIntOrZero(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void updateMacrosUI(FoodModel food, TextView protein, TextView fat, TextView carb, TextView calories, NutritionCircleView circle) {
        protein.setText("Белки (" + String.format("%.1f", food.getProtein()) + " гр)");
        fat.setText("Жиры (" + String.format("%.1f", food.getFat()) + " гр)");
        carb.setText("Углеводы (" + String.format("%.1f", food.getCarb()) + " гр)");
        calories.setText("Калории: " + String.format("%.0f", food.getCalories()) + " ккал");
        circle.setMacros((float) food.getProtein(), (float) food.getFat(), (float) food.getCarb());
    }

    private void setupAutoComplete(
            @NonNull AutoCompleteTextView autoCompleteView,
            ArrayAdapter<String> adapter,
            Runnable onItemSelected,
            BooleanSupplier isDropdownShownSupplier,
            Consumer<Boolean> setDropdownState,
            FoodModel foodModel,
            TextView proteinView,
            TextView fatView,
            TextView carbView,
            TextView caloriesView,
            NutritionCircleView circleView
    ) {
        autoCompleteView.setAdapter(adapter);
        autoCompleteView.setDropDownVerticalOffset(2); // Отступ для выпадающего списка

        // Обработчик клика по полю
        autoCompleteView.setOnClickListener(v -> {
            boolean isShown = isDropdownShownSupplier.getAsBoolean();
            if (isShown) {
                autoCompleteView.dismissDropDown();
            } else {
                autoCompleteView.showDropDown();
            }
            setDropdownState.accept(!isShown);
        });

        // Обработчик выбора элемента в списке
        autoCompleteView.setOnItemClickListener((parent, view, position, id) -> {
            setDropdownState.accept(false);
            if (onItemSelected != null) onItemSelected.run(); // Вспомогательная функция (если передана)

            String selected = parent.getItemAtPosition(position).toString();
            int amount = parseIntOrZero(selected.split(" ")[0]); // Получаем количество пищи

            if (amount > 0) {
                // Обновляем макросы для выбранного количества
                updateMacrosByAmount(foodModel, amount, proteinView, fatView, carbView, circleView, caloriesView);
            }
        });

        // Обработчик фокуса
        autoCompleteView.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                setDropdownState.accept(false);
                autoCompleteView.dismissDropDown();
            }
        });
    }

    @NonNull
    @Contract("_, _ -> new")
    private ArrayAdapter<String> createStyledAdapter(Context context, List<String> items) {
        return new ArrayAdapter<String>(context, R.layout.item_dropdown_small_padding, items) {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                view.setBackgroundColor(ContextCompat.getColor(context, R.color.light_gray2));
                return view;
            }
        };
    }

    @SuppressLint("DefaultLocale")
    private void updateMacrosByAmount(
            FoodModel foodModel,
            int amount,
            TextView proteinView,
            TextView fatView,
            TextView carbView,
            NutritionCircleView circleView,
            TextView caloriesView
    ) {
        if (foodModel == null || foodModel.getAmount() == 0) return;

        double multiplier = (double) amount / foodModel.getAmount();

        float protein = (float) (foodModel.getProtein() * multiplier);
        float fat = (float) (foodModel.getFat() * multiplier);
        float carb = (float) (foodModel.getCarb() * multiplier);
        float calories = (float) (foodModel.getCalories() * multiplier);

        proteinView.setText(String.format("Белки (%.1f гр)", protein));
        fatView.setText(String.format("Жиры (%.1f гр)", fat));
        carbView.setText(String.format("Углеводы (%.1f гр)", carb));
        caloriesView.setText(String.format("Калории: %.0f ккал", calories));

        circleView.setMacros(protein, fat, carb);
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
