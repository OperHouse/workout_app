package com.example.workoutapp.Adapters.WorkoutAdapters;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
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

import com.example.workoutapp.Data.WorkoutDao.CARDIO_SET_DETAILS_TABLE_DAO;
import com.example.workoutapp.Data.WorkoutDao.STRENGTH_SET_DETAILS_TABLE_DAO;
import com.example.workoutapp.Data.WorkoutDao.WORKOUT_EXERCISE_TABLE_DAO;
import com.example.workoutapp.MainActivity;
import com.example.workoutapp.Models.WorkoutModels.ExerciseModel;
import com.example.workoutapp.R;

import net.sqlcipher.database.SQLiteDatabase;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OutsideAdapter extends RecyclerView.Adapter<OutsideAdapter.MyViewHolder> {

    private final WORKOUT_EXERCISE_TABLE_DAO workoutExerciseTableDao;
    private final STRENGTH_SET_DETAILS_TABLE_DAO strengthSetDetailsTableDao;
    private final CARDIO_SET_DETAILS_TABLE_DAO cardioSetDetailsTableDao;
    private List<ExerciseModel> exerciseModelList;
    private final Context context;
    private OnExerciseListChangedListener listChangedListener;



    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public OutsideAdapter(@NonNull Fragment fragment) {
        this.context = fragment.requireContext();
        SQLiteDatabase db = MainActivity.getAppDataBase();
        this.workoutExerciseTableDao = new WORKOUT_EXERCISE_TABLE_DAO(db);
        this.strengthSetDetailsTableDao = new STRENGTH_SET_DETAILS_TABLE_DAO(db);
        this.cardioSetDetailsTableDao = new CARDIO_SET_DETAILS_TABLE_DAO(db);
        setHasStableIds(true);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.ex_item_workout_layout, parent, false);
        return new MyViewHolder(v, strengthSetDetailsTableDao, cardioSetDetailsTableDao, context);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        if (exerciseModelList == null || exerciseModelList.isEmpty() || position < 0 || position >= exerciseModelList.size()) {
            return;
        }

        ExerciseModel exerciseModel = exerciseModelList.get(position);
        holder.bind(exerciseModel);
    }

    @Override
    public int getItemCount() {
        return exerciseModelList != null ? exerciseModelList.size() : 0;
    }

    @Override
    public long getItemId(int position) {
        if (exerciseModelList == null || position < 0 || position >= exerciseModelList.size())
            return RecyclerView.NO_ID;
        return exerciseModelList.get(position).getExercise_id();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateExList(List<ExerciseModel> ExModelList) {
        this.exerciseModelList = ExModelList;
        notifyDataSetChanged();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView name, type;
        Button addSet, delEx;
        ConstraintLayout strengthCL, cardioCL;
        RecyclerView innerRecycler;
        InnerAdapter innerAdapter;
        ExerciseModel exerciseModel;

        private final STRENGTH_SET_DETAILS_TABLE_DAO strengthDao;
        private final CARDIO_SET_DETAILS_TABLE_DAO cardioDao;
        private final Context ctx;

        MyViewHolder(@NonNull View itemView,
                     STRENGTH_SET_DETAILS_TABLE_DAO strengthDao,
                     CARDIO_SET_DETAILS_TABLE_DAO cardioDao,
                     Context ctx) {
            super(itemView);
            this.strengthDao = strengthDao;
            this.cardioDao = cardioDao;
            this.ctx = ctx;

            name = itemView.findViewById(R.id.exerciseName_OUTSIDE_CARD_TV);
            type = itemView.findViewById(R.id.typeExercise_OUTSIDE_CARD_TV);
            addSet = itemView.findViewById(R.id.addSet_OUTSIDE_CARD_Btn);
            delEx = itemView.findViewById(R.id.deleteExercise_OUTSIDE_CARD_BTN);
            strengthCL = itemView.findViewById(R.id.strength_OUTSIDE_CARD_CL);
            cardioCL = itemView.findViewById(R.id.cardio_OUTSIDE_CARD_CL);
            innerRecycler = itemView.findViewById(R.id.OUTSIDE_CARD_inner_RV);
            innerRecycler.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
            innerRecycler.setNestedScrollingEnabled(false);
        }

        void bind(ExerciseModel exerciseModel) {
            this.exerciseModel = exerciseModel;
            name.setText(exerciseModel.getExerciseName());
            type.setText(exerciseModel.getExerciseType());

            int exerciseId = (int) exerciseModel.getExercise_id();

            if (innerAdapter == null) {
                // Исправлено: передаем слушатель прямо в конструктор InnerAdapter
                innerAdapter = new InnerAdapter(exerciseModel.getSets(), ctx, exerciseId, (changedExerciseId, isEmpty) -> {
                    if (isEmpty) {
                        strengthCL.setVisibility(View.GONE);
                        cardioCL.setVisibility(View.GONE);
                        innerRecycler.setVisibility(View.GONE);
                    }
                }, strengthDao, cardioDao, exerciseModel.getExerciseType(), exerciseModel);
                innerRecycler.setAdapter(innerAdapter);
                innerAdapter.attachSwipeToDelete(innerRecycler);
            } else {
                innerAdapter.updateData(exerciseModel.getSets());
            }

            if (!exerciseModel.getSets().isEmpty()) {
                if ("Время".equalsIgnoreCase(exerciseModel.getExerciseType()) || "Кардио".equalsIgnoreCase(exerciseModel.getExerciseType())) {
                    strengthCL.setVisibility(View.GONE);
                    cardioCL.setVisibility(View.VISIBLE);
                } else {
                    strengthCL.setVisibility(View.VISIBLE);
                    cardioCL.setVisibility(View.GONE);
                }
                innerRecycler.setVisibility(View.VISIBLE);
            } else {
                strengthCL.setVisibility(View.GONE);
                cardioCL.setVisibility(View.GONE);
                innerRecycler.setVisibility(View.GONE);
            }

            addSet.setOnClickListener(v -> {
                boolean wasEmpty = exerciseModel.getSets().isEmpty();
                executor.execute(() -> {
                    Object newSet;
                    if ("Время".equalsIgnoreCase(exerciseModel.getExerciseType()) || "Кардио".equalsIgnoreCase(exerciseModel.getExerciseType())) {
                        cardioDao.addCardioSet(exerciseId);
                        newSet = cardioDao.getLastCardioSet(exerciseId);
                    } else {
                        strengthDao.addStrengthSet(exerciseId);
                        newSet = strengthDao.getLastStrengthSet(exerciseId);
                    }

                    mainHandler.post(() -> {
                        innerAdapter.addSet(newSet);
                        Log.d("SyncDebug", "Exercise: " + exerciseModel.getExerciseName() + " UID: " + exerciseModel.getExercise_uid());
                        MainActivity.getSyncManager().updateExerciseSets(exerciseModel);
                        if (wasEmpty) {
                            if ("Время".equalsIgnoreCase(exerciseModel.getExerciseType()) ||
                                    "Кардио".equalsIgnoreCase(exerciseModel.getExerciseType())) {
                                cardioCL.setVisibility(View.VISIBLE);
                                strengthCL.setVisibility(View.GONE);
                            } else {
                                cardioCL.setVisibility(View.GONE);
                                strengthCL.setVisibility(View.VISIBLE);
                            }
                            innerRecycler.setVisibility(View.VISIBLE);
                        }
                    });
                });

            });

            delEx.setOnClickListener(v -> delConfirmDialog(exerciseModel, getBindingAdapterPosition()));
        }

        private void delConfirmDialog(ExerciseModel elm, int position) {
            Dialog dialogCreateEx = new Dialog(ctx);
            dialogCreateEx.setContentView(R.layout.confirm_dialog_layout);
            Objects.requireNonNull(dialogCreateEx.getWindow())
                    .setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialogCreateEx.setCancelable(true);

            Button deleteBtn = dialogCreateEx.findViewById(R.id.delete_confirm_D_BTN);
            Button chanelBtn = dialogCreateEx.findViewById(R.id.delete_cancel_D_BTN);
            TextView text1 = dialogCreateEx.findViewById(R.id.delete_title_D_TV);
            TextView text2 = dialogCreateEx.findViewById(R.id.delete_message_D_TV);

            deleteBtn.setText("Удалить");
            text1.setText("Удаление упражнения");
            text2.setText("Вы действительно хотите удалить упражнение и все его подходы?");

            if (dialogCreateEx.getWindow() != null) {
                dialogCreateEx.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }

            chanelBtn.setOnClickListener(v1 -> dialogCreateEx.dismiss());

            deleteBtn.setOnClickListener(v12 -> {
                dialogCreateEx.dismiss();
                if (position >= 0 && position < (exerciseModelList != null ? exerciseModelList.size() : 0)) {
                    executor.execute(() -> {
                        workoutExerciseTableDao.deleteExercise(elm);
                        mainHandler.post(() -> {
                            exerciseModelList.remove(position);
                            notifyItemRemoved(position);
                            if (listChangedListener != null) {
                                listChangedListener.onExerciseListChanged(exerciseModelList);
                            }
                        });
                    });
                }
            });

            dialogCreateEx.show();
        }
    }
    public void setOnExerciseListChangedListener(OnExerciseListChangedListener listener) {
        this.listChangedListener = listener;
    }
    public interface OnExerciseListChangedListener {
        void onExerciseListChanged(List<ExerciseModel> newList);
    }
}