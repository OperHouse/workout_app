package com.example.workoutapp.Fragments.WorkoutFragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workoutapp.Adapters.WorkoutAdapters.OutsideAdapter;
import com.example.workoutapp.Data.ProfileDao.ActivityGoalDao;
import com.example.workoutapp.Data.ProfileDao.DailyActivityTrackingDao;
import com.example.workoutapp.Data.WorkoutDao.CARDIO_SET_DETAILS_TABLE_DAO;
import com.example.workoutapp.Data.WorkoutDao.STRENGTH_SET_DETAILS_TABLE_DAO;
import com.example.workoutapp.Data.WorkoutDao.WORKOUT_EXERCISE_TABLE_DAO;
import com.example.workoutapp.Fragments.FoodEquivalentFragment.FoodEquivalentFragment;
import com.example.workoutapp.MainActivity;
import com.example.workoutapp.Models.ProfileModels.ActivityGoalModel;
import com.example.workoutapp.Models.ProfileModels.DailyActivityTrackingModel;
import com.example.workoutapp.Models.WorkoutModels.CardioSetModel;
import com.example.workoutapp.Models.WorkoutModels.ExerciseModel;
import com.example.workoutapp.Models.WorkoutModels.StrengthSetModel;
import com.example.workoutapp.R;
import com.example.workoutapp.Tools.ActivityRingView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class WorkoutFragment extends Fragment {


    private OutsideAdapter workoutAdapter;
    private RecyclerView workoutRecyclerView;
    private List<ExerciseModel> exList;
    private WORKOUT_EXERCISE_TABLE_DAO workoutExerciseTableDao;
    private CARDIO_SET_DETAILS_TABLE_DAO cardioSetDetailsTableDao;
    private STRENGTH_SET_DETAILS_TABLE_DAO strengthSetDetailsTableDao;

    private float currentBurnedCalories = 0f;
    private float currentDistance = 0f;
    private int currentSteps = 0;

    private int selectedCardFoodIcon = R.drawable.ic_donut; // Иконка по умолчанию
    private int selectedCardFoodCal = 250;

    public WorkoutFragment() {
        // Required empty public constructor
    }

    public void setExercises(List<ExerciseModel> exercises) {
        this.exList = exercises; // просто сохраняем ссылку
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private static class FoodPreview {
        int iconRes;
        int calPerUnit;

        FoodPreview(int iconRes, int cal) {
            this.iconRes = iconRes;
            this.calPerUnit = cal;
        }
    }

    // Поля класса WorkoutFragment
    private FoodPreview selectedFood;
    private ImageView foodIconIv;
    private TextView foodCountTv;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_workout, container, false);

        TextView dateTextWorkout = view.findViewById(R.id.workout_date_TV);
        Button addExBtn = view.findViewById(R.id.workout_add_ex_BTN);
        Button finalWorkBtn = view.findViewById(R.id.workout_finish_BTN);
        LinearLayout foodBtn = view.findViewById(R.id.food_equivalent_LL);
        foodIconIv = view.findViewById(R.id.card_food_icon);
        foodCountTv = view.findViewById(R.id.card_food_count);


        // Инициализируем recyclerView
        workoutRecyclerView = view.findViewById(R.id.workout_list_RV);
        workoutRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        workoutRecyclerView.setItemAnimator(null);

        workoutAdapter = new OutsideAdapter(this);
        workoutAdapter.updateExList(exList);
        workoutRecyclerView.setAdapter(workoutAdapter);

        updateUI(view, exList);

        workoutAdapter.setOnExerciseListChangedListener(newList -> updateUI(view, newList));
        addExBtn.setOnClickListener(v -> {
            Fragment selectionFragment = new Selection_Ex_Preset_Fragment();
            FragmentManager fragmentManager = getParentFragmentManager(); // или getFragmentManager()
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

            fragmentTransaction.hide(this)  // Скрываем текущий WorkoutFragment
                    .add(R.id.frameLayout, selectionFragment, "selection_ex") // Добавляем новый фрагмент с тегом
                    .addToBackStack(null)  // Чтобы можно было вернуться назад
                    .commit();
        });



        setupRandomFood(view);

        foodBtn.setOnClickListener(v -> {
            // Передаем актуальные глобальные переменные, которые обновились в loadActivity
            FoodEquivalentFragment fragment = FoodEquivalentFragment.newInstance(
                    currentBurnedCalories,
                    currentDistance,
                    currentSteps,
                    selectedCardFoodIcon
            );

            getParentFragmentManager().beginTransaction()
                    .replace(R.id.frameLayout, fragment)
                    .addToBackStack(null)
                    .commit();
        });



        finalWorkBtn.setOnClickListener(v -> {
            // 1. Инициализация DAO
            WORKOUT_EXERCISE_TABLE_DAO workoutExerciseTableDao = new WORKOUT_EXERCISE_TABLE_DAO(MainActivity.getAppDataBase());
            CARDIO_SET_DETAILS_TABLE_DAO cardioSetDetailsTableDao = new CARDIO_SET_DETAILS_TABLE_DAO(MainActivity.getAppDataBase());
            STRENGTH_SET_DETAILS_TABLE_DAO strengthSetDetailsTableDao = new STRENGTH_SET_DETAILS_TABLE_DAO(MainActivity.getAppDataBase());
            MainActivity mainActivity = (MainActivity) getActivity();

            if (mainActivity == null) return;

            // 2. Цикл обработки упражнений из текущего списка (exList)
            for (ExerciseModel elm : exList) {
                // Если в упражнении вообще нет подходов в памяти - удаляем везде
                if (elm.getSets() == null || elm.getSets().isEmpty()) {
                    workoutExerciseTableDao.deleteExercise(elm);
                    mainActivity.getSyncManager().deleteExerciseFromCloud(elm);
                    continue;
                }

                int orderCounter = 1;
                boolean exerciseDeleted = false;

                // Создаем копию списка сетов для итерации, чтобы избежать ConcurrentModificationException
                List<Object> currentSets = new ArrayList<>(elm.getSets());

                for (Object set : currentSets) {
                    boolean isEmpty = false;

                    // Проверка на пустые значения
                    if (set instanceof StrengthSetModel) {
                        StrengthSetModel s = (StrengthSetModel) set;
                        isEmpty = (s.getStrength_set_rep() == 0 || s.getStrength_set_weight() == 0);
                    } else if (set instanceof CardioSetModel) {
                        CardioSetModel c = (CardioSetModel) set;
                        isEmpty = (c.getCardio_set_temp() == 0 || c.getCardio_set_time() == 0 || c.getCardio_set_distance() == 0);
                    }

                    if (isEmpty) {
                        // Удаляем пустой сет из базы
                        if (set instanceof StrengthSetModel) {
                            strengthSetDetailsTableDao.deleteStrengthSet((StrengthSetModel) set, (long) elm.getExercise_id());
                        } else if (set instanceof CardioSetModel) {
                            cardioSetDetailsTableDao.deleteCardioSet((CardioSetModel) set, (long) elm.getExercise_id());
                        }

                        // Удаляем из локального объекта в памяти
                        elm.getSets().remove(set);

                        // Если это был последний сет - удаляем упражнение целиком
                        if (elm.getSets().isEmpty()) {
                            workoutExerciseTableDao.deleteExercise(elm);
                            mainActivity.getSyncManager().deleteExerciseFromCloud(elm);
                            exerciseDeleted = true;
                            break; // Выходим из цикла сетов этого упражнения
                        }
                    } else {
                        // Если сет не пустой - обновляем его порядок в базе
                        if (set instanceof StrengthSetModel) {
                            ((StrengthSetModel) set).setStrength_set_order(orderCounter);
                            strengthSetDetailsTableDao.updateSetOrder((StrengthSetModel) set);
                        } else if (set instanceof CardioSetModel) {
                            ((CardioSetModel) set).setCardio_set_order(orderCounter);
                            cardioSetDetailsTableDao.updateSetOrder((CardioSetModel) set);
                        }
                        orderCounter++;
                    }
                }

                // Если упражнение выжило - помечаем как завершенное
                if (!exerciseDeleted) {
                    workoutExerciseTableDao.markExerciseAsFinished(elm.getExercise_id());
                }
            }

            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {

                // Перечитываем данные ПРЯМО перед отправкой
                List<ExerciseModel> finalSyncList = workoutExerciseTableDao.getAllExercisesForSync();

                Log.d("SYNC_FINAL", "Отправка на сервер. Кол-во упражнений: " + finalSyncList.size());

                // Отправляем
                mainActivity.getSyncManager().syncMultipleExercise(finalSyncList);

                // Обновляем UI
                exList = workoutExerciseTableDao.getExByState("unfinished");
                if (getView() != null) updateUI(getView(), exList);
                workoutAdapter.updateExList(exList);

                Toast.makeText(requireContext(), "Тренировка сохранена в облаке!", Toast.LENGTH_SHORT).show();

            }, 1000); // Задержка 300 миллисекунд

//            List<ExerciseModel> freshExercisesForCloud = workoutExerciseTableDao.getAllExercisesForSync();
//
//            FirestoreSyncManager syncManager = new FirestoreSyncManager();
//            syncManager.syncAllWorkouts(freshExercisesForCloud);


//            exList = workoutExerciseTableDao.getExByState("unfinished");
//
//            if (getView() != null) {
//                updateUI(getView(), exList);
//            }
//
//            if (workoutAdapter != null) {
//                workoutAdapter.updateExList(exList);
//            }

            Toast.makeText(requireContext(), "Тренировка сохранена и синхронизирована!", Toast.LENGTH_SHORT).show();
        });

        ActivityRingView activityRingView = view.findViewById(R.id.activityRing);

        // Находим контейнеры, которые мы добавили через <include>
        View stepsCard = view.findViewById(R.id.statSteps);
        View burnedCard = view.findViewById(R.id.statBurned);
        View distanceCard = view.findViewById(R.id.statDistance);
        View warning = view.findViewById(R.id.activity_warning_container);

        // Связываем всё вместе
        activityRingView.setupLabels(stepsCard, burnedCard, distanceCard, warning);

        // Устанавливаем данные из БД
        loadActivity(activityRingView);

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, d MMMM", new Locale("ru", "RU"));
        String formattedDate = dateFormat.format(calendar.getTime());
        formattedDate = formattedDate.substring(0, 1).toUpperCase() + formattedDate.substring(1);
        dateTextWorkout.setText(formattedDate);

        return view;
    }

    private void updateUI(View rootView, List<ExerciseModel> list) {
        TextView textView1 = rootView.findViewById(R.id.workout_title_D_TV);
        TextView textView2 = rootView.findViewById(R.id.workout_hint_D_TV);
        ImageView image = rootView.findViewById(R.id.workout_image_D_IV);
        Button finalWorkBtn = rootView.findViewById(R.id.workout_finish_BTN);

        if (list == null || list.isEmpty()) {
            textView1.setVisibility(View.VISIBLE);
            textView2.setVisibility(View.VISIBLE);
            image.setVisibility(View.VISIBLE);
            finalWorkBtn.setVisibility(View.GONE);
            if (workoutRecyclerView != null) workoutRecyclerView.setVisibility(View.GONE);
        } else {
            textView1.setVisibility(View.GONE);
            textView2.setVisibility(View.GONE);
            image.setVisibility(View.GONE);
            finalWorkBtn.setVisibility(View.VISIBLE);
            if (workoutRecyclerView != null) workoutRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    public void refreshWorkoutData() {
        if (workoutAdapter != null) {
            workoutAdapter.updateExList(exList); // из setExercises()
            assert getView() != null;
            updateUI(getView(), exList);
        }
    }

    private void loadActivity(ActivityRingView ringView) {

        DailyActivityTrackingDao activityDao = new DailyActivityTrackingDao(MainActivity.getAppDataBase());

        ActivityGoalDao goalDao = new ActivityGoalDao(MainActivity.getAppDataBase());

        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Calendar.getInstance().getTime());

        DailyActivityTrackingModel activity = activityDao.getActivityByDate(today);

        this.currentSteps = activity != null ? activity.getDaily_activity_tracking_steps() : 0;
        this.currentBurnedCalories = activity != null ?  activity.getDaily_activity_tracking_caloriesBurned() : 0f;
        this.currentDistance = (float) (currentSteps * 0.75 / 1000.0);

        // Расчет количества еды
        if (selectedFood != null && currentBurnedCalories > 0) {
            int count = (int) (currentBurnedCalories / selectedFood.calPerUnit);
            foodCountTv.setText("x" + count);
        } else {
            foodCountTv.setText("x0");
        }


        ActivityGoalModel goal = goalDao.getLatestGoal();

        int stepsGoal;
        int burnedGoal;
        boolean isDefaultGoals;

        if (goal != null) {
            stepsGoal = goal.getActivity_goal_steps();
            burnedGoal = goal.getActivity_goal_caloriesToBurn();
            isDefaultGoals = false;
        } else {
            stepsGoal = 10000;
            burnedGoal = 300;
            isDefaultGoals = true;
        }


        ringView.setActivityData(currentSteps, stepsGoal, (int) currentBurnedCalories, burnedGoal, currentDistance, isDefaultGoals);
    }

    private void setupRandomFood(View view) {
        // Список доступной еды
        int[][] foodPool = {
                {R.drawable.ic_donut, 250},
                {R.drawable.ic_burger, 510},
                {R.drawable.ic_pizza, 285},
                {R.drawable.ic_chicken_leg, 215},
                {R.drawable.ic_fries, 340},
                {R.drawable.ic_ice_cream, 190},
                {R.drawable.ic_nugget, 45}
        };

        int[] randomSelection = foodPool[new Random().nextInt(foodPool.length)];

        // Инициализируем те самые переменные, которые используются везде
        selectedCardFoodIcon = randomSelection[0];
        selectedCardFoodCal = randomSelection[1];

        // ВАЖНО: Инициализируем объект selectedFood, который проверяется в loadActivity
        selectedFood = new FoodPreview(selectedCardFoodIcon, selectedCardFoodCal);

        foodIconIv.setImageResource(selectedCardFoodIcon);
    }

}


