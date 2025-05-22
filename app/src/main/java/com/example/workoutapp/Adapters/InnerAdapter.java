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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workoutapp.DAO.TempWorkoutDao;
import com.example.workoutapp.MainActivity;
import com.example.workoutapp.Models.SetsModel;
import com.example.workoutapp.R;

import java.util.ArrayList;
import java.util.List;

public class InnerAdapter extends RecyclerView.Adapter<InnerAdapter.InnerViewHolder> {
    private List<SetsModel> setsList;
    private int exerciseId;
    private TempWorkoutDao TempWorkDao;

    // Список для хранения измененных данных, которые будут записаны в БД
    private List<SetsModel> modifiedSets = new ArrayList<>();


    public interface OnSetListChangedListener {
        void onSetListChanged(int exerciseId, boolean isEmpty);
    }

    private OnSetListChangedListener setListChangedListener;

    public InnerAdapter(List<SetsModel> setList, int ex_id) {
        this.setsList = setList;
        this.TempWorkDao = new TempWorkoutDao(MainActivity.getAppDataBase());
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
    public void onBindViewHolder(@NonNull InnerViewHolder holder, @SuppressLint("RecyclerView") int position) {
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

        if(!set.getIsSelected()) {

            // Создаём новый TextWatcher для веса
            holder.weightWatcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    try {
                        int weight = Integer.parseInt(s.toString());
                        // Если новое значение отличается от текущего, обновляем его
                        if (weight != set.getWeight()) {
                            if (modifiedSets.isEmpty()) {
                                modifiedSets.add(findSetById(set.getSet_id()));
                                modifiedSets.get(0).setWeight(weight);
                            } else if (getSetId(set.getSet_id())) {
                                for (SetsModel a : modifiedSets) {
                                    if (a.getSet_id() == set.getSet_id()) {
                                        a.setWeight(weight);
                                        break;
                                    }
                                }
                            } else {
                                modifiedSets.add(findSetById(set.getSet_id()));
                                for (SetsModel a : modifiedSets) {
                                    if (a.getSet_id() == set.getSet_id()) {
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
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                }

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

            holder.reps.setEnabled(true);
            holder.weight.setEnabled(true);

            hideKeyboardOnFocusLost(holder.weight);
            hideKeyboardOnFocusLost(holder.reps);

            // Устанавливаем состояние Checkbox как не выбранное
            holder.isSelected.setChecked(false);  // Здесь убираем флаг "выбрано"
            holder.isSelected.setBackgroundResource(R.drawable.checkbox_unchecked);

            holder.liner.setBackgroundResource(R.drawable.card_border2);


        }else {
            // Блокируем редактирование поля ввода для веса
            holder.weight.setEnabled(false);
            holder.weight.removeTextChangedListener(holder.weightWatcher);  // Убираем TextWatcher, чтобы предотвратить изменение


            // Блокируем редактирование поля ввода для повторений
            holder.reps.setEnabled(false);
            holder.reps.removeTextChangedListener(holder.repsWatcher);  // Убираем TextWatcher, чтобы предотвратить изменение


            // Устанавливаем фон для заблокированного состояния
            holder.isSelected.setBackgroundResource(R.drawable.checkbox_checked);
            holder.isSelected.setChecked(true);

            holder.liner.setBackgroundResource(R.drawable.card_border3);

        }
        holder.weight.setBackgroundResource(R.drawable.edit_text_back2);
        holder.reps.setBackgroundResource(R.drawable.edit_text_back2);


        holder.isSelected.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                // Получаем текущее значение вес и повторений из EditText
                String weightText = holder.weight.getText().toString().trim();
                String repsText = holder.reps.getText().toString().trim();

                // Проверяем, что оба поля не пустые и не равны нулю
                boolean isValid = !weightText.isEmpty() && !repsText.isEmpty() &&
                        Integer.parseInt(weightText) != 0 && Integer.parseInt(repsText) != 0;

                if(isValid){
                // Если условия выполнены, продолжаем обработку
                    if(isChecked ){
                        TempWorkDao.updateTempSetIsSelected(exerciseId, set.getSet_id(), true);
                        set.setIsSelected(true);
                        for (SetsModel s: modifiedSets) {
                            if(s.getSet_id() == set.getSet_id()){
                                s.setIsSelected(true); // это лишнее действие и надо подумать как его упрознить
                                TempWorkDao.updateOrInsertTempSet(s, exerciseId);
                                set.setWeight(s.getWeight());
                                set.setReps(s.getReps());
                                modifiedSets.remove(s);
                                break;
                            }
                        }
                    }else if(set.getIsSelected()){
                        TempWorkDao.updateTempSetIsSelected(exerciseId, set.getSet_id(), false);
                        set.setIsSelected(false);
                    }

                    holder.itemView.post(() -> notifyItemChanged(position));

                }else {
                    holder.weight.setBackgroundResource(R.drawable.edit_text_back3);
                    holder.reps.setBackgroundResource(R.drawable.edit_text_back3);
                    holder.isSelected.setChecked(false);
                }
            }
        });



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
            notifyDataSetChanged();// Notify the adapter that the data has changed
            notifyDataChanged();
        }
    }
    public void attachSwipeToDelete(RecyclerView recyclerView, int ex_id) {
        recyclerView.setItemAnimator(null);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getBindingAdapterPosition();

                // Проверка на соответствие exerciseId
                if (ex_id != exerciseId) {
                    notifyItemChanged(position); // отменяем свайп
                    return;
                }

                // Проверка на границы
                if (position >= 0 && position < setsList.size()) {
                    SetsModel set = setsList.get(position);

                    // Удаление из БД
                    TempWorkDao.deleteTempSetAndRearrangeNumbers(ex_id, set.getSet_id());

                    // Удаление из списка
                    setsList.remove(position);
                    notifyItemRemoved(position);

                    // Перенумерация оставшихся
                    for (int i = 0; i < setsList.size(); i++) {
                        SetsModel updatedSet = setsList.get(i);
                        updatedSet.setSet_id(i + 1); // Нумерация с 1
                    }

                    notifyItemRangeChanged(0, setsList.size());

                    notifyDataChanged();

                    // Логирование
                    Log.d("InnerAdapter", "Set deleted: " + set.getSet_id());
                } else {
                    Log.w("InnerAdapter", "Swipe position out of bounds: " + position);
                    // Отмена свайпа, так как позиция невалидная
                    notifyItemChanged(position);
                }
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
                        TempWorkDao.updateOrInsertTempSet(modifiedSet, exerciseId);
                        Log.d("InnerAdapter", "Changes saved to the database for set: " + modifiedSet.getSet_id());
                    } else {
                        Log.d("InnerAdapter", "No changes for set: " + modifiedSet.getSet_id());
                    }
                } else {
                    Log.d("InnerAdapter", "Set with id " + modifiedSet.getSet_id() + " not found in setsList");
                }
            }
            updateSets();
            Log.d("InnerAdapter", "Changes saved to the database via method");
        }else {
            Log.d("InnerAdapter", "Nothing to save");
        }

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
        //notifyDataSetChanged(); // Notify the adapter that the data has changed

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
        editText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                // Скрыть клавиатуру
                InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }

                saveModifiedSetsToDb(); //Обновление БД
                v.clearFocus(); // Снять фокус с EditText
                return true; // Обработка завершена
            }
            return false;
        });
    }

    public void setOnSetListChangedListener(OnSetListChangedListener listener) {
        this.setListChangedListener = listener;
    }
    private void notifyDataChanged() {
        if (setListChangedListener != null) {
            setListChangedListener.onSetListChanged(exerciseId, setsList.isEmpty());
        }
    }


    public static class InnerViewHolder extends RecyclerView.ViewHolder {
        EditText weight, reps;
        TextWatcher weightWatcher;
        TextWatcher repsWatcher;
        CheckBox isSelected;
        LinearLayout liner;

        public InnerViewHolder(View itemView) {
            super(itemView);
            weight = itemView.findViewById(R.id.kg_textEd);
            reps = itemView.findViewById(R.id.reps_textEd);
            isSelected = itemView.findViewById(R.id.isSelected);
            liner = itemView.findViewById(R.id.linearLayout);
        }
    }


}
