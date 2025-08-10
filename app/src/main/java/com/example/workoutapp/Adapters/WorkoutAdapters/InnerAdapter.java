package com.example.workoutapp.Adapters.WorkoutAdapters;

import android.annotation.SuppressLint;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workoutapp.Models.WorkoutModels.CardioSetModel;
import com.example.workoutapp.Models.WorkoutModels.StrengthSetModel;
import com.example.workoutapp.R;

import java.util.List;

public class InnerAdapter extends RecyclerView.Adapter<InnerAdapter.InnerViewHolder> {

    private List<Object> setList;
    private int exerciseId;
    private OnSetListChangedListener listener;

    private static final int VIEW_TYPE_STRENGTH = 1;
    private static final int VIEW_TYPE_CARDIO = 2;

    public InnerAdapter(List<Object> setList, int ex_id) {
        this.setList = setList;
        this.exerciseId = ex_id;
    }

    // Метод для установки слушателя
    public void setOnSetListChangedListener(OnSetListChangedListener listener) {
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        Object set = setList.get(position);
        if (set instanceof StrengthSetModel) {
            return VIEW_TYPE_STRENGTH;
        } else if (set instanceof CardioSetModel) {
            return VIEW_TYPE_CARDIO;
        }
        return -1; // Возвращаем -1, если тип неизвестен
    }

    @NonNull
    @Override
    public InnerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v;
        if (viewType == VIEW_TYPE_STRENGTH) {
            v = inflater.inflate(R.layout.set_strength_item_card, parent, false);
        } else {
            v = inflater.inflate(R.layout.set_cardio_item_card, parent, false);
        }
        return new InnerViewHolder(v, viewType);
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void onBindViewHolder(@NonNull InnerViewHolder holder, int position) {
        Object set = setList.get(position);

        // Удаляем старые TextWatcher'ы
        if (holder.weightWatcher != null) holder.weight.removeTextChangedListener(holder.weightWatcher);
        if (holder.repsWatcher != null) holder.reps.removeTextChangedListener(holder.repsWatcher);
        if (holder.timeWatcher != null) holder.time.removeTextChangedListener(holder.timeWatcher);
        if (holder.distanceWatcher != null) holder.distance.removeTextChangedListener(holder.distanceWatcher);

        // Общая логика для CheckBox, которую можно применять к обоим типам
        holder.isSelected.setOnCheckedChangeListener(null); // Убираем старый слушатель, чтобы избежать повторных вызовов
        holder.isSelected.setChecked("finished".equals(getStateFromObject(set)));

        if (holder.viewType == VIEW_TYPE_STRENGTH) {
            StrengthSetModel strengthSet = (StrengthSetModel) set;

            // Устанавливаем значения
            holder.weight.setText(strengthSet.getWeight() > 0 ? String.valueOf(strengthSet.getWeight()) : "");
            holder.reps.setText(strengthSet.getRep() > 0 ? String.valueOf(strengthSet.getRep()) : "");

            if ("finished".equals(strengthSet.getState())) {
                holder.weight.setEnabled(false);
                holder.reps.setEnabled(false);
                holder.isSelected.setBackgroundResource(R.drawable.checkbox_checked);
                holder.liner.setBackgroundResource(R.drawable.card_border3);
            } else {
                holder.weight.setEnabled(true);
                holder.reps.setEnabled(true);
                holder.isSelected.setBackgroundResource(R.drawable.checkbox_unchecked);
                holder.liner.setBackgroundResource(R.drawable.card_border2);

                holder.weightWatcher = new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }

                };
                holder.repsWatcher = new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                };
                holder.weight.addTextChangedListener(holder.weightWatcher);
                holder.reps.addTextChangedListener(holder.repsWatcher);
            }
        } else { // VIEW_TYPE_CARDIO
            CardioSetModel cardioSet = (CardioSetModel) set;

            // Устанавливаем значения
            holder.time.setText(cardioSet.getTime() > 0 ? String.valueOf(cardioSet.getTime()) : "");
            holder.distance.setText(cardioSet.getDistance() > 0 ? String.valueOf(cardioSet.getDistance()) : "");

            if ("finished".equals(cardioSet.getState())) {
                holder.time.setEnabled(false);
                holder.distance.setEnabled(false);
                holder.isSelected.setBackgroundResource(R.drawable.checkbox_checked);
                holder.liner.setBackgroundResource(R.drawable.card_border3);
            } else {
                holder.time.setEnabled(true);
                holder.distance.setEnabled(true);
                holder.isSelected.setBackgroundResource(R.drawable.checkbox_unchecked);
                holder.liner.setBackgroundResource(R.drawable.card_border2);

                holder.timeWatcher = new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                };
                holder.distanceWatcher = new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                };
                holder.time.addTextChangedListener(holder.timeWatcher);
                holder.distance.addTextChangedListener(holder.distanceWatcher);
            }
        }

        // Общая логика для CheckBox
        holder.isSelected.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // ... Ваша логика для CheckBox.
            // Нужно будет определить, какой именно объект (strengthSet или cardioSet)
            // и вызывать соответствующие методы TempWorkDao
            holder.itemView.post(() -> notifyItemChanged(position));
        });
    }

    private String getStateFromObject(Object set) {
        if (set instanceof StrengthSetModel) {
            return ((StrengthSetModel) set).getState();
        } else if (set instanceof CardioSetModel) {
            return ((CardioSetModel) set).getState();
        }
        return null;
    }

    @Override
    public int getItemCount() {
        return setList.size();
    }

    public void updateData(List<Object> newSetsList, int ex_id) {
        this.setList = newSetsList;
        this.exerciseId = ex_id;
        notifyDataSetChanged();
    }

    public void attachSwipeToDelete(RecyclerView recyclerView, int ex_id) {
        // ... Ваша логика для свайпа.
    }

    // Интерфейс для связи с родительским адаптером
    public interface OnSetListChangedListener {
        void onSetListChanged(int changedExerciseId, boolean isEmpty);
    }

    public static class InnerViewHolder extends RecyclerView.ViewHolder {
        // Поля для силовых упражнений
        EditText weight, reps;
        TextWatcher weightWatcher, repsWatcher;

        // Поля для кардио-упражнений
        EditText time, distance;
        TextWatcher timeWatcher, distanceWatcher;

        CheckBox isSelected;
        LinearLayout liner;
        int viewType;

        public InnerViewHolder(@NonNull View itemView, int viewType) {
            super(itemView);
            this.viewType = viewType;

            if (viewType == VIEW_TYPE_STRENGTH) {
                weight = itemView.findViewById(R.id.weight_ET);
                reps = itemView.findViewById(R.id.reps_ET);
            } else { // VIEW_TYPE_CARDIO
                time = itemView.findViewById(R.id.time_ET);
                distance = itemView.findViewById(R.id.distance_ET);
            }
            isSelected = itemView.findViewById(R.id.isSelected);
            liner = itemView.findViewById(R.id.linearLayout);
        }
    }
}