package com.example.workoutapp.Fragments.WorkoutFragments;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
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
import com.example.workoutapp.Tools.WorkoutMode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

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
    SearchView searchView;
    private boolean isExercisesTypeDropdownManuallyShown = false;
    private boolean isBodyPartDropdownManuallyShown = false;

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
    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View RootViewAddExFragment = inflater.inflate(R.layout.fragment_add_ex, container, false);


        presetRecycler = RootViewAddExFragment.findViewById(R.id.presetRecycler);
        presetsList = presetNameDao.getAllPresets();
        presetAdapter = new PresetsAdapter(this);

        presetRecycler.setHasFixedSize(true);
        presetRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        presetRecycler.setAdapter(presetAdapter);

        presetAdapter.updatePresetsList(presetsList);

        exRecycler = RootViewAddExFragment.findViewById(R.id.ExercisePresets_RV);
        exList = baseExerciseDao.getAllExercises();
        exAdapter = new ExAdapter(requireContext(), exList, WorkoutMode.NOT_SELECTED);

        exRecycler.setHasFixedSize(true);
        exRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        exRecycler.setAdapter(exAdapter);

        searchView = RootViewAddExFragment.findViewById(R.id.searchExercise2);
        searchView.clearFocus();

        Button CreateExBtn = RootViewAddExFragment.findViewById(R.id.ExerciseBtn);
        Button CreatePresetBtn = RootViewAddExFragment.findViewById(R.id.presetBtn);
        ImageButton BackBtn = RootViewAddExFragment.findViewById(R.id.imageButtonBack);
        textPreset = RootViewAddExFragment.findViewById(R.id.textView7);
        textEx = RootViewAddExFragment.findViewById(R.id.textView8);

        presetVisibility(textPreset, "Жми '+ Добавить тренировку' чтобы начать", presetsList.isEmpty());
        exerciseVisibility(textEx, "Жми '+ Добавить упражнение' чтобы начать", exList.isEmpty());


        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchView.clearFocus();
                presetRecycler.requestFocus();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterExerciseList(newText);
                filterPresetsList(newText);
                return true;
            }
        });

        BackBtn.setOnClickListener(v -> {
            FragmentManager fragmentManager = getFragmentManager();
            assert fragmentManager != null;
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.frameLayout, new WorkoutFragment());
            fragmentTransaction.commit();
        });

        CreatePresetBtn.setOnClickListener(v -> {
            FragmentManager fragmentManager = getFragmentManager();
            assert fragmentManager != null;
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.frameLayout, new CreatePresetFragment());
            fragmentTransaction.commit();
        });

        CreateExBtn.setOnClickListener(v -> {
            showDialogCreateEx();
            searchView.clearFocus();
        });

        setupSwipeLeftForRecycler(presetRecycler, presetsList, true);
        setupSwipeRightForRecycler(presetRecycler, presetsList, true);

        setupSwipeLeftForRecycler(exRecycler, exList, false);
        setupSwipeRightForRecycler(exRecycler, exList, false);

        return RootViewAddExFragment;
    }


    private void showDialogCreateEx() {
        Dialog dialogCreateEx = new Dialog(requireContext());
        dialogCreateEx.setContentView(R.layout.create_ex_dialog_box);
        Objects.requireNonNull(dialogCreateEx.getWindow()).setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialogCreateEx.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        dialogCreateEx.show();

        ImageButton backBtn = dialogCreateEx.findViewById(R.id.imageButtonBack1);
        EditText nameEx = dialogCreateEx.findViewById(R.id.editText);
        AutoCompleteTextView spinnerTypeEx = dialogCreateEx.findViewById(R.id.exerciseType_ACTV);
        AutoCompleteTextView spinnerBodyType = dialogCreateEx.findViewById(R.id.bodyType_ACTV);
        Button createExecise_Btn = dialogCreateEx.findViewById(R.id.createWorkBtn);

        String[] ExercisesType = {"Гантели", "Гриф", "Вес тела", "Кроссовер", "Тренажер", "Время", "Другое"};
        String[] BodyPart = {"Грудь", "Плечи", "Ноги", "Руки", "Тренажер", "Спина", "Пресс", "Кардио", "Другое"};

        ArrayAdapter<String> ExercisesTypeAdapter = createStyledAdapter(requireContext(), Arrays.asList(ExercisesType));
        ArrayAdapter<String> BodyPartAdapter = createStyledAdapter(requireContext(), Arrays.asList(BodyPart));

        spinnerTypeEx.setText(ExercisesType[0], false);
        spinnerBodyType.setText(BodyPart[0], false);
        setupAutoComplete(
                spinnerTypeEx,
                ExercisesTypeAdapter,
                () -> {
                    String selected = spinnerTypeEx.getText().toString().toLowerCase();
                },
                () -> isExercisesTypeDropdownManuallyShown,
                value -> isExercisesTypeDropdownManuallyShown = value
        );

        setupAutoComplete(
                spinnerBodyType,
                BodyPartAdapter,
                () -> {
                    String selected = spinnerBodyType.getText().toString().toLowerCase();
                },
                () -> isBodyPartDropdownManuallyShown,
                value -> isBodyPartDropdownManuallyShown = value
        );

        backBtn.setOnClickListener(v -> {
            dialogCreateEx.dismiss();
            isExercisesTypeDropdownManuallyShown = false;
            isBodyPartDropdownManuallyShown = false;
        });

        createExecise_Btn.setOnClickListener(v -> {
            String exName = nameEx.getText().toString().trim();
            String exType = spinnerTypeEx.getText().toString().trim();
            String bodyType = spinnerBodyType.getText().toString().trim();

            if (exName.isEmpty()) {
                nameEx.setError("Пожалуйста, введите название упражнения");
                return;
            }

            BaseExModel newExercise = new BaseExModel();
            newExercise.setExName(exName);
            newExercise.setExType(exType);
            newExercise.setBodyType(bodyType);

            baseExerciseDao.addExercise(newExercise);

            exList = new ArrayList<>();
            exList.addAll(baseExerciseDao.getAllExercises());
            exAdapter.updateExList(exList);
            exerciseVisibility(textEx, "Жми '+ Добавить упражнение' чтобы начать", exList.isEmpty());


            dialogCreateEx.dismiss();
            isExercisesTypeDropdownManuallyShown = false;
            isBodyPartDropdownManuallyShown = false;

            Toast.makeText(requireContext(), "Упражнение добавлено!", Toast.LENGTH_SHORT).show();
        });

        spinnerTypeEx.setOnDismissListener(() -> isExercisesTypeDropdownManuallyShown = false);
        spinnerBodyType.setOnDismissListener(() -> isBodyPartDropdownManuallyShown = false);

        spinnerTypeEx.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) isExercisesTypeDropdownManuallyShown = false;
        });

        spinnerBodyType.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) isBodyPartDropdownManuallyShown = false;
        });
    }
    private void showDialogChangeEx(BaseExModel exerciseToChange, int position){

        Dialog dialogCreateEx = new Dialog(requireContext());
        dialogCreateEx.setContentView(R.layout.create_ex_dialog_box);
        Objects.requireNonNull(dialogCreateEx.getWindow()).setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialogCreateEx.show();

        AtomicBoolean isDialogClosedByOutsideClick = new AtomicBoolean(false);

        dialogCreateEx.setCancelable(true);
        dialogCreateEx.setCanceledOnTouchOutside(true);

        dialogCreateEx.getWindow().setBackgroundDrawable(new ColorDrawable(0));

        String oldName = exerciseToChange.getExName();
        ImageButton backBtn = dialogCreateEx.findViewById(R.id.imageButtonBack1);
        EditText nameEx = dialogCreateEx.findViewById(R.id.editText);
        AutoCompleteTextView spinnerTypeEx = dialogCreateEx.findViewById(R.id.exerciseType_ACTV);
        AutoCompleteTextView spinnerBodyType = dialogCreateEx.findViewById(R.id.bodyType_ACTV);
        Button changeExercise_Btn = dialogCreateEx.findViewById(R.id.createWorkBtn);
        TextView text = dialogCreateEx.findViewById(R.id.textView1);

        text.setText("Изменение упражнения");
        changeExercise_Btn.setText("Изменить упражнение");
        nameEx.setText(exerciseToChange.getExName());

        String[] ExercisesType = {"Гантели", "Гриф", "Вес тела", "Кроссовер", "Тренажер", "Время", "Другое"};
        String[] BodyPart = {"Грудь", "Плечи", "Ноги", "Руки", "Тренажер", "Спина", "Пресс", "Кардио", "Другое"};

        ArrayAdapter<String> ExercisesTypeAdapter = createStyledAdapter(requireContext(), Arrays.asList(ExercisesType));
        ArrayAdapter<String> BodyPartAdapter = createStyledAdapter(requireContext(), Arrays.asList(BodyPart));

        spinnerTypeEx.setText(ExercisesType[0], false);
        spinnerBodyType.setText(BodyPart[0], false);
        setupAutoComplete(
                spinnerTypeEx,
                ExercisesTypeAdapter,
                () -> {
                    String selected = spinnerTypeEx.getText().toString().toLowerCase();
                },
                () -> isExercisesTypeDropdownManuallyShown,
                value -> isExercisesTypeDropdownManuallyShown = value
        );

        setupAutoComplete(
                spinnerBodyType,
                BodyPartAdapter,
                () -> {
                    String selected = spinnerTypeEx.getText().toString().toLowerCase();
                },
                () -> isBodyPartDropdownManuallyShown,
                value -> isBodyPartDropdownManuallyShown = value
        );


        backBtn.setOnClickListener(v -> {
            exAdapter.notifyItemChanged(position);
            dialogCreateEx.dismiss();
            isExercisesTypeDropdownManuallyShown = false;
            isBodyPartDropdownManuallyShown = false;
        });

        dialogCreateEx.setOnDismissListener(dialog -> {
            if (isDialogClosedByOutsideClick.get()) {
                exAdapter.notifyItemChanged(position);
            }
        });

        spinnerTypeEx.setOnDismissListener(() -> isExercisesTypeDropdownManuallyShown = false);
        spinnerBodyType.setOnDismissListener(() -> isBodyPartDropdownManuallyShown = false);

        spinnerTypeEx.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) isExercisesTypeDropdownManuallyShown = false;
        });

        spinnerBodyType.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) isBodyPartDropdownManuallyShown = false;
        });

        changeExercise_Btn.setOnClickListener(v -> {
            String exName = nameEx.getText().toString().trim();
            String exType = spinnerTypeEx.getText().toString();
            String bodyType_new = spinnerBodyType.getText().toString();

            if (exName.isEmpty()) {
                nameEx.setError("Пожалуйста, введите название упражнения");
                return;
            }

            BaseExModel newExercise = new BaseExModel();
            newExercise.setExName(exName);
            newExercise.setExType(exType);
            newExercise.setBodyType(bodyType_new);

            baseExerciseDao.updateExercise(oldName, newExercise);

            exList.clear();
            exList.addAll(baseExerciseDao.getAllExercises());
            exAdapter.updateExList(exList);

            dialogCreateEx.dismiss();
            isExercisesTypeDropdownManuallyShown = false;
            isBodyPartDropdownManuallyShown = false;

            Toast.makeText(requireContext(), "Упражнение изменено!", Toast.LENGTH_SHORT).show();
        });

        dialogCreateEx.setOnCancelListener(dialog -> isDialogClosedByOutsideClick.set(true));
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

    private void showDeleteConfirmationDialog(BaseExModel exerciseToDelete, int position, RecyclerView r) {
        Dialog dialogCreateEx = new Dialog(requireContext());
        dialogCreateEx.setContentView(R.layout.confirm_dialog_layout);
        Objects.requireNonNull(dialogCreateEx.getWindow()).setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        AtomicBoolean isDialogClosedByOutsideClick = new AtomicBoolean(false);

        dialogCreateEx.setCancelable(true);
        dialogCreateEx.setCanceledOnTouchOutside(true);

        Button deleteBtn = dialogCreateEx.findViewById(R.id.btnDelete);
        Button chanelBtn = dialogCreateEx.findViewById(R.id.btnChanel);

        if(dialogCreateEx.getWindow() != null){
            dialogCreateEx.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }

        chanelBtn.setOnClickListener(v -> {
            exAdapter.notifyItemChanged(position);
            dialogCreateEx.dismiss();

        });

        deleteBtn.setOnClickListener(v -> {
            deleteExercise(exerciseToDelete, position);
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
    private void showDeleteConfirmationDialog(ExerciseModel presetToDelete, int position, RecyclerView r) {
        Dialog dialogCreateEx = new Dialog(requireContext());
        dialogCreateEx.setContentView(R.layout.confirm_dialog_layout);
        Objects.requireNonNull(dialogCreateEx.getWindow()).setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        AtomicBoolean isDialogClosedByOutsideClick = new AtomicBoolean(false);

        dialogCreateEx.setCancelable(true);
        dialogCreateEx.setCanceledOnTouchOutside(true);

        Button deleteBtn = dialogCreateEx.findViewById(R.id.btnDelete);
        Button chanelBtn = dialogCreateEx.findViewById(R.id.btnChanel);

        TextView text1 = dialogCreateEx.findViewById(R.id.textView);
        TextView text2 = dialogCreateEx.findViewById(R.id.text1);

        text1.setText("Удаление Пресета");
        text2.setText("Вы действительно хотите удалить Пресет?");

        if(dialogCreateEx.getWindow() != null){
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
                searchView.clearFocus();
                int position = viewHolder.getAdapterPosition();
                if (isPreset) {
                    ExerciseModel item = (ExerciseModel) dataList.get(position);
                    showDeleteConfirmationDialog(item, position, recyclerView);
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
                searchView.clearFocus();
                int position = viewHolder.getAdapterPosition();
                if (isPreset) {
                    ExerciseModel preset = (ExerciseModel) dataList.get(position);
                    Bundle bundle = new Bundle();
                    // Переиспользуем CreatePresetFragment для изменения
                    bundle.putSerializable("preset", (Serializable) preset);
                    CreatePresetFragment createPresetFragment = new CreatePresetFragment();
                    createPresetFragment.setArguments(bundle);

                    FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
                    transaction.replace(R.id.frameLayout, createPresetFragment);
                    transaction.commit();
                } else {
                    BaseExModel ex = (BaseExModel) dataList.get(position);
                    showDialogChangeEx(ex, position);
                }
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
        exAdapter.getFilter().filter(text);
        if (exAdapter.getItemCount() == 0) {
            exerciseVisibility(
                    textEx,
                    "Упражнения с таким названием нет. \nЖми '+ Добавить упражнение', чтобы создать его!",
                    true
            );
        } else {
            textEx.setVisibility(View.GONE);
        }
    }

    private void filterPresetsList(String text) {
        //presetAdapter.getFilter().filter(text);

        if (presetAdapter.getItemCount() == 0) {
            presetVisibility(
                    textPreset,
                    "Тренировки с таким упражнением нет. \nЖми '+ Добавить тренировку', чтобы создать её!",
                    true
            );
        } else {
            textPreset.setVisibility(View.GONE);
        }
    }


    private void deleteExercise(BaseExModel exerciseToDelete, int position) {
        // Удаляем упражнение из базы данных
        baseExerciseDao.deleteExercise(exerciseToDelete.getExName());

        // Обновляем список и адаптер
        exList.remove(position);
        exAdapter.updateExList(exList);
        exAdapter.notifyItemRemoved(position);
        exerciseVisibility(textEx, "Жми '+ Добавить упражнение' чтобы начать", exList.isEmpty());


        Toast.makeText(requireContext(), "Упражнение удалено", Toast.LENGTH_SHORT).show();
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
        presetVisibility(textPreset, "Жми '+ Добавить тренировку' чтобы начать", presetsList.isEmpty());

        Toast.makeText(requireContext(), "Пресет удален", Toast.LENGTH_SHORT).show();
    }

    private void presetVisibility(TextView t1, String message, boolean show) {
        t1.setText(message);
        t1.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void exerciseVisibility(TextView t2, String message, boolean show) {
        t2.setText(message);
        t2.setVisibility(show ? View.VISIBLE : View.GONE);
    }
}