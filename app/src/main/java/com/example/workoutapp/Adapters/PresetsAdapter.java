package com.example.workoutapp.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workoutapp.Models.ExModel;
import com.example.workoutapp.Models.PresetModel;
import com.example.workoutapp.R;
import com.example.workoutapp.Data.TempDataBaseEx;
import com.example.workoutapp.Workout.WorkoutFragment;

import java.util.List;

public class PresetsAdapter  extends RecyclerView.Adapter<PresetsAdapter.MyViewHolder> {

    private final Context context;
    private List<PresetModel> presetsList;
    private TempDataBaseEx tempDataBaseEx;
    private Fragment fragment;

    public PresetsAdapter(@NonNull Fragment fragment) {
        this.context = fragment.requireContext();
        this.tempDataBaseEx = new TempDataBaseEx(context);
        this.fragment = fragment;
    }

    @NonNull
    @Override
    public PresetsAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.preset_item_layout, parent, false);
        return new PresetsAdapter.MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
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

        holder.itemView.setOnClickListener(v ->{
                for (ExModel s:currentPresetModel.getExercises()) {
                    String exName = s.getExName();
                    boolean exerciseExists = tempDataBaseEx.checkIfExerciseExists(exName);
                    if(!exerciseExists){
                        tempDataBaseEx.addExercise(s.getExName(), s.getExType(), s.getBodyType());
                    }
                }
                // Переход к новому фрагменту
                FragmentManager fragmentManager = fragment.getParentFragmentManager(); // Use the fragment reference to get FragmentManager
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.frameLayout, new WorkoutFragment()); // Replace with the new fragment
                fragmentTransaction.addToBackStack(null); // Add to back stack if you want to navigate back
                fragmentTransaction.commit();
            });



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
