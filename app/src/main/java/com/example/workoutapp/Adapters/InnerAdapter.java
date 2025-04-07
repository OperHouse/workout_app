package com.example.workoutapp.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
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

    @SuppressLint("RestrictedApi")
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
                    // Если новое значение отличается от текущего, обновляем его
                    if (weight != set.getWeight()) {
                        if(modifiedSets.isEmpty()){
                            modifiedSets.add(findSetById(set.getSet_id()));
                            modifiedSets.get(0).setWeight(weight);
                        } else if (getSetId(set.getSet_id())) {
                            for (SetsModel a: modifiedSets) {
                                if(a.getSet_id() == set.getSet_id()){
                                    a.setWeight(weight);
                                    break;
                                }
                            }
                        }else{
                            modifiedSets.add(findSetById(set.getSet_id()));
                            for (SetsModel a: modifiedSets) {
                                if(a.getSet_id() == set.getSet_id()){
                                    a.setWeight(weight);
                                    break;
                                }
                            }
                        }

                    }
                } catch (NumberFormatException e) {
                    set.setWeight(0); // если пользователь очистил поле, ставим 0
                }
            }
        };
        holder.weight.addTextChangedListener(holder.weightWatcher);

        // Создаем новый TextWatcher для изменения повторений
        holder.repsWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    int reps = Integer.parseInt(s.toString());
                    // Если новое значение отличается от текущего, обновляем его
                    if (reps != set.getReps()) {
                        if (modifiedSets.isEmpty()) {
                            modifiedSets.add(findSetById(set.getSet_id()));
                            modifiedSets.get(0).setReps(reps);
                        } else if (getSetId(set.getSet_id())) {
                            for (SetsModel a : modifiedSets) {
                                if (a.getSet_id() == set.getSet_id()) {
                                    a.setReps(reps);
                                    break;
                                }
                            }
                        } else {
                            modifiedSets.add(findSetById(set.getSet_id()));
                            for (SetsModel a : modifiedSets) {
                                if (a.getSet_id() == set.getSet_id()) {
                                    a.setReps(reps);
                                    break;
                                }
                            }
                        }
                    }
                } catch (NumberFormatException e) {
                    set.setReps(0); // если пользователь очистил поле, ставим 0
                }
            }
        };
        holder.reps.addTextChangedListener(holder.repsWatcher);
        /*// Создаём новый TextWatcher для веса
        holder.weightWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    int weight = Integer.parseInt(s.toString());
                    // Если новое значение отличается от текущего, обновляем его
                    if (weight != set.getWeight()) {
                        // Создаем новый объект с измененным значением веса
                        SetsModel modifiedSet = findSetById(set.getSet_id());
                        if (modifiedSet != null) {
                            modifiedSet.setWeight(weight); // Обновляем вес только в modifiedSets
                            // Добавляем в список измененных данных, если это не было добавлено ранее
                            if (!modifiedSets.contains(modifiedSet)) {
                                modifiedSets.add(modifiedSet);
                            }
                        }
                    }
                } catch (NumberFormatException e) {
                    // Если пользователь очистил поле, ставим 0
                    set.setWeight(0);
                }
            }

        };
        holder.weight.addTextChangedListener(holder.weightWatcher);

        // Создаем новый TextWatcher для изменения повторений
        holder.repsWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    int reps = Integer.parseInt(s.toString());
                    // Если новое значение отличается от текущего, обновляем его
                    if (reps != set.getReps()) {
                        // Создаем новый объект с измененным значением повторений
                        SetsModel modifiedSet = findSetById(set.getSet_id());
                        if (modifiedSet != null) {
                            modifiedSet.setReps(reps); // Обновляем повторения только в modifiedSets
                            // Добавляем в список измененных данных, если это не было добавлено ранее
                            if (!modifiedSets.contains(modifiedSet)) {
                                modifiedSets.add(modifiedSet);
                            }
                        }
                    }
                } catch (NumberFormatException e) {
                    // Если пользователь очистил поле, ставим 0
                    set.setReps(0);
                }
            }
        };
        holder.reps.addTextChangedListener(holder.repsWatcher);*/



        hideKeyboardOnFocusLost(holder.weight);
        hideKeyboardOnFocusLost(holder.reps);


    }

    private boolean getSetId(int setId) {
        for (SetsModel s: modifiedSets) {
            if(s.getSet_id() == setId){return true;}
        }
        return false;
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

    /*@Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);

        if (!modifiedSets.isEmpty()) {
            for (SetsModel modifiedSet : modifiedSets) {
                // Поиск этого сета в setsList по set_id
                SetsModel originalSet = findSetById(modifiedSet.getSet_id());

                if (originalSet != null) {
                    boolean isChanged = false;

                    // Сравниваем значения в originalSet и modifiedSet
                    if (originalSet.getWeight() != modifiedSet.getWeight()) {
                        isChanged = true;
                    }
                    if (originalSet.getReps() != modifiedSet.getReps()) {
                        isChanged = true;
                    }

                    // Если данные изменены, то обновляем их в базе данных
                    if (isChanged) {
                        tempDataBaseEx.updateOrInsertSet(modifiedSet, exerciseId);
                        Log.d("InnerAdapter", "Changes saved to the database for set: " + modifiedSet.getSet_id());
                    } else {
                        Log.d("InnerAdapter", "No changes for set: " + modifiedSet.getSet_id());
                    }
                }
            }
        }
    }
    // Метод для поиска сета по set_id в setsList
    private SetsModel findSetById(int setId) {
        for (SetsModel set : setsList) {
            if (set.getSet_id() == setId) {
                return set;
            }
        }
        return null;  // Если не нашли
    }*/
    public void attachSwipeToDelete(RecyclerView recyclerView, int ex_id) {
        recyclerView.setItemAnimator(null);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                SetsModel set = setsList.get(position);
                tempDataBaseEx.deleteSetAndRearrangeNumbers(ex_id, set.getSet_id());
                setsList.remove(position);
                notifyItemRemoved(position);
                // Перенумеруем оставшиеся сеты в списке setsList, обновив их set_id
                for (int i = 0; i <setsList.size(); i++) {
                    SetsModel updatedSet = setsList.get(i);
                    updatedSet.setSet_id(i+1);  // Обновляем set_id (нумерация с 1)
                }

                // Оповещаем адаптер о том, что данные изменились
                notifyItemRangeChanged(0, setsList.size());
                tempDataBaseEx.logAllExercisesAndSets();
                Log.d("InnerAdapter", "Set deleted: " + set.getSet_id());
            }
        });
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }
    public void saveModifiedSetsToDb() {
        Log.d("InnerAdapter", "Processing exercise: " + exerciseId);
        if (setsList != null && !setsList.isEmpty() && modifiedSets != null && !modifiedSets.isEmpty()) {

            for (SetsModel modifiedSet : modifiedSets) {
                // Найдем соответствующий сет в setsList по set_id
                SetsModel originalSet = findSetById(modifiedSet.getSet_id());

                // Если сет найден в setsList
                if (originalSet != null) {
                    boolean isChanged = false;

                    // Сравниваем значения в originalSet (из setsList) и modifiedSet
                    if (originalSet.getWeight() != modifiedSet.getWeight()) {
                        isChanged = true;
                    }
                    if (originalSet.getReps() != modifiedSet.getReps()) {
                        isChanged = true;
                    }

                    // Если данные изменены, сохраняем их в базе данных
                    if (isChanged) {
                        tempDataBaseEx.updateOrInsertSet(modifiedSet, exerciseId);
                        Log.d("InnerAdapter", "Changes saved to the database for set: " + modifiedSet.getSet_id());
                    } else {
                        Log.d("InnerAdapter", "No changes for set: " + modifiedSet.getSet_id());
                    }
                } else {
                    Log.d("InnerAdapter", "Set with id " + modifiedSet.getSet_id() + " not found in setsList");
                }
            }
            updateSets();
        }
        Log.d("InnerAdapter", "Changes saved to the database via method");
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateSets() {

        for (SetsModel s: modifiedSets ) {
            for (SetsModel a: setsList ) {
                if(s.getSet_id() == a.getSet_id()){
                    a.setWeight(s.getWeight());
                    a.setReps(s.getReps());
                }
            }
        }
            modifiedSets.clear();
            notifyDataSetChanged(); // Notify the adapter that the data has changed

    }

    // Метод для поиска сета по его ID в базе данных
    private SetsModel findSetById(int setId) {
        for (SetsModel set : setsList) {
            if (set.getSet_id() == setId) {
                // Возвращаем новый объект с теми же данными, что и у исходного
                return new SetsModel(set.getSet_id(), set.getWeight(), set.getReps());
            }
        }
        return null;  // Если не нашли, возвращаем null
    }
    // Метод для скрытия клавиатуры
    private void hideKeyboardOnFocusLost(EditText editText) {
        editText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    editText.clearFocus();
                }
            }
        });

        //====ЗДЕСЬ ДОБАВИТЬ ОБНОВЛЕНИЕ СЕТА В БД======//
        editText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                // Скрыть клавиатуру
                InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
                // Снять фокус с EditText
                v.clearFocus();
                return true; // Обработка завершена
            }
            return false;
        });
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
