package com.example.workoutapp.Fragments.ProfileFragments;

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
import com.example.workoutapp.Data.ProfileDao.ActivityGoalDao;
import com.example.workoutapp.Data.ProfileDao.DailyActivityTrackingDao;
import com.example.workoutapp.Data.ProfileDao.DailyFoodTrackingDao;
import com.example.workoutapp.Data.ProfileDao.FoodGainGoalDao; // <-- НОВЫЙ ИМПОРТ DAO
import com.example.workoutapp.Data.ProfileDao.GeneralGoalDao;
import com.example.workoutapp.Data.ProfileDao.UserProfileDao;
import com.example.workoutapp.Data.ProfileDao.WeightHistoryDao;
import com.example.workoutapp.MainActivity;
import com.example.workoutapp.Models.ProfileModels.ActivityGoalModel;
import com.example.workoutapp.Models.ProfileModels.DailyActivityTrackingModel;
import com.example.workoutapp.Models.ProfileModels.DailyFoodTrackingModel;
import com.example.workoutapp.Models.ProfileModels.FoodGainGoalModel;
import com.example.workoutapp.Models.ProfileModels.GeneralGoalModel;
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
    private GeneralGoalDao generalGoalDao;
    private ActivityGoalDao activityGoalDao;
    private FoodGainGoalDao foodGainGoalDao; // <-- НОВОЕ ПОЛЕ DAO

    // ===============================================
    // 1. ХОЛДЕР ДАННЫХ ДЛЯ ПРОФИЛЯ
    // ===============================================
    private static class ProfileDataHolder {
        public UserProfileModel user;
        public WeightHistoryModel latestWeight;
        public DailyFoodTrackingModel todayFood;
        public DailyActivityTrackingModel todayActivity;
        public GeneralGoalModel generalGoalModel;

        // Новые поля
        public ActivityGoalModel activityGoal;
        public FoodGainGoalModel foodGoal; // <-- ЦЕЛЬ ПИТАНИЯ
    }

    // ===============================================
    // 2. ФУНКЦИЯ ПОЛУЧЕНИЯ РЕАЛЬНЫХ ДАННЫХ ИЗ DAO
    // ===============================================
    private ProfileDataHolder fetchProfileData() {
        ProfileDataHolder data = new ProfileDataHolder();

        // 1. Профиль и Вес
        data.user = userProfileDAO.getProfile();
        data.latestWeight = weightHistoryDAO.getLatestWeight();

        // 2. Цели
        data.generalGoalModel = generalGoalDao.getLatestGoal();
        data.activityGoal = activityGoalDao.getLatestGoal();
        data.foodGoal = foodGainGoalDao.getLatestGoal(); // <-- ПОЛУЧЕНИЕ ЦЕЛИ ПИТАНИЯ

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
        userProfileDAO = new UserProfileDao(MainActivity.getAppDataBase());
        weightHistoryDAO = new WeightHistoryDao(MainActivity.getAppDataBase());
        foodTrackingDAO = new DailyFoodTrackingDao(MainActivity.getAppDataBase());
        activityTrackingDAO = new DailyActivityTrackingDao(MainActivity.getAppDataBase());
        generalGoalDao = new GeneralGoalDao(MainActivity.getAppDataBase());
        activityGoalDao = new ActivityGoalDao(MainActivity.getAppDataBase());
        foodGainGoalDao = new FoodGainGoalDao(MainActivity.getAppDataBase()); // <-- ИНИЦИАЛИЗАЦИЯ DAO

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
        groupNames.add(res.getString(R.string.group_progress));       // Прогресс
        groupNames.add(res.getString(R.string.group_profile));        // Профиль
        groupNames.add(res.getString(R.string.group_general_goal));   // Общая цель
        groupNames.add(res.getString(R.string.group_activity_goal));  // Цели активности
        groupNames.add(res.getString(R.string.group_food_goal));      // Цели питания


        Map<String, Integer> childListResIdMap = new HashMap<>();
        childListResIdMap.put(res.getString(R.string.group_profile), R.array.children_profile);
        childListResIdMap.put(res.getString(R.string.group_general_goal), R.array.children_general_goal);
        childListResIdMap.put(res.getString(R.string.group_activity_goal), R.array.children_activity_goal);
        childListResIdMap.put(res.getString(R.string.group_food_goal), R.array.children_food_goal);
        childListResIdMap.put(res.getString(R.string.group_progress), R.array.children_progress);

        List<ProfileItemModel> flatList = new ArrayList<>();
        int groupIndexCounter = 0;

        for (String groupName : groupNames) {
            ProfileItemModel group = new ProfileItemModel(groupName, false);
            if (groupName.equals(res.getString(R.string.group_progress))) {
                group.isExpanded = true;  // Группа "Прогресс" всегда раскрыта
            }
            flatList.add(group);

            Integer childArrayResId = childListResIdMap.get(groupName);
            if (childArrayResId != null) {
                String[] childrenTemplates = res.getStringArray(childArrayResId);
                for (int i = 0; i < childrenTemplates.length; i++) {
                    String formattedChildTitle = formatChildTitle(groupName, childrenTemplates[i], i, data, res);

                    // Создаем новый элемент
                    ProfileItemModel childItem = new ProfileItemModel(formattedChildTitle, groupIndexCounter);

                    // Устанавливаем isExpanded для группы "Прогресс" (или другой группы, которую вы хотите изначально раскрыть)
                    if (groupName.equals(res.getString(R.string.group_progress))) {
                        childItem.isVisible = true;  // Все дочерние элементы группы "Прогресс" будут видимы по умолчанию
                    }

                    flatList.add(childItem);
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
                        value = " " + data.user.getUserName();
                        break;
                    case 1: // Рост:
                        value = " " +String.format(Locale.getDefault(), "%.1f", data.user.getUserHeight());
                        unit = " см";
                        break;
                    case 2: // Вес: (берем из истории веса)
                        if (data.latestWeight != null) {
                            value = " " + String.format(Locale.getDefault(), "%.1f", data.latestWeight.getWeightValue());
                            unit = " кг";
                        }
                        break;
                    case 3: // Возраст:
                        value = " " + String.valueOf(data.user.getUserAge());
                        unit = "";
                        break;
                }
            }
        }

        else if (groupName.equals(res.getString(R.string.group_general_goal))) {
            if (data.generalGoalModel != null) {
                switch (index) {
                    case 0:
                        value = " " + data.generalGoalModel.getGoalText(); // Например "Рекомпозиция"
                        break;
                    case 1:
                        // Получаем число
                        int workoutsCount = data.generalGoalModel.getWorkoutsWeekly();

                        // 1. Устанавливаем значение
                        value = " " + workoutsCount;

                        // 2. Определяем правильное склонение слова
                        unit = " " + getTimesDeclension(workoutsCount);

                        break;
                    case 2:
                        // Получаем число
                        int daysCount = data.generalGoalModel.getFoodTrackingWeekly();

                        // 1. Устанавливаем значение
                        value = " " + daysCount;

                        // 2. Определяем правильное склонение слова
                        unit = " " + getDaysDeclension(daysCount);

                        break;
                }
            }
        }
        else if (groupName.equals(res.getString(R.string.group_activity_goal))) {
            // ИСПОЛЬЗУЕМ activityGoal
            if (data.activityGoal != null) {
                switch (index) {
                    case 0: // Калории
                        value = " " + data.activityGoal.getCaloriesToBurn();
                        unit = " ккал";
                        break;
                    case 1: // Шаги
                        value = " " + data.activityGoal.getStepsGoal();
                        unit = " шагов";
                        break;
                }
            }
        }
        else if (groupName.equals(res.getString(R.string.group_food_goal))) {
            // ИСПОЛЬЗУЕМ foodGoal (Цель питания)
            if (data.foodGoal != null) {
                switch (index) {
                    case 0: // Калории
                        value = " " + data.foodGoal.getCaloriesGoal();
                        unit = " ккал";
                        break;
                    case 1: // Белки (форматируем как целое число)
                        value = " " + String.format(Locale.getDefault(), "%.0f", data.foodGoal.getProteinGoal());
                        unit = " г";
                        break;
                    case 2: // Жиры (форматируем как целое число)
                        value = " " + String.format(Locale.getDefault(), "%.0f", data.foodGoal.getFatGoal());
                        unit = " г";
                        break;
                    case 3: // Углеводы (форматируем как целое число)
                        value = " " + String.format(Locale.getDefault(), "%.0f", data.foodGoal.getCarbGoal());
                        unit = " г";
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

        if (groupName.equals(getResources().getString(R.string.group_general_goal))) {
            if (getFragmentManager() != null) {
                getFragmentManager().beginTransaction()
                        .replace(R.id.frameLayout, new GeneralGoalFragment()) // Используйте ID вашего контейнера
                        .addToBackStack(null)
                        .commit();
            }
        }

        if (groupName.equals(getResources().getString(R.string.group_activity_goal))) {
            if (getFragmentManager() != null) {
                getFragmentManager().beginTransaction()
                        .replace(R.id.frameLayout, new ActivityGoalFragment()) // Используйте ID вашего контейнера
                        .addToBackStack(null)
                        .commit();
            }
        }

        if (groupName.equals(getResources().getString(R.string.group_food_goal))) {
            if (getFragmentManager() != null) {
                getFragmentManager().beginTransaction()
                        .replace(R.id.frameLayout, new FoodGoalFragment()) // Используйте ID вашего контейнера
                        .addToBackStack(null)
                        .commit();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshProfileData();
    }

    public static String getDaysDeclension(int number) {
        // В русском языке числа 11-14 всегда имеют форму "дней"
        if (number % 100 >= 11 && number % 100 <= 14) {
            return "дней";
        }

        int lastDigit = number % 10;

        // Число заканчивается на 1 (кроме 11): 1, 21, 31
        if (lastDigit == 1) {
            return "день";
        }

        // Число заканчивается на 2, 3, 4 (кроме 12-14): 2, 3, 4, 22, 23
        if (lastDigit >= 2 && lastDigit <= 4) {
            return "дня";
        }

        // Все остальные случаи: 0, 5, 6, 10, 15, 20, 25...
        return "дней";
    }

    public static String getTimesDeclension(int number) {
        // В русском языке числа 11-14 всегда имеют форму "раз"
        if (number % 100 >= 11 && number % 100 <= 14) {
            return "раз";
        }

        int lastDigit = number % 10;

        // Число заканчивается на 1 (кроме 11): 1, 21, 31
        if (lastDigit == 1) {
            return "раз";
        }

        // Число заканчивается на 2, 3, 4 (кроме 12-14): 2, 3, 4, 22, 23
        if (lastDigit >= 2 && lastDigit <= 4) {
            return "раза";
        }

        // Все остальные случаи: 0, 5, 6, 10, 15, 20, 25...
        return "раз";
    }

    private void refreshProfileData() {
        ProfileDataHolder profileData = fetchProfileData();
        List<ProfileItemModel> updatedList = prepareDataForRecyclerView(profileData);
        profileAdapter.dataList.clear();
        profileAdapter.dataList.addAll(updatedList);
        profileAdapter.notifyDataSetChanged();
    }
}