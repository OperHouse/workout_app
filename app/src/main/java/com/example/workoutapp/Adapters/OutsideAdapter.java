package com.example.workoutapp.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workoutapp.Models.SetsModel;
import com.example.workoutapp.Models.TempExModel;
import com.example.workoutapp.R;
import com.example.workoutapp.TempDataBaseEx;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OutsideAdapter extends RecyclerView.Adapter<OutsideAdapter.MyViewHolder> {

    private final Context context;
    private TempDataBaseEx tempDataBaseEx;
    private List<TempExModel> tempExModelList;

    // Список для хранения всех адаптеров InnerAdapter
    private final Map<Integer, InnerAdapter> allInnerAdapters = new HashMap<>();  // Используем Map для хранения адаптеров

    public OutsideAdapter(@NonNull Fragment fragment) {
        this.context = fragment.requireContext();
        this.tempDataBaseEx = new TempDataBaseEx(context);
    }

    @NonNull
    @Override
    public OutsideAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.ex_item_workout_layout, parent, false);
        return new OutsideAdapter.MyViewHolder(v);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onBindViewHolder(@NonNull OutsideAdapter.MyViewHolder holder, int position) {
        if (tempExModelList != null && !tempExModelList.isEmpty()) {
            TempExModel tempExModelElm = tempExModelList.get(position);
            holder.name.setText(tempExModelElm.getExName());
            holder.type.setText(tempExModelElm.getTypeEx());

            // Проверяем, есть ли уже адаптер для этого элемента

            // Получаем ID упражнения
            int exerciseId = tempExModelElm.getEx_id();

            // Проверяем, существует ли адаптер для этого упражнения в Map
            InnerAdapter innerAdapter = allInnerAdapters.get(exerciseId);

            if (innerAdapter == null) {
                // Если адаптер еще не существует, создаем новый
                innerAdapter = new InnerAdapter(tempExModelElm.getSetsList(), tempDataBaseEx, exerciseId);
                holder.innerRecycler.setAdapter(innerAdapter);
                innerAdapter.attachSwipeToDelete(holder.innerRecycler, exerciseId);

                // Сохраняем адаптер в Map
                allInnerAdapters.put(exerciseId, innerAdapter);
            } else {
                // Если адаптер уже существует, обновляем данные
                innerAdapter.updateData(tempExModelElm.getSetsList(), exerciseId);
                innerAdapter.notifyDataSetChanged();
            }

            // Обработчик нажатия на кнопку "Добавить сет"
            holder.addSet.setOnClickListener(v -> {
                // Сохраняем изменения во всех адаптерах перед добавлением нового сета
                saveAllInnerAdapters();

                // Добавляем новый сет
                tempDataBaseEx.addSet(tempExModelElm.getEx_id());
                tempExModelElm.setSetsList(tempDataBaseEx.getExerciseSets(tempExModelElm.getEx_id()));

                // Обновляем данные и уведомляем адаптер
                notifyDataSetChanged();

                // Логируем все упражнения и сеты
                tempDataBaseEx.logAllExercisesAndSets();
            });
        }
    }

    // Метод для сохранения изменений во всех адаптерах
    public void saveAllInnerAdapters() {
        // Перебираем все адаптеры в Map, используя entrySet()
        for (Map.Entry<Integer, InnerAdapter> entry : allInnerAdapters.entrySet()) {
            InnerAdapter adapter = entry.getValue();
            adapter.saveModifiedSetsToDb();
        }
        Log.d("OutsideAdapter", "All inner adapter changes saved");
    }

    @Override
    public int getItemCount() {
        if (tempExModelList != null) {
            return tempExModelList.size();
        }
        return 0;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateExList(List<TempExModel> tempExModelList) {
        this.tempExModelList = tempExModelList;
        notifyDataSetChanged();
    }
    public void saveChangesToDatabase() {
        for (TempExModel exModel : tempExModelList) {
            // Внутри каждого элемента сохраняем изменения через адаптер
            if (exModel.getSetsList() != null && !exModel.getSetsList().isEmpty()) {
                for (SetsModel set : exModel.getSetsList()) {
                    tempDataBaseEx.updateOrInsertSet(set, exModel.getEx_id());
                }
            }
        }
        Log.d("OutsideAdapter", "All changes saved to the database");
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView name;
        TextView type;
        Button addSet;

        RecyclerView innerRecycler;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            type = itemView.findViewById(R.id.type);
            addSet = itemView.findViewById(R.id.addSetBtn);
            innerRecycler = itemView.findViewById(R.id.innerRecycle);
            innerRecycler.setLayoutManager(new LinearLayoutManager(itemView.getContext())); // Устанавливаем LayoutManager для внутреннего RecyclerView
        }
    }
}
