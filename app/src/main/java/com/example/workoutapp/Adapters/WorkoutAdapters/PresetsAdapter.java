package com.example.workoutapp.Adapters.WorkoutAdapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import com.example.workoutapp.Data.WorkoutDao.BASE_EXERCISE_TABLE_DAO;
import com.example.workoutapp.Data.WorkoutDao.CONNECTING_WORKOUT_PRESET_TABLE_DAO;
import com.example.workoutapp.Data.WorkoutDao.WORKOUT_PRESET_NAME_TABLE_DAO;
import com.example.workoutapp.MainActivity;
import com.example.workoutapp.Models.WorkoutModels.BaseExModel;
import com.example.workoutapp.Models.WorkoutModels.ExerciseModel;
import com.example.workoutapp.R;
import java.util.List;

public class PresetsAdapter extends RecyclerView.Adapter<PresetsAdapter.MyViewHolder> {

    private List<ExerciseModel> presetsList;
    private final WORKOUT_PRESET_NAME_TABLE_DAO presetNameDao;
    private final CONNECTING_WORKOUT_PRESET_TABLE_DAO connectingPresetDao;
    private final BASE_EXERCISE_TABLE_DAO baseExerciseDao;
    private final Fragment fragment;

    public PresetsAdapter(@NonNull Fragment fragment) {
        this.fragment = fragment;
        this.presetNameDao = new WORKOUT_PRESET_NAME_TABLE_DAO(MainActivity.getAppDataBase());
        this.connectingPresetDao = new CONNECTING_WORKOUT_PRESET_TABLE_DAO(MainActivity.getAppDataBase());
        this.baseExerciseDao = new BASE_EXERCISE_TABLE_DAO(MainActivity.getAppDataBase());
    }

    @NonNull
    @Override
    public PresetsAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.preset_item_layout, parent, false);
        return new PresetsAdapter.MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        if (presetsList == null || position >= presetsList.size()) {
            return;
        }

        ExerciseModel currentPreset = presetsList.get(position);
        holder.namePreset.setText(currentPreset.getExerciseName());

        StringBuilder exercisesListText = new StringBuilder();
        List<Long> baseExIds = connectingPresetDao.getBaseExIdsByPresetId(currentPreset.getExercise_id());

        for (Long baseExId : baseExIds) {
            BaseExModel exercise = baseExerciseDao.getExerciseById(baseExId);
            if (exercise != null) {
                exercisesListText.append(exercise.getExName())
                        .append(" (")
                        .append(exercise.getExType())
                        .append("), ");
            }
        }

        if (exercisesListText.length() > 0) {
            exercisesListText.setLength(exercisesListText.length() - 2);
        }

        holder.exListText.setText(exercisesListText.toString());

        holder.itemView.setOnClickListener(v -> {
            // Здесь должна быть логика для загрузки пресета в текущую тренировку.
            // Например, вызов метода во Fragment'е или DAO для добавления упражнений
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updatePresetsList(List<ExerciseModel> exerciseModelList) {
        this.presetsList = exerciseModelList;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return presetsList != null ? presetsList.size() : 0;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView namePreset, exListText;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            namePreset = itemView.findViewById(R.id.namePreset);
            exListText = itemView.findViewById(R.id.exListText);
        }
    }
}