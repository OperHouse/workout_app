package com.example.workoutapp.Fragments.WorkoutFragments;

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
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
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

import com.example.workoutapp.Adapters.WorkoutAdapters.ExAdapter;
import com.example.workoutapp.Adapters.WorkoutAdapters.PresetsAdapter;
import com.example.workoutapp.Data.WorkoutDao.BASE_EXERCISE_TABLE_DAO;
import com.example.workoutapp.Data.WorkoutDao.CONNECTING_WORKOUT_PRESET_TABLE_DAO;
import com.example.workoutapp.Data.WorkoutDao.WORKOUT_PRESET_NAME_TABLE_DAO;
import com.example.workoutapp.MainActivity;
import com.example.workoutapp.Models.WorkoutModels.BaseExModel;
import com.example.workoutapp.Models.WorkoutModels.ExerciseModel;
import com.example.workoutapp.R;
import com.example.workoutapp.Tools.FirestoreSyncManager;
import com.example.workoutapp.Tools.UidGenerator;
import com.example.workoutapp.Tools.WorkoutMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;


public class Selection_Ex_Preset_Fragment extends Fragment {
    private BASE_EXERCISE_TABLE_DAO baseExerciseDao;
    private WORKOUT_PRESET_NAME_TABLE_DAO presetNameDao;
    private CONNECTING_WORKOUT_PRESET_TABLE_DAO connectingPresetDao;
    private List<BaseExModel> exList;
    private List<ExerciseModel> presetsList;
    private ExAdapter exAdapter;
    private PresetsAdapter presetAdapter;
    private RecyclerView exRecycler;
    private RecyclerView presetRecycler;
    private TextView textPreset;
    private TextView textEx;
    private EditText edtSearchText;
    private FirestoreSyncManager syncManager;


    public Selection_Ex_Preset_Fragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Инициализация новых DAO-классов
        this.baseExerciseDao = new BASE_EXERCISE_TABLE_DAO(MainActivity.getAppDataBase());
        this.presetNameDao = new WORKOUT_PRESET_NAME_TABLE_DAO(MainActivity.getAppDataBase());
        this.connectingPresetDao = new CONNECTING_WORKOUT_PRESET_TABLE_DAO(MainActivity.getAppDataBase());
        this.syncManager = new FirestoreSyncManager();
    }

    @SuppressLint({"MissingInflatedId", "ClickableViewAccessibility", "RestrictedApi"})
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View RootViewAddExFragment = inflater.inflate(R.layout.fragment_add_ex, container, false);


        presetRecycler = RootViewAddExFragment.findViewById(R.id.selection_workout_recycler_rv);
        presetsList = presetNameDao.getAllPresets();
        presetAdapter = new PresetsAdapter(this,this::showPresetDetailDialog);

        presetRecycler.setHasFixedSize(true);
        presetRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        presetRecycler.setAdapter(presetAdapter);

        presetAdapter.updatePresetsList(presetsList);

        exRecycler = RootViewAddExFragment.findViewById(R.id.selection_exercise_recycler_rv);
        exList = baseExerciseDao.getAllExercises();
        exAdapter = new ExAdapter(this, requireContext(), WorkoutMode.NOT_SELECTED);
        exAdapter.updateExList(exList);

        exRecycler.setHasFixedSize(true);
        exRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        exRecycler.setAdapter(exAdapter);

        final String REQUEST_KEY = "preset_updated_key";

        // Устанавливаем слушатель результатов фрагмента
        getParentFragmentManager().setFragmentResultListener(REQUEST_KEY, this, (key, bundle) -> {
            // Этот код выполнится, когда CreatePresetFragment отправит результат
            if (key.equals(REQUEST_KEY)) {

                // 1. Перезагружаем список пресетов из базы данных
                presetsList = presetNameDao.getAllPresets();
                presetAdapter.updatePresetsList(presetsList);

                // 2. Уведомляем адаптер
                presetAdapter.notifyDataSetChanged();

                // 3. Обновляем видимость
                presetVisibility(textPreset, getString(R.string.hint_add_workout), presetsList.isEmpty());

                // Опционально: сбрасываем поиск
                if (edtSearchText != null && !edtSearchText.getText().toString().isEmpty()) {
                    edtSearchText.setText("");
                }

            }
        });

        View customSearchViewContainer = RootViewAddExFragment.findViewById(R.id.selection_search_include);
        CardView cardContainer = (CardView) customSearchViewContainer; // <-- Скорректировано: приводим тип к CardView

        // Находим внутренний ConstraintLayout, который имеет ID CARD_search_CL и где меняется фон.
        ConstraintLayout layoutSearch = cardContainer.findViewById(R.id.CARD_search_CL);

        // Остальные дочерние элементы ищутся внутри layoutSearch (CARD_search_CL)
        edtSearchText = layoutSearch.findViewById(R.id.edt_search_text);
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
                presetRecycler.requestFocus(); // как в старом onQueryTextSubmit
                return true;
            }
            return false;
        });

        // Слушатель изменений текста (аналог onQueryTextChange)
        edtSearchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String newText = s.toString();
                filterExerciseList(newText);
                filterPresetsList(newText);

                // Показать/спрятать кнопку очистки
                ivClearText.setVisibility(newText.trim().isEmpty() ? View.GONE : View.VISIBLE);
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
        RootViewAddExFragment.setOnTouchListener((v, event) -> {
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

        // очистка текста
        ivClearText.setOnClickListener(v -> edtSearchText.setText(""));

        Button CreateExBtn = RootViewAddExFragment.findViewById(R.id.selection_add_exercise_btn);
        Button CreatePresetBtn = RootViewAddExFragment.findViewById(R.id.selection_add_workout_btn);
        ImageButton BackBtn = RootViewAddExFragment.findViewById(R.id.selection_back_btn);
        textPreset = RootViewAddExFragment.findViewById(R.id.selection_workouts_hint_tv);
        textEx = RootViewAddExFragment.findViewById(R.id.selection_exercises_hint_tv);

        presetVisibility(textPreset, getString(R.string.hint_add_workout), presetsList.isEmpty());
        exerciseVisibility(textEx, getString(R.string.hint_add_exercise), exList.isEmpty());


        BackBtn.setOnClickListener(v -> {
            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            if (fragmentManager.getBackStackEntryCount() > 0) {
                fragmentManager.popBackStack();
            } else {
                // Если в стеке нет фрагментов, можно закрыть активность.
                requireActivity().finish();
            }
        });

        CreatePresetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    hideKeyboard(edtSearchText);
                    Fragment createWorkoutPresetFragment = new CreatePresetFragment();
                    FragmentManager fragmentManager = getParentFragmentManager(); // или getFragmentManager()
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                    fragmentTransaction
                            .hide(Selection_Ex_Preset_Fragment.this)
                            .add(R.id.frameLayout, createWorkoutPresetFragment, "create_workout_preset") // Добавляем новый фрагмент с тегом
                            .addToBackStack(null)  // Чтобы можно было вернуться назад
                            .commit();
            }
        });

        CreateExBtn.setOnClickListener(v -> {
            showDialogCreateEx();
            edtSearchText.clearFocus();
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

        setupSwipeLeftForRecycler(presetRecycler, presetsList, true);

        setupSwipeLeftForRecycler(exRecycler, exList, false);
        setupSwipeRightForRecycler(exRecycler, exList, false);

        return RootViewAddExFragment;
    }

    // В Selection_Ex_Preset_Fragment.java




    @SuppressLint({"RestrictedApi", "ClickableViewAccessibility"})
    private void showDialogCreateEx() {
        edtSearchText.clearFocus();
        Dialog dialogCreateEx = new Dialog(requireContext());
        dialogCreateEx.setContentView(R.layout.create_ex_dialog_box);
        Objects.requireNonNull(dialogCreateEx.getWindow()).setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialogCreateEx.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        dialogCreateEx.show();

        // ==== Инициализация элементов ====
        ImageButton btnDialogClose = dialogCreateEx.findViewById(R.id.nutrition_close_D_BTN);
        EditText etExerciseName = dialogCreateEx.findViewById(R.id.exercise_name_D_ET);
        TextView errorExerciseName = dialogCreateEx.findViewById(R.id.exercise_name_D_error_TV);
        AutoCompleteTextView actvExerciseType = dialogCreateEx.findViewById(R.id.exerciseType_ACTV);
        AutoCompleteTextView actvBodyType = dialogCreateEx.findViewById(R.id.bodyType_ACTV);
        Button btnCreateExercise = dialogCreateEx.findViewById(R.id.nutrition_create_D_BTN);
        View rootLayout = dialogCreateEx.findViewById(R.id.root_CL);


        // ==== Адаптеры ====
        String[] exerciseTypes = getResources().getStringArray(R.array.exercise_types);
        String[] bodyParts = getResources().getStringArray(R.array.body_parts);

        ArrayAdapter<String> adapterExerciseTypes = new ArrayAdapter<>(
                requireContext(),
                R.layout.spinners_style,
                exerciseTypes
        );

        ArrayAdapter<String> adapterBodyParts = new ArrayAdapter<>(
                requireContext(),
                R.layout.spinners_style,
                bodyParts
        );

        actvExerciseType.setAdapter(adapterExerciseTypes);
        actvBodyType.setAdapter(adapterBodyParts);
        actvExerciseType.setText(exerciseTypes[0], false);
        actvBodyType.setText(bodyParts[0], false);


        //Потеря фокуса элемента при нажатии в любое место диалога(по мимо других кнопок)
        rootLayout.setOnTouchListener((v, event) -> {
            actvExerciseType.clearFocus();
            actvBodyType.clearFocus();
            etExerciseName.clearFocus();
            return false;
        });




        actvExerciseType.setOnItemClickListener((parent, view, position, id) -> {
            actvExerciseType.clearFocus();
        });
        //Сброс фокуса при выборе элемента из списка
        actvBodyType.setOnItemClickListener((parent, view, position, id) -> {
            actvBodyType.clearFocus();
        });
        //Сброс фокуса при повторном нажатии на окно списка
        actvExerciseType.setOnTouchListener((v, event) -> {
            if(actvExerciseType.isPopupShowing())
                actvExerciseType.clearFocus();
            else
                actvExerciseType.showDropDown();
            return false;
        });

        actvBodyType.setOnTouchListener((v, event) -> {
            if(actvBodyType.isPopupShowing())
                actvBodyType.clearFocus();
            else
                actvBodyType.showDropDown();
            return false;
        });
        //Сокрытие клавиатуры при нажатии на другой элемент
        etExerciseName.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                hideKeyboard(v);
            }
        });


        // ==== Закрытие клавиатуры при нажатии на Done ====
        etExerciseName.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT) {
                hideKeyboard(v);
                etExerciseName.clearFocus();
                return true;
            }
            return false;
        });


        // ==== Кнопка закрытия диалога ====
        btnDialogClose.setOnClickListener(v -> dialogCreateEx.dismiss());

        // ==== Кнопка Создать ====
        btnCreateExercise.setOnClickListener(v -> {
            String exName = etExerciseName.getText().toString().trim();
            String exType = actvExerciseType.getText().toString().trim();
            String bodyPart = actvBodyType.getText().toString().trim();

            if (exName.isEmpty()) {
                errorExerciseName.setVisibility(View.VISIBLE);
                return;
            }

            // Создаем модель
            BaseExModel newExercise = new BaseExModel();
            newExercise.setBase_ex_name(exName);
            newExercise.setBase_ex_type(exType);
            newExercise.setBase_ex_bodyType(bodyPart);

            // ГЕНЕРИРУЕМ UID ЗДЕСЬ
            newExercise.setBase_ex_uid(UidGenerator.generateBaseExUid());

            // Сохраняем в локальную БД (убедись, что addExercise учитывает поле UID)
            long id = baseExerciseDao.addExercise(newExercise);
            newExercise.setBase_ex_id((int) id); // Устанавливаем полученный ID от SQLite

            // Добавляем в списки и адаптер
            exList.add(new BaseExModel(newExercise)); // Конструктор копирования должен копировать и UID
            exAdapter.addExercise(newExercise);

            // Сразу отправляем в облако (если нужно автоматическое сохранение)
            syncManager.syncBaseExerciseChange(null, newExercise);

            exerciseVisibility(textEx, getString(R.string.hint_add_exercise), exList.isEmpty());
            exRecycler.requestLayout();

            dialogCreateEx.dismiss();
        });

    }

    @SuppressLint({"ClickableViewAccessibility", "RestrictedApi"})
    private void showDialogChangeEx(BaseExModel exerciseToChange, int position) {
        edtSearchText.clearFocus();
        Dialog dialogCreateEx = new Dialog(requireContext());
        dialogCreateEx.setContentView(R.layout.create_ex_dialog_box);
        Objects.requireNonNull(dialogCreateEx.getWindow()).setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialogCreateEx.show();

        AtomicBoolean isDialogClosedByOutsideClick = new AtomicBoolean(false);

        dialogCreateEx.setCancelable(true);
        dialogCreateEx.setCanceledOnTouchOutside(true);

        if (dialogCreateEx.getWindow() != null) {
            dialogCreateEx.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }

        // ==== Инициализация элементов ====
        ImageButton btnDialogClose = dialogCreateEx.findViewById(R.id.nutrition_close_D_BTN);
        TextView dialogTitle = dialogCreateEx.findViewById(R.id.nutrition_title_D_TV);
        EditText etExerciseName = dialogCreateEx.findViewById(R.id.exercise_name_D_ET);
        AutoCompleteTextView actvExerciseType = dialogCreateEx.findViewById(R.id.exerciseType_ACTV);
        AutoCompleteTextView actvBodyType = dialogCreateEx.findViewById(R.id.bodyType_ACTV);
        Button btnChangeExercise = dialogCreateEx.findViewById(R.id.nutrition_create_D_BTN);
        View rootLayout = dialogCreateEx.findViewById(R.id.root_CL);
        TextView errorExerciseName = dialogCreateEx.findViewById(R.id.exercise_name_D_error_TV);




        // ==== Адаптеры ====
        String[] exerciseTypes = getResources().getStringArray(R.array.exercise_types);
        String[] bodyParts = getResources().getStringArray(R.array.body_parts);

        ArrayAdapter<String> adapterExerciseTypes = new ArrayAdapter<>(
                requireContext(),
                R.layout.spinners_style,
                exerciseTypes
        );

        ArrayAdapter<String> adapterBodyParts = new ArrayAdapter<>(
                requireContext(),
                R.layout.spinners_style,
                bodyParts
        );

        etExerciseName.setText(exerciseToChange.getBase_ex_name());
        dialogTitle.setText(getString(R.string.change_exercise_title));
        btnChangeExercise.setText(getString(R.string.change_exercise_hint));
        actvExerciseType.setText(exerciseToChange.getBase_ex_type(), false);
        actvBodyType.setText(exerciseToChange.getBase_ex_bodyType(), false);

        actvExerciseType.setAdapter(adapterExerciseTypes);
        actvBodyType.setAdapter(adapterBodyParts);

        //Потеря фокуса элемента при нажатии в любое место диалога(по мимо других кнопок)
        rootLayout.setOnTouchListener((v, event) -> {
            actvExerciseType.clearFocus();
            actvBodyType.clearFocus();
            etExerciseName.clearFocus();
            return false;
        });

        actvExerciseType.setOnItemClickListener((parent, view, pos, id) -> {
            actvExerciseType.clearFocus();
        });
        //Сброс фокуса при выборе элемента из списка
        actvBodyType.setOnItemClickListener((parent, view, pos, id) -> {
            actvBodyType.clearFocus();
        });

        //Сброс фокуса при повторном нажатии на окно списка
        actvExerciseType.setOnTouchListener((v, event) -> {
            if(actvExerciseType.isPopupShowing())
                actvExerciseType.clearFocus();
            else
                actvExerciseType.showDropDown();
            return false;
        });

        actvBodyType.setOnTouchListener((v, event) -> {
            if(actvBodyType.isPopupShowing())
                actvBodyType.clearFocus();
            else
                actvBodyType.showDropDown();
            return false;
        });

        //Сокрытие клавиатуры при нажатии на другой элемент
        etExerciseName.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                hideKeyboard(v);
            }
        });

        // ==== Закрытие клавиатуры при нажатии на Done ====
        etExerciseName.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT) {
                hideKeyboard(v);
                etExerciseName.clearFocus();
                return true;
            }
            return false;
        });

        btnDialogClose.setOnClickListener(v -> {
            exAdapter.notifyItemChanged(position);
            dialogCreateEx.dismiss();
        });

        dialogCreateEx.setOnDismissListener(dialog -> {
            if (isDialogClosedByOutsideClick.get()) {
                exAdapter.notifyItemChanged(position);
            }
        });

        btnChangeExercise.setOnClickListener(v -> {
            String exName = etExerciseName.getText().toString().trim();
            String exType = actvExerciseType.getText().toString();
            String bodyType_new = actvBodyType.getText().toString();
            String oldName = exerciseToChange.getBase_ex_name();

            if (exName.isEmpty()) {
                errorExerciseName.setVisibility(View.VISIBLE);
                return;
            }

            exerciseToChange.setBase_ex_name(exName);
            exerciseToChange.setBase_ex_type(exType);
            exerciseToChange.setBase_ex_bodyType(bodyType_new);

            baseExerciseDao.updateExercise(exerciseToChange);
            syncManager.syncBaseExerciseChange(oldName, exerciseToChange);


            int position1 = -1;
            for (int i = 0; i < exList.size(); i++) {
                if (exList.get(i).getBase_ex_id() == exerciseToChange.getBase_ex_id()) {
                    position1 = i;
                    break;
                }
            }

            if (position1 != -1) {
                exList.set(position1, exerciseToChange);
                exAdapter.updateExerciseById(exerciseToChange);
            }else
                exAdapter.notifyItemChanged(position);

            exerciseVisibility(textEx, getString(R.string.hint_add_exercise), exList.isEmpty());
            dialogCreateEx.dismiss();

        });

        dialogCreateEx.setOnCancelListener(dialog -> isDialogClosedByOutsideClick.set(true));
    }

    private void showDeleteConfirmationDialog(BaseExModel exerciseToDelete, int position, RecyclerView r) {
        edtSearchText.clearFocus();
        Dialog dialogCreateEx = new Dialog(requireContext());
        dialogCreateEx.setContentView(R.layout.confirm_dialog_layout);
        Objects.requireNonNull(dialogCreateEx.getWindow()).setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        AtomicBoolean isDialogClosedByOutsideClick = new AtomicBoolean(false);

        dialogCreateEx.setCancelable(true);
        dialogCreateEx.setCanceledOnTouchOutside(true);

        Button deleteBtn = dialogCreateEx.findViewById(R.id.delete_confirm_D_BTN);
        Button chanelBtn = dialogCreateEx.findViewById(R.id.delete_cancel_D_BTN);
        TextView textHint = dialogCreateEx.findViewById(R.id.delete_message_D_TV);
        TextView textTitle = dialogCreateEx.findViewById(R.id.delete_title_D_TV);

        textHint.setText(getString(R.string.delete_exercise_confirmation));
        textTitle.setText(getString(R.string.delete_exercise_title));

        if (dialogCreateEx.getWindow() != null) {
            dialogCreateEx.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }

        chanelBtn.setOnClickListener(v -> {
            exAdapter.notifyItemChanged(position);
            dialogCreateEx.dismiss();

        });

        deleteBtn.setOnClickListener(v -> {
            String nameToDelete = exerciseToDelete.getBase_ex_name();
            baseExerciseDao.deleteExercise(exerciseToDelete.getBase_ex_id());
            syncManager.syncBaseExerciseChange(nameToDelete, null);
            exList.remove(position);
            exAdapter.deleteExerciseById(exerciseToDelete.getBase_ex_id());
            exerciseVisibility(textEx, getString(R.string.hint_add_exercise), exList.isEmpty());
            r.requestLayout();
            dialogCreateEx.dismiss();
        });

        dialogCreateEx.setOnDismissListener(dialog -> {
            if (isDialogClosedByOutsideClick.get()) {
                exAdapter.notifyItemChanged(position);
            }
        });

        dialogCreateEx.setOnCancelListener(dialog -> isDialogClosedByOutsideClick.set(true));
        dialogCreateEx.show();
    }

    private void showDeletePresetConfirmationDialog(ExerciseModel presetToDelete, int position, RecyclerView r) {
        edtSearchText.clearFocus();
        Dialog dialogCreateEx = new Dialog(requireContext());
        dialogCreateEx.setContentView(R.layout.confirm_dialog_layout);
        Objects.requireNonNull(dialogCreateEx.getWindow()).setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        AtomicBoolean isDialogClosedByOutsideClick = new AtomicBoolean(false);

        dialogCreateEx.setCancelable(true);
        dialogCreateEx.setCanceledOnTouchOutside(true);

        Button deleteBtn = dialogCreateEx.findViewById(R.id.delete_confirm_D_BTN);
        Button chanelBtn = dialogCreateEx.findViewById(R.id.delete_cancel_D_BTN);

        TextView text1 = dialogCreateEx.findViewById(R.id.delete_title_D_TV);
        TextView text2 = dialogCreateEx.findViewById(R.id.delete_message_D_TV);

        text1.setText(getString(R.string.delete_preset_title));
        text2.setText(getString(R.string.delete_preset_confirmation));

        if (dialogCreateEx.getWindow() != null) {
            dialogCreateEx.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }

        chanelBtn.setOnClickListener(v -> {
            presetAdapter.notifyItemChanged(position);
            dialogCreateEx.dismiss();
        });

        deleteBtn.setOnClickListener(v -> {
            deletePreset(presetToDelete, position);
            r.requestLayout();
            dialogCreateEx.dismiss();
        });

        dialogCreateEx.setOnDismissListener(dialog -> {
            if (isDialogClosedByOutsideClick.get()) {
                presetAdapter.notifyItemChanged(position);
            }
        });

        dialogCreateEx.setOnCancelListener(dialog -> isDialogClosedByOutsideClick.set(true));

        dialogCreateEx.show();
    }


    private void setupSwipeLeftForRecycler(RecyclerView recyclerView, List<?> dataList, boolean isPreset) {
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                edtSearchText.clearFocus();
                int position = viewHolder.getAdapterPosition();
                if (isPreset) {
                    ExerciseModel item = (ExerciseModel) dataList.get(position);
                    showDeletePresetConfirmationDialog(item, position, recyclerView);
                } else {
                    BaseExModel item = (BaseExModel) dataList.get(position);
                    showDeleteConfirmationDialog(item, position, recyclerView);
                }
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

    private void setupSwipeRightForRecycler(RecyclerView recyclerView, List<?> dataList, boolean isPreset) {
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                edtSearchText.clearFocus();
                int position = viewHolder.getAdapterPosition();
                BaseExModel ex = (BaseExModel) dataList.get(position);
                showDialogChangeEx(ex, position);

            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder,
                                    float dX, float dY, int actionState, boolean isCurrentlyActive) {
                new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                        .addActionIcon(R.drawable.ic_edit_foreground)
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

    private void filterExerciseList(String text) {
        List<BaseExModel> filteredList = new ArrayList<>();
        for (BaseExModel ex : exList) {
            if (ex.getBase_ex_name().toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(ex);

            }
        }
        exAdapter.updateExList(filteredList);

        // Показ сообщения, если список пуст
        if (filteredList.isEmpty()) {
            textEx.setText(getString(R.string.hint_no_exercise_found));
            textEx.setVisibility(View.VISIBLE);
        } else {
            textEx.setVisibility(View.GONE);
        }
    }

    private void filterPresetsList(String text) {
        List<ExerciseModel> filteredList = new ArrayList<>();
        for (ExerciseModel preset : presetsList) {
            if (preset.getExerciseName().toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(preset);
            }
        }
        presetAdapter.updatePresetsList(filteredList);

        // Показ сообщения, если список пуст
        if (filteredList.isEmpty()) {
            textPreset.setText(getString(R.string.hint_no_preset_found));
            textPreset.setVisibility(View.VISIBLE);
        } else {
            textPreset.setVisibility(View.GONE);
        }
    }

    private void deletePreset(ExerciseModel presetToDelete, int position) {
        // Удаляем записи из соединяющей таблицы
        connectingPresetDao.deleteExercisesByPresetId(presetToDelete.getExercise_id());
        // Удаляем сам пресет
        presetNameDao.deletePreset(presetToDelete.getExercise_id());

        // Обновляем список и адаптер
        presetsList.remove(position);
        presetAdapter.updatePresetsList(presetsList);
        presetAdapter.notifyItemRemoved(position);
        presetVisibility(textPreset, getString(R.string.hint_add_workout), presetsList.isEmpty());

    }

    private void presetVisibility(TextView t1, String message, boolean show) {
        t1.setText(message);
        t1.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void exerciseVisibility(TextView t2, String message, boolean show) {
        t2.setText(message);
        t2.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void showPresetDetailDialog(ExerciseModel preset) {
        edtSearchText.clearFocus();
        Dialog dialog = new Dialog(this.requireContext());
        dialog.setContentView(R.layout.dialog_preset_detail);
        Objects.requireNonNull(dialog.getWindow())
                .setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        TextView title = dialog.findViewById(R.id.dialog_preset_detail_title_D_TV);
        ImageView closeBtn = dialog.findViewById(R.id.preset_close_D_BTN);
        Button changePresetBtn = dialog.findViewById(R.id.dialog_preset_detail_change_Btn);
        RecyclerView exRecycler = dialog.findViewById(R.id.dialog_preset_detail_RV);

        // Получаем список упражнений из базы
        List<Long> baseExIds = connectingPresetDao.getBaseExIdsByPresetId(preset.getExercise_id());
        List<BaseExModel> exerciseList = new java.util.ArrayList<>();
        for (Long id : baseExIds) {
            BaseExModel ex = baseExerciseDao.getExerciseById(id);
            if (ex != null) {
                exerciseList.add(new BaseExModel(ex)); // глубокая копия
            }
        }

        // Если упражнений много, увеличиваем высоту
        if (exerciseList.size() > 5) {
            ViewGroup.LayoutParams params = exRecycler.getLayoutParams();
            params.height = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    350,
                    this.requireContext().getResources().getDisplayMetrics() // Заменено fragment.requireContext() на this.requireContext()
            );
            exRecycler.setLayoutParams(params);
        }

        // Настраиваем адаптер упражнений
        // Конструктор ExAdapter требует OnWorkoutPresetLongClickListener. Текущий фрагмент реализует его.
        ExAdapter adapter = new ExAdapter(this, this.requireContext(), WorkoutMode.CREATE_PRESET); // Заменено fragment на this
        adapter.updateExList(exerciseList);

        exRecycler.setHasFixedSize(true);
        exRecycler.setLayoutManager(new LinearLayoutManager(this.requireContext())); // Заменено fragment.requireContext() на this.requireContext()
        exRecycler.setAdapter(adapter);

        // Заголовок
        title.setText(preset.getExerciseName());

        // Кнопка "Изменить"
        changePresetBtn.setOnClickListener(v -> {
            dialog.dismiss();

            Fragment createWorkoutPresetFragment = new CreatePresetFragment(preset, WorkoutMode.EDIT_PRESET);
            FragmentManager fragmentManager = this.getParentFragmentManager(); // Заменено fragment.getParentFragmentManager() на this.getParentFragmentManager()
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

            fragmentTransaction
                    .hide(this) // Заменено fragment на this
                    .add(R.id.frameLayout, createWorkoutPresetFragment, "create_workout_preset") // Добавляем новый фрагмент с тегом
                    .addToBackStack(null)  // Чтобы можно было вернуться назад
                    .commit();
        });

        // Кнопка закрытия
        closeBtn.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }


}