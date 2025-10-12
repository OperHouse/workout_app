package com.example.workoutapp.Fragments.ProfileFragments;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workoutapp.Adapters.ProfileAdapters.ProfileAdapter;
import com.example.workoutapp.Data.ProfileDao.DailyActivityTrackingDao;
import com.example.workoutapp.Data.ProfileDao.DailyFoodTrackingDao;
import com.example.workoutapp.Data.ProfileDao.GoalDao;
import com.example.workoutapp.Data.ProfileDao.UserProfileDao;
import com.example.workoutapp.Data.ProfileDao.WeightHistoryDao;
import com.example.workoutapp.Data.Tables.AppDataBase; // Для инициализации DAO
import com.example.workoutapp.Models.ProfileModels.DailyActivityTrackingModel;
import com.example.workoutapp.Models.ProfileModels.DailyFoodTrackingModel;
import com.example.workoutapp.Models.ProfileModels.GoalModel;
import com.example.workoutapp.Models.ProfileModels.ProfileItemModel;
import com.example.workoutapp.Models.ProfileModels.UserProfileModel;
import com.example.workoutapp.Models.ProfileModels.WeightHistoryModel;
import com.example.workoutapp.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ProfileFragment extends Fragment implements ProfileAdapter.OnChildItemClickListener {

    private RecyclerView groupRecyclerView;
    private ProfileAdapter profileAdapter;

    // Поля для DAO
    private UserProfileDao userProfileDAO;
    private WeightHistoryDao weightHistoryDAO;
    private DailyFoodTrackingDao foodTrackingDAO;
    private DailyActivityTrackingDao activityTrackingDAO;
    private GoalDao goalDAO;

    // ===============================================
    // 1. ХОЛДЕР ДАННЫХ ДЛЯ ПРОФИЛЯ
    // ===============================================
    private static class ProfileDataHolder {
        public UserProfileModel user;
        public WeightHistoryModel latestWeight;
        public DailyFoodTrackingModel todayFood;
        public DailyActivityTrackingModel todayActivity;
        public GoalModel currentGoal;
    }

    // ===============================================
    // 2. ФУНКЦИЯ ПОЛУЧЕНИЯ РЕАЛЬНЫХ ДАННЫХ ИЗ DAO
    // ===============================================
    private ProfileDataHolder fetchProfileData() {
        ProfileDataHolder data = new ProfileDataHolder();

        // Получение данных. Если DAO возвращает null, это автоматически обрабатывается
        // в методе formatChildTitle, который использует "Н/Д" по умолчанию.

        // 1. Профиль и Вес
        data.user = userProfileDAO.getProfileById(1); // Предполагаем ID=1 для единственного пользователя
        data.latestWeight = weightHistoryDAO.getLatestWeight();

        // 2. Цели
        data.currentGoal = goalDAO.getCurrentGoal();

        // 3. Ежедневные трекеры (для Активности)
        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().getTime());
        data.todayFood = foodTrackingDAO.getFoodTrackingByDate(todayDate);
        data.todayActivity = activityTrackingDAO.getActivityByDate(todayDate);

        return data;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View ProfileView = inflater.inflate(R.layout.fragment_profile, container, false);

        // --- ИНИЦИАЛИЗАЦИЯ DAO ---
        Context context = getContext();
        if (context != null) {
            AppDataBase dbHelper = AppDataBase.getInstance(context); // Предполагаем, что AppDataBase имеет статический метод getInstance

            userProfileDAO = new UserProfileDao(dbHelper);
            weightHistoryDAO = new WeightHistoryDao(dbHelper);
            foodTrackingDAO = new DailyFoodTrackingDao(dbHelper);
            activityTrackingDAO = new DailyActivityTrackingDao(dbHelper);
            goalDAO = new GoalDao(dbHelper);
        }
        // -------------------------

        // Настройка отображения даты
        TextView dateTextView = ProfileView.findViewById(R.id.dateTextProfile);
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, d MMMM", new Locale("ru", "RU"));
        String formattedDate = dateFormat.format(calendar.getTime());
        formattedDate = formattedDate.substring(0, 1).toUpperCase() + formattedDate.substring(1);
        dateTextView.setText(formattedDate);

        // ===============================================
        // 3. ПОЛУЧЕНИЕ И ИНТЕГРАЦИЯ ДАННЫХ
        // ===============================================

        ProfileDataHolder profileData = fetchProfileData();
        List<ProfileItemModel> dataItems = prepareDataForRecyclerView(profileData);

        // Настройка RecyclerView
        groupRecyclerView = ProfileView.findViewById(R.id.group_RV);
        groupRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        groupRecyclerView.setNestedScrollingEnabled(false);

        profileAdapter = new ProfileAdapter(dataItems, getResources(), this);
        groupRecyclerView.setAdapter(profileAdapter);

        return ProfileView;
    }

    private List<ProfileItemModel> prepareDataForRecyclerView(ProfileDataHolder data) {
        Resources res = getResources();

        List<String> groupNames = new ArrayList<>();
        groupNames.add(res.getString(R.string.group_profile));
        groupNames.add(res.getString(R.string.group_nutrition));
        groupNames.add(res.getString(R.string.group_activity));
        groupNames.add(res.getString(R.string.group_data));

        Map<String, Integer> childListResIdMap = new HashMap<>();
        childListResIdMap.put(groupNames.get(0), R.array.children_profile);
        childListResIdMap.put(groupNames.get(1), R.array.children_nutrition);
        childListResIdMap.put(groupNames.get(2), R.array.children_activity);

        List<ProfileItemModel> flatList = new ArrayList<>();
        int groupIndexCounter = 0;

        for (String groupName : groupNames) {

            ProfileItemModel group = new ProfileItemModel(groupName, false);
            flatList.add(group);

            Integer childArrayResId = childListResIdMap.get(groupName);

            if (childArrayResId != null) {
                String[] childrenTemplates = res.getStringArray(childArrayResId);

                for (int i = 0; i < childrenTemplates.length; i++) {
                    String formattedChildTitle = formatChildTitle(groupName, childrenTemplates[i], i, data, res);
                    flatList.add(new ProfileItemModel(formattedChildTitle, groupIndexCounter));
                }
            }

            groupIndexCounter = flatList.size();
        }
        return flatList;
    }

    // ===============================================
    // 4. ФУНКЦИЯ ДЛЯ ФОРМАТИРОВАНИЯ ЗАГОЛОВКОВ
    // ===============================================

    private String formatChildTitle(String groupName, String template, int index, ProfileDataHolder data, Resources res) {
        String value = "Н/Д"; // Значение по умолчанию
        String unit = "";     // Единица измерения

        // --- Группа "Профиль" ---
        if (groupName.equals(res.getString(R.string.group_profile))) {
            if (data.user != null) {
                switch (index) {
                    case 0: // Имя:
                        value = data.user.getUserName();
                        break;
                    case 1: // Рост:
                        value = String.format(Locale.getDefault(), "%.1f", data.user.getUserHeight());
                        unit = " см";
                        break;
                    case 2: // Вес: (берем из истории веса)
                        if (data.latestWeight != null) {
                            value = String.format(Locale.getDefault(), "%.1f", data.latestWeight.getWeightValue());
                            unit = " кг";
                        }
                        break;
                    case 3: // Возраст:
                        value = data.user.getUserAge();
                        unit = "";
                        break;
                    case 4: // Цель: (общая текстовая цель)
                        if (data.currentGoal != null) {
                            value = data.currentGoal.getUserGoalText();
                            unit = "";
                        } else {
                            value = "Не задана";
                        }
                        break;
                }
            }
        }

        // --- Группа "Питание" (Цели) ---
        else if (groupName.equals(res.getString(R.string.group_nutrition))) {
            if (data.currentGoal != null) {
                double goalValue = 0;

                switch (index) {
                    case 0: // Калории:
                        goalValue = data.currentGoal.getGoalCaloriesGain();
                        unit = " ккал";
                        break;
                    case 1: // Белки:
                        goalValue = data.currentGoal.getGoalProtein();
                        unit = " г";
                        break;
                    case 2: // Жиры:
                        goalValue = data.currentGoal.getGoalFat();
                        unit = " г";
                        break;
                    case 3: // Углеводы:
                        goalValue = data.currentGoal.getGoalCarb();
                        unit = " г";
                        break;
                }

                // Форматируем только цель
                value = String.format(Locale.getDefault(), "%.0f", goalValue);
            }
        }

        // --- Группа "Активность" ---
        else if (groupName.equals(res.getString(R.string.group_activity))) {

            // Получаем цель и факт для Активности
            GoalModel goal = data.currentGoal;
            DailyActivityTrackingModel activity = data.todayActivity;

            if (goal != null || activity != null) {
                switch (index) {
                    case 0: // Цель подвижности: (калории сжечь)
                        if (goal != null) {
                            value = String.format(Locale.getDefault(), "%.0f ккал", goal.getGoalToBurnCalories());
                            unit = " (цель)";
                        }
                        break;
                    case 1: // Цель шагов (в день): (Факт / Цель)
                        int actualSteps = (activity != null) ? activity.getTrackingActivitySteps() : 0;
                        int goalSteps = (goal != null) ? goal.getGoalSteps() : 0;

                        // Если цель не задана, показываем только факт
                        if (goal == null) {
                            value = String.format(Locale.getDefault(), "%d", actualSteps);
                            unit = " факт";
                        } else {
                            value = String.format(Locale.getDefault(), "%d / %d", actualSteps, goalSteps);
                            unit = " (факт/цель)";
                        }
                        break;
                    case 2: // Цель тренировок (в неделю):
                        if (goal != null) {
                            value = String.format(Locale.getDefault(), "%d", goal.getGoalWorkoutsWeekly());
                            unit = " раз (цель)";
                        }
                        break;
                    case 3: // Последняя тренировка:
                        // Для этого поля нужны данные из таблицы тренировок,
                        // которых у нас нет. Оставляем Н/Д, или имитируем отсутствие данных.
                        // value = "Вчера (30 мин)";
                        unit = "";
                        break;
                }
            }
        }

        // Если значение осталось "Н/Д", убираем единицу измерения.
        if (value.equals(" Н/Д")) {
            unit = "";
        } else {
            // Если значение получено, добавляем разделитель и убираем "Н/Д"
            template = template.trim();
        }

        // Объединяем шаблон, значение и единицу измерения
        return template + value + unit;
    }

    @Override
    public void onChildItemClick(int flatPosition, String title) {
        // Логика перехода:

        ProfileItemModel clickedItem = profileAdapter.dataList.get(flatPosition);
        if (clickedItem.groupIndex == -1) return;

        // Получаем заголовок родительской группы
        ProfileItemModel parentGroup = profileAdapter.dataList.get(clickedItem.groupIndex);
        String groupName = parentGroup.title;

        if (groupName.equals(getResources().getString(R.string.group_profile))) {
            if (getFragmentManager() != null) {
                getFragmentManager().beginTransaction()
                        .replace(R.id.frameLayout, new ProfileSettingsFragment()) // Используйте ID вашего контейнера
                        .addToBackStack(null)
                        .commit();
            }
        }
    }
}