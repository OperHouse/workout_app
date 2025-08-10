package com.example.workoutapp.Adapters.WorkoutAdapters;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workoutapp.Data.WorkoutDao.WORKOUT_EXERCISE_TABLE_DAO;
import com.example.workoutapp.MainActivity;
import com.example.workoutapp.Models.WorkoutModels.ExerciseModel;
import com.example.workoutapp.R;
import java.util.List;
import java.util.Objects;

public class OutsideAdapter extends RecyclerView.Adapter<OutsideAdapter.MyViewHolder> {

    private WORKOUT_EXERCISE_TABLE_DAO workoutExerciseTableDao;
    private List<ExerciseModel> exerciseModelList;
    private Fragment fragment;
    private final Context context;

    public OutsideAdapter(@NonNull Fragment fragment) {
        this.context = fragment.requireContext();
        this.fragment = fragment;
        this.workoutExerciseTableDao = new WORKOUT_EXERCISE_TABLE_DAO(MainActivity.getAppDataBase());
    }

    @NonNull
    @Override
    public OutsideAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.ex_item_workout_layout, parent, false);
        return new OutsideAdapter.MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        if (exerciseModelList == null || exerciseModelList.isEmpty()) return;

        ExerciseModel exerciseModelListElm = exerciseModelList.get(position);
        holder.name.setText(exerciseModelListElm.getExerciseName());
        holder.type.setText(exerciseModelListElm.getExerciseType());

        final int exerciseId = (int) exerciseModelListElm.getExercise_id();

        // --- Логика для динамического отображения заголовков ---
        if ("strength".equalsIgnoreCase(exerciseModelListElm.getExerciseType())) {
            holder.strengthCL.setVisibility(View.VISIBLE);
            holder.cardioCL.setVisibility(View.GONE);
        } else if ("cardio".equalsIgnoreCase(exerciseModelListElm.getExerciseType())) {
            holder.strengthCL.setVisibility(View.GONE);
            holder.cardioCL.setVisibility(View.VISIBLE);
        } else {
            // Скрываем оба, если тип неизвестен
            holder.strengthCL.setVisibility(View.GONE);
            holder.cardioCL.setVisibility(View.GONE);
        }

        InnerAdapter innerAdapter = new InnerAdapter(exerciseModelListElm.getSets(), exerciseId);
        holder.innerRecycler.setAdapter(innerAdapter);

        // Устанавливаем слушатель для изменения видимости
        innerAdapter.setOnSetListChangedListener((changedExerciseId, isEmpty) -> {
            if (exerciseId == changedExerciseId) {
                holder.innerRecycler.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
            }
        });

        // Проверяем, пуст ли список сетов, чтобы скрыть RecyclerView
        holder.innerRecycler.setVisibility(exerciseModelListElm.getSets().isEmpty() ? View.GONE : View.VISIBLE);

        innerAdapter.attachSwipeToDelete(holder.innerRecycler, exerciseId);

        holder.addSet.setOnClickListener(v -> {

        });

        holder.delEx.setOnClickListener(v -> delConfirmDialog(exerciseModelListElm, position));
    }

    private void delConfirmDialog(ExerciseModel elm, int position) {
        Dialog dialogCreateEx = new Dialog(context);
        dialogCreateEx.setContentView(R.layout.confirm_dialog_layout);
        Objects.requireNonNull(dialogCreateEx.getWindow()).setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialogCreateEx.setCancelable(true);

        Button deleteBtn = dialogCreateEx.findViewById(R.id.btnDelete);
        Button chanelBtn = dialogCreateEx.findViewById(R.id.btnChanel);
        TextView text1 = dialogCreateEx.findViewById(R.id.text1);
        TextView text2 = dialogCreateEx.findViewById(R.id.text2);

        deleteBtn.setText("Удалить");
        text1.setText("Удаление упражнения");
        text2.setText("Вы действительно хотите удалить упражнение и все его подходы?");

        if (dialogCreateEx.getWindow() != null) {
            dialogCreateEx.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }

        chanelBtn.setOnClickListener(v -> dialogCreateEx.dismiss());

        deleteBtn.setOnClickListener(v -> {
            dialogCreateEx.dismiss();

            exerciseModelList.remove(position);
            notifyItemRemoved(position);
        });

        dialogCreateEx.show();
    }

    public void saveAllInnerAdapters() {
        // Логика сохранения должна быть пересмотрена.
    }

    @Override
    public int getItemCount() {
        return exerciseModelList != null ? exerciseModelList.size() : 0;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateExList(List<ExerciseModel> ExModelList) {
        this.exerciseModelList = ExModelList;
        notifyDataSetChanged();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        TextView type;
        Button addSet;
        Button delEx;
        ConstraintLayout strengthCL;
        ConstraintLayout cardioCL;
        RecyclerView innerRecycler;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            type = itemView.findViewById(R.id.type);
            addSet = itemView.findViewById(R.id.addSetBtn);
            delEx = itemView.findViewById(R.id.delExBtn);
            strengthCL = itemView.findViewById(R.id.strength_CL);
            cardioCL = itemView.findViewById(R.id.cardio_CL);
            innerRecycler = itemView.findViewById(R.id.innerRecycle);
            innerRecycler.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
        }
    }
}