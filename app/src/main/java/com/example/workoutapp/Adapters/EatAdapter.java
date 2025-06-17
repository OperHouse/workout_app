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
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class EatAdapter extends RecyclerView.Adapter<EatAdapter.MyViewHolder>{

    private final Context context;
    private final BaseEatDao baseEatDao;
    private final OnEatItemClickListener listener;
    private final Fragment fragment;
    private String currentFilter = "";
    private List<EatModel> eatList = new ArrayList<>(); //Лист, который используется для отображения в адаптере
    public List<EatModel> filteredList = new ArrayList<>();



    public EatAdapter(@NonNull Context context, @NonNull OnEatItemClickListener listener, Fragment fragment) {
        this.context = context;
        this.fragment = fragment;
        this.listener = listener;
        this.baseEatDao = new BaseEatDao(MainActivity.getAppDataBase());
    }
    public EatAdapter(@NonNull Context context, Fragment fragment) {
        this.context = context;
        this.fragment = fragment;
        this.baseEatDao = new BaseEatDao(MainActivity.getAppDataBase());
        listener = null;
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
        if (!eatList.isEmpty() || !filteredList.isEmpty()) {
            EatModel eat;
                if(!Objects.equals(currentFilter, "")){
                    eat = filteredList.get(position);
                }else{
                    eat = eatList.get(position);
                }

                holder.nameEat.setText(eat.getEat_name());
                holder.amountEat.setText("(" + eat.getAmount() + " " + eat.getMeasurement_type() + ")");

                @SuppressLint("DefaultLocale") String protein = String.format("%.1f", eat.getProtein());
                @SuppressLint("DefaultLocale") String fat = String.format("%.1f", eat.getFat());
                @SuppressLint("DefaultLocale") String carb = String.format("%.1f", eat.getCarb());
                @SuppressLint("DefaultLocale") String calories = String.format("%.0f", eat.getCalories());

                holder.pfcText.setText("Б: " + protein + " / Ж: " + fat + " / У: " + carb);
                holder.eatCalories.setText(calories + " ккал");

                holder.linerLayoutMain.setBackgroundResource(eat.getIsSelected() ? R.drawable.card_border4_blue : R.drawable.card_border);
                holder.linerLayoutSecond.setBackgroundResource(eat.getIsSelected() ? R.drawable.card_border : R.drawable.card_border2);

                holder.itemView.setOnClickListener(v -> {
                    if(listener != null){
                        listener.onEatItemClick(context, eat);
                    }
                });



        }

    }

    @Override
    public int getItemCount() {
        if (currentFilter.isEmpty() && !eatList.isEmpty()) {
            return eatList.size();
        }else if(!currentFilter.isEmpty() && !filteredList.isEmpty()){
            return filteredList.size();
        }else {return 0;}

    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateEatList(List<EatModel> eatModelList) {
            this.eatList = eatModelList.stream().map(EatModel::new).collect(Collectors.toList());
    }

    public List<EatModel> getList() {
        return eatList;
    }



    public List<EatModel> getPressedEat(){
        List<EatModel> eatPressedList = new ArrayList<>();
        for (EatModel elm: eatList) {
            if(elm.getIsSelected()){
                eatPressedList.add(elm);
            }
        }
        return eatPressedList;
    }

    public void removeEatElm(EatModel eatElmToRemove){
        eatList.remove(eatElmToRemove);
    }
    public void eatPressedSort(EatModel eatElmPressed){
        eatList.add(0,new EatModel(eatElmPressed));
    }

    public void unPressedSort(EatModel eatElmBase) {
        int eat_id = eatElmBase.getEat_id();
        boolean isBroken = false;

        for (int i = 0; i < eatList.size(); i++) {
            EatModel e = eatList.get(i);
            if (!e.getIsSelected()) {
                if (e.getEat_id() > eat_id) {
                    eatList.add(i, new EatModel(eatElmBase));
                    isBroken = true;
                    break;
                }
            }
        }
        if (!isBroken) {
            eatList.add(new EatModel(eatElmBase)); // вызывается, если break НЕ сработал
        }
    }

    public void deleteEat(EatModel eatToDelete){
        eatList.remove(eatToDelete);
        notifyDataSetChanged();
    }
public void changeFilterText(String text){
        currentFilter = text;
}


    public void setFilteredList(String text){
        currentFilter = text;
        filteredList.clear();
        for (EatModel elm:eatList) {
            if(elm.getEat_name().toLowerCase().contains(currentFilter)){
                filteredList.add(elm);
            }
        }
        notifyDataSetChanged();
    };


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
