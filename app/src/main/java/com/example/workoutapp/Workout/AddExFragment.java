package com.example.workoutapp.Workout;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workoutapp.Adapters.ExAdapter;
import com.example.workoutapp.Adapters.PresetsAdapter;
import com.example.workoutapp.Data.DataBase;
import com.example.workoutapp.Models.ExModel;
import com.example.workoutapp.Models.PresetModel;
import com.example.workoutapp.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;


public class AddExFragment extends Fragment {
    private DataBase dataBase;
    private List<ExModel> exList;
    private List<PresetModel> presetsList;
    private ExAdapter exAdapter;
    private PresetsAdapter presetAdapter;
    private RecyclerView exRecycler;
    private RecyclerView presetRecycler;
    private TextView textPreset;
    private TextView textEx;
    SearchView searchView;

    public AddExFragment() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dataBase = new DataBase(requireContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View RootViewAddExFragment = inflater.inflate(R.layout.fragment_add_ex, container, false);



        presetRecycler = RootViewAddExFragment.findViewById(R.id.presetRecycler);
        presetsList = dataBase.getAllPresets();
        presetAdapter = new PresetsAdapter(AddExFragment.this);

        presetRecycler.setHasFixedSize(true);
        presetRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        presetRecycler.setAdapter(presetAdapter);

        presetAdapter.updatePresetsList(presetsList);

        exRecycler = RootViewAddExFragment.findViewById(R.id.ExerciseRecyclerViewPresets);
        exList = dataBase.getAllExercise();
        exAdapter = new ExAdapter(AddExFragment.this, false);

        exRecycler.setHasFixedSize(true);
        exRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        exRecycler.setAdapter(exAdapter);

        exAdapter.updateExList2(exList);

        searchView = RootViewAddExFragment.findViewById(R.id.searchExercise2);
        searchView.clearFocus(); // Убираем фокус по умолчанию

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
                return false; // обработка при нажатии Enter не нужна
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterExerciseList(newText);
                filterPresetsList(newText);
                return true;
            }
        });

        BackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getFragmentManager();
                assert fragmentManager != null;
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                try {
                    fragmentTransaction.replace(R.id.frameLayout, WorkoutFragment.class.newInstance());
                } catch (IllegalAccessException | java.lang.InstantiationException e) {
                    throw new RuntimeException(e);
                }
                fragmentTransaction.commit();
            }
        });

        CreatePresetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getFragmentManager();
                assert fragmentManager != null;
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                try {
                    fragmentTransaction.replace(R.id.frameLayout, CreatePresetFragment.class.newInstance());
                } catch (IllegalAccessException | java.lang.InstantiationException e) {
                    throw new RuntimeException(e);
                }
                fragmentTransaction.commit();
            }
        });

        CreateExBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialogCreateEx();
                searchView.clearFocus();
            }
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
        AppCompatSpinner spinnerTypeEx = dialogCreateEx.findViewById(R.id.spinnerTypeEx);
        AppCompatSpinner spinnerBodyType = dialogCreateEx.findViewById(R.id.spinnerBodyType);
        Button createWorkBtn = dialogCreateEx.findViewById(R.id.createWorkBtn);

        // Список типов упражнений
        String[] ExercisesType = {"Гантели", "Гриф", "Вес тела", "Кроссовер", "Тренажер", "Время", "Другое"};
        ArrayList<String> List1 = new ArrayList<>(Arrays.asList(ExercisesType));
        ArrayAdapter<String> adapter1 = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, List1);
        adapter1.setDropDownViewResource(R.layout.spinners_style);
        spinnerTypeEx.setAdapter(adapter1);

        // Список частей тела
        String[] BodyPart = {"Грудь", "Плечи", "Ноги", "Руки", "Спина", "Пресс", "Кардио", "Другое"};
        ArrayList<String> List2 = new ArrayList<>(Arrays.asList(BodyPart));
        ArrayAdapter<String> adapter2 = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, List2);
        adapter2.setDropDownViewResource(R.layout.spinners_style);
        spinnerBodyType.setAdapter(adapter2);

        // Обработчик кнопки "Назад"
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogCreateEx.dismiss();
            }
        });

        // Обработчик кнопки "Создать упражнение"
        createWorkBtn.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onClick(View v) {
                String exName = nameEx.getText().toString().trim();
                String exType = "("+ spinnerTypeEx.getSelectedItem().toString() + ")";
                String bodyType = spinnerBodyType.getSelectedItem().toString();

                // Проверка, что все поля заполнены
                if (exName.isEmpty()) {
                    nameEx.setError("Пожалуйста, введите название упражнения");
                    return;
                }

                // Создание нового объекта ExModel
                ExModel newExercise = new ExModel();
                newExercise.setExName(exName);
                newExercise.setExType(exType);
                newExercise.setBodyType(bodyType);

                // Добавление упражнения в базу данных
                dataBase.addExercise(newExercise);

                exList.clear();
                exList.addAll(dataBase.getAllExercise());
                exAdapter.notifyDataSetChanged();
                exerciseVisibility(textEx, "Жми '+ Добавить упражнение' чтобы начать", exList.isEmpty());

                exAdapter.updateExList2(exList);

                // Закрытие диалога
                dialogCreateEx.dismiss();

                // Информирование пользователя о добавлении упражнения (можно, например, через Toast)
                Toast.makeText(requireContext(), "Упражнение добавлено!", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void showDialogChangeEx(ExModel exerciseToChange, int position){

        Dialog dialogCreateEx = new Dialog(requireContext());
        dialogCreateEx.setContentView(R.layout.create_ex_dialog_box);
        Objects.requireNonNull(dialogCreateEx.getWindow()).setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialogCreateEx.show();

        AtomicBoolean isDialogClosedByOutsideClick = new AtomicBoolean(false);

        dialogCreateEx.setCancelable(true); // Делаем диалог закрываемым
        dialogCreateEx.setCanceledOnTouchOutside(true); // Закрыть при клике вне диалога

        dialogCreateEx.getWindow().setBackgroundDrawable(new ColorDrawable(0));

        String oldName = exerciseToChange.getExName();
        ImageButton backBtn = dialogCreateEx.findViewById(R.id.imageButtonBack1);
        EditText nameEx = dialogCreateEx.findViewById(R.id.editText);
        AppCompatSpinner spinnerTypeEx = dialogCreateEx.findViewById(R.id.spinnerTypeEx);
        AppCompatSpinner spinnerBodyType = dialogCreateEx.findViewById(R.id.spinnerBodyType);
        Button createWorkBtn = dialogCreateEx.findViewById(R.id.createWorkBtn);
        TextView text = dialogCreateEx.findViewById(R.id.textView1);

        text.setText("Изменение упражнения");
        createWorkBtn.setText("Изменить упражнение");
        nameEx.setText(exerciseToChange.getExName());

        // Список типов упражнений
        String[] ExercisesType = {"Гантели", "Гриф", "Вес тела", "Кроссовер", "Тренажер", "Время", "Другое"};
        ArrayList<String> List1 = new ArrayList<>(Arrays.asList(ExercisesType));
        ArrayAdapter<String> adapter1 = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, List1);
        adapter1.setDropDownViewResource(R.layout.spinners_style);
        spinnerTypeEx.setAdapter(adapter1);


        // Список частей тела
        String[] BodyPart = {"Грудь", "Плечи", "Ноги", "Руки", "Тренажер", "Спина", "Пресс", "Кардио", "Другое"};
        ArrayList<String> List2 = new ArrayList<>(Arrays.asList(BodyPart));
        ArrayAdapter<String> adapter2 = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, List2);
        adapter2.setDropDownViewResource(R.layout.spinners_style);
        spinnerBodyType.setAdapter(adapter2);

        // Устанавливаем значения в спиннеры, если они есть
        String exerciseType = exerciseToChange.getExType().replaceAll("[()]", "").trim(); // Убираем скобки и пробелы
        String bodyType = exerciseToChange.getBodyType().replaceAll("[()]", "").trim();


        // Устанавливаем тип упражнения в спиннер
        int typeIndex = List1.indexOf(exerciseType);
        if (typeIndex != -1) {
            spinnerTypeEx.setSelection(typeIndex);
        }

        // Устанавливаем часть тела в спиннер
        int bodyIndex = List2.indexOf(bodyType);
        if (bodyIndex != -1) {
            spinnerBodyType.setSelection(bodyIndex);
        }

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exAdapter.notifyItemChanged(position);
                dialogCreateEx.dismiss();;
            }
        });

        dialogCreateEx.setOnDismissListener(dialog -> {
            if (isDialogClosedByOutsideClick.get()) {
                // Диалог был закрыт нажатием вне
                exAdapter.notifyItemChanged(position);
            }
        });

        createWorkBtn.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onClick(View v) {
                String exName = nameEx.getText().toString().trim();
                String exType = "("+ spinnerTypeEx.getSelectedItem().toString() + ")";
                String bodyType = spinnerBodyType.getSelectedItem().toString();

                // Проверка, что все поля заполнены
                if (exName.isEmpty()) {
                    nameEx.setError("Пожалуйста, введите название упражнения");
                    return;
                }

                // Создание нового объекта ExModel
                ExModel newExercise = new ExModel();
                newExercise.setExName(exName);
                newExercise.setExType(exType);
                newExercise.setBodyType(bodyType);

                // Добавление упражнения в базу данных
                dataBase.changeExercise(oldName,newExercise);

                exList.clear();
                exList.addAll(dataBase.getAllExercise());
                exAdapter.notifyDataSetChanged();

                exAdapter.updateExList2(exList);

                // Закрытие диалога
                dialogCreateEx.dismiss();

                // Информирование пользователя о добавлении упражнения (можно, например, через Toast)
                Toast.makeText(requireContext(), "Упражнение измененно!", Toast.LENGTH_SHORT).show();
            }
        });

        dialogCreateEx.setOnCancelListener(dialog -> isDialogClosedByOutsideClick.set(true));



    }
    private void showDeleteConfirmationDialog(ExModel exerciseToDelete, int position, RecyclerView r) {
        Dialog dialogCreateEx = new Dialog(requireContext());
        dialogCreateEx.setContentView(R.layout.confirm_dialog_layout);
        Objects.requireNonNull(dialogCreateEx.getWindow()).setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        AtomicBoolean isDialogClosedByOutsideClick = new AtomicBoolean(false);
        // Разрешаем закрытие диалога при нажатии вне его
        dialogCreateEx.setCancelable(true); // Делаем диалог закрываемым
        dialogCreateEx.setCanceledOnTouchOutside(true); // Закрыть при клике вне диалога

        Button deleteBtn = dialogCreateEx.findViewById(R.id.btnDelete);
        Button chanelBtn = dialogCreateEx.findViewById(R.id.btnChanel);


        if(dialogCreateEx.getWindow() != null){
            dialogCreateEx.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }

        chanelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exAdapter.notifyItemChanged(position);
                dialogCreateEx.dismiss();

            }
        });

        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteExercise(exerciseToDelete, position);
                r.requestLayout();
                dialogCreateEx.dismiss();
            }
        });

        dialogCreateEx.setOnDismissListener(dialog -> {
            if (isDialogClosedByOutsideClick.get()) {
                // Диалог был закрыт нажатием вне
                exAdapter.notifyItemChanged(position);
            }
        });

        // Слушаем закрытие по клику вне
        dialogCreateEx.setOnCancelListener(dialog -> isDialogClosedByOutsideClick.set(true));
        dialogCreateEx.show();
    }
    private void showDeleteConfirmationDialog(PresetModel presetToDelete, int position, RecyclerView r) {
        Dialog dialogCreateEx = new Dialog(requireContext());
        dialogCreateEx.setContentView(R.layout.confirm_dialog_layout);
        Objects.requireNonNull(dialogCreateEx.getWindow()).setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        AtomicBoolean isDialogClosedByOutsideClick = new AtomicBoolean(false);

        // Разрешаем закрытие диалога при нажатии вне его
        dialogCreateEx.setCancelable(true); // Делаем диалог закрываемым
        dialogCreateEx.setCanceledOnTouchOutside(true); // Закрыть при клике вне диалога

        Button deleteBtn = dialogCreateEx.findViewById(R.id.btnDelete);
        Button chanelBtn = dialogCreateEx.findViewById(R.id.btnChanel);

        // Находим и изменяем текстовые элементы
        TextView text1 = dialogCreateEx.findViewById(R.id.textView);
        TextView text2 = dialogCreateEx.findViewById(R.id.text1);

        text1.setText("Удаление Пресета");
        text2.setText("Вы действительно хотите удалить Пресет?");

        if(dialogCreateEx.getWindow() != null){
            dialogCreateEx.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }

        chanelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presetAdapter.notifyItemChanged(position); // Обновляем адаптер для presetRecycler
                dialogCreateEx.dismiss();
            }
        });

        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                List<ExModel> p = new ArrayList<>(presetToDelete.getExercises());

                for (ExModel elm: p) {
                    dataBase.deletePreset(presetToDelete.getPresetName(),elm.getExName());
                }
                presetsList.remove(position);
                presetVisibility(textPreset, "Жми '+ Добавить тренировку' чтобы начать", presetsList.isEmpty());
                presetAdapter.notifyItemRemoved(position);
                r.requestLayout();
                dialogCreateEx.dismiss();
            }
        });

        dialogCreateEx.setOnDismissListener(dialog -> {
            if (isDialogClosedByOutsideClick.get()) {
                // Диалог был закрыт нажатием вне
                presetAdapter.notifyItemChanged(position);
            }
        });

        // Слушаем закрытие по клику вне
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
                    PresetModel item = (PresetModel) dataList.get(position);
                    showDeleteConfirmationDialog(item, position, recyclerView);
                } else {
                    ExModel item = (ExModel) dataList.get(position);
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
                    PresetModel preset = (PresetModel) dataList.get(position);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("preset", preset);
                    ChangePresetFragment changePresetFragment = new ChangePresetFragment();
                    changePresetFragment.setArguments(bundle);

                    FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
                    transaction.replace(R.id.frameLayout, changePresetFragment);
                    transaction.commit();
                } else {
                    ExModel ex = (ExModel) dataList.get(position);
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
        List<ExModel> filteredList = new ArrayList<>();
        for (ExModel ex : dataBase.getAllExercise()) {
            if (ex.getExName().toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(ex);
            }
        }

        exAdapter.updateExList2(filteredList);

        if (filteredList.isEmpty()) {
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
        List<PresetModel> filteredPresets = new ArrayList<>();
        for (PresetModel preset : dataBase.getAllPresets()) {
            for (ExModel ex : preset.getExercises()) {
                if (ex.getExName().toLowerCase().contains(text.toLowerCase())) {
                    filteredPresets.add(preset);
                    break;
                }
            }
        }

        presetAdapter.updatePresetsList(filteredPresets);

        if (filteredPresets.isEmpty()) {
            presetVisibility(
                    textPreset,
                    "Тренировки с таким упражнением нет. \nЖми '+ Добавить тренировку', чтобы создать её!",
                    true
            );
        } else {
            textPreset.setVisibility(View.GONE);
        }
    }




    private void deleteExercise(ExModel exerciseToDelete, int position) {
        // Удаляем упражнение из базы данных
        String exerciseName = exerciseToDelete.getExName();

        // Удаляем упражнение из базы данных
        dataBase.deleteExercise(exerciseName);

        // Удаляем упражнение из списка
        exList.remove(position);
        exerciseVisibility(textEx, "Жми '+ Добавить упражнение' чтобы начать", exList.isEmpty());

        // Уведомляем адаптер о том, что данные изменились
        exAdapter.notifyItemRemoved(position);

        // Информируем пользователя
        Toast.makeText(requireContext(), "Упражнение удалено", Toast.LENGTH_SHORT).show();
    }

    //===========================================================================================//


    private void presetVisibility(TextView t1, String message, boolean show) {
        t1.setText(message);
        t1.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void exerciseVisibility(TextView t2, String message, boolean show) {
        t2.setText(message);
        t2.setVisibility(show ? View.VISIBLE : View.GONE);
    }


}