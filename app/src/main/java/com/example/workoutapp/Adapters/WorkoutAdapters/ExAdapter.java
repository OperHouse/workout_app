package com.example.workoutapp.Adapters.WorkoutAdapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workoutapp.Models.WorkoutModels.BaseExModel;
import com.example.workoutapp.R;
import com.example.workoutapp.Tools.WorkoutMode;

import java.util.ArrayList;
import java.util.List;

public class ExAdapter extends RecyclerView.Adapter<ExAdapter.MyViewHolder> implements Filterable {

    private final Context context;
    private List<BaseExModel> exListAll;
    private List<BaseExModel> exListFiltered;
    private WorkoutMode currentMode;

    public ExAdapter(Context context, List<BaseExModel> exList, WorkoutMode mode) {
        this.context = context;
        this.exListAll = exList;
        this.exListFiltered = new ArrayList<>(exList);
        this.currentMode = mode;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.ex_item_layout, parent, false);
        return new MyViewHolder(v);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        BaseExModel currentEx = exListAll.get(position);
        holder.name.setText(currentEx.getExName());
        holder.type.setText("(" + currentEx.getExType() + ")");
        holder.bodyPart.setText(currentEx.getBodyType());


        if (currentEx.getIsPressed()) {
            holder.linearLayout.setBackground(ContextCompat.getDrawable(context, R.drawable.card_border2));
        } else {
            holder.linearLayout.setBackground(ContextCompat.getDrawable(context, R.drawable.card_border));
        }

        // Обрабатываем нажатие, меняя состояние в модели и обновляя элемент
        holder.itemView.setOnClickListener(v -> {
            if(currentMode == WorkoutMode.SELECTED){
                currentEx.setIsPressed(!currentEx.getIsPressed());
                notifyItemChanged(holder.getAdapterPosition());
            }else{

            }
        });
    }

    @Override
    public int getItemCount() {
        return exListAll.size();
    }

    // Этот метод теперь фильтрует основной список, чтобы вернуть только выбранные элементы
    public List<BaseExModel> getSelectedItems() {
        List<BaseExModel> selected = new ArrayList<>();
        for (BaseExModel item : exListAll) {
            if (item.getIsPressed()) {
                selected.add(item);
            }
        }
        return selected;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String charString = constraint.toString();
                if (charString.isEmpty()) {
                    exListFiltered = exListAll;
                } else {
                    List<BaseExModel> filteredList = new ArrayList<>();
                    for (BaseExModel row : exListAll) {
                        if (row.getExName().toLowerCase().contains(charString.toLowerCase()) ||
                                row.getExType().toLowerCase().contains(charString.toLowerCase())) {
                            filteredList.add(row);
                        }
                    }
                    exListFiltered = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = exListFiltered;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                exListFiltered = (ArrayList<BaseExModel>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    public void updateExList(List<BaseExModel> exList) {
        this.exListAll = new ArrayList<>();
        for (BaseExModel ex : exList) {
            this.exListAll.add(new BaseExModel(ex)); // создаём новый объект на основе существующего
        }
        notifyDataSetChanged();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView name, type, bodyPart;
        LinearLayout linearLayout; // ⭐ Добавлен LinearLayout

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.nameEat);
            type = itemView.findViewById(R.id.amountEat);
            bodyPart = itemView.findViewById(R.id.bodyPart);
            linearLayout = itemView.findViewById(R.id.linearLayout);
        }
    }
}