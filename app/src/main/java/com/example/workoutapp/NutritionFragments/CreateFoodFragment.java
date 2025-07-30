package com.example.workoutapp.NutritionFragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.workoutapp.DAO.BaseEatDao;
import com.example.workoutapp.MainActivity;
import com.example.workoutapp.NutritionMode;
import com.example.workoutapp.NutritionModels.FoodModel;
import com.example.workoutapp.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;


public class CreateFoodFragment extends Fragment {


    private NutritionMode mode;

    private boolean isAmountDropdownManuallyShown = false;
    private boolean isTypeDropdownManuallyShown = false;
    private BaseEatDao baseEatDao;

    public CreateFoodFragment() {
        // Required empty public constructor
    }

    public CreateFoodFragment(NutritionMode mode) {
        this.mode = mode;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.baseEatDao = new BaseEatDao(MainActivity.getAppDataBase());

    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View createEatFragment = inflater.inflate(R.layout.fragment_create_eat, container, false);


        Button createEatBtn = createEatFragment.findViewById(R.id.createEatBtn);
        ImageButton imageButtonBack = createEatFragment.findViewById(R.id.imageButtonBack);

        // Получение ссылок
        EditText editTextNameEat = createEatFragment.findViewById(R.id.editTextNameEat);
        EditText editTextProtein = createEatFragment.findViewById(R.id.editTextProtein);
        EditText editTextFat = createEatFragment.findViewById(R.id.editTextFat);
        EditText editTextCarb = createEatFragment.findViewById(R.id.editTextCarb);
        EditText editTextCalories = createEatFragment.findViewById(R.id.editTextCalories);

        AutoCompleteTextView autoCompleteType = createEatFragment.findViewById(R.id.autoCompleteType);
        EditText editTextAmount = createEatFragment.findViewById(R.id.editTextAmount);
        AutoCompleteTextView autoCompleteAmount = createEatFragment.findViewById(R.id.autoCompleteAmount);


        imageButtonBack.setOnClickListener(v -> {
            requireActivity().getOnBackPressedDispatcher().onBackPressed();
        });

        TextWatcher macroTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                recalculateCalories(editTextProtein, editTextFat, editTextCarb, editTextCalories);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        editTextProtein.addTextChangedListener(macroTextWatcher);
        editTextFat.addTextChangedListener(macroTextWatcher);
        editTextCarb.addTextChangedListener(macroTextWatcher);

        // Значения
        ArrayList<String> amountList = new ArrayList<>();
        for (int i = 1; i <= 50; i++) amountList.add(String.valueOf(i));

        String[] measurementTypes = {"шт", "гр", "мл", "кружки"};

        // Адаптеры
        ArrayAdapter<String> amountAdapter = createStyledAdapter(requireContext(), amountList);
        ArrayAdapter<String> typeAdapter = createStyledAdapter(requireContext(), Arrays.asList(measurementTypes));

        // Начальные значения
        autoCompleteAmount.setText(amountList.get(0), false);
        autoCompleteType.setText(measurementTypes[0], false);

        // Настройка выпадающих списков
        setupAutoComplete(
                autoCompleteAmount,
                amountAdapter,
                null,
                () -> isAmountDropdownManuallyShown,
                value -> isAmountDropdownManuallyShown = value
        );

        setupAutoComplete(
                autoCompleteType,
                typeAdapter,
                () -> {
                    String selected = autoCompleteType.getText().toString().toLowerCase();
                    if (selected.equals("гр") || selected.equals("мл")) {
                        autoCompleteAmount.setVisibility(View.GONE);
                        editTextAmount.setVisibility(View.VISIBLE);
                    } else {
                        autoCompleteAmount.setVisibility(View.VISIBLE);
                        editTextAmount.setVisibility(View.GONE);
                    }
                },
                () -> isTypeDropdownManuallyShown,
                value -> isTypeDropdownManuallyShown = value
        );
        createEatBtn.setOnClickListener(v -> {
            try {
                String foodName = editTextNameEat.getText().toString().trim();
                double protein = parseDoubleOrZero(editTextProtein.getText().toString());
                double fat = parseDoubleOrZero(editTextFat.getText().toString());
                double carb = parseDoubleOrZero(editTextCarb.getText().toString());
                double calories = parseDoubleOrZero(editTextCalories.getText().toString());

                int amount;
                if (editTextAmount.getVisibility() == View.VISIBLE) {
                    amount = parseIntOrZero(editTextAmount.getText().toString());
                } else {
                    amount = parseIntOrZero(autoCompleteAmount.getText().toString());
                }

                String measurementType = autoCompleteType.getText().toString().trim();

                FoodModel newEat = new FoodModel(
                        0, // eat_id, можно оставить 0
                        foodName,
                        protein,
                        fat,
                        carb,
                        calories,
                        amount,
                        measurementType
                );
                baseEatDao.addEat(newEat);
                baseEatDao.logAllEat();

                if (mode ==  NutritionMode.CREATE_PRESET) {
                    requireActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.frameLayout, new CreateMealPresetFragment())
                            .commit();
                } else if (mode == NutritionMode.ADD_MEAL) {
                    requireActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.frameLayout, new CreateMealPresetFragment(NutritionMode.ADD_MEAL))
                            .commit();
                } else {
                    requireActivity().getOnBackPressedDispatcher().onBackPressed();
                }

            } catch (Exception e) {
                Toast.makeText(requireContext(), "Ошибка при создании еды: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        return createEatFragment;
    }

    private void setupAutoComplete(
            AutoCompleteTextView autoCompleteView,
            ArrayAdapter<String> adapter,
            Runnable onItemSelected,
            BooleanSupplier isDropdownShownSupplier,
            Consumer<Boolean> setDropdownState
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
        });

        autoCompleteView.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                setDropdownState.accept(false);
                autoCompleteView.dismissDropDown();
            }
        });
    }
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