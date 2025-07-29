package com.example.workoutapp.NutritionAdapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workoutapp.DAO.ConnectingMealDao;
import com.example.workoutapp.DAO.MealFoodDao;
import com.example.workoutapp.MainActivity;
import com.example.workoutapp.NutritionModels.FoodModel;
import com.example.workoutapp.R;

import java.util.List;
import java.util.stream.Collectors;

public class InnerFoodAdapter  extends RecyclerView.Adapter<InnerFoodAdapter.InnerViewHolder>{

    private List<FoodModel> innerAdapterFoodList;

    private final MealFoodDao mealFoodDao;
    private final ConnectingMealDao connectingMealDao;
    private final Context context;
    private int meal_id;

    public InnerFoodAdapter(Context context, List<FoodModel> innerAdapterFoodList, int meal_id) {
        this.innerAdapterFoodList = innerAdapterFoodList.stream().map(FoodModel::new).collect(Collectors.toList());
        this.meal_id = meal_id;


        this.context = context;
        this.mealFoodDao = new MealFoodDao(MainActivity.getAppDataBase());
        this.connectingMealDao = new ConnectingMealDao(MainActivity.getAppDataBase());
    }

    @NonNull
    @Override
    public InnerFoodAdapter.InnerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.inside_eat_elm_card, parent, false);
        return new InnerFoodAdapter.InnerViewHolder(v);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull InnerFoodAdapter.InnerViewHolder holder, int position) {
        FoodModel foodElm = innerAdapterFoodList.get(position);

        holder.foodName.setText(foodElm.getFood_name());
        holder.foodAmount.setText(foodElm.getAmount() + " " + foodElm.getMeasurement_type());
        @SuppressLint("DefaultLocale") String calories = String.format("%.0f", foodElm.getCalories());
        holder.foodCalories.setText(calories);



    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateData(List<FoodModel> updateFoodList, int meal_id) {
        this.meal_id = meal_id;
        this.innerAdapterFoodList = updateFoodList.stream().map(FoodModel::new).collect(Collectors.toList());
    }

    @Override
    public int getItemCount() {
        return innerAdapterFoodList.size();
    }

    public void attachSwipeToDelete(RecyclerView recyclerView, int meal_id) {
        recyclerView.setItemAnimator(null);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getBindingAdapterPosition();


                if (position >= 0 && position < innerAdapterFoodList.size()) {
                    FoodModel foodToDelete = innerAdapterFoodList.get(position);

                    // Удаление из таблицы еды
                    mealFoodDao.deleteMealFoodById(foodToDelete.getFood_id());

                    // Удаление связи из ConnectingMealTable
                    connectingMealDao.deleteConnection(meal_id, foodToDelete.getFood_id());

                    // Удаление из адаптера
                    innerAdapterFoodList.remove(position);
                    notifyItemRemoved(position);

                    Toast.makeText(context, "Еда удалена", Toast.LENGTH_SHORT).show();

                    connectingMealDao.logAllConnections();
                    mealFoodDao.logAllMealFoods();
                }
            }
        });

        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    public class InnerViewHolder extends RecyclerView.ViewHolder {

        TextView foodName, foodAmount, foodCalories;

        public InnerViewHolder(@NonNull View itemView) {
            super(itemView);
            foodName = itemView.findViewById(R.id.nameEat);
            foodAmount = itemView.findViewById(R.id.amountEat);
            foodCalories = itemView.findViewById(R.id.eatCalories);
        }
    }
}
