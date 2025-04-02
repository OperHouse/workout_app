package com.example.workoutapp.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workoutapp.Models.TempExModel;
import com.example.workoutapp.R;
import com.example.workoutapp.TempDataBaseEx;

import java.util.List;

public class OutsideAdapter extends RecyclerView.Adapter<OutsideAdapter.MyViewHolder> {

    private final Context context;
    TempDataBaseEx tempDataBaseEx;
    private List<TempExModel> tempExModelList;


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

    @Override
    public void onBindViewHolder(@NonNull OutsideAdapter.MyViewHolder holder, int position) {
        if(tempExModelList != null && !tempExModelList.isEmpty()){
            TempExModel tempExModelElm = tempExModelList.get(position);
            holder.name.setText(tempExModelElm.getExName());
            holder.type.setText(tempExModelElm.getTypeEx());

            // Проверяем, есть ли уже адаптер для этого элемента
            InnerAdapter innerAdapter = (InnerAdapter) holder.innerRecycler.getAdapter();
            if (innerAdapter == null) {
                // Если адаптер еще не установлен, создаем новый
                innerAdapter = new InnerAdapter(tempExModelElm.getSetsList());
                holder.innerRecycler.setAdapter(innerAdapter);
            } else {
                // Если адаптер уже существует, обновляем данные
                innerAdapter.updateData(tempExModelElm.getSetsList());
            }

            holder.addSet.setOnClickListener(v -> {
                // Handle adding a new set (this is a simple example)
                // You can replace this with a dialog to let the user choose the weight, reps, etc.
                tempDataBaseEx.addSet(tempExModelElm.getEx_id());
                tempExModelElm.setSetsList(tempDataBaseEx.getExerciseSets(tempExModelElm.getEx_id()));
                notifyDataSetChanged();
                tempDataBaseEx.logAllExercisesAndSets();


            });
        }
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
            innerRecycler.setLayoutManager(new LinearLayoutManager(itemView.getContext())); // Set the layout manager for the inner RecyclerVie
        }
    }
}
