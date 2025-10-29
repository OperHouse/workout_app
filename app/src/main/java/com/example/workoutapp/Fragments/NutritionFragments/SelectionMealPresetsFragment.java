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
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

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
import com.example.workoutapp.Adapters.NutritionAdapters.PresetMealAdapter;
import com.example.workoutapp.Data.NutritionDao.ConnectingMealPresetDao;
import com.example.workoutapp.Data.NutritionDao.PresetEatDao;
import com.example.workoutapp.Data.NutritionDao.PresetMealNameDao;
import com.example.workoutapp.MainActivity;
import com.example.workoutapp.Models.NutritionModels.MealModel;
import com.example.workoutapp.R;
import com.example.workoutapp.Tools.NutritionMode;
import com.example.workoutapp.Tools.OnPresetMealSelectedListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class SelectionMealPresetsFragment extends Fragment implements OnPresetMealSelectedListener {

    private RecyclerView presetRecycler;
    private PresetMealNameDao presetMealNameDao;
    private PresetEatDao presetEatDao;
    private ConnectingMealPresetDao connectingMealPresetDao;
    private PresetMealAdapter presetMealAdapter;
    private EditText edtSearchText;
    private String searchText = "";

    TextView textPressedBtn;

    public SelectionMealPresetsFragment() {
        // Required empty public constructor
    }


    @Override
    public void onPresetMealSelected() {
        Bundle result = new Bundle();
        result.putBoolean("meal_preset_added", true);
        getParentFragmentManager().setFragmentResult("preset_added_result", result);

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.presetMealNameDao = new PresetMealNameDao(MainActivity.getAppDataBase());
        this.presetEatDao = new PresetEatDao(MainActivity.getAppDataBase());
        this.connectingMealPresetDao = new ConnectingMealPresetDao(MainActivity.getAppDataBase());
    }

    @SuppressLint({"ClickableViewAccessibility", "RestrictedApi"})
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View AddMealFragmentView = inflater.inflate(R.layout.fragment_selection_meal_presets, container, false);

        ImageButton backBtn = AddMealFragmentView.findViewById(R.id.imageButtonBack);
        Button createPresetBtn = AddMealFragmentView.findViewById(R.id.select_meal_preset_create_preset_Btn);
        presetRecycler = AddMealFragmentView.findViewById(R.id.select_meal_preset_RV);
        textPressedBtn = AddMealFragmentView.findViewById(R.id.select_meal_preset_title_TV);

        View customSearchViewContainer = AddMealFragmentView.findViewById(R.id.select_meal_preset_search_include);
        CardView cardContainer = (CardView) customSearchViewContainer; // <-- Скорректировано: приводим тип к CardView

        // Находим внутренний ConstraintLayout, который имеет ID CARD_search_CL и где меняется фон.
        ConstraintLayout layoutSearch = cardContainer.findViewById(R.id.CARD_search_CL);

        // Остальные дочерние элементы ищутся внутри layoutSearch (CARD_search_CL)
        edtSearchText = layoutSearch.findViewById(R.id.edt_search_text);
        ImageView ivSearchIcon = layoutSearch.findViewById(R.id.iv_search_icon);
        ImageView ivClearText = layoutSearch.findViewById(R.id.iv_clear_text);

        edtSearchText.setHint(getString(R.string.hint_searchView_food_presets));

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
                presetRecycler.requestFocus(); // как в старом onQueryTextSubmit
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
                    presetMealAdapter.filteredList.clear();
                    presetMealAdapter.changeFilterText(searchText);
                    presetMealAdapter.notifyDataSetChanged();
                }else{
                    presetMealAdapter.setFilteredList(searchText);
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
        AddMealFragmentView.setOnTouchListener((v, event) -> {
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

        List<MealModel> presets = presetMealNameDao.getAllPresetMealModels(
                connectingMealPresetDao,
                presetEatDao
        );

        if(!presets.isEmpty()){
            textPressedBtn.setVisibility(View.GONE);
        }

        presetMealAdapter = new PresetMealAdapter(this, requireContext(), this::showPresetDetailDialog, this);
        presetRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        presetRecycler.setAdapter(presetMealAdapter);
        presetMealAdapter.updatePresetMealsList(presets);


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

        createPresetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard(edtSearchText);
                Fragment selectionFragment = new CreateMealPresetFragment();
                FragmentManager fragmentManager = getParentFragmentManager(); // или getFragmentManager()
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                fragmentTransaction
                        .hide(SelectionMealPresetsFragment.this)
                        .add(R.id.frameLayout, selectionFragment, "create_meal_preset") // Добавляем новый фрагмент с тегом
                        .addToBackStack(null)  // Чтобы можно было вернуться назад
                        .commit();
            }
        });

        setupSwipeLeftForRecycler(presetRecycler, presets);


        getParentFragmentManager().setFragmentResultListener(
                "preset_created",
                getViewLifecycleOwner(),
                (requestKey, bundle) -> {
                    boolean created = bundle.getBoolean("created", false);
                    if (created) {
                        // Если вставка в БД делалась в фоне, лучше перезапрашивать явно
                        loadPresets();
                    }
                }
        );
        return AddMealFragmentView;
    }

    @SuppressLint("RestrictedApi")
    private void showPresetDetailDialog(MealModel preset) {
        Dialog dialog = new Dialog(requireContext());
        edtSearchText.clearFocus();
        dialog.setContentView(R.layout.dialog_preset_detail);
        Objects.requireNonNull(dialog.getWindow()).setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        TextView title = dialog.findViewById(R.id.dialog_preset_detail_title_D_TV);
        ImageView closeBtn = dialog.findViewById(R.id.preset_close_D_BTN);
        Button changePresetBtn = dialog.findViewById(R.id.dialog_preset_detail_change_Btn);
        RecyclerView eatRecycler = dialog.findViewById(R.id.dialog_preset_detail_RV);

        if (preset.getMeal_food_list().size() > 5) {
            ViewGroup.LayoutParams params = eatRecycler.getLayoutParams();
            params.height = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    350,
                    requireContext().getResources().getDisplayMetrics()
            );
            eatRecycler.setLayoutParams(params);
        }

        FoodAdapter foodAdapter = new FoodAdapter(requireContext(), SelectionMealPresetsFragment.this);

        eatRecycler.setHasFixedSize(true);
        eatRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        eatRecycler.setAdapter(foodAdapter);


        foodAdapter.updateEatList(preset.getMeal_food_list());
        title.setText(preset.getMeal_name());

        changePresetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                Fragment selectionFragment = new CreateMealPresetFragment(preset.getMeal_name_id(), NutritionMode.EDIT_PRESET);
                FragmentManager fragmentManager = getParentFragmentManager(); // или getFragmentManager()
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                fragmentTransaction
                        .hide(SelectionMealPresetsFragment.this)
                        .add(R.id.frameLayout, selectionFragment, "create_meal_preset") // Добавляем новый фрагмент с тегом
                        .addToBackStack(null)  // Чтобы можно было вернуться назад
                        .commit();
            }
        });


        title.setText(preset.getMeal_name());
        closeBtn.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
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
                MealModel item = (MealModel) presetMealAdapter.getList().get(position);
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
    private void showDeleteConfirmationDialog(MealModel presetToDelete, int position, RecyclerView r) {
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

        text1.setText("Удаление пресета");
        text2.setText("Вы действивтельно хотите удалить пресет \"" + presetToDelete.getMeal_name() + "\"");

        if(dialogDeleteEat.getWindow() != null){
            dialogDeleteEat.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }

        chanelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presetMealAdapter.notifyItemChanged(position);
                dialogDeleteEat.dismiss();

            }
        });

        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int presetId = presetToDelete.getMeal_name_id();
                List<Integer> eatIds = connectingMealPresetDao.getEatIdsForPreset(presetId);
                connectingMealPresetDao.deleteAllForPreset(presetId);
                presetMealNameDao.deleteMealPresetName(presetId);


                for(Integer id: eatIds){
                    if(!connectingMealPresetDao.doesEatIdExist(id)){
                        presetEatDao.deletePresetFood(id);
                    }
                }

                connectingMealPresetDao.logAllMealPresetConnections();
                presetMealNameDao.logAllMealPresetNames();
                presetEatDao.logAllPresetFood();

                if (!searchText.isEmpty()) {
                    presetMealAdapter.removePresetElm(presetToDelete);
                    presetMealAdapter.setFilteredList(searchText);
                }else {
                    presetMealAdapter.removePresetElm(presetToDelete);
                    presetMealAdapter.notifyDataSetChanged();
                }

                if(presetMealAdapter.getItemCount() == 0){
                    textPressedBtn.setVisibility(View.VISIBLE);
                }
                r.requestLayout();
                dialogDeleteEat.dismiss();
            }
        });

        dialogDeleteEat.setOnDismissListener(dialog -> {
            if (isDialogClosedByOutsideClick.get()) {
                // Диалог был закрыт нажатием вне
                presetMealAdapter.notifyItemChanged(position);
            }
        });

        // Слушаем закрытие по клику вне
        dialogDeleteEat.setOnCancelListener(dialog -> isDialogClosedByOutsideClick.set(true));
        dialogDeleteEat.show();
    }

    private void loadPresets() {
        // Перезапрашиваем из БД
        List<MealModel> updatedPresets = presetMealNameDao.getAllPresetMealModels(
                connectingMealPresetDao,
                presetEatDao
        );

        if (updatedPresets == null) updatedPresets = new ArrayList<>();

        // Показ/скрытие текста "нет пресетов"
        if (!updatedPresets.isEmpty()) {
            textPressedBtn.setVisibility(View.GONE);
        } else {
            textPressedBtn.setVisibility(View.VISIBLE);
        }

        // Обновляем адаптер (см. ниже реализацию метода адаптера)
        presetMealAdapter.updatePresetMealsList(updatedPresets);
    }
}