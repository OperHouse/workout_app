package com.example.workoutapp.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workoutapp.DAO.BaseEatDao;
import com.example.workoutapp.MainActivity;
import com.example.workoutapp.NutritionModels.EatModel;
import com.example.workoutapp.OnEatItemClickListener;
import com.example.workoutapp.R;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class EatAdapter extends RecyclerView.Adapter<EatAdapter.MyViewHolder>{

    private final Context context;
    private final BaseEatDao baseEatDao;
    private List<EatModel> eatList;
    public List<EatModel> noClickedList;
    private final List<EatModel> eatListMain;
    private final OnEatItemClickListener listener;
    private final Fragment fragment;
    private String currentFilter = "";



    public EatAdapter(@NonNull Context context, @NonNull OnEatItemClickListener listener, Fragment fragment) {
        this.context = context;
        this.baseEatDao = new BaseEatDao(MainActivity.getAppDataBase());
        this.listener = listener;
        this.eatList = new ArrayList<>();
        this.fragment = fragment;
        this.noClickedList = new ArrayList<>();
        this.eatListMain = baseEatDao.getAllEat();

    }
    @NonNull
    @Override
    public EatAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.eat_elm_card, parent, false);
        return new EatAdapter.MyViewHolder(v);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull EatAdapter.MyViewHolder holder, int position) {
        if (eatList != null && !eatList.isEmpty()) {
            EatModel eat = eatList.get(position);
            holder.nameEat.setText(eat.getEat_name());
            holder.amountEat.setText("(" + eat.getAmount() + " " + eat.getMeasurement_type() + ")");

            @SuppressLint("DefaultLocale") String protein = String.format("%.1f", eat.getProtein());
            @SuppressLint("DefaultLocale") String fat = String.format("%.1f", eat.getFat());
            @SuppressLint("DefaultLocale") String carb = String.format("%.1f", eat.getCarb());
            @SuppressLint("DefaultLocale") String calories = String.format("%.0f", eat.getCalories());

            holder.pfcText.setText("Б: " + protein + " / Ж: " + fat + " / У: " + carb);
            holder.eatCalories.setText(calories + " ккал");

            if (eat.getIsSelected()) {
                holder.linerLayoutMain.setBackgroundResource(R.drawable.card_border4_blue);
                holder.linerLayoutSecond.setBackgroundResource(R.drawable.card_border);
            }else {
                holder.linerLayoutMain.setBackgroundResource(R.drawable.card_border);
                holder.linerLayoutSecond.setBackgroundResource(R.drawable.card_border2);
            }

            holder.itemView.setOnClickListener(v -> {
                listener.onEatItemClick(context, eat);

                // Только если еда уже была выбрана через диалог
                if (eat.getIsSelected()) {
                    if (!noClickedList.contains(eat)) {
                        handleItemSelected(eat);
                    } else {
                        handleItemDeselected(eat);
                    }

                    if (fragment != null) {
                        try {
                            fragment.getClass().getMethod("clearSearchFocus").invoke(fragment);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    noClickedList.clear();
                    notifyDataSetChanged();
                }
            });







        }

    }

    @Override
    public int getItemCount() {
        if (eatList != null) {
            return eatList.size();
        }
        return 0;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateEatList(List<EatModel> eatModelList) {
        if (!eatList.isEmpty()){
            this.eatList = eatModelList;
            isSelectedInMain();
            sortUpdateEatListFiltered();
            notifyDataSetChanged();
        } else {
            this.eatList = eatModelList;
            notifyDataSetChanged();
        }

    }

    public void handleItemSelected(EatModel eatElm) {
        // Перемещаем выбранный элемент в начало списка
        eatList.remove(eatElm);
        eatList.add(0, eatElm);
        eatList.get(0).setIsSelected(true);

        // Обновляем состояние элемента в exListMain
        updateExListMainState(eatElm.getEat_id(), true);

        // Переносим невзаимодействующие элементы в noClickedList и удаляем их из exList
        Iterator<EatModel> iterator = eatList.iterator();
        while (iterator.hasNext()) {
            EatModel elm = iterator.next();
            if (!elm.getIsSelected()) {
                iterator.remove();
                noClickedList.add(elm);
            }
        }

        // Добавляем оставшиеся элементы обратно в exList
        eatList.addAll(noClickedList);
    }

    public void handleItemDeselected(EatModel eatElm) {
        // Убираем из видимого списка
        eatList.remove(eatElm);

        // Обновляем состояние
        updateExListMainState(eatElm.getEat_id(), false);

        // Удаляем все невыбранные элементы
        List<EatModel> toRemove = new ArrayList<>();
        for (EatModel elm : eatList) {
            if (!elm.getIsSelected()) {
                toRemove.add(elm);
            }
        }
        eatList.removeAll(toRemove);


        // Добавляем обратно подходящие элементы из основного списка
        for (EatModel elm : eatListMain) {
            if (!elm.getIsSelected()) {
                if (currentFilter.isEmpty()) {
                    eatList.add(elm);
                } else if (matchesFilter(elm)) {
                    eatList.add(elm);
                }
            }
        }


        notifyDataSetChanged();
    }

    private void updateExListMainState(int eat_id, boolean isPressed) {
        for (int i = 0; i < eatListMain.size(); i++) {
            EatModel mainElm = eatListMain.get(i);
            if (mainElm.getEat_id() == eat_id) {
                mainElm.setIsSelected(isPressed);
                break;
            }
        }
    }

    private boolean matchesFilter(EatModel elm) {
        // Проверяем, соответствует ли элемент текущему фильтру
        return elm.getEat_name().toLowerCase().contains(currentFilter);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateEatListFiltered(String filter) {
        this.currentFilter = filter.toLowerCase();// сохраняем текущий фильтр
        if(filter.isEmpty()) {
            this.eatList = new ArrayList<>();
            for (EatModel eatMain: eatListMain){
                this.eatList.add(new EatModel(
                        eatMain.getEat_id(),
                        eatMain.getEat_name(),
                        eatMain.getProtein(),
                        eatMain.getFat(),
                        eatMain.getCarb(),
                        eatMain.getCalories(),
                        eatMain.getAmount(),
                        eatMain.getMeasurement_type(),
                        eatMain.getIsSelected()

                ));
            }
        }else {
            eatList.clear();
            for (EatModel eat : eatListMain) {
                if (currentFilter.isEmpty()) {
                    eatList.add(eat);
                } else if (matchesFilter(eat)) {
                    eatList.add(eat);
                }
            }
        }
        sortUpdateEatListFiltered();


        notifyDataSetChanged();
    }

    private void sortUpdateEatListFiltered() {
        for (int i = 0; i < eatList.size(); i++) {
            EatModel eatListItem = eatList.get(i);


            for (EatModel mainItem : eatListMain) {
                if (eatListItem.getEat_id() == mainItem.getEat_id()) {


                    if (eatListItem.getIsSelected()) {
                        mainItem.setIsSelected(true);
                    }
                    break;
                }
            }
        }

        eatList.sort(new Comparator<EatModel>() {
            @Override
            public int compare(EatModel eat1, EatModel eat2) {
                if (eat1.getIsSelected() && !eat2.getIsSelected()) {
                    return -1;
                } else if (!eat1.getIsSelected() && eat2.getIsSelected()) {
                    return 1;
                }
                return 0;
            }
        });

    }


    public List<EatModel> getList() {
        return eatList;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void changeEatListMain(EatModel eatToDell){
            this.eatListMain.remove(eatToDell);

    }

    void isSelectedInMain(){

        for (int i = 0; i < eatList.size(); i++) {
            EatModel eatListItem = eatList.get(i);


            for (EatModel mainItem : eatListMain) {
                if (eatListItem.getEat_id() == mainItem.getEat_id()) {
                    if(!eatListItem.getIsSelected() && mainItem.getIsSelected()){
                        eatListItem.setIsSelected(true);
                    }
                }
            }
        }
    }

    void handleSort(){
        // Переносим невзаимодействующие элементы в noClickedList и удаляем их из exList
        Iterator<EatModel> iterator = eatList.iterator();
        while (iterator.hasNext()) {
            EatModel elm = iterator.next();
            if (!elm.getIsSelected()) {
                iterator.remove();
                noClickedList.add(elm);
            }
        }

        // Добавляем оставшиеся элементы обратно в exList
        eatList.addAll(noClickedList);
    }

    public List<EatModel> getPressedEat(){
        List<EatModel> eatPressedList = new ArrayList<>();

        for (EatModel elm: eatListMain) {
            if(elm.getIsSelected()){
                eatPressedList.add(elm);
            }
        }

        return eatPressedList;
    }

    public void updateStatsMainEat(EatModel eatElm){
        for (EatModel mainElm: eatListMain) {
            if(mainElm.getEat_id() == eatElm.getEat_id()){
                mainElm.setAmount(eatElm.getAmount());
                mainElm.setCalories(eatElm.getCalories());
                mainElm.setProtein(eatElm.getProtein());
                mainElm.setFat(eatElm.getFat());
                mainElm.setCarb(eatElm.getCarb());
            }
        }
    }



    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView nameEat;
        TextView amountEat;
        TextView pfcText;
        TextView eatCalories;
        LinearLayout linerLayoutSecond;
        LinearLayout linerLayoutMain;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            nameEat = itemView.findViewById(R.id.nameEat);
            amountEat = itemView.findViewById(R.id.amountEat);
            pfcText = itemView.findViewById(R.id.pfcText);
            eatCalories = itemView.findViewById(R.id.eatCalories);
            linerLayoutSecond = itemView.findViewById(R.id.linearLayout);
            linerLayoutMain = itemView.findViewById(R.id.linearLayoutMain);
        }
    }
}
