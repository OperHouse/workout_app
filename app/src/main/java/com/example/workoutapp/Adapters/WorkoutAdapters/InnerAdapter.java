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
import com.example.workoutapp.Models.WorkoutModels.CardioSetModel;
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

    private static final int VIEW_TYPE_STRENGTH = 1;
    private static final int VIEW_TYPE_CARDIO = 2;

    private final STRENGTH_SET_DETAILS_TABLE_DAO strengthSetDetailsTableDao;
    private final CARDIO_SET_DETAILS_TABLE_DAO cardioSetDetailsTableDao;

    public final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private String exerciseType;

    public InnerAdapter(List<Object> setList, Context context, int exerciseId, OnSetListChangedListener listener, STRENGTH_SET_DETAILS_TABLE_DAO strengthDao, CARDIO_SET_DETAILS_TABLE_DAO cardioDao, String exType) {
        this.setList = setList;
        this.context = context;
        this.exerciseId = exerciseId;
        this.listener = listener;
        this.strengthSetDetailsTableDao = strengthDao;
        this.cardioSetDetailsTableDao = cardioDao;
        setHasStableIds(true);
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
            ((StrengthViewHolder) holder).bind((StrengthSetModel) set, isFinished, strengthSetDetailsTableDao, executor);
        } else if (holder instanceof CardioViewHolder && set instanceof CardioSetModel) {
            ((CardioViewHolder) holder).bind((CardioSetModel) set, isFinished, cardioSetDetailsTableDao, executor);
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
        }
    }

    private String getSetState(Object set) {
        if (set instanceof StrengthSetModel) {
            return ((StrengthSetModel) set).getState();
        } else if (set instanceof CardioSetModel) {
            return ((CardioSetModel) set).getState();
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
                        strengthSetDetailsTableDao.deleteStrengthSet((StrengthSetModel) set);
                    } else if (set instanceof CardioSetModel) {
                        cardioSetDetailsTableDao.deleteCardioSet((CardioSetModel) set);
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
            liner = itemView.findViewById(R.id.linearLayout);
        }

        public void bind(StrengthSetModel set, boolean isFinished, STRENGTH_SET_DETAILS_TABLE_DAO dao, ExecutorService executor) {
            weight.setText(set.getWeight() == 0 ? "" : String.valueOf(set.getWeight()));
            reps.setText(set.getRep() == 0 ? "" : String.valueOf(set.getRep()));


            // Очистим фон от ошибок при повторном биндинге
            weight.setBackgroundResource(R.drawable.edit_text_back2); // Заменить на свой дефолтный фон
            reps.setBackgroundResource(R.drawable.edit_text_back2);

            isSelected.setOnCheckedChangeListener(null); // убрать listener, чтобы не триггерился при setChecked
            isSelected.setChecked(isFinished);

            liner.setBackgroundResource(isFinished ? R.drawable.card_border3 : R.drawable.card_border2);
            weight.setEnabled(!isFinished);
            reps.setEnabled(!isFinished);

            isSelected.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    boolean weightEmpty = weight.getText().toString().trim().isEmpty();
                    boolean repsEmpty = reps.getText().toString().trim().isEmpty();

                    if (weightEmpty || repsEmpty) {
                        if (weightEmpty) {
                            weight.setBackgroundResource(R.drawable.edit_text_back3); // Подсветка красным
                        }
                        if (repsEmpty) {
                            reps.setBackgroundResource(R.drawable.edit_text_back3);
                        }

                        isSelected.setChecked(false); // Откатываем чекбокс
                        return;
                    }

                    set.setState("finished");
                    executor.execute(() -> dao.updateStrengthSet(set));

                    itemView.post(() -> {
                        liner.setBackgroundResource(R.drawable.card_border3);
                        weight.setEnabled(false);
                        reps.setEnabled(false);
                    });
                } else {
                    set.setState("active");
                    executor.execute(() -> dao.updateStrengthSet(set));

                    itemView.post(() -> {
                        liner.setBackgroundResource(R.drawable.card_border2);
                        weight.setEnabled(true);
                        reps.setEnabled(true);
                    });
                }
            });

            // Watchers
            weight.addTextChangedListener(new SimpleTextWatcher(s -> {
                double value = parseDoubleSafe(s);
                weight.setBackgroundResource(R.drawable.edit_text_back2);
                set.setWeight(value);
                executor.execute(() -> dao.updateStrengthSet(set));
            }));
            reps.addTextChangedListener(new SimpleTextWatcher(s -> {
                int value = parseIntSafe(s);
                reps.setBackgroundResource(R.drawable.edit_text_back2);
                set.setRep(value);
                executor.execute(() -> dao.updateStrengthSet(set));
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
            liner = itemView.findViewById(R.id.linearLayout);
        }

        public void bind(CardioSetModel set, boolean isFinished, CARDIO_SET_DETAILS_TABLE_DAO dao, ExecutorService executor) {
            time.setText(set.getTime() == 0 ? "" : String.valueOf(set.getTime()));
            distance.setText(set.getDistance() == 0 ? "" : String.valueOf(set.getDistance()));
            temp.setText(set.getTemp() == 0 ? "" : String.valueOf(set.getTemp()));

            // Сбросим фон на стандартный (без ошибок)
            time.setBackgroundResource(R.drawable.edit_text_back2);
            distance.setBackgroundResource(R.drawable.edit_text_back2);
            temp.setBackgroundResource(R.drawable.edit_text_back2);

            isSelected.setOnCheckedChangeListener(null); // сброс слушателя перед установкой чекбокса
            isSelected.setChecked(isFinished);

            liner.setBackgroundResource(isFinished ? R.drawable.card_border3 : R.drawable.card_border2);
            time.setEnabled(!isFinished);
            distance.setEnabled(!isFinished);
            temp.setEnabled(!isFinished);

            isSelected.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    boolean timeEmpty = time.getText().toString().trim().isEmpty();
                    boolean distanceEmpty = distance.getText().toString().trim().isEmpty();
                    boolean tempEmpty = temp.getText().toString().trim().isEmpty();

                    boolean hasError = false;

                    if (timeEmpty) {
                        time.setBackgroundResource(R.drawable.edit_text_back3);
                        hasError = true;
                    }
                    if (distanceEmpty) {
                        distance.setBackgroundResource(R.drawable.edit_text_back3);
                        hasError = true;
                    }
                    if (tempEmpty) {
                        temp.setBackgroundResource(R.drawable.edit_text_back3);
                        hasError = true;
                    }

                    if (hasError) {
                        isSelected.setChecked(false); // откатываем чекбокс
                        return;
                    }

                    set.setState("finished");
                    executor.execute(() -> dao.updateCardioSet(set));

                    itemView.post(() -> {
                        liner.setBackgroundResource(R.drawable.card_border3);
                        time.setEnabled(false);
                        distance.setEnabled(false);
                        temp.setEnabled(false);
                    });

                } else {
                    set.setState("active");
                    executor.execute(() -> dao.updateCardioSet(set));

                    itemView.post(() -> {
                        liner.setBackgroundResource(R.drawable.card_border2);
                        time.setEnabled(true);
                        distance.setEnabled(true);
                        temp.setEnabled(true);
                    });
                }
            });

            time.addTextChangedListener(new SimpleTextWatcher(s -> {
                set.setTime(parseIntSafe(s));
                time.setBackgroundResource(R.drawable.edit_text_back2);
                executor.execute(() -> dao.updateCardioSet(set));
            }));
            distance.addTextChangedListener(new SimpleTextWatcher(s -> {
                set.setDistance(parseDoubleSafe(s));
                distance.setBackgroundResource(R.drawable.edit_text_back2);
                executor.execute(() -> dao.updateCardioSet(set));
            }));
            temp.addTextChangedListener(new SimpleTextWatcher(s -> {
                set.setTemp(parseDoubleSafe(s));
                temp.setBackgroundResource(R.drawable.edit_text_back2);
                executor.execute(() -> dao.updateCardioSet(set));
            }));
        }
    }

    // Simple TextWatcher utility
    private static class SimpleTextWatcher implements TextWatcher {
        private final Consumer<String> onTextChanged;

        public SimpleTextWatcher(Consumer<String> onTextChanged) {
            this.onTextChanged = onTextChanged;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged(Editable s) {
            onTextChanged.accept(s.toString());
        }
    }

    // Helper methods
    private static double parseDoubleSafe(String s) {
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static int parseIntSafe(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
