package com.example.workoutapp.Adapters;

import android.annotation.SuppressLint;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workoutapp.Models.SetsModel;
import com.example.workoutapp.R;
import com.example.workoutapp.TempDataBaseEx;

import java.util.ArrayList;
import java.util.List;

public class InnerAdapter extends RecyclerView.Adapter<InnerAdapter.InnerViewHolder> {
    private List<SetsModel> setsList;
    private int exerciseId;
    private TempDataBaseEx tempDataBaseEx;

    // Список для хранения измененных данных, которые будут записаны в БД
    private List<SetsModel> modifiedSets = new ArrayList<>();

    public InnerAdapter(List<SetsModel> setList, TempDataBaseEx tempDb, int ex_id) {
        this.setsList = setList;
        this.tempDataBaseEx = tempDb;
        this.exerciseId = ex_id;
    }

    @NonNull
    @Override
    public InnerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.set_item_layout, parent, false);
        return new InnerViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull InnerViewHolder holder, int position) {
        SetsModel set = setsList.get(position);

        // Удаляем старые TextWatcher'ы если есть
        if (holder.weightWatcher != null) {
            holder.weight.removeTextChangedListener(holder.weightWatcher);
        }
        if (holder.repsWatcher != null) {
            holder.reps.removeTextChangedListener(holder.repsWatcher);
        }

        // Устанавливаем значения
        holder.weight.setText(set.getWeight() > 0 ? String.valueOf(set.getWeight()) : "");
        holder.reps.setText(set.getReps() > 0 ? String.valueOf(set.getReps()) : "");

        // Создаём новый TextWatcher для веса
        holder.weightWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    int weight = Integer.parseInt(s.toString());
                    set.setWeight(weight);
                    if (!modifiedSets.contains(set)) modifiedSets.add(set);
                } catch (NumberFormatException e) {
                    set.setWeight(0);
                }
            }
        };
        holder.weight.addTextChangedListener(holder.weightWatcher);

        // Новый TextWatcher для повторений
        holder.repsWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    int reps = Integer.parseInt(s.toString());
                    set.setReps(reps);
                    if (!modifiedSets.contains(set)) modifiedSets.add(set);
                } catch (NumberFormatException e) {
                    set.setReps(0);
                }
            }
        };
        holder.reps.addTextChangedListener(holder.repsWatcher);
    }

    @Override
    public int getItemCount() {
        return setsList.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateData(List<SetsModel> newSetsList, int ex_id) {
        this.exerciseId = ex_id;
        if (setsList.size() != newSetsList.size()) {
            this.setsList.clear();
            this.setsList.addAll(newSetsList);
            notifyDataSetChanged(); // Notify the adapter that the data has changed
        }
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);

        // Записываем все изменения в базу данных, только когда адаптер удаляется
        if (!modifiedSets.isEmpty()) {
            for (SetsModel modifiedSet : modifiedSets) {
                tempDataBaseEx.updateOrInsertSet(modifiedSet, exerciseId);
            }
            Log.d("InnerAdapter", "Changes saved to the database");
        }
    }
    public void attachSwipeToDelete(RecyclerView recyclerView, int ex_id) {
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                SetsModel set = setsList.get(position);
                tempDataBaseEx.deleteSet(ex_id, set.getSet());
                setsList.remove(position);
                notifyItemRemoved(position);
                tempDataBaseEx.logAllExercisesAndSets();
                Log.d("InnerAdapter", "Set deleted: " + set.getSet());
            }
        });
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }
    public void saveModifiedSetsToDb() {
        if (setsList != null && !setsList.isEmpty()) {
            for (SetsModel set : setsList) {
                if (set.getWeight() > 0 && set.getReps() > 0) {
                    tempDataBaseEx.updateOrInsertSet(set, exerciseId);
                }
            }
            Log.d("InnerAdapter", "Changes saved to the database via method");
        }
    }



    public static class InnerViewHolder extends RecyclerView.ViewHolder {
        EditText weight, reps;
        TextWatcher weightWatcher;
        TextWatcher repsWatcher;

        public InnerViewHolder(View itemView) {
            super(itemView);
            weight = itemView.findViewById(R.id.kg_textEd);
            reps = itemView.findViewById(R.id.reps_textEd);
        }
    }


}
