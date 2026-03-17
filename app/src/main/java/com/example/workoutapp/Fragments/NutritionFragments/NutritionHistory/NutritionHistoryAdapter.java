package com.example.workoutapp.Fragments.NutritionFragments.NutritionHistory;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workoutapp.R;

import java.util.List;
import java.util.Locale;

public class NutritionHistoryAdapter extends RecyclerView.Adapter<NutritionHistoryAdapter.ViewHolder> {

    private final List<NutritionSessionModel> sessionList;
    private final OnNutrClickListener listener;

    public interface OnNutrClickListener {
        void onDayClick(NutritionSessionModel session);
    }

    public NutritionHistoryAdapter(List<NutritionSessionModel> sessionList, OnNutrClickListener listener) {
        this.sessionList = sessionList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_nutrition_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NutritionSessionModel session = sessionList.get(position);

        // Установка даты
        holder.tvDate.setText(session.getDate());

        // Установка калорий
        holder.tvCalories.setText(String.format(Locale.getDefault(), "%,.0f ккал", session.getTotalCalories()));

        // Установка количества приемов пищи
        holder.tvMealsCount.setText("Приемов пищи: " + session.getMealsCount());

        // Форматирование: Название + Перенос строки + Значение + г
        holder.tvProtein.setText(String.format(Locale.getDefault(), "Белки\n%.0f г", session.getTotalProtein()));
        holder.tvFat.setText(String.format(Locale.getDefault(), "Жиры\n%.0f г", session.getTotalFat()));
        holder.tvCarb.setText(String.format(Locale.getDefault(), "Углеводы\n%.0f г", session.getTotalCarbs()));

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDayClick(session);
            }
        });
    }

    @Override
    public int getItemCount() {
        return sessionList != null ? sessionList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvCalories, tvMealsCount, tvProtein, tvFat, tvCarb;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.nutr_item_date);
            tvCalories = itemView.findViewById(R.id.nutr_item_calories);
            tvMealsCount = itemView.findViewById(R.id.nutr_item_meals_count);
            tvProtein = itemView.findViewById(R.id.nutr_item_protein);
            tvFat = itemView.findViewById(R.id.nutr_item_fat);
            tvCarb = itemView.findViewById(R.id.nutr_item_carb);
        }
    }
}