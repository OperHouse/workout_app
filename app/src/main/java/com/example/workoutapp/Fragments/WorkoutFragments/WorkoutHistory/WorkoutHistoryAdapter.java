package com.example.workoutapp.Fragments.WorkoutFragments.WorkoutHistory;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workoutapp.Models.WorkoutModels.WorkoutSessionModel;
import com.example.workoutapp.R;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class WorkoutHistoryAdapter extends RecyclerView.Adapter<WorkoutHistoryAdapter.ViewHolder> {

    private final List<WorkoutSessionModel> sessions;
    private OnWorkoutClickListener listener;

    // Интерфейс для клика (если захочешь открывать детали тренировки)
    public interface OnWorkoutClickListener {
        void onWorkoutClick(WorkoutSessionModel session);
    }

    public WorkoutHistoryAdapter(List<WorkoutSessionModel> sessions, OnWorkoutClickListener listener) {
        this.sessions = sessions;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.worhout_history_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WorkoutSessionModel session = sessions.get(position);

        holder.tvDate.setText(session.getWorkoutDate());
        holder.tvTitle.setText(session.getWorkoutTitle());

        int count = session.getExercises().size();
        holder.tvAmount.setText(count + " " + getExerciseWord(count));

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onWorkoutClick(session);
        });

        // Кнопка-стрелка тоже может служить для клика
        holder.btnDetails.setOnClickListener(v -> {
            if (listener != null) listener.onWorkoutClick(session);
        });
    }

    @Override
    public int getItemCount() {
        return sessions.size();
    }

    // Утилита для правильного склонения
    private String getExerciseWord(int count) {
        if (count % 100 >= 11 && count % 100 <= 14) return "упражнений";
        switch (count % 10) {
            case 1: return "упражнение";
            case 2:
            case 3:
            case 4: return "упражнения";
            default: return "упражнений";
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvTitle, tvAmount;
        MaterialButton btnDetails;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.workout_history_card_date_TV);
            tvTitle = itemView.findViewById(R.id.workout_history_card_title_TV);
            tvAmount = itemView.findViewById(R.id.workout_history_card_amount_ex_TV);
            btnDetails = itemView.findViewById(R.id.imageButton);
        }
    }
}