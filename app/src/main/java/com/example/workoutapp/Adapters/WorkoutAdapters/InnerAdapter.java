package com.example.workoutapp.Adapters.WorkoutAdapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workoutapp.Data.WorkoutDao.CARDIO_SET_DETAILS_TABLE_DAO;
import com.example.workoutapp.Data.WorkoutDao.STRENGTH_SET_DETAILS_TABLE_DAO;
import com.example.workoutapp.MainActivity;
import com.example.workoutapp.Models.WorkoutModels.CardioSetModel;
import com.example.workoutapp.Models.WorkoutModels.ExerciseModel;
import com.example.workoutapp.Models.WorkoutModels.StrengthSetModel;
import com.example.workoutapp.R;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class InnerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Object> setList;
    private final int exerciseId;
    private OnSetListChangedListener listener;
    private final Context context;
    private final ExerciseModel currentExercise;

    private static final int VIEW_TYPE_STRENGTH = 1;
    private static final int VIEW_TYPE_CARDIO = 2;

    private final STRENGTH_SET_DETAILS_TABLE_DAO strengthSetDetailsTableDao;
    private final CARDIO_SET_DETAILS_TABLE_DAO cardioSetDetailsTableDao;

    public final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private String exerciseType;

    public InnerAdapter(List<Object> setList, Context context, int exerciseId, OnSetListChangedListener listener, STRENGTH_SET_DETAILS_TABLE_DAO strengthDao, CARDIO_SET_DETAILS_TABLE_DAO cardioDao, String exType,
                        ExerciseModel currentExercise) {
        this.setList = setList;
        this.context = context;
        this.exerciseId = exerciseId;
        this.listener = listener;
        this.strengthSetDetailsTableDao = strengthDao;
        this.cardioSetDetailsTableDao = cardioDao;
        setHasStableIds(true);
        this.currentExercise = currentExercise;
        this.exerciseType = exType;
    }

    public void setOnSetListChangedListener(OnSetListChangedListener listener) {
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        Object item = setList.get(position);
        if (item instanceof StrengthSetModel) return VIEW_TYPE_STRENGTH;
        else if (item instanceof CardioSetModel) return VIEW_TYPE_CARDIO;
        return -1;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_STRENGTH) {
            View view = inflater.inflate(R.layout.set_strength_item_card, parent, false);
            return new StrengthViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.set_cardio_item_card, parent, false);
            return new CardioViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Object set = setList.get(position);
        boolean isFinished = "finished".equalsIgnoreCase(getSetState(set));

        if (holder instanceof StrengthViewHolder && set instanceof StrengthSetModel) {
            ((StrengthViewHolder) holder).bind((StrengthSetModel) set, isFinished, strengthSetDetailsTableDao, executor, currentExercise, (long) exerciseId);
        } else if (holder instanceof CardioViewHolder && set instanceof CardioSetModel) {
            ((CardioViewHolder) holder).bind((CardioSetModel) set, isFinished, cardioSetDetailsTableDao, executor, currentExercise, (long) exerciseId);
        }
    }

    @Override
    public int getItemCount() {
        return setList != null ? setList.size() : 0;
    }

    @Override
    public long getItemId(int position) {
        if (setList == null || position < 0 || position >= setList.size()) return RecyclerView.NO_ID;
        Object set = setList.get(position);
        if (set instanceof StrengthSetModel) {
            return ((StrengthSetModel) set).getStrength_set_id();
        } else if (set instanceof CardioSetModel) {
            return ((CardioSetModel) set).getCardio_set_id();
        }
        return RecyclerView.NO_ID;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateData(List<Object> newData) {
        this.setList = newData;
        notifyDataSetChanged();
    }

    public void addSet(Object newSet) {
        if (newSet != null) {
            setList.add(newSet);
            notifyItemInserted(setList.size() - 1);
        }
    }

    public void removeSet(int position) {
        if (position >= 0 && position < setList.size()) {
            setList.remove(position);
            notifyItemRemoved(position);
            if (listener != null) {
                listener.onSetListChanged(exerciseId, setList.isEmpty());
            }
            if (position < getItemCount()) {
                notifyItemRangeChanged(position, getItemCount() - position);
            }
            // Синхронизируем удаление
            MainActivity.getSyncManager().updateExerciseSets(currentExercise);
        }
    }

    private String getSetState(Object set) {
        if (set instanceof StrengthSetModel) {
            return ((StrengthSetModel) set).getStrength_set_state();
        } else if (set instanceof CardioSetModel) {
            return ((CardioSetModel) set).getCardio_set_state();
        }
        return "";
    }

    public void attachSwipeToDelete(RecyclerView recyclerView) {
        recyclerView.setItemAnimator(null);
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getBindingAdapterPosition();
                if (position == RecyclerView.NO_POSITION) return;

                Object set = setList.get(position);
                executor.execute(() -> {
                    if (set instanceof StrengthSetModel) {
                        strengthSetDetailsTableDao.deleteStrengthSet((StrengthSetModel) set, (long) exerciseId);
                    } else if (set instanceof CardioSetModel) {
                        cardioSetDetailsTableDao.deleteCardioSet((CardioSetModel) set, (long) exerciseId);
                    }

                    handler.post(() -> removeSet(position));
                });
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    public interface OnSetListChangedListener {
        void onSetListChanged(int changedExerciseId, boolean isEmpty);
    }

    // ViewHolder for Strength
    public static class StrengthViewHolder extends RecyclerView.ViewHolder {
        EditText weight, reps;
        CheckBox isSelected;
        LinearLayout liner;

        public StrengthViewHolder(@NonNull View itemView) {
            super(itemView);
            weight = itemView.findViewById(R.id.weight_ET);
            reps = itemView.findViewById(R.id.reps_ET);
            isSelected = itemView.findViewById(R.id.isSelected);
            liner = itemView.findViewById(R.id.foodCaloriesContainer_D_LL);
        }

        public void bind(StrengthSetModel set, boolean isFinished, STRENGTH_SET_DETAILS_TABLE_DAO dao, ExecutorService executor, ExerciseModel currentExercise, long exId) {
            weight.setText(set.getStrength_set_weight() == 0 ? "" : String.valueOf(set.getStrength_set_weight()));
            reps.setText(set.getStrength_set_rep() == 0 ? "" : String.valueOf(set.getStrength_set_rep()));

            weight.setBackgroundResource(R.drawable.edit_text_back2);
            reps.setBackgroundResource(R.drawable.edit_text_back2);

            isSelected.setOnCheckedChangeListener(null);
            isSelected.setChecked(isFinished);

            liner.setBackgroundResource(isFinished ? R.drawable.card_border3 : R.drawable.card_border2);
            weight.setEnabled(!isFinished);
            reps.setEnabled(!isFinished);

            isSelected.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    boolean weightEmpty = weight.getText().toString().trim().isEmpty();
                    boolean repsEmpty = reps.getText().toString().trim().isEmpty();

                    if (weightEmpty || repsEmpty) {
                        if (weightEmpty) weight.setBackgroundResource(R.drawable.edit_text_back3);
                        if (repsEmpty) reps.setBackgroundResource(R.drawable.edit_text_back3);
                        isSelected.setChecked(false);
                        return;
                    }

                    set.setStrength_set_state("finished");
                    executor.execute(() -> {
                        dao.updateStrengthSet(set, exId);
                        itemView.post(() -> {
                            liner.setBackgroundResource(R.drawable.card_border3);
                            weight.setEnabled(false);
                            reps.setEnabled(false);
                            MainActivity.getSyncManager().updateExerciseSets(currentExercise);
                        });
                    });
                } else {
                    set.setStrength_set_state("active");
                    executor.execute(() -> {
                        dao.updateStrengthSet(set, exId);
                        itemView.post(() -> {
                            liner.setBackgroundResource(R.drawable.card_border2);
                            weight.setEnabled(true);
                            reps.setEnabled(true);
                            MainActivity.getSyncManager().updateExerciseSets(currentExercise);
                        });
                    });
                }
            });

            weight.addTextChangedListener(new SimpleTextWatcher(s -> {
                double value = parseDoubleSafe(s);
                set.setStrength_set_weight(value);
                weight.setBackgroundResource(R.drawable.edit_text_back2);
                executor.execute(() -> {
                    dao.updateStrengthSet(set, exId);
                    MainActivity.getSyncManager().updateExerciseSets(currentExercise);
                });
            }));
            reps.addTextChangedListener(new SimpleTextWatcher(s -> {
                int value = parseIntSafe(s);
                set.setStrength_set_rep(value);
                reps.setBackgroundResource(R.drawable.edit_text_back2);
                executor.execute(() -> {
                    dao.updateStrengthSet(set, exId);
                    MainActivity.getSyncManager().updateExerciseSets(currentExercise);
                });
            }));
        }
    }

    // ViewHolder for Cardio
    public static class CardioViewHolder extends RecyclerView.ViewHolder {
        EditText time, distance, temp;
        CheckBox isSelected;
        LinearLayout liner;

        public CardioViewHolder(@NonNull View itemView) {
            super(itemView);
            time = itemView.findViewById(R.id.time_ET);
            distance = itemView.findViewById(R.id.distance_ET);
            temp = itemView.findViewById(R.id.temp_ET);
            isSelected = itemView.findViewById(R.id.isSelected);
            liner = itemView.findViewById(R.id.foodCaloriesContainer_D_LL);
        }

        public void bind(CardioSetModel set, boolean isFinished, CARDIO_SET_DETAILS_TABLE_DAO dao, ExecutorService executor, ExerciseModel currentExercise, long exId) {
            time.setText(set.getCardio_set_time() == 0 ? "" : String.valueOf(set.getCardio_set_time()));
            distance.setText(set.getCardio_set_distance() == 0 ? "" : String.valueOf(set.getCardio_set_distance()));
            temp.setText(set.getCardio_set_temp() == 0 ? "" : String.valueOf(set.getCardio_set_temp()));

            time.setBackgroundResource(R.drawable.edit_text_back2);
            distance.setBackgroundResource(R.drawable.edit_text_back2);
            temp.setBackgroundResource(R.drawable.edit_text_back2);

            isSelected.setOnCheckedChangeListener(null);
            isSelected.setChecked(isFinished);

            liner.setBackgroundResource(isFinished ? R.drawable.card_border3 : R.drawable.card_border2);
            time.setEnabled(!isFinished);
            distance.setEnabled(!isFinished);
            temp.setEnabled(!isFinished);

            isSelected.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    if (time.getText().toString().trim().isEmpty() || distance.getText().toString().trim().isEmpty() || temp.getText().toString().trim().isEmpty()) {
                        isSelected.setChecked(false);
                        return;
                    }
                    set.setCardio_set_state("finished");
                } else {
                    set.setCardio_set_state("active");
                }

                executor.execute(() -> {
                    dao.updateCardioSet(set, exId);
                    itemView.post(() -> {
                        liner.setBackgroundResource(isChecked ? R.drawable.card_border3 : R.drawable.card_border2);
                        time.setEnabled(!isChecked);
                        distance.setEnabled(!isChecked);
                        temp.setEnabled(!isChecked);
                        MainActivity.getSyncManager().updateExerciseSets(currentExercise);
                    });
                });
            });

            time.addTextChangedListener(new SimpleTextWatcher(s -> {
                set.setCardio_set_time(parseIntSafe(s));
                executor.execute(() -> {
                    dao.updateCardioSet(set, exId);
                    MainActivity.getSyncManager().updateExerciseSets(currentExercise);
                });
            }));
            distance.addTextChangedListener(new SimpleTextWatcher(s -> {
                set.setCardio_set_distance(parseDoubleSafe(s));
                executor.execute(() -> {
                    dao.updateCardioSet(set, exId);
                    MainActivity.getSyncManager().updateExerciseSets(currentExercise);
                });
            }));
            temp.addTextChangedListener(new SimpleTextWatcher(s -> {
                set.setCardio_set_temp(parseDoubleSafe(s));
                executor.execute(() -> {
                    dao.updateCardioSet(set, exId);
                    MainActivity.getSyncManager().updateExerciseSets(currentExercise);
                });
            }));
        }
    }

    private static class SimpleTextWatcher implements TextWatcher {
        private final Consumer<String> onTextChanged;
        public SimpleTextWatcher(Consumer<String> onTextChanged) { this.onTextChanged = onTextChanged; }
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
        @Override public void afterTextChanged(Editable s) { onTextChanged.accept(s.toString()); }
    }

    private static double parseDoubleSafe(String s) { try { return Double.parseDouble(s); } catch (NumberFormatException e) { return 0; } }
    private static int parseIntSafe(String s) { try { return Integer.parseInt(s); } catch (NumberFormatException e) { return 0; } }
}