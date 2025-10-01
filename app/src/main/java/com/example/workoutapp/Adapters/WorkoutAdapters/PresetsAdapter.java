package com.example.workoutapp.Adapters.WorkoutAdapters;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workoutapp.Data.WorkoutDao.BASE_EXERCISE_TABLE_DAO;
import com.example.workoutapp.Data.WorkoutDao.CONNECTING_WORKOUT_PRESET_TABLE_DAO;
import com.example.workoutapp.Data.WorkoutDao.WORKOUT_EXERCISE_TABLE_DAO;
import com.example.workoutapp.Data.WorkoutDao.WORKOUT_PRESET_NAME_TABLE_DAO;
import com.example.workoutapp.Fragments.WorkoutFragments.CreatePresetFragment;
import com.example.workoutapp.Fragments.WorkoutFragments.WorkoutFragment;
import com.example.workoutapp.MainActivity;
import com.example.workoutapp.Models.WorkoutModels.BaseExModel;
import com.example.workoutapp.Models.WorkoutModels.ExerciseModel;
import com.example.workoutapp.R;
import com.example.workoutapp.Tools.WorkoutMode;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PresetsAdapter extends RecyclerView.Adapter<PresetsAdapter.MyViewHolder> {

    private List<ExerciseModel> presetsList;
    private final WORKOUT_PRESET_NAME_TABLE_DAO presetNameDao;
    private final CONNECTING_WORKOUT_PRESET_TABLE_DAO connectingPresetDao;
    private final BASE_EXERCISE_TABLE_DAO baseExerciseDao;
    private final Fragment fragment;


    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public PresetsAdapter(@NonNull Fragment fragment) {
        this.fragment = fragment;
        this.presetNameDao = new WORKOUT_PRESET_NAME_TABLE_DAO(MainActivity.getAppDataBase());
        this.connectingPresetDao = new CONNECTING_WORKOUT_PRESET_TABLE_DAO(MainActivity.getAppDataBase());
        this.baseExerciseDao = new BASE_EXERCISE_TABLE_DAO(MainActivity.getAppDataBase());
    }

    @NonNull
    @Override
    public PresetsAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.preset_item_layout, parent, false);
        return new PresetsAdapter.MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        if (presetsList == null || position >= presetsList.size()) {
            return;
        }

        ExerciseModel currentPreset = presetsList.get(position);
        holder.namePreset.setText(currentPreset.getExerciseName());

        StringBuilder exercisesListText = new StringBuilder();
        List<Long> baseExIds = connectingPresetDao.getBaseExIdsByPresetId(currentPreset.getExercise_id());

        for (Long baseExId : baseExIds) {
            BaseExModel exercise = baseExerciseDao.getExerciseById(baseExId);
            if (exercise != null) {
                exercisesListText.append(exercise.getExName())
                        .append(" (")
                        .append(exercise.getExType())
                        .append("), ");
            }
        }

        if (exercisesListText.length() > 0) {
            exercisesListText.setLength(exercisesListText.length() - 2);
        }

        holder.exListText.setText(exercisesListText.toString());

        holder.itemView.setOnClickListener(v -> {
            // Запускаем операции с базой данных в фоновом потоке
            executor.execute(() -> {
                for (Long baseExId : baseExIds) {

                    // Добавляем упражнение в текущую тренировку
                    WORKOUT_EXERCISE_TABLE_DAO workoutExerciseDao =
                            new WORKOUT_EXERCISE_TABLE_DAO(MainActivity.getAppDataBase());
                    workoutExerciseDao.addExercise(
                            baseExerciseDao.getExerciseById(baseExId).getExName(),
                            baseExerciseDao.getExerciseById(baseExId).getExType(),
                            baseExerciseDao.getExerciseById(baseExId).getBodyType()
                    );
                }

                // После завершения, возвращаемся в основной поток для обновления UI
                mainHandler.post(() -> {
                    // Возвращаемся на предыдущий фрагмент в стеке (WorkoutFragment)
                    FragmentManager fragmentManager = fragment.getParentFragmentManager();
                    if (fragmentManager.getBackStackEntryCount() > 0) {
                        fragmentManager.popBackStack();
                    }

                    MainActivity mainActivity = (MainActivity) fragment.requireActivity();
                    mainActivity.reloadExercisesFromDb();


                    // Получаем существующий WorkoutFragment по тегу
                    WorkoutFragment workoutFragment = (WorkoutFragment)
                            mainActivity.getSupportFragmentManager().findFragmentByTag("workout");

                    if (workoutFragment == null) {
                        workoutFragment = new WorkoutFragment();
                        workoutFragment.setExercises(mainActivity.getCachedExercises());
                        mainActivity.getSupportFragmentManager().beginTransaction()
                                .replace(R.id.frameLayout, workoutFragment, "workout")
                                .commit();
                    } else {
                        workoutFragment.setExercises(mainActivity.getCachedExercises());
                        workoutFragment.refreshWorkoutData();

                        // Если фрагмент скрыт, покажем его
                        mainActivity.getSupportFragmentManager().beginTransaction()
                                .show(workoutFragment)
                                .commit();
                    }

                    // Возвращаемся на WorkoutFragment
                    mainActivity.showOrAddFragment("workout", workoutFragment);

                    // Показываем сообщение пользователю
                    Toast.makeText(fragment.requireContext(), "Упражнения из пресета добавлены!", Toast.LENGTH_SHORT).show();
                });
            });
        });

        holder.itemView.setOnLongClickListener(v -> {
            showPresetDetailDialog(currentPreset);
            return true;
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updatePresetsList(List<ExerciseModel> exerciseModelList) {
        this.presetsList = exerciseModelList;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return presetsList != null ? presetsList.size() : 0;
    }

    private void showPresetDetailDialog(ExerciseModel preset) {
        Dialog dialog = new Dialog(fragment.requireContext());
        dialog.setContentView(R.layout.dialog_preset_detail);
        Objects.requireNonNull(dialog.getWindow())
                .setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        TextView title = dialog.findViewById(R.id.textView1);
        ImageButton closeBtn = dialog.findViewById(R.id.imageButtonBack1);
        Button changePresetBtn = dialog.findViewById(R.id.changePresetBtn);
        RecyclerView exRecycler = dialog.findViewById(R.id.recyclerView);

        // Получаем список упражнений из базы
        List<Long> baseExIds = connectingPresetDao.getBaseExIdsByPresetId(preset.getExercise_id());
        List<BaseExModel> exerciseList = new java.util.ArrayList<>();
        for (Long id : baseExIds) {
            BaseExModel ex = baseExerciseDao.getExerciseById(id);
            if (ex != null) {
                exerciseList.add(new BaseExModel(ex)); // глубокая копия
            }
        }

        // Если упражнений много, увеличиваем высоту
        if (exerciseList.size() > 5) {
            ViewGroup.LayoutParams params = exRecycler.getLayoutParams();
            params.height = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    350,
                    fragment.requireContext().getResources().getDisplayMetrics()
            );
            exRecycler.setLayoutParams(params);
        }

        // Настраиваем адаптер упражнений
        ExAdapter adapter = new ExAdapter(fragment, fragment.requireContext(), WorkoutMode.CREATE_PRESET);
        adapter.updateExList(exerciseList);

        exRecycler.setHasFixedSize(true);
        exRecycler.setLayoutManager(new LinearLayoutManager(fragment.requireContext()));
        exRecycler.setAdapter(adapter);

        // Заголовок
        title.setText(preset.getExerciseName());

        // Кнопка "Изменить"
        changePresetBtn.setOnClickListener(v -> {
            dialog.dismiss();
            FragmentManager fragmentManager = fragment.getParentFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.frameLayout, new CreatePresetFragment(preset, WorkoutMode.EDIT_PRESET))
                    .addToBackStack(null) // <-- Вот это ключевое изменение
                    .commit();
        });

        // Кнопка закрытия
        closeBtn.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }


    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView namePreset, exListText;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            namePreset = itemView.findViewById(R.id.namePreset);
            exListText = itemView.findViewById(R.id.exListText);
        }
    }

}