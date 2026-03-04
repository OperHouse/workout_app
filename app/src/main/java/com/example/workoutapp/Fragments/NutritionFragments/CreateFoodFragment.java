package com.example.workoutapp.Fragments.NutritionFragments;

import static com.google.android.material.internal.ViewUtils.hideKeyboard;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.workoutapp.Data.NutritionDao.BaseEatDao;
import com.example.workoutapp.MainActivity;
import com.example.workoutapp.Models.NutritionModels.FoodModel;
import com.example.workoutapp.R;
import com.example.workoutapp.Tools.NutritionMode;
import com.example.workoutapp.Tools.UidGenerator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;

public class CreateFoodFragment extends Fragment {

    private NutritionMode mode;
    private BaseEatDao baseEatDao;
    private View loadingOverlay; // Оверлей загрузки
    private static final String TAG = "CreateFoodFragment";

    public CreateFoodFragment() { }

    public CreateFoodFragment(NutritionMode mode) {
        this.mode = mode;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.baseEatDao = new BaseEatDao(MainActivity.getAppDataBase());
    }

    @SuppressLint({"ClickableViewAccessibility", "RestrictedApi"})
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_eat, container, false);

        // Инициализация UI
        loadingOverlay = view.findViewById(R.id.loading_overlay);
        Button createEatBtn = view.findViewById(R.id.fragment_create_food_create_food_Btn);
        ImageButton imageButtonBack = view.findViewById(R.id.fragment_create_food_back_IB);

        TextInputEditText editTextNameEat = view.findViewById(R.id.fragment_create_food_food_name_TIET);
        TextInputEditText editTextProtein = view.findViewById(R.id.fragment_create_food_protein_TIET);
        TextInputEditText editTextFat = view.findViewById(R.id.fragment_create_food_fat_TIET);
        TextInputEditText editTextCarb = view.findViewById(R.id.fragment_create_food_carbs_TIET);
        TextInputEditText editTextCalories = view.findViewById(R.id.fragment_create_food_calories_TIET);

        TextInputLayout editAmountTIL = view.findViewById(R.id.fragment_create_food_amound_food_TIL);
        TextInputEditText editAmountTIET = view.findViewById(R.id.fragment_create_food_amound_food_TIET);
        TextInputLayout dropdownAmountTIL = view.findViewById(R.id.fragment_create_food_amound_food_dropdown_TIL);
        AutoCompleteTextView dropdownAmountTIET = view.findViewById(R.id.fragment_create_food_amound_food_dropdown_TIET);

        AutoCompleteTextView autoCompleteType = view.findViewById(R.id.fragment_create_food_measurement_type_dropdown_TIET);
        View rootLayout = view.findViewById(R.id.root_CL);

        imageButtonBack.setOnClickListener(v -> requireActivity().getOnBackPressedDispatcher().onBackPressed());

        // === Пересчёт калорий ===
        TextWatcher macroTextWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                recalculateCalories(editTextProtein, editTextFat, editTextCarb, editTextCalories);
            }
            @Override public void afterTextChanged(Editable s) {}
        };

        editTextProtein.addTextChangedListener(macroTextWatcher);
        editTextFat.addTextChangedListener(macroTextWatcher);
        editTextCarb.addTextChangedListener(macroTextWatcher);

        // === Адаптеры ===
        ArrayList<String> amountList = new ArrayList<>();
        for (int i = 1; i <= 50; i++) amountList.add(String.valueOf(i));
        String[] measurementTypes = {"шт", "гр", "мл", "кружки"};

        ArrayAdapter<String> amountAdapter = new ArrayAdapter<>(requireContext(), R.layout.spinners_style, amountList);
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(requireContext(), R.layout.spinners_style, measurementTypes);

        dropdownAmountTIET.setAdapter(amountAdapter);
        autoCompleteType.setAdapter(typeAdapter);
        autoCompleteType.setText(measurementTypes[0], false);

        // === Логика переключения ГР/ШТ ===
        Runnable applyVisibilityByType = () -> {
            String selected = autoCompleteType.getText().toString().trim().toLowerCase();
            if (selected.equals("гр") || selected.equals("мл")) {
                editAmountTIL.setVisibility(View.VISIBLE);
                dropdownAmountTIL.setVisibility(View.GONE);
            } else {
                dropdownAmountTIL.setVisibility(View.VISIBLE);
                editAmountTIL.setVisibility(View.GONE);
                if (dropdownAmountTIET.getText().toString().trim().isEmpty()) {
                    dropdownAmountTIET.setText(amountList.get(0), false);
                }
            }
        };
        applyVisibilityByType.run();

        // === Слушатели (Клавиатура/Фокус) ===
        View.OnFocusChangeListener hideKB = (v, hasFocus) -> { if (!hasFocus) hideKeyboard(v); };
        editTextNameEat.setOnFocusChangeListener(hideKB);
        editAmountTIET.setOnFocusChangeListener(hideKB);
        editTextProtein.setOnFocusChangeListener(hideKB);
        editTextFat.setOnFocusChangeListener(hideKB);
        editTextCarb.setOnFocusChangeListener(hideKB);
        editTextCalories.setOnFocusChangeListener(hideKB);

        autoCompleteType.setOnItemClickListener((parent, v, position, id) -> {
            applyVisibilityByType.run();
            autoCompleteType.clearFocus();
        });

        rootLayout.setOnTouchListener((v, event) -> {
            view.clearFocus();
            return false;
        });

        // === Кнопка "Создать еду" ===
        createEatBtn.setOnClickListener(v -> {

            String foodName = editTextNameEat.getText().toString().trim();
            if (foodName.isEmpty()) {
                Toast.makeText(getContext(), "Введите название продукта", Toast.LENGTH_SHORT).show();
                return;
            }

            double p = parseDoubleOrZero(editTextProtein.getText().toString());
            double f = parseDoubleOrZero(editTextFat.getText().toString());
            double c = parseDoubleOrZero(editTextCarb.getText().toString());
            double cal = parseDoubleOrZero(editTextCalories.getText().toString());

            int amount = (editAmountTIL.getVisibility() == View.VISIBLE)
                    ? parseIntOrZero(editAmountTIET.getText().toString())
                    : parseIntOrZero(dropdownAmountTIET.getText().toString());

            String type = autoCompleteType.getText().toString().trim();
            String foodUid = UidGenerator.generateBaseFoodUid();

            FoodModel newEat = new FoodModel(
                    0,
                    foodName,
                    p,
                    f,
                    c,
                    cal,
                    amount,
                    type,
                    foodUid
            );

            try {

                // 1️⃣ Сохраняем ЛОКАЛЬНО (сразу)
                long newFoodId = baseEatDao.addFoodReturnID(newEat);

                if (newFoodId > 0) {

                    // 2️⃣ Отправляем в Firestore (фон)
                    MainActivity.getSyncManager().uploadFood(newEat);

                    // 3️⃣ Сообщаем вызывающему фрагменту
                    Bundle result = new Bundle();
                    result.putLong("new_food_id", newFoodId);
                    getParentFragmentManager().setFragmentResult("new_food_added", result);

                    Toast.makeText(getContext(), "Продукт создан", Toast.LENGTH_SHORT).show();

                    // 4️⃣ Навигация
                    performNavigation();
                }

            } catch (Exception e) {
                Log.e(TAG, "Ошибка создания продукта: " + e.getMessage());
                Toast.makeText(getContext(), "Ошибка создания продукта", Toast.LENGTH_LONG).show();
            }
        });

        return view;
    }

    private void showLoading(boolean isShowing) {
        if (loadingOverlay != null) {
            loadingOverlay.setVisibility(isShowing ? View.VISIBLE : View.GONE);
        }
    }

    private void performNavigation() {
        if (mode == NutritionMode.CREATE_PRESET) {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.frameLayout, new CreateMealPresetFragment())
                    .commit();
        } else {
            getParentFragmentManager().popBackStack();
        }
    }

    private double parseDoubleOrZero(String value) {
        try { return Double.parseDouble(value.trim().replace(',', '.')); }
        catch (NumberFormatException e) { return 0.0; }
    }

    private int parseIntOrZero(String value) {
        try { return Integer.parseInt(value.trim()); }
        catch (NumberFormatException e) { return 0; }
    }

    @SuppressLint("DefaultLocale")
    private void recalculateCalories(EditText pF, EditText fF, EditText cF, EditText calF) {
        double p = parseDoubleOrZero(pF.getText().toString());
        double f = parseDoubleOrZero(fF.getText().toString());
        double c = parseDoubleOrZero(cF.getText().toString());
        double cal = (p * 4) + (f * 9) + (c * 4);
        calF.setText(String.format("%.1f", cal));
    }
}