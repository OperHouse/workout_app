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

    public InnerAdapter(List<SetsModel> setList, TempDataBaseEx tempDb) {
        this.setsList = setList;
        this.tempDataBaseEx = tempDb;
    }

    @NonNull
    @Override
    public InnerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.set_item_layout, parent, false);
        return new InnerViewHolder(v);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull InnerViewHolder holder, int position) {
        SetsModel set = setsList.get(position);

        // Убедитесь, что данные корректно отображаются
        if (set.getWeight() != 0.0 && set.getReps() != 0) {
            holder.weight.setText(Double.toString(set.getWeight()));
            holder.reps.setText(String.valueOf(set.getReps()));
        }

        holder.weight.clearFocus();
        holder.reps.clearFocus();

        // Добавляем слушатели на EditText для веса и повторений
        holder.weight.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
                // Не нужно ничего делать
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                if (!charSequence.toString().isEmpty()) {
                    try {
                        double weight = Double.parseDouble(charSequence.toString());
                        set.setWeight(weight);

                        // Добавляем измененный объект в список, если его еще нет
                        if (!modifiedSets.contains(set)) {
                            modifiedSets.add(set);
                        }
                    } catch (NumberFormatException e) {
                        Log.e("InnerAdapter", "Invalid weight input", e);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // Не нужно ничего делать
            }
        });

        // Слушатель для изменения количества повторений
        holder.reps.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
                // Не нужно ничего делать
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                if (!charSequence.toString().isEmpty()) {
                    try {
                        int reps = Integer.parseInt(charSequence.toString());
                        set.setReps(reps);

                        // Добавляем измененный объект в список, если его еще нет
                        if (!modifiedSets.contains(set)) {
                            modifiedSets.add(set);
                        }
                    } catch (NumberFormatException e) {
                        Log.e("InnerAdapter", "Invalid reps input", e);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // Не нужно ничего делать
            }
        });
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



    public static class InnerViewHolder extends RecyclerView.ViewHolder {
        EditText weight, reps;

        public InnerViewHolder(View itemView) {
            super(itemView);
            weight = itemView.findViewById(R.id.kg_textEd);
            reps = itemView.findViewById(R.id.reps_textEd);
        }
    }
}
