package com.example.workoutapp.Fragments.NutritionFragments;

import static com.google.android.material.internal.ViewUtils.hideKeyboard;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
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
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;

public class CreateFoodFragment extends Fragment {

    private NutritionMode mode;
    private boolean isAmountDropdownManuallyShown = false;
    private boolean isTypeDropdownManuallyShown = false;
    private BaseEatDao baseEatDao;

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
        View createEatFragment = inflater.inflate(R.layout.fragment_create_eat, container, false);

        Button createEatBtn = createEatFragment.findViewById(R.id.fragment_create_food_create_food_Btn);
        ImageButton imageButtonBack = createEatFragment.findViewById(R.id.fragment_create_food_back_IB);

        // Основные поля
        TextInputEditText editTextNameEat = createEatFragment.findViewById(R.id.fragment_create_food_food_name_TIET);
        TextInputEditText editTextProtein = createEatFragment.findViewById(R.id.fragment_create_food_protein_TIET);
        TextInputEditText editTextFat = createEatFragment.findViewById(R.id.fragment_create_food_fat_TIET);
        TextInputEditText editTextCarb = createEatFragment.findViewById(R.id.fragment_create_food_carbs_TIET);
        TextInputEditText editTextCalories = createEatFragment.findViewById(R.id.fragment_create_food_calories_TIET);

        // Поля количества (оба варианта внутри FrameLayout)
        TextInputLayout editAmountTIL = createEatFragment.findViewById(R.id.fragment_create_food_amound_food_TIL);
        TextInputEditText editAmountTIET = createEatFragment.findViewById(R.id.fragment_create_food_amound_food_TIET);
        TextInputLayout dropdownAmountTIL = createEatFragment.findViewById(R.id.fragment_create_food_amound_food_dropdown_TIL);
        AutoCompleteTextView dropdownAmountTIET = createEatFragment.findViewById(R.id.fragment_create_food_amound_food_dropdown_TIET);

        // Тип измерений
        AutoCompleteTextView autoCompleteType = createEatFragment.findViewById(R.id.fragment_create_food_measurement_type_dropdown_TIET);

        // Контейнер для потери фокуса при нажатии в пустое место
        View rootLayout = createEatFragment.findViewById(R.id.root_CL);

        imageButtonBack.setOnClickListener(v -> requireActivity().getOnBackPressedDispatcher().onBackPressed());

        // === пересчёт калорий ===
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

        // === данные и адаптеры ===
        ArrayList<String> amountList = new ArrayList<>();
        for (int i = 1; i <= 50; i++) amountList.add(String.valueOf(i));
        String[] measurementTypes = {"шт", "гр", "мл", "кружки"};


        ArrayAdapter<String> amountAdapter = new ArrayAdapter<>(
                requireContext(),
                R.layout.spinners_style,
                amountList
        );
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(
                requireContext(),
                R.layout.spinners_style,
                measurementTypes
        );

        dropdownAmountTIET.setAdapter(amountAdapter);
        autoCompleteType.setAdapter(typeAdapter);
        autoCompleteType.setText(measurementTypes[0], false);

        // === логика отображения количества ===
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


        // === слушатели фокуса и клавиатуры ===
        View.OnFocusChangeListener hideKeyboardOnBlur = (v, hasFocus) -> {
            if (!hasFocus) hideKeyboard(v);
        };

        editTextNameEat.setOnFocusChangeListener(hideKeyboardOnBlur);
        editAmountTIET.setOnFocusChangeListener(hideKeyboardOnBlur);
        editTextProtein.setOnFocusChangeListener(hideKeyboardOnBlur);
        editTextFat.setOnFocusChangeListener(hideKeyboardOnBlur);
        editTextCarb.setOnFocusChangeListener(hideKeyboardOnBlur);
        editTextCalories.setOnFocusChangeListener(hideKeyboardOnBlur);

        // закрытие клавиатуры при нажатии Done
        editTextCalories.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT) {
                hideKeyboard(v);
                v.clearFocus();
                return true;
            }
            return false;
        });
        editTextProtein.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT) {
                hideKeyboard(v);
                v.clearFocus();
                return true;
            }
            return false;
        });
        editTextFat.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT) {
                hideKeyboard(v);
                v.clearFocus();
                return true;
            }
            return false;
        });
        editTextCarb.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT) {
                hideKeyboard(v);
                v.clearFocus();
                return true;
            }
            return false;
        });
        editTextNameEat.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT) {
                hideKeyboard(v);
                v.clearFocus();
                return true;
            }
            return false;
        });
        editAmountTIET.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT) {
                hideKeyboard(v);
                v.clearFocus();
                return true;
            }
            return false;
        });



        // потери фокуса при касании в пустое место
        rootLayout.setOnTouchListener((v, event) -> {
            editTextNameEat.clearFocus();
            editAmountTIET.clearFocus();
            editTextProtein.clearFocus();
            editTextFat.clearFocus();
            editTextCarb.clearFocus();
            editTextCalories.clearFocus();
            autoCompleteType.clearFocus();
            dropdownAmountTIET.clearFocus();
            return false;
        });

        // закрытие дропдаунов при выборе
        autoCompleteType.setOnItemClickListener((parent, view, position, id) -> {
            applyVisibilityByType.run(); // <-- ИСПРАВЛЕНИЕ: Вызываем логику переключения полей
            autoCompleteType.clearFocus();
        });
        dropdownAmountTIET.setOnItemClickListener((parent, view, position, id) -> dropdownAmountTIET.clearFocus());

        // обработка касания для открытия/закрытия dropdown
        autoCompleteType.setOnClickListener(v -> {
            if (!autoCompleteType.isPopupShowing()) {
                autoCompleteType.clearFocus();
            }
            else autoCompleteType.showDropDown();
        });

        dropdownAmountTIET.setOnClickListener(v -> {
            if (!dropdownAmountTIET.isPopupShowing()){
                dropdownAmountTIET.clearFocus();
            }
            else
                dropdownAmountTIET.showDropDown();
        });

        // === кнопка "Создать еду" ===
        createEatBtn.setOnClickListener(v -> {
            try {
                String foodName = editTextNameEat.getText().toString().trim();
                double protein = parseDoubleOrZero(editTextProtein.getText().toString());
                double fat = parseDoubleOrZero(editTextFat.getText().toString());
                double carb = parseDoubleOrZero(editTextCarb.getText().toString());
                double calories = parseDoubleOrZero(editTextCalories.getText().toString());

                int amount;
                if (editAmountTIL.getVisibility() == View.VISIBLE) {
                    amount = parseIntOrZero(editAmountTIET.getText().toString());
                } else {
                    amount = parseIntOrZero(dropdownAmountTIET.getText().toString());
                }

                String measurementType = autoCompleteType.getText().toString().trim();

                FoodModel newEat = new FoodModel(
                        0, foodName, protein, fat, carb, calories, amount, measurementType
                );




                try {
                    // 1. Сохраняем и получаем ID (предполагается, что baseEatDao.addEat возвращает long ID)
                    long newFoodId = baseEatDao.addFoodReturnID(newEat);

                    if (newFoodId > 0) {
                        // 2. ОТПРАВЛЯЕМ ID ЧЕРЕЗ FragmentResultListener
                        Bundle result = new Bundle();
                        result.putLong("new_food_id", newFoodId); // 🔥 ИСПОЛЬЗУЕМ putLong!
                        getParentFragmentManager().setFragmentResult("new_food_added", result);
                    }

                    // 3. Возвращаемся ТОЛЬКО ПОСЛЕ ОТПРАВКИ РЕЗУЛЬТАТА
                    if (mode == NutritionMode.CREATE_PRESET) {
                        getParentFragmentManager().beginTransaction()
                                .replace(R.id.frameLayout, new CreateMealPresetFragment())
                                .commit();
                    } else if (mode == NutritionMode.ADD_MEAL) {
                        getParentFragmentManager().popBackStack(); // Возвращает к скрытому CreateMealPresetFragment
                    }
                } catch (Exception e) {
                    Toast.makeText(requireContext(), "Ошибка при создании еды: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(requireContext(), "Ошибка при создании еды: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        return createEatFragment;
    }

    private double parseDoubleOrZero(String value) {
        try {
            return Double.parseDouble(value.trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private int parseIntOrZero(String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    @SuppressLint("DefaultLocale")
    private void recalculateCalories(EditText proteinField, EditText fatField, EditText carbField, EditText caloriesField) {
        double protein = parseDoubleOrZero(proteinField.getText().toString());
        double fat = parseDoubleOrZero(fatField.getText().toString());
        double carb = parseDoubleOrZero(carbField.getText().toString());

        double calories = (protein * 4) + (fat * 9) + (carb * 4);

        caloriesField.setText(String.format("%.1f", calories));
    }


}
