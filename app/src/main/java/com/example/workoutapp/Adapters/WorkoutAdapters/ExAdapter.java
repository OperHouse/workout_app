package com.example.workoutapp.Adapters.WorkoutAdapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workoutapp.Data.WorkoutDao.WORKOUT_EXERCISE_TABLE_DAO;
import com.example.workoutapp.Fragments.WorkoutFragments.WorkoutFragment;
import com.example.workoutapp.MainActivity;
import com.example.workoutapp.Models.WorkoutModels.BaseExModel;
import com.example.workoutapp.R;
import com.example.workoutapp.Tools.WorkoutMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ExAdapter extends RecyclerView.Adapter<ExAdapter.MyViewHolder> {

    private final Context context;
    private List<BaseExModel> exListAll = new ArrayList<>();
    private List<BaseExModel> exListFiltered = new ArrayList<>();;
    private WorkoutMode currentMode;
    private Fragment fragment;
    private String currentFilter = "";

    public ExAdapter(Fragment fragment, Context context, WorkoutMode mode) {
        this.context = context;
        this.currentMode = mode;
        this.fragment = fragment;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.ex_item_layout, parent, false);
        return new MyViewHolder(v);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        if (!exListAll.isEmpty() || !exListFiltered.isEmpty()) {

            BaseExModel exercise;
            if(!Objects.equals(currentFilter, "")){
                exercise = exListFiltered.get(position);
            }else{
                exercise = exListAll.get(position);
            }
            holder.name.setText(exercise.getExName());
            holder.type.setText("(" + exercise.getExType() + ")");
            holder.bodyPart.setText(exercise.getBodyType());


            if (exercise.getIsPressed()) {
                holder.linearLayout.setBackground(ContextCompat.getDrawable(context, R.drawable.card_border2));
            } else {
                holder.linearLayout.setBackground(ContextCompat.getDrawable(context, R.drawable.card_border));
            }

            // Обрабатываем нажатие, меняя состояние в модели и обновляя элемент
            holder.itemView.setOnClickListener(v -> {
                if (currentMode == WorkoutMode.SELECTED) {
                    if(!exercise.getIsPressed()){
                        exercisePressedSort(new BaseExModel(exercise));
                        exListAll.get(0).setIsPressed(true);
                        removeExercise(exercise);
                    }else{
                        unPressedSort(new BaseExModel(exercise, false));
                        exListAll.remove(exercise);
                    }

                    notifyDataSetChanged();
                } else if (currentMode == WorkoutMode.NOT_SELECTED) {
                    WORKOUT_EXERCISE_TABLE_DAO workoutExerciseDao =
                            new WORKOUT_EXERCISE_TABLE_DAO(MainActivity.getAppDataBase());

                    workoutExerciseDao.addExercise(
                            exercise.getExName(),
                            exercise.getExType(),
                            exercise.getBodyType()
                    );

                    // Обновляем кэш
                    MainActivity mainActivity = (MainActivity) fragment.requireActivity();
                    mainActivity.reloadExercisesFromDb();

                    // Получаем существующий WorkoutFragment по тегу
                    WorkoutFragment workoutFragment = (WorkoutFragment)
                            mainActivity.getSupportFragmentManager().findFragmentByTag("workout");

                    if (workoutFragment == null) {
                        workoutFragment = new WorkoutFragment();
                        workoutFragment.setExercises(mainActivity.getCachedExercises());
                        mainActivity.getSupportFragmentManager().beginTransaction()
                                .replace(R.id.frameLayout, workoutFragment, "workout")
                                .commit();
                    } else {
                        workoutFragment.setExercises(mainActivity.getCachedExercises());
                        workoutFragment.refreshWorkoutData();

                        // Если фрагмент скрыт, покажем его
                        mainActivity.getSupportFragmentManager().beginTransaction()
                                .show(workoutFragment)
                                .commit();
                    }

                    // Возвращаемся на WorkoutFragment
                    mainActivity.showOrAddFragment("workout", workoutFragment);
                }
            });
        }
    }

    public void removeExercise(BaseExModel ExerciseToRemove){
        exListAll.remove(ExerciseToRemove);
    }
    public void exercisePressedSort(BaseExModel exercisePressed){
        exListAll.add(0,new BaseExModel(exercisePressed));
    }

    public void unPressedSort(BaseExModel exercise) {
        long exercise_id = exercise.getBase_ex_id();
        boolean isBroken = false;

        for (int i = 0; i < exListAll.size(); i++) {
            BaseExModel e = exListAll.get(i);
            if (!e.getIsPressed()) {
                if (e.getBase_ex_id() > exercise_id) {
                    exListAll.add(i, new BaseExModel(exercise));
                    isBroken = true;
                    break;
                }
            }
        }
        if (!isBroken) {
            exListAll.add(new BaseExModel(exercise)); // вызывается, если break НЕ сработал
        }
    }

    public void deleteEat(BaseExModel exerciseToDelete){
        exListAll.remove(exerciseToDelete);
        notifyDataSetChanged();
    }
    public void changeFilterText(String text){
        currentFilter = text;
    }


    public void setFilteredList(String text){
        currentFilter = text;
        exListFiltered.clear();
        for (BaseExModel elm:exListAll) {
            if(elm.getExName().toLowerCase().contains(currentFilter)){
                exListFiltered.add(elm);
            }
        }
        notifyDataSetChanged();
    };

    @Override
    public int getItemCount() {
        if (currentFilter.isEmpty() && !exListAll.isEmpty()) {
            return exListAll.size();
        }else if(!currentFilter.isEmpty() && !exListFiltered.isEmpty()){
            return exListFiltered.size();
        }else {return 0;}
    }

    // Этот метод теперь фильтрует основной список, чтобы вернуть только выбранные элементы
    public List<BaseExModel> getSelectedItems() {
        List<BaseExModel> selected = new ArrayList<>();
        for (BaseExModel item : exListAll) {
            if (item.getIsPressed()) {
                selected.add(item);
            }
        }
        return selected;
    }


    public void updateExList(List<BaseExModel> exList) {
        this.exListAll = new ArrayList<>();
        if (exList != null) {
            for (BaseExModel ex : exList) {
                this.exListAll.add(new BaseExModel(ex)); // глубокая копия
            }
        }
        notifyDataSetChanged();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView name, type, bodyPart;
        LinearLayout linearLayout;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.nameEat);
            type = itemView.findViewById(R.id.amountEat);
            bodyPart = itemView.findViewById(R.id.bodyPart);
            linearLayout = itemView.findViewById(R.id.linearLayout);
        }
    }
}