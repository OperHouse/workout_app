package com.example.workoutapp.Fragments.NutritionFragments;

import static com.google.android.material.internal.ViewUtils.hideKeyboard;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workoutapp.Adapters.NutritionAdapters.FoodAdapter;
import com.example.workoutapp.Data.NutritionDao.BaseEatDao;
import com.example.workoutapp.Data.NutritionDao.ConnectingMealDao;
import com.example.workoutapp.Data.NutritionDao.ConnectingMealPresetDao;
import com.example.workoutapp.Data.NutritionDao.MealFoodDao;
import com.example.workoutapp.Data.NutritionDao.PresetEatDao;
import com.example.workoutapp.Data.NutritionDao.PresetMealNameDao;
import com.example.workoutapp.MainActivity;
import com.example.workoutapp.Models.NutritionModels.FoodModel;
import com.example.workoutapp.R;
import com.example.workoutapp.Tools.NutritionCircleView;
import com.example.workoutapp.Tools.NutritionMode;
import com.example.workoutapp.Tools.OnEatItemClickListener;
import com.example.workoutapp.Tools.OnPresetMealSelectedListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;


public class CreateMealPresetFragment extends Fragment implements OnEatItemClickListener, OnPresetMealSelectedListener {
    private RecyclerView eatRecycler;
    private BaseEatDao baseEatDao;
    private PresetMealNameDao presetMealNameDao;
    private PresetEatDao presetEatDao;
    private ConnectingMealPresetDao connectingMealPresetDao;
    private List<FoodModel> eatList;
    private List<FoodModel> baseEatList;
    public FoodAdapter foodAdapter;
    private EditText edtSearchText;
    private String searchText = "";

    private boolean isAmountDropdownManuallyShown = false;
    private static long pendingNewFoodId = -1;


    private NutritionMode currentMode = NutritionMode.CREATE_PRESET;
    private int mealId = -1; // для добавления в конкретный приём пищи

    public CreateMealPresetFragment(int mealId, NutritionMode mode) {
        this.mealId = mealId;
        this.currentMode = mode;
    }

    public CreateMealPresetFragment() {
    }
    @Override
    public void onResume() {
        super.onResume();

        // 🔥 ЛОГ: Проверяем значение pendingNewFoodId при возобновлении
        Log.d("PresetFragment", "onResume called. pendingNewFoodId is: " + pendingNewFoodId);

        if (pendingNewFoodId != -1) {
            Log.d("PresetFragment", "CONDITION MET. Loading food ID: " + pendingNewFoodId);

            // ВАЖНО: Ваша функция addNewFoodToAdapter принимает int, поэтому нужно приведение (int)
            addNewFoodToAdapter((int) pendingNewFoodId);

            // СБРАСЫВАЕМ ФЛАГ
            pendingNewFoodId = -1;
        } else {
            Log.d("PresetFragment", "CONDITION FAILED (pendingNewFoodId is -1). No update needed.");
        }
    }
    public CreateMealPresetFragment( NutritionMode mode) {
        this.currentMode = mode;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.baseEatDao = new BaseEatDao(MainActivity.getAppDataBase());
        this.presetMealNameDao = new PresetMealNameDao(MainActivity.getAppDataBase());
        this.presetEatDao = new PresetEatDao(MainActivity.getAppDataBase());
        this.connectingMealPresetDao = new ConnectingMealPresetDao(MainActivity.getAppDataBase());

        getParentFragmentManager().setFragmentResultListener(
                "new_food_added",
                this, // Используем 'this' (фрагмент) как LifecycleOwner
                (requestKey, result) -> {
                    if (requestKey.equals("new_food_added")) {

                        // Получаем ID как long, используя -1L как значение по умолчанию
                        long newFoodIdLong = result.getLong("new_food_id", -1L);

                        if (newFoodIdLong != -1L) {
                            // Вызываем вашу функцию обновления
                            addNewFoodToAdapter(newFoodIdLong);
                        }
                    }
                });
    }

    @SuppressLint({"ClickableViewAccessibility", "RestrictedApi"})
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View addEatToPresetFragment = inflater.inflate(R.layout.fragment_create_meal_preset, container, false);

        ImageButton backBtn = addEatToPresetFragment.findViewById(R.id.create_meal_preset_fragment_back_IB);
        Button createFoodBtn = addEatToPresetFragment.findViewById(R.id.create_meal_preset_fragment_create_eat_Btn);
        Button createPresetBtn = addEatToPresetFragment.findViewById(R.id.create_meal_preset_fragment_next_Btn);
        TextView text = addEatToPresetFragment.findViewById(R.id.create_meal_preset_fragment_title_TV);
        TextView text_2 = addEatToPresetFragment.findViewById(R.id.create_meal_preset_fragment_hint_TV);
        eatRecycler = addEatToPresetFragment.findViewById(R.id.create_meal_preset_fragment_food_RV);

        View customSearchViewContainer = addEatToPresetFragment.findViewById(R.id.create_preset_fragment_search_include);
        CardView cardContainer = (CardView) customSearchViewContainer; // <-- Скорректировано: приводим тип к CardView

        // Находим внутренний ConstraintLayout, который имеет ID CARD_search_CL и где меняется фон.
        ConstraintLayout layoutSearch = cardContainer.findViewById(R.id.CARD_search_CL);

        // Остальные дочерние элементы ищутся внутри layoutSearch (CARD_search_CL)
        edtSearchText = layoutSearch.findViewById(R.id.edt_search_text);
        ImageView ivSearchIcon = layoutSearch.findViewById(R.id.iv_search_icon);
        ImageView ivClearText = layoutSearch.findViewById(R.id.iv_clear_text);

        edtSearchText.setHint(getString(R.string.hint_searchView_food));

        edtSearchText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                layoutSearch.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.search_back_active));
                ivSearchIcon.setColorFilter(ContextCompat.getColor(requireContext(), R.color.white));
                ivClearText.setColorFilter(ContextCompat.getColor(requireContext(), R.color.white));
                edtSearchText.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
                edtSearchText.setHintTextColor(ContextCompat.getColor(requireContext(), R.color.white));
            } else {
                layoutSearch.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.search_back_normal));
                ivSearchIcon.setColorFilter(ContextCompat.getColor(requireContext(), R.color.light_gray2));
                ivClearText.setColorFilter(ContextCompat.getColor(requireContext(), R.color.light_gray2));
                edtSearchText.setTextColor(ContextCompat.getColor(requireContext(), R.color.light_gray2));
                edtSearchText.setHintTextColor(ContextCompat.getColor(requireContext(), R.color.light_gray2));
            }
        });

        // Поведение "как SearchView" при нажатии "Поиск" на клавиатуре
        edtSearchText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                v.clearFocus();
                eatRecycler.requestFocus(); // как в старом onQueryTextSubmit
                return true;
            }
            return false;
        });

        // Слушатель изменений текста (аналог onQueryTextChange)
        edtSearchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchText = s.toString();
                if(Objects.equals(searchText, "")){
                    foodAdapter.filteredList.clear();
                    foodAdapter.changeFilterText(searchText);
                    foodAdapter.notifyDataSetChanged();
                    if(foodAdapter.getItemCount() != 0){
                        text.setVisibility(View.GONE);
                        text.setText(getString(R.string.hint_add_food_preset));
                        createPresetBtn.setVisibility(View.VISIBLE);
                    }
                }else{
                    foodAdapter.setFilteredList(searchText);
                    if(foodAdapter.getItemCount() == 0){
                        text.setVisibility(View.VISIBLE);
                        text.setText("Кажется, такой еды нет :(");
                        createPresetBtn.setVisibility(View.GONE);
                    }else {
                        text.setVisibility(View.GONE);
                        text.setText(getString(R.string.hint_add_food_preset));
                        createPresetBtn.setVisibility(View.VISIBLE);
                    }
                }

                // Показать/спрятать кнопку очистки
                ivClearText.setVisibility(searchText.trim().isEmpty() ? View.GONE : View.VISIBLE);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Кнопка очистки текста
        ivClearText.setOnClickListener(v -> {
            edtSearchText.setText("");
            edtSearchText.clearFocus();
        });
        addEatToPresetFragment.setOnTouchListener((v, event) -> {
            // Проверяем, если поле ввода активно
            if (edtSearchText.hasFocus()) {
                // 1. Снимаем фокус с поля ввода
                edtSearchText.clearFocus();

                // 2. Скрываем клавиатуру с помощью InputMethodManager
                InputMethodManager imm = (InputMethodManager) requireActivity()
                        .getSystemService(Context.INPUT_METHOD_SERVICE);

                if (imm != null) {
                    // v.getWindowToken() получает токен от корневого View
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }

                // Сообщаем, что касание было обработано, чтобы оно не попало к дочерним View
                return true;
            }
            // Если поле не было в фокусе, позволяем событию двигаться дальше
            return false;
        });

        edtSearchText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {

                // 1. Убираем фокус с поля ввода
                v.clearFocus();

                // 2. Скрываем клавиатуру
                hideKeyboard(v);


                return true; // Сообщаем, что событие обработано
            }
            return false;
        });

        // очистка текста
        ivClearText.setOnClickListener(v -> edtSearchText.setText(""));



        eatList = baseEatDao.getAllEat();

        //Обособленная копия eatList, которая никак не связанна с другими листами
        baseEatList = eatList.stream().map(FoodModel::new).collect(Collectors.toList());
        foodAdapter = new FoodAdapter(requireContext(), this, CreateMealPresetFragment.this);
        if (currentMode == NutritionMode.EDIT_PRESET ){
            List<Integer> connectedEatIds = connectingMealPresetDao.getEatIdsForPreset(mealId);

            for (int i = connectedEatIds.size() - 1; i >= 0; i--) {
                int eatId = connectedEatIds.get(i);
                FoodModel eat = presetEatDao.getPresetFoodById(eatId);

                if (eat != null) {
                    // Удаляем из eatList все элементы с таким же именем
                    Iterator<FoodModel> iterator = eatList.iterator();
                    while (iterator.hasNext()) {
                        FoodModel existing = iterator.next();
                        if (existing.getFood_name().equals(eat.getFood_name())) {
                            iterator.remove();
                            eat.setFood_id(existing.getFood_id());
                        }
                    }

                    // Добавляем в начало и отмечаем выбранным
                    eat.setIsSelected(true);
                    eatList.add(0, eat);
                }
            }

            text_2.setText("Изменение пресета");
        } else if (currentMode == NutritionMode.ADD_MEAL) {
            text_2.setText("Добавление еды");
        }


        eatRecycler.setHasFixedSize(true);
        eatRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        eatRecycler.setAdapter(foodAdapter);


        foodAdapter.updateEatList(eatList);

        if (eatList.isEmpty()){
            createPresetBtn.setVisibility(View.GONE);
        }else{
            createPresetBtn.setVisibility(View.VISIBLE);
            text.setVisibility(View.GONE);
        }

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getFragmentManager();
                assert fragmentManager != null;
                if (fragmentManager.getBackStackEntryCount() > 0) {
                    fragmentManager.popBackStack();
                }
            }
        });

        createFoodBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard(edtSearchText);
                Fragment createFoodFragment = new CreateFoodFragment(currentMode);
                FragmentManager fragmentManager = getParentFragmentManager(); // или getFragmentManager()
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                fragmentTransaction
                        .hide(CreateMealPresetFragment.this)
                        .add(R.id.frameLayout, createFoodFragment, "create_food_fragment") // Добавляем новый фрагмент с тегом
                        .addToBackStack(null)  // Чтобы можно было вернуться назад
                        .commit();
            }
        });


        setupSwipeLeftForRecycler(eatRecycler, eatList);

        createPresetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (currentMode == NutritionMode.ADD_MEAL) {
                    // Получаем выбранные продукты
                    List<FoodModel> selected = foodAdapter.getPressedEat();
                    if (selected.isEmpty()) {
                        Toast.makeText(requireContext(), "Выберите еду для добавления", Toast.LENGTH_SHORT).show();
                        return;
                    }



                    ConnectingMealDao connectingMealDao = new ConnectingMealDao(MainActivity.getAppDataBase());
                    MealFoodDao mealFoodDao = new MealFoodDao(MainActivity.getAppDataBase());
                    for (FoodModel food : selected) {
                        connectingMealDao.connectingSingleFood(mealId, mealFoodDao.addSingleFood(food));
                    }

                    onPresetMealSelected();
                    requireActivity().getOnBackPressedDispatcher().onBackPressed();

                }else {
                    showAddPresetNameDialog();
                }


            }
        });



        return addEatToPresetFragment;
    }


    @Override
    public void onEatItemClick(Context context, FoodModel foodModel) {
        showFoodDialog(context, foodModel);
    }
    @SuppressLint({"SetTextI18n", "ClickableViewAccessibility", "RestrictedApi"})
    public void showFoodDialog(Context context, FoodModel foodModel) {
        edtSearchText.clearFocus();
        if(!foodModel.getIsSelected()) {
            android.app.Dialog dialog = new android.app.Dialog(context);
            dialog.setContentView(R.layout.amount_dialog);
            Objects.requireNonNull(dialog.getWindow()).setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

            ConstraintLayout DialogRootLayout = dialog.findViewById(R.id.amount_dialog_root_CL);
            TextView title = dialog.findViewById(R.id.nutrition_title_D_TV);
            TextView amountLabel = dialog.findViewById(R.id.nutrition_hint_D_TV);
            TextInputEditText textInputEditText = dialog.findViewById(R.id.fragment_create_food_amound_food_TIET);
            AutoCompleteTextView dropdownInner  = dialog.findViewById(R.id.fragment_create_food_amound_food_dropdown_TIET);
            TextInputLayout dropdownOutVisibility = dialog.findViewById(R.id.fragment_create_food_amound_food_dropdown_TIL);
            Button updateBtn = dialog.findViewById(R.id.nutrition_create_D_BTN);
            ImageButton closeBtn = dialog.findViewById(R.id.nutrition_close_D_BTN);

            TextView proteinTV = dialog.findViewById(R.id.nutrition_protein_label_D_TV);
            TextView fatTV = dialog.findViewById(R.id.nutrition_fat_label_D_TV);
            TextView carbTV = dialog.findViewById(R.id.nutrition_carb_label_D_TV);
            TextView caloriesTV = dialog.findViewById(R.id.nutrition_calories_label_D_TV);
            TextView errorTV = dialog.findViewById(R.id.nutritionD_error_TV);
            TextView errorDropDownTV = dialog.findViewById(R.id.nutrition_D_error_drop_down_TV);
            NutritionCircleView circle = dialog.findViewById(R.id.nutrition_circle_D_CUSTOM);

            // Заголовок и круг
            title.setText("Добавить: " + foodModel.getFood_name());
            circle.setMacros(
                    (float) foodModel.getProtein(),
                    (float) foodModel.getFat(),
                    (float) foodModel.getCarb()
            );
            DialogRootLayout.setOnTouchListener((v, event) -> {
                dropdownInner.clearFocus();
                textInputEditText.clearFocus();
                return false;
            });
            textInputEditText.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT) {
                    hideKeyboard(v);
                    v.clearFocus();
                    return true;
                }
                return false;
            });


            // Обновляем текст с граммами
            proteinTV.setText("Белки (" + foodModel.getProtein() + " гр)");
            fatTV.setText("Жиры (" + foodModel.getFat() + " гр)");
            carbTV.setText("Углеводы (" + foodModel.getCarb() + " гр)");
            caloriesTV.setText("Калории: " + foodModel.getCalories() + " ккал");


            String measurementType = foodModel.getMeasurement_type().toLowerCase();
            amountLabel.setText("Количество пищи в (" + measurementType + ")");

            List<String> options = new ArrayList<>();
            for (int i = 1; i <= 50; i++) {
                options.add(i + " " + measurementType);
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), R.layout.spinners_style, options);

            if (measurementType.equals("гр") || measurementType.equals("грамм") ||
                    measurementType.equals("мл") || measurementType.equals("миллилитр")) {
                dropdownOutVisibility.setVisibility(View.GONE);
                textInputEditText.setVisibility(View.VISIBLE);
            } else {
                textInputEditText.setVisibility(View.GONE);
                dropdownOutVisibility.setVisibility(View.VISIBLE);

                setupAutoComplete(
                        dropdownInner,
                        adapter,
                        null,
                        () -> isAmountDropdownManuallyShown,
                        value -> isAmountDropdownManuallyShown = value,
                        foodModel,
                        proteinTV,
                        fatTV,
                        carbTV,
                        caloriesTV,
                        circle
                );
                dropdownInner.setText(options.get(0), false);
            }

            dropdownInner.setOnClickListener(v -> {
                if (!dropdownInner.isPopupShowing()){
                    dropdownInner.clearFocus();
                }
                else
                    dropdownInner.showDropDown();
            });

            textInputEditText.addTextChangedListener(new android.text.TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    String input = s.toString().trim();
                    int amount = parseIntOrZero(input);
                    if (amount > 0) {
                        updateMacrosByAmount(foodModel, amount, proteinTV, fatTV, carbTV, circle, caloriesTV);

                    }
                }

                @Override
                public void afterTextChanged(android.text.Editable s) {
                }
            });

            closeBtn.setOnClickListener(v -> dialog.dismiss());

            updateBtn.setOnClickListener(v -> {
                int amount;
                if (textInputEditText.getVisibility() == View.VISIBLE) {
                    String input = textInputEditText.getText().toString().trim();
                    if (input.isEmpty()) {
                        errorTV.setVisibility(View.VISIBLE);
                        return;
                    }
                    amount = parseIntOrZero(input);
                } else {
                    String selected = dropdownInner.getText().toString().trim();
                    if (selected.isEmpty()) {
                        errorDropDownTV.setVisibility(View.VISIBLE);
                        return;
                    }
                    try {
                        amount = Integer.parseInt(selected.split(" ")[0]);
                    } catch (Exception e) {
                        Toast.makeText(context, "Неверный формат", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                FoodModel baseFoodModel = null;
                for (FoodModel base : baseEatList) {
                    if (base.getFood_id() == foodModel.getFood_id()) {
                        baseFoodModel = base;
                        break;
                    }
                }
                if (baseFoodModel == null) return; // не нашли базу — выходим
                // 1. Вычисляем множитель
                FoodModel newEatElm = getFoodModel(foodModel, amount, baseFoodModel);

                if (!searchText.isEmpty()) {
                    foodAdapter.removeEatElm(foodModel);
                    foodAdapter.eatPressedSort(newEatElm);
                    foodAdapter.setFilteredList(searchText);
                }else {
                    foodAdapter.removeEatElm(foodModel);
                    foodAdapter.eatPressedSort(newEatElm);
                    foodAdapter.notifyDataSetChanged();
                }

                dialog.dismiss();
            });
            dialog.show();
            dialog.setOnDismissListener(d -> {
                isAmountDropdownManuallyShown = false;
            });
        }else {
            FoodModel baseFoodModel = null;
            for (FoodModel base : baseEatList) {
                if (base.getFood_id() == foodModel.getFood_id()) {
                    baseFoodModel = new FoodModel(base);
                    break;
                }
            }
            if (baseFoodModel == null) return; // не нашли базу — выходим

            FoodModel newEatElm = new FoodModel(foodModel);
            newEatElm.setProtein(baseFoodModel.getProtein());
            newEatElm.setFat(baseFoodModel.getFat());
            newEatElm.setCarb(baseFoodModel.getCarb());
            newEatElm.setCalories(baseFoodModel.getCalories());
            newEatElm.setAmount(baseFoodModel.getAmount());
            newEatElm.setIsSelected(false);

            if (!searchText.isEmpty()) {
                foodAdapter.removeEatElm(foodModel);
                foodAdapter.unPressedSort(newEatElm);
                foodAdapter.setFilteredList(searchText);
            }else {
                foodAdapter.removeEatElm(foodModel);
                foodAdapter.unPressedSort(newEatElm);
                foodAdapter.notifyDataSetChanged();
            }
            edtSearchText.clearFocus();
        }


    }

    @NonNull
    private static FoodModel getFoodModel(FoodModel foodModel, int amount, FoodModel baseFoodModel) {
        double multiplier = (double) amount / baseFoodModel.getAmount();

        FoodModel newEatElm = new FoodModel(foodModel);
        newEatElm.setProtein(baseFoodModel.getProtein() * multiplier);
        newEatElm.setFat(baseFoodModel.getFat() * multiplier);
        newEatElm.setCarb(baseFoodModel.getCarb() * multiplier);
        newEatElm.setCalories((baseFoodModel.getCalories() * multiplier));
        newEatElm.setAmount(amount);
        newEatElm.setIsSelected(true); // устанавливаем флаг
        return newEatElm;
    }


    private int parseIntOrZero(@NonNull String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
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
        autoCompleteView.setDropDownVerticalOffset(2);

        autoCompleteView.setOnClickListener(v -> {
            boolean isShown = isDropdownShownSupplier.getAsBoolean();
            if (isShown) {
                autoCompleteView.dismissDropDown();
            } else {
                autoCompleteView.showDropDown();
            }
            setDropdownState.accept(!isShown);
        });

        autoCompleteView.setOnItemClickListener((parent, view, position, id) -> {
            setDropdownState.accept(false);
            if (onItemSelected != null) onItemSelected.run();
            String selected = parent.getItemAtPosition(position).toString();
            int amount = parseIntOrZero(selected.split(" ")[0]);
            if (amount > 0) {
                updateMacrosByAmount(foodModel, amount, proteinView, fatView, carbView, circleView, caloriesView);
            }
        });

        autoCompleteView.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                setDropdownState.accept(false);
                autoCompleteView.dismissDropDown();
            }
        });
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
        // Ищем оригинальные значения
        FoodModel baseFoodModel = null;
        for (FoodModel base : baseEatList) {
            if (base.getFood_id() == foodModel.getFood_id()) {
                baseFoodModel = base;
                break;
            }
        }
        if (baseFoodModel == null) return;

        double multiplier = (double) amount / baseFoodModel.getAmount();

        float protein = (float) (baseFoodModel.getProtein() * multiplier);
        float fat = (float) (baseFoodModel.getFat() * multiplier);
        float carb = (float) (baseFoodModel.getCarb() * multiplier);
        float calories = (float) (baseFoodModel.getCalories() * multiplier);

        proteinView.setText(String.format("Белки (%.1f гр)", protein));
        fatView.setText(String.format("Жиры (%.1f гр)", fat));
        carbView.setText(String.format("Углеводы (%.1f гр)", carb));
        caloriesView.setText(String.format("Калории: %.0f ккал", calories));

        circleView.setMacros(protein, fat, carb);
    }

    private void setupSwipeLeftForRecycler(RecyclerView recyclerView, List<?> dataList) {
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                edtSearchText.clearFocus();
                int position = viewHolder.getAdapterPosition();
                FoodModel item = foodAdapter.getList().get(position);
                showDeleteConfirmationDialog(item, position, recyclerView);

            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder,
                                    float dX, float dY, int actionState, boolean isCurrentlyActive) {
                new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                        .addActionIcon(R.drawable.ic_trash_can_foreground)
                        .create()
                        .decorate();

                if (Math.abs(dX) > viewHolder.itemView.getWidth() * 0.3) {
                    dX = viewHolder.itemView.getWidth() * 0.3f * (dX > 0 ? 1 : -1);
                }

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };

        new ItemTouchHelper(simpleCallback).attachToRecyclerView(recyclerView);
    }



    @SuppressLint("SetTextI18n")
    private void showDeleteConfirmationDialog(FoodModel eatToDelete, int position, RecyclerView r) {
        Dialog dialogDeleteEat = new Dialog(requireContext());
        dialogDeleteEat.setContentView(R.layout.confirm_dialog_layout);
        Objects.requireNonNull(dialogDeleteEat.getWindow()).setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        AtomicBoolean isDialogClosedByOutsideClick = new AtomicBoolean(false);
        // Разрешаем закрытие диалога при нажатии вне его
        dialogDeleteEat.setCancelable(true); // Делаем диалог закрываемым
        dialogDeleteEat.setCanceledOnTouchOutside(true); // Закрыть при клике вне диалога


        TextView text1 = dialogDeleteEat.findViewById(R.id.delete_title_D_TV);
        TextView text2 = dialogDeleteEat.findViewById(R.id.delete_message_D_TV);
        Button deleteBtn = dialogDeleteEat.findViewById(R.id.delete_confirm_D_BTN);
        Button chanelBtn = dialogDeleteEat.findViewById(R.id.delete_cancel_D_BTN);

        text1.setText("Удаление еды");
        text2.setText("Вы действивтельно хотите удалить \"" + eatToDelete.getFood_name() + "\"");

        if(dialogDeleteEat.getWindow() != null){
            dialogDeleteEat.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }

        chanelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                foodAdapter.notifyItemChanged(position);
                dialogDeleteEat.dismiss();

            }
        });

        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                baseEatDao.deleteEat(eatToDelete.getFood_id());
                if (!searchText.isEmpty()) {
                    foodAdapter.removeEatElm(eatToDelete);
                    foodAdapter.setFilteredList(searchText);
                }else {
                    foodAdapter.deleteEat(eatToDelete);
                }

                r.requestLayout();
                dialogDeleteEat.dismiss();
            }
        });

        dialogDeleteEat.setOnDismissListener(dialog -> {
            if (isDialogClosedByOutsideClick.get()) {
                // Диалог был закрыт нажатием вне
                foodAdapter.notifyItemChanged(position);
            }
        });

        // Слушаем закрытие по клику вне
        dialogDeleteEat.setOnCancelListener(dialog -> isDialogClosedByOutsideClick.set(true));
        dialogDeleteEat.show();
    }


    @SuppressLint({"SetTextI18n", "DefaultLocale", "ClickableViewAccessibility", "RestrictedApi"})
    private void showAddPresetNameDialog(){
        edtSearchText.clearFocus();
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.amount_dialog);
        Objects.requireNonNull(dialog.getWindow()).setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        dialog.show();

        List<FoodModel> pressedEat = foodAdapter.getPressedEat();

        ConstraintLayout DialogRootLayout = dialog.findViewById(R.id.amount_dialog_root_CL);
        TextView title = dialog.findViewById(R.id.nutrition_title_D_TV);
        TextView amountLabel = dialog.findViewById(R.id.nutrition_hint_D_TV);
        TextView errorTV = dialog.findViewById(R.id.nutritionD_error_preset_name_TV);
        TextInputLayout amountVisibility = dialog.findViewById(R.id.fragment_create_food_amound_food_TIL);
        Button updateBtn = dialog.findViewById(R.id.nutrition_create_D_BTN);
        ImageButton closeBtn = dialog.findViewById(R.id.nutrition_close_D_BTN);
        CardView card = dialog.findViewById(R.id.dialog_CD);
        ImageView image = dialog.findViewById(R.id.nutrition_image_D_IV);



        TextInputEditText textInputNameEditText = dialog.findViewById(R.id.fragment_create_food_name_food_TIET);
        TextInputLayout nameVisibility = dialog.findViewById(R.id.fragment_create_food_name_food_TIL);

        TextView proteinTV = dialog.findViewById(R.id.nutrition_protein_label_D_TV);
        TextView fatTV = dialog.findViewById(R.id.nutrition_fat_label_D_TV);
        TextView carbTV = dialog.findViewById(R.id.nutrition_carb_label_D_TV);
        TextView caloriesTV = dialog.findViewById(R.id.nutrition_calories_label_D_TV);
        NutritionCircleView circle = dialog.findViewById(R.id.nutrition_circle_D_CUSTOM);


        if(currentMode == NutritionMode.EDIT_PRESET){
            title.setText("Изменение пресета");
            amountLabel.setText("Название пресета");
            textInputNameEditText.setText(presetMealNameDao.getMealPresetNameById(mealId));
            updateBtn.setText("Изменить пресет");
            nameVisibility.setVisibility(View.VISIBLE);
            amountVisibility.setVisibility(View.GONE);

        }else {
            title.setText("Создание пресета");
            amountLabel.setText("Название пресета");
            updateBtn.setText("Создать пресет");
            nameVisibility.setVisibility(View.VISIBLE);
            amountVisibility.setVisibility(View.GONE);
            if (pressedEat.isEmpty()){
                card.setVisibility(View.GONE);
                image.setVisibility(View.VISIBLE);
            }

        }


        DialogRootLayout.setOnTouchListener((v, event) -> {
            textInputNameEditText.clearFocus();
            hideKeyboard(textInputNameEditText);
            return false;
        });
        textInputNameEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT) {
                hideKeyboard(v);
                v.clearFocus();
                return true;
            }
            return false;
        });
            double protein = 0;
            double fat = 0;
            double carbs = 0;
            double calories = 0;
            for (FoodModel elm: pressedEat) {
                protein += elm.getProtein();
                fat += elm.getFat();
                carbs += elm.getCarb();
                calories += elm.getCalories();
            }
            circle.setMacros(
                    (float) protein,
                    (float) fat,
                    (float) carbs
            );


            // Обновляем текст с граммами
        proteinTV.setText(String.format("Белки (%.1f гр)", (float) protein));
        fatTV.setText(String.format("Жиры (%.1f гр)", (float) fat));
        carbTV.setText(String.format("Углеводы (%.1f гр)", (float) carbs));
        caloriesTV.setText(String.format("Калории: %.0f ккал", (float) calories));



        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String presetMealName = textInputNameEditText.getText().toString().trim();


                if (presetMealName.isEmpty()) {
                    errorTV.setVisibility(View.VISIBLE);
                    return;
                }
                if (pressedEat.isEmpty()) {
                    Toast.makeText(requireContext(), "Выберите элементы!", Toast.LENGTH_SHORT).show();
                    return;
                }



                if (mealId > 0) {
                    // 1. Обновляем имя пресета
                    presetMealNameDao.updatePresetName(mealId, presetMealName);

                    // 2. Чистим старые связи
                    connectingMealPresetDao.deleteAllForPreset(mealId);

                    // 3. Повторно связываем продукты
                    for (FoodModel eat : pressedEat) {
                        FoodModel existing = presetEatDao.findDuplicateFood(eat);
                        int eatId;

                        if (existing != null) {
                            eatId = existing.getFood_id();
                        } else {
                            presetEatDao.addPresetFood(eat);
                            eatId = presetEatDao.getLastInsertedPresetFoodId();
                        }

                        connectingMealPresetDao.addMealPresetConnection(mealId, eatId);
                    }

                } else {
                    // 1. Добавляем имя нового пресета
                    long mealNameId = presetMealNameDao.addMealPresetName(presetMealName);

                    // 2. Связываем продукты
                    for (FoodModel eat : pressedEat) {
                        FoodModel existing = presetEatDao.findDuplicateFood(eat);
                        int eatId;

                        if (existing != null) {
                            eatId = existing.getFood_id();
                        } else {
                            presetEatDao.addPresetFood(eat);
                            eatId = presetEatDao.getLastInsertedPresetFoodId();
                        }

                        connectingMealPresetDao.addMealPresetConnection((int) mealNameId, eatId);
                    }
                }


                    dialog.dismiss();
                Bundle result = new Bundle();
                result.putBoolean("created", true);
                getParentFragmentManager().setFragmentResult("preset_created", result);
                FragmentManager fragmentManager = getFragmentManager();
                assert fragmentManager != null;
                if (fragmentManager.getBackStackEntryCount() > 0) {
                    fragmentManager.popBackStack();
                }
            }
        });
        dialog.show();
    }

    /**
     * Добавляет только что созданный продукт в список и обновляет UI.
     * @param newFood Новый объект FoodModel.
     */
    private void addNewFoodToAdapter(long newFoodId) {
        // Предполагается, что allEatsList - это список, используемый FoodAdapter
        if (baseEatList != null && foodAdapter != null) {
                FoodModel newFood = baseEatDao.getFoodById(newFoodId);
            baseEatList.add(0, newFood);
                foodAdapter.addSingleFood(newFood);
                // 3. Уведомляем адаптер о вставке
                foodAdapter.notifyItemInserted(0);
            }
    }


    @Override
    public void onPresetMealSelected() {
        Bundle result = new Bundle();
        result.putBoolean("meal_preset_added", true);
        getParentFragmentManager().setFragmentResult("preset_added_result", result);
    }
}