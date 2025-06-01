package com.example.workoutapp.Adapters;

import android.annotation.SuppressLint;
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
import androidx.recyclerview.widget.RecyclerView;

import com.example.workoutapp.DAO.EatDao;
import com.example.workoutapp.MainActivity;
import com.example.workoutapp.NutritionModels.EatModel;
import com.example.workoutapp.R;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class EatAdapter extends RecyclerView.Adapter<EatAdapter.MyViewHolder>{

    private final Context context;
    private final EatDao eatDao;
    private final Fragment fragment;
    private List<EatModel> eatList;

    private boolean isAmountDropdownManuallyShown = false;

    public EatAdapter(@NonNull Fragment fragment) {
        this.context = fragment.requireContext();
        this.eatDao = new EatDao(MainActivity.getAppDataBase());
        this.fragment = fragment;  // Store the fragment reference
        this.eatList = eatDao.getAllEat();

    }
    @NonNull
    @Override
    public EatAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.eat_elm_card, parent, false);
        return new EatAdapter.MyViewHolder(v);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull EatAdapter.MyViewHolder holder, int position) {
        if (eatList != null && !eatList.isEmpty()) {
            EatModel eat = eatList.get(position);
            holder.nameEat.setText(eat.getEat_name());
            holder.amountEat.setText("(" + eat.getAmount() + "-" + eat.getMeasurement_type() + ")");
            holder.pfcText.setText("Б: " + eat.getProtein() + " / Ж: " + eat.getFat() + " / У: " + eat.getCarb());
            holder.eatCalories.setText(eat.getCalories() + " ккал");


            holder.itemView.setOnClickListener(v -> {
                showEatDialog(context, eat);
            });
        }
    }

    @Override
    public int getItemCount() {
        if (eatList != null) {
            return eatList.size();
        }
        return 0;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateEatList(List<EatModel> eatModelList) {
        this.eatList = eatModelList;


        notifyDataSetChanged();
    }

    private void showEatDialog(Context context, EatModel eatModel) {
        android.app.Dialog dialog = new android.app.Dialog(context);
        dialog.setContentView(R.layout.amount_dialog);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        // View references
        TextView title = dialog.findViewById(R.id.textView1);
        TextView amountLabel = dialog.findViewById(R.id.textView2);
        com.example.workoutapp.NutritionCircleView circle = dialog.findViewById(R.id.NutritionCircleView);
        AutoCompleteTextView autoComplete = dialog.findViewById(R.id.autoCompleteAmount);
        EditText editText = dialog.findViewById(R.id.editTextAmount);
        Button createBtn = dialog.findViewById(R.id.createWorkBtn);
        ImageButton closeBtn = dialog.findViewById(R.id.imageButtonBack1);

        // Новые TextView для макросов
        TextView textViewProtein = dialog.findViewById(R.id.textViewProtein);
        TextView textViewFat = dialog.findViewById(R.id.textViewFat);
        TextView textViewCarb = dialog.findViewById(R.id.textViewCarb);

        // Заголовок и круг
        title.setText("Добавить: " + eatModel.getEat_name());
        circle.setMacros(
                (float) eatModel.getProtein(),
                (float) eatModel.getFat(),
                (float) eatModel.getCarb()
        );

        // Обновляем текст с граммами
        textViewProtein.setText("Белки (" + eatModel.getProtein() + " гр)");
        textViewFat.setText("Жиры (" + eatModel.getFat() + " гр)");
        textViewCarb.setText("Углеводы (" + eatModel.getCarb() + " гр)");

        // Остальной твой код...
        String measurementType = eatModel.getMeasurement_type().toLowerCase();
        amountLabel.setText("Количество пищи в (" + measurementType + ")");

        List<String> options = new ArrayList<>();
        for (int i = 1; i <= 50; i++) {
            options.add(i + " " + measurementType);
        }

        ArrayAdapter<String> adapter = createStyledAdapter(context, options);

        if (measurementType.equals("гр") || measurementType.equals("грамм") ||
                measurementType.equals("мл") || measurementType.equals("миллилитр")) {
            autoComplete.setVisibility(View.GONE);
            editText.setVisibility(View.VISIBLE);
        } else {
            editText.setVisibility(View.GONE);
            autoComplete.setVisibility(View.VISIBLE);

            setupAutoComplete(
                    autoComplete,
                    adapter,
                    null,
                    () -> isAmountDropdownManuallyShown,
                    value -> isAmountDropdownManuallyShown = value,
                    eatModel,
                    textViewProtein,
                    textViewFat,
                    textViewCarb,
                    circle
            );
            autoComplete.setText(options.get(0), false);
        }

        editText.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String input = s.toString().trim();
                int amount = parseIntOrZero(input);
                if (amount > 0) {
                    updateMacrosByAmount(eatModel, amount, textViewProtein, textViewFat, textViewCarb, circle);
                }
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        closeBtn.setOnClickListener(v -> dialog.dismiss());

        createBtn.setOnClickListener(v -> {
            int amount;
            if (editText.getVisibility() == View.VISIBLE) {
                String input = editText.getText().toString().trim();
                if (input.isEmpty()) {
                    Toast.makeText(context, "Введите количество", Toast.LENGTH_SHORT).show();
                    return;
                }
                amount = parseIntOrZero(input);
            } else {
                String selected = autoComplete.getText().toString().trim();
                if (selected.isEmpty()) {
                    Toast.makeText(context, "Выберите количество", Toast.LENGTH_SHORT).show();
                    return;
                }
                try {
                    amount = Integer.parseInt(selected.split(" ")[0]);
                } catch (Exception e) {
                    Toast.makeText(context, "Неверный формат", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            Toast.makeText(context, "Добавлено: " + amount + " " + measurementType, Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
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

    private int parseIntOrZero(String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    private void setupAutoComplete(
            AutoCompleteTextView autoCompleteView,
            ArrayAdapter<String> adapter,
            Runnable onItemSelected,
            BooleanSupplier isDropdownShownSupplier,
            Consumer<Boolean> setDropdownState,
            EatModel eatModel,
            TextView proteinView,
            TextView fatView,
            TextView carbView,
            com.example.workoutapp.NutritionCircleView circleView
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
                updateMacrosByAmount(eatModel, amount, proteinView, fatView, carbView, circleView);
            }
        });

        autoCompleteView.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                setDropdownState.accept(false);
                autoCompleteView.dismissDropDown();
            }
        });
    }

    private void updateMacrosByAmount(
            EatModel eatModel,
            int amount,
            TextView proteinView,
            TextView fatView,
            TextView carbView,
            com.example.workoutapp.NutritionCircleView circleView
    ) {
        double multiplier = (double) amount / eatModel.getAmount();
        float protein = (float) (eatModel.getProtein() * multiplier);
        float fat = (float) (eatModel.getFat() * multiplier);
        float carb = (float) (eatModel.getCarb() * multiplier);

        proteinView.setText(String.format("Белки (%.1f гр)", protein));
        fatView.setText(String.format("Жиры (%.1f гр)", fat));
        carbView.setText(String.format("Углеводы (%.1f гр)", carb));

        circleView.setMacros(protein, fat, carb);
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView nameEat;
        TextView amountEat;
        TextView pfcText;
        TextView eatCalories;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            nameEat = itemView.findViewById(R.id.nameEat);
            amountEat = itemView.findViewById(R.id.amountEat);
            pfcText = itemView.findViewById(R.id.pfcText);
            eatCalories = itemView.findViewById(R.id.eatCalories);
        }
    }
}
