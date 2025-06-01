package com.example.workoutapp.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workoutapp.DAO.ExerciseDao;
import com.example.workoutapp.DAO.TempWorkoutDao;
import com.example.workoutapp.MainActivity;
import com.example.workoutapp.Models.ExModel;
import com.example.workoutapp.R;
import com.example.workoutapp.WorkoutFragments.WorkoutFragment;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class ExAdapter extends RecyclerView.Adapter<ExAdapter.MyViewHolder> {

    private final Context context;
    private ExerciseDao ExDao;
    private TempWorkoutDao TempWorkDao;
    private Fragment fragment;
    private List<ExModel> exList;
    private List<ExModel> noClickedList;
    private boolean isSelectable;
    private final List<ExModel> exListMain; // Неизменяемый список
    private String currentFilter = "";

    public ExAdapter(@NonNull Fragment fragment, boolean isSelectable) {
        this.context = fragment.requireContext();
        this.ExDao = new ExerciseDao(MainActivity.getAppDataBase());
        this.TempWorkDao = new TempWorkoutDao(MainActivity.getAppDataBase());
        this.isSelectable = isSelectable;
        this.exList = new ArrayList<>();
        this.noClickedList = new ArrayList<>();
        this.fragment = fragment;  // Store the fragment reference
        this.exListMain = ExDao.getAllExercises();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.ex_item_layout, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        if (exList != null && !exList.isEmpty()) {
            ExModel exListElm = exList.get(position);
            holder.nameEx.setText(exListElm.getExName());
            holder.exType.setText(exListElm.getExType());
            holder.bodyPart.setText(exListElm.getBodyType());



            //==================Установка нажатого элемента===================//
            if(isSelectable) {
                // Устанавливаем фоновое изображение для LinearLayout
                if (exListElm.getIsPressed()) {
                    // Если элемент был нажат, меняем фоновый drawable
                    holder.linearLayout.setBackground(ContextCompat.getDrawable(context, R.drawable.card_border2));  // Новый фон
                } else {
                    // Если элемент не был нажат, возвращаем исходный фон
                    holder.linearLayout.setBackground(ContextCompat.getDrawable(context, R.drawable.card_border));  // Обычный фон
                }

                // Обработка нажатия на элемент
                holder.itemView.setOnClickListener(v -> {
                    if (!exListElm.getIsPressed()) {
                        handleItemSelected(exListElm);
                    } else {
                        handleItemDeselected(exListElm);
                    }

                    if (fragment != null) {
                        try {
                            fragment.getClass().getMethod("clearSearchFocus").invoke(fragment);
                        } catch (Exception e) {
                            e.printStackTrace(); // метод не найден или произошла ошибка
                        }
                    }
                    noClickedList.clear();
                    notifyDataSetChanged();
                });
            }else {
                 holder.itemView.setOnClickListener(v -> {
                     // Получаем название упражнения из элемента
                     String exerciseName = exListElm.getExName();

                     // Проверяем, существует ли упражнение в базе данных
                     boolean exerciseExists = TempWorkDao.checkIfTempExerciseExists(exerciseName);

                     if (!exerciseExists) {
                         // Если упражнения нет, добавляем его в базу данных
                         TempWorkDao.addTempExercise(exerciseName, exListElm.getExType(), exListElm.getBodyType());  // Передаем название и тип упражнения
                         // Переход к новому фрагменту
                         FragmentManager fragmentManager = fragment.getParentFragmentManager(); // Use the fragment reference to get FragmentManager
                         FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                         fragmentTransaction.replace(R.id.frameLayout, new WorkoutFragment()); // Replace with the new fragment
                         fragmentTransaction.addToBackStack(null); // Add to back stack if you want to navigate back
                         fragmentTransaction.commit();
                     }else{
                         Toast.makeText(context, "Такое упражнение уже добавлено!", Toast.LENGTH_SHORT).show();
                     }
                     //Вывод в логи базы данных
                     //tempDataBaseEx.logAllExercisesAndSets();


                 });


            }
        }


    }

    @Override
    public int getItemCount() {
        if (exList != null) {
            return exList.size();
        }
        return 0;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateExList(List<ExModel> exModelList) {
        this.exList = exModelList;

        // Проходим по exList и для каждого элемента проверяем, если он нажат
        for (int i = 0; i < exList.size(); i++) {
            ExModel exListItem = exList.get(i);

            // Если элемент нажат в exList, то ищем его в exListMain и устанавливаем нажатым
            for (ExModel mainItem : exListMain) {
                if (Objects.equals(exListItem.getExName(), mainItem.getExName()) &&
                        Objects.equals(exListItem.getExType(), mainItem.getExType()) &&
                        Objects.equals(exListItem.getBodyType(), mainItem.getBodyType())) {

                    // Если exListItem нажат, делаем такой же элемент в exListMain нажатым
                    if (exListItem.getIsPressed()) {
                        mainItem.setIsPressed(true);
                    }
                    break; // Выход из внутреннего цикла, так как элемент найден
                }
            }
        }

        exList.sort(new Comparator<ExModel>() {
            @Override
            public int compare(ExModel ex1, ExModel ex2) {
                if (ex1.getIsPressed() && !ex2.getIsPressed()) {
                    return -1;
                } else if (!ex1.getIsPressed() && ex2.getIsPressed()) {
                    return 1;
                }
                return 0;
            }
        });
        notifyDataSetChanged();
    }



    @SuppressLint("NotifyDataSetChanged")
    public void updateExList2(List<ExModel> exModelList) {
        this.exList = exModelList;
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateExListFiltered(String filter) {
        this.currentFilter = filter.toLowerCase();// сохраняем текущий фильтр
        if(filter.isEmpty()) {
            this.exList = new ArrayList<>();
            for (ExModel ex : exListMain) {
                // Создаём копию объекта, чтобы избежать общей ссылки (если нужно)
                this.exList.add(new ExModel(
                        ex.getExName(),
                        ex.getExType(),
                        ex.getBodyType(),
                        ex.getIsPressed()
                ));
            }
        }else {
            exList.clear();
            for (ExModel elm : exListMain) {
                if (currentFilter.isEmpty()) {
                    exList.add(elm);
                } else if (matchesFilter(elm)) {
                    exList.add(elm);
                }
            }
        }

        // Проходим по exList и для каждого элемента проверяем, если он нажат
        for (int i = 0; i < exList.size(); i++) {
            ExModel exListItem = exList.get(i);

            // Если элемент нажат в exList, то ищем его в exListMain и устанавливаем нажатым
            for (ExModel mainItem : exListMain) {
                if (Objects.equals(exListItem.getExName(), mainItem.getExName()) &&
                        Objects.equals(exListItem.getExType(), mainItem.getExType()) &&
                        Objects.equals(exListItem.getBodyType(), mainItem.getBodyType())) {

                    // Если exListItem нажат, делаем такой же элемент в exListMain нажатым
                    if (exListItem.getIsPressed()) {
                        mainItem.setIsPressed(true);
                    }
                    break; // Выход из внутреннего цикла, так как элемент найден
                }
            }
        }

        exList.sort(new Comparator<ExModel>() {
            @Override
            public int compare(ExModel ex1, ExModel ex2) {
                if (ex1.getIsPressed() && !ex2.getIsPressed()) {
                    return -1;
                } else if (!ex1.getIsPressed() && ex2.getIsPressed()) {
                    return 1;
                }
                return 0;
            }
        });
        notifyDataSetChanged();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView nameEx, exType, bodyPart;
        public ImageView imageExType;
        public LinearLayout linearLayout;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            nameEx = itemView.findViewById(R.id.nameEat);
            exType = itemView.findViewById(R.id.amountEat);
            bodyPart = itemView.findViewById(R.id.bodyPart);
            imageExType = itemView.findViewById(R.id.imageExType);
            linearLayout = itemView.findViewById(R.id.linearLayout);
        }
    }

    public Context getContext() {
        return context;
    }

    public List<ExModel> getList() {

       List<ExModel> clickedList = new ArrayList<>();

        for (ExModel elm:exListMain) {
            if(elm.getIsPressed()){clickedList.add(elm);}
        }

        return clickedList;
    }

    private void handleItemSelected(ExModel exListElm) {
        // Перемещаем выбранный элемент в начало списка
        exList.remove(exListElm);
        exList.add(0, exListElm);
        exList.get(0).setIsPressed(true);

        // Обновляем состояние элемента в exListMain
        updateExListMainState(exListElm, true);

        // Переносим невзаимодействующие элементы в noClickedList и удаляем их из exList
        Iterator<ExModel> iterator = exList.iterator();
        while (iterator.hasNext()) {
            ExModel elm = iterator.next();
            if (!elm.getIsPressed()) {
                iterator.remove();
                noClickedList.add(elm);
            }
        }

        // Добавляем оставшиеся элементы обратно в exList
        exList.addAll(noClickedList);
    }

    private void handleItemDeselected(ExModel exListElm) {
        // Убираем из видимого списка
        exList.remove(exListElm);

        // Обновляем состояние
        updateExListMainState(exListElm, false);

        // Удаляем все невыбранные элементы
        List<ExModel> toRemove = new ArrayList<>();
        for (ExModel elm : exList) {
            if (!elm.getIsPressed()) {
                toRemove.add(elm);
            }
        }
        exList.removeAll(toRemove);


        // Добавляем обратно подходящие элементы из основного списка
        for (ExModel elm : exListMain) {
            if (!elm.getIsPressed()) {
                if (currentFilter.isEmpty()) {
                    exList.add(elm);
                } else if (matchesFilter(elm)) {
                    exList.add(elm);
                }
            }
        }


        notifyDataSetChanged();
    }

    private boolean matchesFilter(ExModel elm) {
        // Проверяем, соответствует ли элемент текущему фильтру
        return elm.getExName().toLowerCase().contains(currentFilter);
    }

    private void updateExListMainState(ExModel exListElm, boolean isPressed) {
        for (int i = 0; i < exListMain.size(); i++) {
            ExModel mainElm = exListMain.get(i);
            if (Objects.equals(mainElm.getExName(), exListElm.getExName()) &&
                    Objects.equals(mainElm.getExType(), exListElm.getExType()) &&
                    Objects.equals(mainElm.getBodyType(), exListElm.getBodyType())) {
                mainElm.setIsPressed(isPressed);
                break;
            }
        }
    }
    @SuppressLint("NotifyDataSetChanged")
    public void filter(String query) {
        currentFilter = query.toLowerCase();
        List<ExModel> filteredList = new ArrayList<>();

        for (ExModel ex : exListMain) {
            if (ex.getExName().toLowerCase().contains(currentFilter)) {
                filteredList.add(new ExModel(
                        ex.getExName(),
                        ex.getExType(),
                        ex.getBodyType(),
                        ex.getIsPressed()
                ));
            }
        }

        this.exList = filteredList;
        notifyDataSetChanged();
    }

}



