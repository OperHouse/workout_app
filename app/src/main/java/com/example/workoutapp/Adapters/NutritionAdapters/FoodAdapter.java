package com.example.workoutapp.Adapters.NutritionAdapters;

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

import com.example.workoutapp.Data.NutritionDao.BaseEatDao;
import com.example.workoutapp.MainActivity;
import com.example.workoutapp.Models.NutritionModels.FoodModel;
import com.example.workoutapp.Tools.OnEatItemClickListener;
import com.example.workoutapp.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class FoodAdapter extends RecyclerView.Adapter<FoodAdapter.MyViewHolder>{

    private final Context context;
    private final BaseEatDao baseEatDao;
    private final OnEatItemClickListener listener;
    private final Fragment fragment;
    private String currentFilter = "";
    private List<FoodModel> eatList = new ArrayList<>(); //Основной лист
    public List<FoodModel> filteredList = new ArrayList<>();



    public FoodAdapter(@NonNull Context context, @NonNull OnEatItemClickListener listener, Fragment fragment) {
        this.context = context;
        this.fragment = fragment;
        this.listener = listener;
        this.baseEatDao = new BaseEatDao(MainActivity.getAppDataBase());
    }
    public FoodAdapter(@NonNull Context context, Fragment fragment) {
        this.context = context;
        this.fragment = fragment;
        this.baseEatDao = new BaseEatDao(MainActivity.getAppDataBase());
        listener = null;
    }


    @NonNull
    @Override
    public FoodAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.eat_elm_card, parent, false);
        return new FoodAdapter.MyViewHolder(v);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull FoodAdapter.MyViewHolder holder, int position) {
        if (!eatList.isEmpty() || !filteredList.isEmpty()) {
            FoodModel eat;
                if(!Objects.equals(currentFilter, "")){
                    eat = filteredList.get(position);
                }else{
                    eat = eatList.get(position);
                }

                holder.nameEat.setText(eat.getFood_name());
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
    public void updateEatList(List<FoodModel> foodModelList) {
            this.eatList = foodModelList.stream().map(FoodModel::new).collect(Collectors.toList());
    }

    public List<FoodModel> getList() {
        return eatList;
    }



    public List<FoodModel> getPressedEat(){
        List<FoodModel> eatPressedList = new ArrayList<>();
        for (FoodModel elm: eatList) {
            if(elm.getIsSelected()){
                eatPressedList.add(elm);
            }
        }
        return eatPressedList;
    }

    public void removeEatElm(FoodModel eatElmToRemove){
        eatList.remove(eatElmToRemove);
    }
    public void eatPressedSort(FoodModel eatElmPressed){
        eatList.add(0,new FoodModel(eatElmPressed));
    }

    public void unPressedSort(FoodModel eatElmBase) {
        int eat_id = eatElmBase.getFood_id();
        boolean isBroken = false;

        for (int i = 0; i < eatList.size(); i++) {
            FoodModel e = eatList.get(i);
            if (!e.getIsSelected()) {
                if (e.getFood_id() > eat_id) {
                    eatList.add(i, new FoodModel(eatElmBase));
                    isBroken = true;
                    break;
                }
            }
        }
        if (!isBroken) {
            eatList.add(new FoodModel(eatElmBase)); // вызывается, если break НЕ сработал
        }
    }

    public void deleteEat(FoodModel eatToDelete){
        eatList.remove(eatToDelete);
        notifyDataSetChanged();
    }
    public void changeFilterText(String text){
        currentFilter = text;
}


    public void setFilteredList(String text){
        currentFilter = text;
        filteredList.clear();
        for (FoodModel elm:eatList) {
            if(elm.getFood_name().toLowerCase().contains(currentFilter)){
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
