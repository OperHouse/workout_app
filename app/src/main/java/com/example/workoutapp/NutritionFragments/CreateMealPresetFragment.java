package com.example.workoutapp.NutritionFragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workoutapp.Adapters.EatAdapter;
import com.example.workoutapp.DAO.EatDao;
import com.example.workoutapp.MainActivity;
import com.example.workoutapp.NutritionModels.EatModel;
import com.example.workoutapp.R;

import java.util.List;


public class CreateMealPresetFragment extends Fragment {
    private RecyclerView eatRecycler;
    private EatDao eatDao;
    private List<EatModel> eatList;
    private EatAdapter eatAdapter;

    public CreateMealPresetFragment() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.eatDao = new EatDao(MainActivity.getAppDataBase());

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View addEatToPresetFragment = inflater.inflate(R.layout.fragment_create_meal_preset, container, false);

        ImageButton backBtn = addEatToPresetFragment.findViewById(R.id.imageButtonBack);
        Button createEatBtn = addEatToPresetFragment.findViewById(R.id.createEatBtn);


        eatRecycler = addEatToPresetFragment.findViewById(R.id.eatRecyclerView);
        eatList = eatDao.getAllEat();
        eatAdapter = new EatAdapter(CreateMealPresetFragment.this);

        eatRecycler.setHasFixedSize(true);
        eatRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        eatRecycler.setAdapter(eatAdapter);

        eatAdapter.updateEatList(eatList);

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replaceFragment(new SelectionMealPresetsFragment());
            }
        });

        createEatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replaceFragment(new CreateEatFragment());
            }
        });



        return addEatToPresetFragment;
    }

    private void replaceFragment(Fragment newFragment) {
        // Получаем менеджер фрагментов
        FragmentManager fragmentManager = getFragmentManager();
        if (fragmentManager != null) {
            // Начинаем транзакцию фрагментов
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            // Заменяем текущий фрагмент на новый
            fragmentTransaction.replace(R.id.frameLayout, newFragment);
            // Добавляем транзакцию в бэкстек (если нужно)
            fragmentTransaction.addToBackStack(null);
            // Выполняем транзакцию
            fragmentTransaction.commit();
        }
    }
}