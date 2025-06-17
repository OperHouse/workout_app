package com.example.workoutapp.NutritionFragments;

import android.app.Dialog;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workoutapp.Adapters.EatAdapter;
import com.example.workoutapp.Adapters.PresetMealAdapter;
import com.example.workoutapp.DAO.ConnectingMealPresetDao;
import com.example.workoutapp.DAO.PresetEatDao;
import com.example.workoutapp.DAO.PresetMealNameDao;
import com.example.workoutapp.MainActivity;
import com.example.workoutapp.NutritionModels.PresetMealModel;
import com.example.workoutapp.R;

import java.util.List;
import java.util.Objects;

public class SelectionMealPresetsFragment extends Fragment {

    private RecyclerView presetRecycler;
    private PresetMealNameDao presetMealNameDao;
    private PresetEatDao presetEatDao;
    private ConnectingMealPresetDao connectingMealPresetDao;
    private PresetMealAdapter presetMealAdapter;

    public SelectionMealPresetsFragment() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.presetMealNameDao = new PresetMealNameDao(MainActivity.getAppDataBase());
        this.presetEatDao = new PresetEatDao(MainActivity.getAppDataBase());
        this.connectingMealPresetDao = new ConnectingMealPresetDao(MainActivity.getAppDataBase());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View AddMealFragmentView = inflater.inflate(R.layout.fragment_selection_meal_presets, container, false);

        ImageButton backBtn = AddMealFragmentView.findViewById(R.id.imageButtonBack);
        Button createPresetBtn = AddMealFragmentView.findViewById(R.id.createPresetBtn);
        presetRecycler = AddMealFragmentView.findViewById(R.id.presetsRecycler);




        List<PresetMealModel> presets = presetMealNameDao.getAllPresetMealModels(
                connectingMealPresetDao,
                presetEatDao
        );


        presetMealAdapter = new PresetMealAdapter(requireContext(), presets, this::showPresetDetailDialog);
        presetRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        presetRecycler.setAdapter(presetMealAdapter);


        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replaceFragment(new NutritionFragment());
            }
        });

        createPresetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replaceFragment(new CreateMealPresetFragment());
            }
        });





        return AddMealFragmentView;
    }

    private void showPresetDetailDialog(PresetMealModel preset) {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_preset_detail);
        Objects.requireNonNull(dialog.getWindow()).setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        TextView title = dialog.findViewById(R.id.textView1);
        ImageButton closeBtn = dialog.findViewById(R.id.imageButtonBack1);
        Button changePresetBtn = dialog.findViewById(R.id.changePresetBtn);
        RecyclerView eatRecycler = dialog.findViewById(R.id.recyclerView);

        if (preset.getPresetMealEat().size() > 5) {
            ViewGroup.LayoutParams params = eatRecycler.getLayoutParams();
            params.height = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    350,
                    requireContext().getResources().getDisplayMetrics()
            );
            eatRecycler.setLayoutParams(params);
        }

        EatAdapter eatAdapter = new EatAdapter(requireContext(), SelectionMealPresetsFragment.this);

        eatRecycler.setHasFixedSize(true);
        eatRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        eatRecycler.setAdapter(eatAdapter);


        eatAdapter.updateEatList(preset.getPresetMealEat());
        title.setText(preset.getPresetMealName());

        changePresetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                replaceFragment(new CreateMealPresetFragment(preset.getPresetMealName_id()));
            }
        });


        title.setText(preset.getPresetMealName());
        closeBtn.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
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