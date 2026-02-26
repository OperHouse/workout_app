package com.example.workoutapp.Fragments.WorkoutFragments.WorkoutHistory;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workoutapp.Models.WorkoutModels.CardioSetModel;
import com.example.workoutapp.Models.WorkoutModels.ExerciseModel;
import com.example.workoutapp.Models.WorkoutModels.StrengthSetModel;
import com.example.workoutapp.R;

import java.util.List;

public class ExerciseDetailsAdapter extends RecyclerView.Adapter<ExerciseDetailsAdapter.ViewHolder> {

    private final List<ExerciseModel> exercises;

    public ExerciseDetailsAdapter(List<ExerciseModel> exercises) {
        this.exercises = exercises;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_details_exercise, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ExerciseModel exercise = exercises.get(position);
        holder.tvName.setText(exercise.getExerciseName());
        holder.tvType.setText(exercise.getExerciseBodyType() + " • " + exercise.getExerciseType());

        // Очищаем контейнер перед добавлением (чтобы при скролле данные не дублировались)
        holder.setsContainer.removeAllViews();

        int setNumber = 1;
        for (Object set : exercise.getSets()) {
            TextView tvSet = new TextView(holder.itemView.getContext());
            tvSet.setTextColor(Color.WHITE);
            tvSet.setTextSize(14);
            tvSet.setPadding(0, 4, 0, 4);

            String setText = "";
            if (set instanceof StrengthSetModel) {
                StrengthSetModel s = (StrengthSetModel) set;
                setText = setNumber + "-й подход - " + s.getStrength_set_weight() + " кг x " + s.getStrength_set_rep();
            } else if (set instanceof CardioSetModel) {
                CardioSetModel c = (CardioSetModel) set;
                setText = setNumber + "-й подход - " + c.getCardio_set_time() + " мин | " + c.getCardio_set_distance() + " км";
            }

            tvSet.setText(setText);
            holder.setsContainer.addView(tvSet);
            setNumber++;
        }
    }

    @Override
    public int getItemCount() {
        return exercises.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvType;
        LinearLayout setsContainer;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.ex_details_name);
            tvType = itemView.findViewById(R.id.ex_details_type);
            setsContainer = itemView.findViewById(R.id.sets_container);
        }
    }
}
