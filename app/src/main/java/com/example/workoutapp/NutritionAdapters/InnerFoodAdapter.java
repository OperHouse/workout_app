package com.example.workoutapp.NutritionAdapters;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class InnerFoodAdapter  extends RecyclerView.Adapter<InnerFoodAdapter.InnerViewHolder>{
    @NonNull
    @Override
    public InnerFoodAdapter.InnerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull InnerFoodAdapter.InnerViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public class InnerViewHolder extends RecyclerView.ViewHolder {
        public InnerViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
