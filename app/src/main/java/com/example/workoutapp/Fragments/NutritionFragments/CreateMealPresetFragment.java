package com.example.workoutapp.Fragments.NutritionFragments;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workoutapp.Data.NutritionDao.ConnectingMealDao;
import com.example.workoutapp.Data.NutritionDao.MealFoodDao;
import com.example.workoutapp.Adapters.NutritionAdapters.FoodAdapter;
import com.example.workoutapp.Data.NutritionDao.BaseEatDao;
import com.example.workoutapp.Data.NutritionDao.ConnectingMealPresetDao;
import com.example.workoutapp.Data.NutritionDao.PresetEatDao;
import com.example.workoutapp.Data.NutritionDao.PresetMealNameDao;
import com.example.workoutapp.MainActivity;
import com.example.workoutapp.Tools.NutritionMode;
import com.example.workoutapp.Models.NutritionModels.FoodModel;
import com.example.workoutapp.Tools.OnEatItemClickListener;
import com.example.workoutapp.R;
import com.example.workoutapp.Tools.NutritionCircleView;

import org.jetbrains.annotations.Contract;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;


public class CreateMealPresetFragment extends Fragment implements OnEatItemClickListener {
    private RecyclerView eatRecycler;
    private BaseEatDao baseEatDao;
    private PresetMealNameDao presetMealNameDao;
    private PresetEatDao presetEatDao;
    private ConnectingMealPresetDao connectingMealPresetDao;
    private List<FoodModel> eatList;
    private List<FoodModel> baseEatList;
    private FoodAdapter foodAdapter;
    SearchView searchEat;
    private String searchText = "";
    private int presetId = -1;

    private boolean isAmountDropdownManuallyShown = false;


    private NutritionMode currentMode = NutritionMode.CREATE_PRESET;
    private int mealId = -1; // для добавления в конкретный приём пищи

    public CreateMealPresetFragment(int mealId, NutritionMode mode) {
        this.mealId = mealId;
        this.currentMode = mode;
    }

    public CreateMealPresetFragment() {
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

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View addEatToPresetFragment = inflater.inflate(R.layout.fragment_create_meal_preset, container, false);

        ImageButton backBtn = addEatToPresetFragment.findViewById(R.id.imageButtonBack);
        Button createEatBtn = addEatToPresetFragment.findViewById(R.id.createEatBtn);
        Button createPresetBtn = addEatToPresetFragment.findViewById(R.id.nextBtn);
        TextView text = addEatToPresetFragment.findViewById(R.id.textView7);
        TextView text_2 = addEatToPresetFragment.findViewById(R.id.textView);
        searchEat = addEatToPresetFragment.findViewById(R.id.searchEat);


        eatRecycler = addEatToPresetFragment.findViewById(R.id.eatRecyclerView);
        eatList = baseEatDao.getAllEat();

        //Обособленная копия eatList, которая никак не связанна с другими листами
        baseEatList = eatList.stream().map(FoodModel::new).collect(Collectors.toList());
        foodAdapter = new FoodAdapter(requireContext(), this, CreateMealPresetFragment.this);
        if (currentMode == NutritionMode.EDIT_PRESET ){
            List<Integer> connectedEatIds = connectingMealPresetDao.getEatIdsForPreset(presetId);

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
                replaceFragment(new SelectionMealPresetsFragment());
            }
        });

        createEatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replaceFragment(new CreateFoodFragment(currentMode));
            }
        });

        searchEat.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Убираем фокус после отправки
                searchEat.clearFocus();
                eatRecycler.requestFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newTextInp) {
                searchText = newTextInp;
                if(Objects.equals(searchText, "")){
                    foodAdapter.filteredList.clear();
                    foodAdapter.changeFilterText(searchText);
                    foodAdapter.notifyDataSetChanged();
                }else{
                    foodAdapter.setFilteredList(newTextInp);
                }

                return true;
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
                    mealFoodDao.logAllMealFoods();
                    connectingMealDao.logAllConnections();
                    requireActivity().getOnBackPressedDispatcher().onBackPressed();

                }else {
                    showAddPresetNameDialog();
                }


            }
        });



        return addEatToPresetFragment;
    }

    private void replaceFragment(Fragment newFragment) {
        // Получаем менеджер фрагментов
        FragmentManager fragmentManager = getFragmentManager();
        if (fragmentManager != null) {
            // Начинаем транзакцию фрагментов
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            // Заменяем текущий фрагмент на новый
            fragmentTransaction.replace(R.id.frameLayout, newFragment);
            // Добавляем транзакцию в бэкстек (если нужно)
            fragmentTransaction.addToBackStack(null);
            // Выполняем транзакцию
            fragmentTransaction.commit();
        }
    }

    @Override
    public void onEatItemClick(Context context, FoodModel foodModel) {
        showFoodDialog(context, foodModel);
    }
    @SuppressLint("SetTextI18n")
    public void showFoodDialog(Context context, FoodModel foodModel) {
        if(!foodModel.getIsSelected()) {
            android.app.Dialog dialog = new android.app.Dialog(context);
            dialog.setContentView(R.layout.amount_dialog);
            Objects.requireNonNull(dialog.getWindow()).setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

            // View references
            TextView title = dialog.findViewById(R.id.textView1);
            TextView amountLabel = dialog.findViewById(R.id.textView2);
            NutritionCircleView circle = dialog.findViewById(R.id.NutritionCircleView);
            AutoCompleteTextView autoComplete = dialog.findViewById(R.id.autoCompleteAmount);
            EditText editText = dialog.findViewById(R.id.editTextAmount);
            Button createBtn = dialog.findViewById(R.id.createWorkBtn);
            ImageButton closeBtn = dialog.findViewById(R.id.imageButtonBack1);

            // Новые TextView для макросов
            TextView textViewProtein = dialog.findViewById(R.id.textViewProtein);
            TextView textViewFat = dialog.findViewById(R.id.textViewFat);
            TextView textViewCarb = dialog.findViewById(R.id.textViewCarb);
            TextView textViewCalories = dialog.findViewById(R.id.textViewCalories);

            // Заголовок и круг
            title.setText("Добавить: " + foodModel.getFood_name());
            circle.setMacros(
                    (float) foodModel.getProtein(),
                    (float) foodModel.getFat(),
                    (float) foodModel.getCarb()
            );



            // Обновляем текст с граммами
            textViewProtein.setText("Белки (" + foodModel.getProtein() + " гр)");
            textViewFat.setText("Жиры (" + foodModel.getFat() + " гр)");
            textViewCarb.setText("Углеводы (" + foodModel.getCarb() + " гр)");
            textViewCalories.setText("Калории: " + foodModel.getCalories() + " ккал");


            String measurementType = foodModel.getMeasurement_type().toLowerCase();
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
                        foodModel,
                        textViewProtein,
                        textViewFat,
                        textViewCarb,
                        textViewCalories,
                        circle
                );
                autoComplete.setText(options.get(0), false);
            }

            editText.addTextChangedListener(new android.text.TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    String input = s.toString().trim();
                    int amount = parseIntOrZero(input);
                    if (amount > 0) {
                        updateMacrosByAmount(foodModel, amount, textViewProtein, textViewFat, textViewCarb, circle, textViewCalories);

                    }
                }

                @Override
                public void afterTextChanged(android.text.Editable s) {
                }
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
            searchEat.clearFocus();
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
            searchEat.clearFocus();
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
                searchEat.clearFocus();
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


        TextView text1 = dialogDeleteEat.findViewById(R.id.text1);
        TextView text2 = dialogDeleteEat.findViewById(R.id.text2);
        Button deleteBtn = dialogDeleteEat.findViewById(R.id.btnDelete);
        Button chanelBtn = dialogDeleteEat.findViewById(R.id.btnChanel);

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


    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    private void showAddPresetNameDialog(){
        Dialog dialogAddMealPresetName = new Dialog(requireContext());
        dialogAddMealPresetName.setContentView(R.layout.add_meal_name_dialog);
        Objects.requireNonNull(dialogAddMealPresetName.getWindow()).setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialogAddMealPresetName.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        dialogAddMealPresetName.show();

        List<FoodModel> pressedEat = foodAdapter.getPressedEat();

        ImageButton backBtn = dialogAddMealPresetName.findViewById(R.id.imageButtonBack1);
        EditText nameMealPreset = dialogAddMealPresetName.findViewById(R.id.editText);
        TextView text1 = dialogAddMealPresetName.findViewById(R.id.textView1);
        TextView text2 = dialogAddMealPresetName.findViewById(R.id.textView2);
        TextView textViewProtein = dialogAddMealPresetName.findViewById(R.id.textViewProtein);
        TextView textViewFat = dialogAddMealPresetName.findViewById(R.id.textViewFat);
        TextView textViewCarb = dialogAddMealPresetName.findViewById(R.id.textViewCarb);
        TextView textViewCalories = dialogAddMealPresetName.findViewById(R.id.textViewCalories);
        Button createMealPresetBtn = dialogAddMealPresetName.findViewById(R.id.createWorkBtn);
        NutritionCircleView circle = dialogAddMealPresetName.findViewById(R.id.NutritionCircleView);
        ConstraintLayout detailsLayout = dialogAddMealPresetName.findViewById(R.id.detailsLayout);

        ImageView image = dialogAddMealPresetName.findViewById(R.id.imageView);


        if (pressedEat == null || pressedEat.isEmpty()) {
            detailsLayout.setVisibility(View.GONE);
        } else {
            detailsLayout.setVisibility(View.VISIBLE);
            image.setVisibility(View.GONE);
            double protein = 0;
            double fat = 0;
            double carbs = 0;
            for (FoodModel elm: pressedEat) {
                protein += elm.getProtein();
                fat += elm.getFat();
                carbs += elm.getCarb();
            }
            double calories = protein * 4 + carbs * 4 + fat * 9;
            circle.setMacros(
                    (float) protein,
                    (float) fat,
                    (float) carbs
            );


            // Обновляем текст с граммами
            textViewProtein.setText(String.format("Белки (%.1f гр)", (float) protein));
            textViewFat.setText(String.format("Жиры (%.1f гр)", (float) fat));
            textViewCarb.setText(String.format("Углеводы (%.1f гр)", (float) carbs));
            textViewCalories.setText(String.format("Калории: %.0f ккал", (float) calories));

        }
        if(currentMode == NutritionMode.EDIT_PRESET){
            text1.setText("Изменение пресета");
            text2.setText("Название пресета");
            nameMealPreset.setText(presetMealNameDao.getMealPresetNameById(presetId));
            createMealPresetBtn.setText("Изменить пресет");

        }else {
            text1.setText("Создание пресета");
            text2.setText("Название пресета");
            createMealPresetBtn.setText("Создать пресет");
        }



        AtomicBoolean isDialogClosedByOutsideClick = new AtomicBoolean(false);

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogAddMealPresetName.dismiss();
            }
        });

        createMealPresetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String presetMealName = nameMealPreset.getText().toString().trim();


                if (pressedEat.isEmpty()) {
                    Toast.makeText(requireContext(), "Выберите элементы!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (presetMealName.isEmpty()) {
                    nameMealPreset.setError("Укажите название пресета!");
                    return;
                }


                if (presetId > 0) {
                    // 1. Обновляем имя пресета
                    presetMealNameDao.updatePresetName(presetId, presetMealName);

                    // 2. Чистим старые связи
                    connectingMealPresetDao.deleteAllForPreset(presetId);

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

                        connectingMealPresetDao.addMealPresetConnection(presetId, eatId);
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


                    // Логи
                    presetEatDao.logAllPresetFood();
                    presetMealNameDao.logAllMealPresetNames();
                    connectingMealPresetDao.logAllMealPresetConnections();

                    dialogAddMealPresetName.dismiss();
                    requireActivity().getOnBackPressedDispatcher().onBackPressed();
            }
        });



        dialogAddMealPresetName.setOnCancelListener(dialog -> isDialogClosedByOutsideClick.set(true));
        dialogAddMealPresetName.show();
    }

    private void clearSearchFocusAndHideKeyboard() {
        if (searchEat != null) {
            searchEat.clearFocus(); // убрать фокус

            // Прячем клавиатуру
            InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(searchEat.getWindowToken(), 0);
            }

            // Удаляем каретку (трюк через queryHint)
            String hint = searchEat.getQueryHint() != null ? searchEat.getQueryHint().toString() : "";
            searchEat.setQueryHint(""); // временно убираем
            searchEat.setQueryHint(hint); // возвращаем обратно
        }
    }

    private float roundStrictlyToOneDecimal(double value) {
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(1, RoundingMode.HALF_UP); // округляем до 1 знака
        return bd.floatValue();
    }
}