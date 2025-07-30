package com.example.workoutapp.NutritionFragments;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workoutapp.NutritionAdapters.FoodAdapter;
import com.example.workoutapp.NutritionAdapters.PresetMealAdapter;
import com.example.workoutapp.DAO.ConnectingMealPresetDao;
import com.example.workoutapp.DAO.PresetEatDao;
import com.example.workoutapp.DAO.PresetMealNameDao;
import com.example.workoutapp.MainActivity;
import com.example.workoutapp.NutritionMode;
import com.example.workoutapp.NutritionModels.MealModel;
import com.example.workoutapp.R;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class SelectionMealPresetsFragment extends Fragment {

    private RecyclerView presetRecycler;
    private PresetMealNameDao presetMealNameDao;
    private PresetEatDao presetEatDao;
    private ConnectingMealPresetDao connectingMealPresetDao;
    private PresetMealAdapter presetMealAdapter;
    private SearchView searchPreset;
    private String searchText = "";

    TextView textPressedBtn;

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
        searchPreset = AddMealFragmentView.findViewById(R.id.searchPresets);
        presetRecycler = AddMealFragmentView.findViewById(R.id.presetsRecycler);
        textPressedBtn = AddMealFragmentView.findViewById(R.id.textView7);

        List<MealModel> presets = presetMealNameDao.getAllPresetMealModels(
                connectingMealPresetDao,
                presetEatDao
        );

        if(!presets.isEmpty()){
            textPressedBtn.setVisibility(View.GONE);
        }

        presetMealAdapter = new PresetMealAdapter(this, requireContext(), this::showPresetDetailDialog);
        presetRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        presetRecycler.setAdapter(presetMealAdapter);
        presetMealAdapter.updatePresetMealsList(presets);


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

        setupSwipeLeftForRecycler(presetRecycler, presets);

        searchPreset.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Убираем фокус после отправки
                searchPreset.clearFocus();
                presetRecycler.requestFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newTextInp) {
                searchText = newTextInp;
                if(Objects.equals(searchText, "")){
                    presetMealAdapter.filteredList.clear();
                    presetMealAdapter.changeFilterText(searchText);
                    presetMealAdapter.notifyDataSetChanged();
                }else{
                    presetMealAdapter.setFilteredList(newTextInp);
                }

                return true;
            }
        });


        return AddMealFragmentView;
    }

    private void showPresetDetailDialog(MealModel preset) {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_preset_detail);
        Objects.requireNonNull(dialog.getWindow()).setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        TextView title = dialog.findViewById(R.id.textView1);
        ImageButton closeBtn = dialog.findViewById(R.id.imageButtonBack1);
        Button changePresetBtn = dialog.findViewById(R.id.changePresetBtn);
        RecyclerView eatRecycler = dialog.findViewById(R.id.recyclerView);

        if (preset.getMeal_food_list().size() > 5) {
            ViewGroup.LayoutParams params = eatRecycler.getLayoutParams();
            params.height = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    350,
                    requireContext().getResources().getDisplayMetrics()
            );
            eatRecycler.setLayoutParams(params);
        }

        FoodAdapter foodAdapter = new FoodAdapter(requireContext(), SelectionMealPresetsFragment.this);

        eatRecycler.setHasFixedSize(true);
        eatRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        eatRecycler.setAdapter(foodAdapter);


        foodAdapter.updateEatList(preset.getMeal_food_list());
        title.setText(preset.getMeal_name());

        changePresetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                replaceFragment(new CreateMealPresetFragment(preset.getMeal_name_id(), NutritionMode.EDIT_PRESET));
            }
        });


        title.setText(preset.getMeal_name());
        closeBtn.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void setupSwipeLeftForRecycler(RecyclerView recyclerView, List<?> dataList) {
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                searchPreset.clearFocus();
                int position = viewHolder.getAdapterPosition();
                MealModel item = (MealModel) presetMealAdapter.getList().get(position);
                showDeleteConfirmationDialog(item, position, recyclerView);

            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder,
                                    float dX, float dY, int actionState, boolean isCurrentlyActive) {
                new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                        .addActionIcon(R.drawable.ic_trash_can_foreground)
                        .create()
                        .decorate();

                if (Math.abs(dX) > viewHolder.itemView.getWidth() * 0.3) {
                    dX = viewHolder.itemView.getWidth() * 0.3f * (dX > 0 ? 1 : -1);
                }

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };

        new ItemTouchHelper(simpleCallback).attachToRecyclerView(recyclerView);
    }

    @SuppressLint("SetTextI18n")
    private void showDeleteConfirmationDialog(MealModel presetToDelete, int position, RecyclerView r) {
        Dialog dialogDeleteEat = new Dialog(requireContext());
        dialogDeleteEat.setContentView(R.layout.confirm_dialog_layout);
        Objects.requireNonNull(dialogDeleteEat.getWindow()).setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        AtomicBoolean isDialogClosedByOutsideClick = new AtomicBoolean(false);
        // Разрешаем закрытие диалога при нажатии вне его
        dialogDeleteEat.setCancelable(true); // Делаем диалог закрываемым
        dialogDeleteEat.setCanceledOnTouchOutside(true); // Закрыть при клике вне диалога


        TextView text1 = dialogDeleteEat.findViewById(R.id.text1);
        TextView text2 = dialogDeleteEat.findViewById(R.id.text2);
        Button deleteBtn = dialogDeleteEat.findViewById(R.id.btnDelete);
        Button chanelBtn = dialogDeleteEat.findViewById(R.id.btnChanel);

        text1.setText("Удаление пресета");
        text2.setText("Вы действивтельно хотите удалить пресет \"" + presetToDelete.getMeal_name() + "\"");

        if(dialogDeleteEat.getWindow() != null){
            dialogDeleteEat.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }

        chanelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presetMealAdapter.notifyItemChanged(position);
                dialogDeleteEat.dismiss();

            }
        });

        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int presetId = presetToDelete.getMeal_name_id();
                List<Integer> eatIds = connectingMealPresetDao.getEatIdsForPreset(presetId);
                connectingMealPresetDao.deleteAllForPreset(presetId);
                presetMealNameDao.deleteMealPresetName(presetId);


                for(Integer id: eatIds){
                    if(!connectingMealPresetDao.doesEatIdExist(id)){
                        presetEatDao.deletePresetFood(id);
                    }
                }

                connectingMealPresetDao.logAllMealPresetConnections();
                presetMealNameDao.logAllMealPresetNames();
                presetEatDao.logAllPresetFood();

                if (!searchText.isEmpty()) {
                    presetMealAdapter.removePresetElm(presetToDelete);
                    presetMealAdapter.setFilteredList(searchText);
                }else {
                    presetMealAdapter.removePresetElm(presetToDelete);
                    presetMealAdapter.notifyDataSetChanged();
                }

                if(presetMealAdapter.getItemCount() == 0){
                    textPressedBtn.setVisibility(View.VISIBLE);
                }
                r.requestLayout();
                dialogDeleteEat.dismiss();
            }
        });

        dialogDeleteEat.setOnDismissListener(dialog -> {
            if (isDialogClosedByOutsideClick.get()) {
                // Диалог был закрыт нажатием вне
                presetMealAdapter.notifyItemChanged(position);
            }
        });

        // Слушаем закрытие по клику вне
        dialogDeleteEat.setOnCancelListener(dialog -> isDialogClosedByOutsideClick.set(true));
        dialogDeleteEat.show();
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