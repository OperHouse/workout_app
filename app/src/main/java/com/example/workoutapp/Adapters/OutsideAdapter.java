package com.example.workoutapp.Adapters;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.util.SparseArray;
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

import com.example.workoutapp.DAO.TempWorkoutDao;
import com.example.workoutapp.MainActivity;
import com.example.workoutapp.Models.TempExModel;
import com.example.workoutapp.R;
import com.example.workoutapp.Workout.WorkoutFragment;

import java.util.List;
import java.util.Objects;

public class OutsideAdapter extends RecyclerView.Adapter<OutsideAdapter.MyViewHolder> {

    private final Context context;
    private TempWorkoutDao TempWorkDao;
    private List<TempExModel> tempExModelList;
    private Fragment fragment;

    // Список для хранения всех адаптеров InnerAdapter
    private RecyclerView outerRecyclerView;
    private final SparseArray<InnerAdapter> allInnerAdapters = new SparseArray<>();

    public OutsideAdapter(@NonNull Fragment fragment, RecyclerView recyclerView) {
        this.context = fragment.requireContext();
        this.fragment = fragment;
        this.TempWorkDao = new TempWorkoutDao(MainActivity.getAppDataBase());
        this.outerRecyclerView = recyclerView; // сохраняем ссылку
    }

    @NonNull
    @Override
    public OutsideAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.ex_item_workout_layout, parent, false);
        return new OutsideAdapter.MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        if (tempExModelList == null || tempExModelList.isEmpty()) return;

        TempExModel tempExModelElm = tempExModelList.get(position);
        holder.name.setText(tempExModelElm.getExName());
        holder.type.setText(tempExModelElm.getTypeEx());

        final int exerciseId = tempExModelElm.getEx_id();


        InnerAdapter innerAdapter = allInnerAdapters.get(exerciseId);
        if (innerAdapter == null) {
            innerAdapter = new InnerAdapter(tempExModelElm.getSetsList(), exerciseId);
            allInnerAdapters.put(exerciseId, innerAdapter);
        } else {
            innerAdapter.updateData(tempExModelElm.getSetsList(), exerciseId);
        }

        RecyclerView.Adapter currentAdapter = holder.innerRecycler.getAdapter();
        if (currentAdapter != innerAdapter) {
            holder.innerRecycler.setAdapter(innerAdapter);
            innerAdapter.attachSwipeToDelete(holder.innerRecycler, exerciseId); // <- важно делать при смене адаптера
        }

        innerAdapter.setOnSetListChangedListener((changedExerciseId, isEmpty) -> {
            if (changedExerciseId == exerciseId) {
                holder.constraintLayout.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
            }
        });

        holder.constraintLayout.setVisibility(tempExModelElm.getSetsList().isEmpty() ? View.GONE : View.VISIBLE);

        InnerAdapter finalInnerAdapter = innerAdapter;
        holder.addSet.setOnClickListener(v -> {
            saveAllInnerAdapters();
            TempWorkDao.addTempSet(exerciseId);
            tempExModelElm.setSetsList(TempWorkDao.getTempSetsByExercise(exerciseId));
            finalInnerAdapter.updateData(tempExModelElm.getSetsList(), exerciseId);
            finalInnerAdapter.notifyItemInserted(finalInnerAdapter.getItemCount() - 1);
            outerRecyclerView.scrollToPosition(0);
        });

        holder.delEx.setOnClickListener(v -> delConfirmDialog(tempExModelElm, position));
    }

    // Метод для отображения диалога подтверждения удаления
    private void delConfirmDialog(TempExModel elm, int position) {
        Dialog dialogCreateEx = new Dialog(context);
        dialogCreateEx.setContentView(R.layout.confirm_dialog_layout);
        Objects.requireNonNull(dialogCreateEx.getWindow()).setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialogCreateEx.setCancelable(true);
        dialogCreateEx.setCanceledOnTouchOutside(true);

        Button deleteBtn = dialogCreateEx.findViewById(R.id.btnDelete);
        Button chanelBtn = dialogCreateEx.findViewById(R.id.btnChanel);
        TextView text1 = dialogCreateEx.findViewById(R.id.textView);
        TextView text2 = dialogCreateEx.findViewById(R.id.text1);

        deleteBtn.setText("Удалить");
        text1.setText("Удаление упражнения");
        text2.setText("Вы действительно хотите удалить упражнение и все его подходы?");

        if (dialogCreateEx.getWindow() != null) {
            dialogCreateEx.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }

        chanelBtn.setOnClickListener(v -> dialogCreateEx.dismiss());

        deleteBtn.setOnClickListener(v -> {
            dialogCreateEx.dismiss();

            int exIdToDelete = elm.getEx_id();
            TempWorkDao.deleteTempExerciseWithSets(exIdToDelete);

            // Обновляем ID всех упражнений
            tempExModelList.remove(position);
            for (int i = 0; i < tempExModelList.size(); i++) {
                TempExModel model = tempExModelList.get(i);
                int newId = i;
                int oldId = model.getEx_id();
                if (oldId != newId) {
                    TempWorkDao.updateTempExerciseId(oldId, newId);
                    TempWorkDao.updateTempSetsExerciseId(oldId, newId);
                }
            }

            //Вот тут я удаляю RecyclerView и потом пересоздаю его
            outerRecyclerView.setAdapter(null);
            ((WorkoutFragment) fragment).refreshAdapter();

        });

        dialogCreateEx.show();
    }


    // Метод для сохранения изменений во всех адаптерах
    public void saveAllInnerAdapters() {
        for (int i = 0; i < allInnerAdapters.size(); i++) {
            InnerAdapter adapter = allInnerAdapters.valueAt(i);
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


    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView name;
        TextView type;
        Button addSet;
        Button delEx;
        ConstraintLayout constraintLayout;

        RecyclerView innerRecycler;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            type = itemView.findViewById(R.id.type);
            addSet = itemView.findViewById(R.id.addSetBtn);
            delEx = itemView.findViewById(R.id.delExBtn);
            constraintLayout = itemView.findViewById(R.id.cons);
            innerRecycler = itemView.findViewById(R.id.innerRecycle);
            innerRecycler.setLayoutManager(new LinearLayoutManager(itemView.getContext())); // Устанавливаем LayoutManager для внутреннего RecyclerView
        }


    }
}