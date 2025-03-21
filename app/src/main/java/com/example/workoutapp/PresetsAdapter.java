package com.example.workoutapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PresetsAdapter  extends RecyclerView.Adapter<PresetsAdapter.MyViewHolder> {

    private final Context context;
    private List<PresetModel> presetsList;

    public PresetsAdapter(@NonNull Fragment fragment) {
        this.context = fragment.requireContext();
    }

    @NonNull
    @Override
    public PresetsAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.preset_item_layout, parent, false);
        return new PresetsAdapter.MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        if (presetsList != null && !presetsList.isEmpty()) {
            PresetModel currentPresetModel = presetsList.get(position);

            holder.namePreset.setText(currentPresetModel.getPresetName());

            // Формируем строку для отображения всех упражнений в пресете
            StringBuilder exercisesListText = new StringBuilder();
            for (ExModel exercise : currentPresetModel.getExercises()) {
                exercisesListText.append(exercise.getExName())
                        .append(" ")
                        .append(exercise.getExType())
                        .append(", ");
            }

            holder.exListText.setText(exercisesListText.toString());
        }
    }
    @SuppressLint("NotifyDataSetChanged")
    public void updatePresetsList(List<PresetModel> PresetModelList) {
        this.presetsList = PresetModelList;
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
