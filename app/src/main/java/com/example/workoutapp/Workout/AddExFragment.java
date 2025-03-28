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
import com.example.workoutapp.DataBase;
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
        exAdapter = new ExAdapter(AddExFragment.this, false, exList);

        exRecycler.setHasFixedSize(true);
        exRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        exRecycler.setAdapter(exAdapter);

        exAdapter.updateExList2(exList);

        SearchView SearchView = RootViewAddExFragment.findViewById(R.id.searchExercise2);
        Button CreateExBtn = RootViewAddExFragment.findViewById(R.id.ExerciseBtn);
        Button CreatePresetBtn = RootViewAddExFragment.findViewById(R.id.presetBtn);
        ImageButton BackBtn = RootViewAddExFragment.findViewById(R.id.imageButtonBack);
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
            public void onClick(View v) {showDialogCreateEx();}
        });

        // Обработчик для смахивания элементов в presetRecycler
        ItemTouchHelper.SimpleCallback presetSimpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false; // Не обрабатываем перемещение
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                PresetModel exerciseToDelete =  presetsList.get(position); // Получаем упражнение из presetList

                // Показываем диалог подтверждения удаления
                showDeleteConfirmationDialog(exerciseToDelete, position, presetRecycler); // Добавьте эту функцию, если она еще не существует
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                // Добавление иконки для удаления при смахивании
                new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                        .addActionIcon(R.drawable.ic_trash_can_foreground)
                        .create()
                        .decorate();

                // Ограничиваем смахивание
                if (Math.abs(dX) > viewHolder.itemView.getWidth() * 0.3) {
                    dX = (float) (viewHolder.itemView.getWidth() * 0.3 * (dX > 0 ? 1 : -1));
                }

                // Вызываем родительский метод для дальнейшей обработки
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };

// Создание и привязка ItemTouchHelper для presetRecycler
        ItemTouchHelper presetItemTouchHelper = new ItemTouchHelper(presetSimpleCallback);
        presetItemTouchHelper.attachToRecyclerView(presetRecycler); // Подключаем к presetRecycler


        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                ExModel exerciseToDelete = exList.get(position); // Получаем упражнение

                // Показываем диалог подтверждения удаления
                showDeleteConfirmationDialog(exerciseToDelete, position, exRecycler);

            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                // Отображаем две кнопки: одна для удаления, другая для изменения
                new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                        .addActionIcon(R.drawable.ic_trash_can_foreground)  // Иконка для удаления
                        .create()
                        .decorate();

                // Ограничиваем смахивание, чтобы не смахивать слишком далеко
                if (Math.abs(dX) > viewHolder.itemView.getWidth() * 0.3) {
                    dX = (float) (viewHolder.itemView.getWidth() * 0.3 * (dX > 0 ? 1 : -1));
                }

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(exRecycler); // Подключаем к RecyclerView



        //=========================================================================================//
        ItemTouchHelper.SimpleCallback presetSwipeRightCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                PresetModel presetToDelete = presetsList.get(position);

                // Создаем новый фрагмент
                ChangePresetFragment changePresetFragment = new ChangePresetFragment();

                // Создаем Bundle и передаем объект PresetModel
                Bundle bundle = new Bundle();
                bundle.putSerializable("preset", presetToDelete); // Передаем объект через Bundle
                changePresetFragment.setArguments(bundle); // Устанавливаем Bundle в фрагмент

                // Переход к новому фрагменту
                FragmentManager fragmentManager = getFragmentManager();
                assert fragmentManager != null;
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.frameLayout, changePresetFragment);
                fragmentTransaction.commit();
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                        .addActionIcon(R.drawable.ic_edit_foreground)
                        .create()
                        .decorate();
                if (Math.abs(dX) > viewHolder.itemView.getWidth() * 0.3) {
                    dX = (float) (viewHolder.itemView.getWidth() * 0.3 * (dX > 0 ? 1 : -1));
                }

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };
        ItemTouchHelper presetSwipeRightHelper = new ItemTouchHelper(presetSwipeRightCallback);
        presetSwipeRightHelper.attachToRecyclerView(presetRecycler);


        ItemTouchHelper.SimpleCallback swipeRightCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                ExModel exerciseToChange = exList.get(position);

                showDialogChangeEx(exerciseToChange, position);
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                        .addActionIcon(R.drawable.ic_edit_foreground)
                        .create()
                        .decorate();
                if (Math.abs(dX) > viewHolder.itemView.getWidth() * 0.3) {
                    dX = (float) (viewHolder.itemView.getWidth() * 0.3 * (dX > 0 ? 1 : -1));
                }

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };

        ItemTouchHelper swipeRightItemTouchHelper = new ItemTouchHelper(swipeRightCallback);
        swipeRightItemTouchHelper.attachToRecyclerView(exRecycler);


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
        String[] BodyPart = {"Грудь", "Плечи", "Ноги", "Руки", "Тренажер", "Спина", "Пресс", "Кардио", "Другое"};
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
        //dialogCreateEx.set
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


    private void deleteExercise(ExModel exerciseToDelete, int position) {
        // Удаляем упражнение из базы данных
        String exerciseName = exerciseToDelete.getExName();

        // Удаляем упражнение из базы данных
        dataBase.deleteExercise(exerciseName);

        // Удаляем упражнение из списка
        exList.remove(position);

        // Уведомляем адаптер о том, что данные изменились
        exAdapter.notifyItemRemoved(position);

        // Информируем пользователя
        Toast.makeText(requireContext(), "Упражнение удалено", Toast.LENGTH_SHORT).show();
    }

    //===========================================================================================//




}