package com.example.workoutapp.Fragments.WorkoutFragments;

import static com.google.android.material.internal.ViewUtils.hideKeyboard;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workoutapp.Adapters.WorkoutAdapters.ExAdapter;
import com.example.workoutapp.Data.WorkoutDao.BASE_EXERCISE_TABLE_DAO;
import com.example.workoutapp.Data.WorkoutDao.CONNECTING_WORKOUT_PRESET_TABLE_DAO;
import com.example.workoutapp.Data.WorkoutDao.WORKOUT_PRESET_NAME_TABLE_DAO;
import com.example.workoutapp.MainActivity;
import com.example.workoutapp.Models.WorkoutModels.BaseExModel;
import com.example.workoutapp.Models.WorkoutModels.ExerciseModel;
import com.example.workoutapp.R;
import com.example.workoutapp.Tools.OnExItemClickListener;
import com.example.workoutapp.Tools.UidGenerator;
import com.example.workoutapp.Tools.WorkoutMode;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class CreatePresetFragment extends Fragment implements OnExItemClickListener {

    private WORKOUT_PRESET_NAME_TABLE_DAO presetNameDao;
    private CONNECTING_WORKOUT_PRESET_TABLE_DAO connectingPresetDao;
    private BASE_EXERCISE_TABLE_DAO baseExerciseDao;

    private List<BaseExModel> exList;
    private ExerciseModel preset;
    private ExAdapter exAdapter;
    private WorkoutMode currentState = WorkoutMode.CREATE_PRESET;
    String searchText = "";

    TextView create_preset_title;
    Button createPresetBtn;

    public CreatePresetFragment() {
    }

    public CreatePresetFragment(ExerciseModel preset, WorkoutMode mode) {
        this.preset = new ExerciseModel(preset); // копия
        this.currentState = mode;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        presetNameDao = new WORKOUT_PRESET_NAME_TABLE_DAO(MainActivity.getAppDataBase());
        connectingPresetDao = new CONNECTING_WORKOUT_PRESET_TABLE_DAO(MainActivity.getAppDataBase());
        baseExerciseDao = new BASE_EXERCISE_TABLE_DAO(MainActivity.getAppDataBase());
    }

    @SuppressLint({"ClickableViewAccessibility", "RestrictedApi"})
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_preset, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.create_preset_fragment_exercises_RV);
        ImageButton back_BTN = view.findViewById(R.id.create_preset_fragment_back_IB);
        TextView create_preset_hint = view.findViewById(R.id.create_preset_fragment_hint_TV);
        createPresetBtn = view.findViewById(R.id.create_preset_fragment_create_Btn);
        create_preset_title = view.findViewById(R.id.create_preset_fragment_title_TV);



        View customSearchViewContainer = view.findViewById(R.id.create_preset_fragment_search_include);
        CardView cardContainer = (CardView) customSearchViewContainer; // <-- Скорректировано: приводим тип к CardView

        // Находим внутренний ConstraintLayout, который имеет ID CARD_search_CL и где меняется фон.
        ConstraintLayout layoutSearch = cardContainer.findViewById(R.id.CARD_search_CL);

        // Остальные дочерние элементы ищутся внутри layoutSearch (CARD_search_CL)
        EditText edtSearchText = layoutSearch.findViewById(R.id.edt_search_text);
        ImageView ivSearchIcon = layoutSearch.findViewById(R.id.iv_search_icon);
        ImageView ivClearText = layoutSearch.findViewById(R.id.iv_clear_text);

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
                recyclerView.requestFocus(); // как в старом onQueryTextSubmit
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
                    exAdapter.exListFiltered.clear();
                    exAdapter.changeFilterText(searchText);
                    exAdapter.notifyDataSetChanged();
                    changeVisibility();
                }else{
                    exAdapter.setFilteredList(searchText);
                    changeVisibility();
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
        view.setOnTouchListener((v, event) -> {
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



        exList = baseExerciseDao.getAllExercises();

        if (currentState == WorkoutMode.EDIT_PRESET && preset != null) {
            List<Long> presetExerciseIds = connectingPresetDao.getBaseExIdsByPresetId(preset.getExercise_id());
            List<BaseExModel> updatedList = new ArrayList<>();

            // 1. Добавляем упражнения из пресета (с выделением)
            for (Long exId : presetExerciseIds) {
                BaseExModel exerciseFromDb = baseExerciseDao.getExerciseById(exId);
                if (exerciseFromDb != null) {
                    BaseExModel copy = new BaseExModel(exerciseFromDb); // глубокое копирование
                    copy.setIsPressed(true); // выделяем
                    updatedList.add(copy);
                }
            }

            // 2. Добавляем остальные упражнения, которых нет в пресете
            for (BaseExModel ex : exList) {
                if (!presetExerciseIds.contains(ex.getBase_ex_id())) {
                    updatedList.add(new BaseExModel(ex)); // глубокое копирование
                }
            }

            exList = new ArrayList<>(updatedList);
        }

        exAdapter = new ExAdapter(this, requireContext(),this, WorkoutMode.SELECTED);
        exAdapter.updateExList(exList);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(exAdapter);

        changeVisibility();

        // текст кнопки в зависимости от режима
        if (currentState == WorkoutMode.EDIT_PRESET) {
            createPresetBtn.setText(getText(R.string.save_change));
            create_preset_hint.setText(getText(R.string.change_preset_hint2));
        }

        createPresetBtn.setOnClickListener(v -> showPresetDialog());

        back_BTN.setOnClickListener(v -> {
            FragmentManager fragmentManager = getFragmentManager();
            assert fragmentManager != null;
            if (fragmentManager.getBackStackEntryCount() > 0) {
                fragmentManager.popBackStack();
            }
        });

        return view;
    }


    @SuppressLint({"RestrictedApi", "ClickableViewAccessibility"})
    private void showPresetDialog() {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.confirm_dialog_preset);
        Objects.requireNonNull(dialog.getWindow()).setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(false);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));

        TextInputEditText namePreset = dialog.findViewById(R.id.preset_name_D_ET);
        TextView errorText = dialog.findViewById(R.id.preset_add_name_dialog_error_TV);
        Button createBtn = dialog.findViewById(R.id.preset_create_D_BTN);
        Button closeBtn = dialog.findViewById(R.id.preset_cancel_D_BTN);
        LinearLayout rootLayout = dialog.findViewById(R.id.confirm_dialog_Root_LL);

        Context context = requireContext();

        // если редактирование — показываем имя и надпись "Сохранить"
        if (currentState == WorkoutMode.EDIT_PRESET && preset != null) {
            namePreset.setText(preset.getExerciseName());
            createBtn.setText(context.getString(R.string.dialog_edit_preset));
        } else {
            createBtn.setText(context.getString(R.string.dialog_create_preset));
        }

        // ==== Закрытие клавиатуры при нажатии на Done ====
        namePreset.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT) {
                hideKeyboard(v);
                namePreset.clearFocus();
                return true;
            }
            return false;
        });

        rootLayout.setOnTouchListener((v, event) -> {
            hideKeyboard(v);
            namePreset.clearFocus();
            return false;
        });

        closeBtn.setOnClickListener(v -> dialog.dismiss());

        createBtn.setOnClickListener(v -> {
            String presetName = namePreset.getText().toString().trim();
            List<BaseExModel> selectedExercises = exAdapter.getSelectedItems();

            if (presetName.isEmpty()) {
                errorText.setVisibility(View.VISIBLE);
                return;
            }

            if (selectedExercises.isEmpty()) {
                Toast.makeText(context, context.getString(R.string.dialog_select_exercise_error), Toast.LENGTH_SHORT).show();
                return;
            }

            String finalUid = null; // UID для синхронизации

            if (currentState == WorkoutMode.CREATE_PRESET) {
                // 1. Генерируем новый UID
                finalUid = UidGenerator.generatePresetWorkoutUid();

                // 2. Сохраняем локально
                long newPresetId = presetNameDao.addPreset(presetName, finalUid);
                for (BaseExModel exercise : selectedExercises) {
                    connectingPresetDao.addPresetExercise(newPresetId, exercise.getBase_ex_id());
                }

            } else if (currentState == WorkoutMode.EDIT_PRESET && preset != null) {
                // 1. Берем существующий UID
                finalUid = preset.getExercise_uid();

                // На случай, если у старого пресета нет UID в объекте, лезем в базу
                if (finalUid == null || finalUid.isEmpty()) {
                    finalUid = presetNameDao.getPresetUidById(preset.getExercise_id());
                }

                // Если и в базе нет (старый пресет), генерируем новый и обновляем базу
                if (finalUid == null || finalUid.isEmpty()) {
                    finalUid = UidGenerator.generatePresetWorkoutUid();
                    // Тут можно добавить метод в DAO для обновления UID, если нужно
                }

                // 2. Обновляем имя и связи локально
                if (!preset.getExerciseName().equals(presetName)) {
                    presetNameDao.updatePresetName(preset.getExercise_id(), presetName);
                }

                connectingPresetDao.deleteExercisesByPresetId(preset.getExercise_id());
                for (BaseExModel exercise : selectedExercises) {
                    connectingPresetDao.addPresetExercise(preset.getExercise_id(), exercise.getBase_ex_id());
                }
            }

            // ================= СИНХРОНИЗАЦИЯ С СЕРВЕРОМ =================
            if (finalUid != null) {
                // Преобразуем выбранные BaseExModel в ExerciseModel для отправки
                List<ExerciseModel> syncList = new ArrayList<>();
                for (BaseExModel be : selectedExercises) {
                    ExerciseModel em = new ExerciseModel();
                    em.setExerciseName(be.getBase_ex_name());
                    em.setExerciseType(be.getBase_ex_type());
                    em.setExerciseBodyType(be.getBase_ex_bodyType());
                    em.setExercise_uid(be.getBase_ex_uid());
                    syncList.add(em);
                }

                // Отправляем в облако (создаст новый или перезапишет старый по UID)
                MainActivity.getSyncManager().syncPresetUpdate(presetName, finalUid, syncList);
            }
            // ============================================================

            final String REQUEST_KEY = "preset_updated_key";
            getParentFragmentManager().setFragmentResult(REQUEST_KEY, new Bundle());

            dialog.dismiss();
            FragmentManager fragmentManager = getFragmentManager();
            if (fragmentManager != null && fragmentManager.getBackStackEntryCount() > 0) {
                fragmentManager.popBackStack();
            }
        });

        dialog.show();
    }



    void changeItemSelection(BaseExModel itemChangeSelection){


        if(itemChangeSelection.getIsPressed()) {
            for (BaseExModel elm:exList) {
                if(elm.getBase_ex_id() == itemChangeSelection.getBase_ex_id()){
                    elm.setIsPressed(false);
                    itemChangeSelection.setIsPressed(false);
                    break;
                }
            }
            if (!searchText.isEmpty()) {
                exAdapter.removeExercise(itemChangeSelection);
                exAdapter.unPressedSort(itemChangeSelection);
                exAdapter.setFilteredList(searchText);
            }else {
                exAdapter.removeExercise(itemChangeSelection);
                exAdapter.unPressedSort(itemChangeSelection);
                exAdapter.notifyDataSetChanged();
            }
        }else{
            for (BaseExModel elm:exList) {
                if(elm.getBase_ex_id() == itemChangeSelection.getBase_ex_id()){
                    elm.setIsPressed(true);
                    itemChangeSelection.setIsPressed(true);
                    break;
                }
            }
            if (!searchText.isEmpty()) {
                exAdapter.removeExercise(itemChangeSelection);
                exAdapter.exercisePressedSort(itemChangeSelection);
                exAdapter.setFilteredList(searchText);
            }else {
                exAdapter.removeExercise(itemChangeSelection);
                exAdapter.exercisePressedSort(itemChangeSelection);
                exAdapter.notifyDataSetChanged();
            }
        }

    }

    void changeVisibility(){
        if(exAdapter.getItemCount() == 0){
            createPresetBtn.setVisibility(View.GONE);
            create_preset_title.setText(R.string.create_preset_title2);
        }
        else {
            createPresetBtn.setVisibility(View.VISIBLE);
            create_preset_title.setText(R.string.create_preset_title);
        }
    }

    @Override
    public void onExItemClick( BaseExModel exModel) {
        changeItemSelection(exModel);
    }
}