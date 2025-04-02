package com.example.workoutapp.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workoutapp.Models.SetsModel;
import com.example.workoutapp.R;

import java.util.List;

public class InnerAdapter extends RecyclerView.Adapter<InnerAdapter.InnerViewHolder> {
    private final List<SetsModel> setsList;

    public InnerAdapter(List<SetsModel> setList) {
        this.setsList = setList;
    }


    @NonNull
    @Override
    public InnerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.set_item_layout, parent, false);
        return new InnerViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull InnerViewHolder holder, int position) {
        SetsModel set = setsList.get(position);

    }

    @Override
    public int getItemCount() {
        return setsList.size();
    }

    public static class InnerViewHolder extends RecyclerView.ViewHolder {
        EditText  weight, reps;

        public InnerViewHolder(View itemView) {
            super(itemView);
            weight = itemView.findViewById(R.id.kg_textEd);
            reps = itemView.findViewById(R.id.reps_textEd);
        }
    }
}
