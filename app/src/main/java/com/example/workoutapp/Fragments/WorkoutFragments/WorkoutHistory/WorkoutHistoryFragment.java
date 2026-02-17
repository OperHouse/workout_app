package com.example.workoutapp.Fragments.WorkoutFragments.WorkoutHistory;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.example.workoutapp.Data.WorkoutDao.WORKOUT_EXERCISE_TABLE_DAO;
import com.example.workoutapp.MainActivity;
import com.example.workoutapp.Models.WorkoutModels.WorkoutSessionModel;
import com.example.workoutapp.R;
import com.example.workoutapp.Tools.OnNavigationVisibilityListener;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointBackward;
import com.google.android.material.datepicker.MaterialDatePicker;

import net.sqlcipher.database.SQLiteDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class WorkoutHistoryFragment extends Fragment {

    private RecyclerView workoutRv;
    private TextView tvNoData;
    private ImageView backBtn, calendarBtn;
    private Button loadMoreBtn;
    private LottieAnimationView lottieEmpty;

    private WorkoutHistoryAdapter adapter;
    private List<WorkoutSessionModel> workoutList = new ArrayList<>();

    private OnNavigationVisibilityListener navigationListener;


    private int currentOffset = 0;
    private final int LIMIT = 7;
    private boolean isFilteredByDate = false;

    public WorkoutHistoryFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_workout_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);

        // Загружаем первую порцию данных (offset = 0)
        loadWorkoutSessions(true);
    }

    private void initViews(View view) {
        backBtn = view.findViewById(R.id.history_back_btn);
        backBtn.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        calendarBtn = view.findViewById(R.id.stats_calendar_btn);
        calendarBtn.setOnClickListener(v -> showDatePicker());

        workoutRv = view.findViewById(R.id.rv_history_list);
        tvNoData = view.findViewById(R.id.tv_no_data);
        loadMoreBtn = view.findViewById(R.id.load_more_BTN);
        lottieEmpty = view.findViewById(R.id.lottie_empty);

        workoutRv.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new WorkoutHistoryAdapter(workoutList, session -> {
            // Открываем фрагмент деталей
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.frameLayout, WorkoutDetailsFragment.newInstance(session))
                    .addToBackStack(null)
                    .commit();
        });
        workoutRv.setAdapter(adapter);

        loadMoreBtn.setOnClickListener(v -> {
            if (isFilteredByDate) {
                isFilteredByDate = false;
                loadWorkoutSessions(true); // Сброс фильтра и показ всех
            } else {
                loadWorkoutSessions(false); // Подгрузка следующих 7
            }
        });
    }

    private void loadWorkoutSessions(boolean isFirstLoad) {
        if (isFirstLoad) {
            currentOffset = 0;
            workoutList.clear();
        }

        new Thread(() -> {
            SQLiteDatabase db = MainActivity.getAppDataBase();

            // Инициализируем DAO (убедись, что метод доступа к БД верный)
            WORKOUT_EXERCISE_TABLE_DAO dao = new WORKOUT_EXERCISE_TABLE_DAO(MainActivity.getAppDataBase());

            // Получаем список сессий
            List<WorkoutSessionModel> sessions = dao.getWorkoutHistory(LIMIT, currentOffset);

            if (isAdded()) {
                requireActivity().runOnUiThread(() -> {
                    if (isFirstLoad && sessions.isEmpty()) {
                        showEmptyState("Кажется, у вас пока нет ни одной завершенной тренировки");
                    } else {
                        hideEmptyState();
                        workoutList.addAll(sessions);
                        adapter.notifyDataSetChanged();

                        if (sessions.size() < LIMIT) {
                            loadMoreBtn.setVisibility(View.GONE);
                        } else {
                            loadMoreBtn.setVisibility(View.VISIBLE);
                            loadMoreBtn.setText("Загрузить еще");
                        }
                        currentOffset += LIMIT;
                    }
                });
            }
        }).start();
    }

    private void showDatePicker() {
        CalendarConstraints constraints = new CalendarConstraints.Builder()
                .setValidator(DateValidatorPointBackward.now())
                .build();

        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTheme(R.style.CustomCalendarTheme)
                .setTitleText("Поиск тренировки по дате")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .setCalendarConstraints(constraints)
                .build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            calendar.setTimeInMillis(selection);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String selectedDate = sdf.format(calendar.getTime());

            loadSpecificDay(selectedDate);
        });

        datePicker.show(getParentFragmentManager(), "DATE_PICKER");
    }

    private void loadSpecificDay(String date) {
        isFilteredByDate = true;
        workoutList.clear();
        loadMoreBtn.setVisibility(View.GONE);

        new Thread(() -> {
            SQLiteDatabase db = MainActivity.getAppDataBase();
            WORKOUT_EXERCISE_TABLE_DAO dao = new WORKOUT_EXERCISE_TABLE_DAO(MainActivity.getAppDataBase());

            // Предположим, ваш метод в DAO теперь возвращает список моделей сессий
            List<WorkoutSessionModel> foundSessions = dao.getWorkoutHistoryByDate(date);

            if (isAdded()) {
                requireActivity().runOnUiThread(() -> {
                    if (foundSessions.isEmpty()) {
                        showEmptyState("В этот день (" + date + ") тренировок не найдено");
                        loadMoreBtn.setVisibility(View.VISIBLE);
                        loadMoreBtn.setText("Показать все");
                    } else {
                        hideEmptyState();
                        workoutList.addAll(foundSessions);
                        adapter.notifyDataSetChanged();
                        loadMoreBtn.setVisibility(View.VISIBLE);
                        loadMoreBtn.setText("Показать все");
                    }
                });
            }
        }).start();
    }

    private void showEmptyState(String message) {
        lottieEmpty.setVisibility(View.VISIBLE);
        lottieEmpty.setAnimation(R.raw.tumbleweed_rolling);
        lottieEmpty.playAnimation();
        tvNoData.setVisibility(View.VISIBLE);
        tvNoData.setText(message);
        loadMoreBtn.setVisibility(View.GONE);
    }

    private void hideEmptyState() {
        lottieEmpty.setVisibility(View.GONE);
        lottieEmpty.cancelAnimation();
        tvNoData.setVisibility(View.GONE);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnNavigationVisibilityListener)
            navigationListener = (OnNavigationVisibilityListener) context;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (navigationListener != null) navigationListener.setBottomNavVisibility(false);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (navigationListener != null) navigationListener.setBottomNavVisibility(true);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        navigationListener = null;
    }
}