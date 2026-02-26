package com.example.workoutapp.Fragments.ProfileFragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workoutapp.Adapters.ProfileAdapters.ProfileAdapter;
import com.example.workoutapp.Data.ProfileDao.ActivityGoalDao;
import com.example.workoutapp.Data.ProfileDao.DailyActivityTrackingDao;
import com.example.workoutapp.Data.ProfileDao.DailyFoodTrackingDao;
import com.example.workoutapp.Data.ProfileDao.FoodGainGoalDao;
import com.example.workoutapp.Data.ProfileDao.GeneralGoalDao;
import com.example.workoutapp.Data.ProfileDao.UserProfileDao;
import com.example.workoutapp.Data.ProfileDao.WeightHistoryDao;
import com.example.workoutapp.Data.WorkoutDao.WORKOUT_EXERCISE_TABLE_DAO;
import com.example.workoutapp.Fragments.ProfileFragments.Divide.ActivityGoalFragment;
import com.example.workoutapp.Fragments.ProfileFragments.Divide.DataManagementFragment;
import com.example.workoutapp.Fragments.ProfileFragments.Divide.FoodGoalFragment;
import com.example.workoutapp.Fragments.ProfileFragments.Divide.GeneralGoalFragment;
import com.example.workoutapp.Fragments.ProfileFragments.Divide.ProfileSettingsFragment;
import com.example.workoutapp.HealthSettingsActivity;
import com.example.workoutapp.MainActivity;
import com.example.workoutapp.Models.ProfileModels.ActivityGoalModel;
import com.example.workoutapp.Models.ProfileModels.DailyActivityTrackingModel;
import com.example.workoutapp.Models.ProfileModels.DailyFoodTrackingModel;
import com.example.workoutapp.Models.ProfileModels.FoodGainGoalModel;
import com.example.workoutapp.Models.ProfileModels.GeneralGoalModel;
import com.example.workoutapp.Models.Helpers.ProfileItemModel;
import com.example.workoutapp.Models.Helpers.ProfileViewModel;
import com.example.workoutapp.Models.ProfileModels.UserProfileModel;
import com.example.workoutapp.Models.ProfileModels.WeightHistoryModel;
import com.example.workoutapp.R;
import com.google.android.material.imageview.ShapeableImageView;

import java.io.File;
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
    private ShapeableImageView profilePhotoIV;
    private TextView helloTV, ageTV, heightTV;

    private boolean isPhotoPicking = false;

    private UserProfileDao userProfileDAO;
    private WeightHistoryDao weightHistoryDAO;
    private DailyFoodTrackingDao foodTrackingDAO;
    private DailyActivityTrackingDao activityTrackingDAO;
    private GeneralGoalDao generalGoalDao;
    private ActivityGoalDao activityGoalDao;
    private FoodGainGoalDao foodGainGoalDao;
    private WORKOUT_EXERCISE_TABLE_DAO workoutDao;

    private ActivityResultLauncher<String> mGetContent;
    private ProfileViewModel viewModel;

    // Вспомогательный класс для данных (Холдер)
    public static class ProfileDataHolder {
        public UserProfileModel user;
        public WeightHistoryModel latestWeight;
        public DailyFoodTrackingModel todayFood;
        public DailyActivityTrackingModel todayActivity;
        public GeneralGoalModel generalGoalModel;
        public String latestWorkout;
        public ActivityGoalModel activityGoal;
        public FoodGainGoalModel foodGoal;
    }

    @FunctionalInterface
    public interface ProfileDataProvider {
        ProfileDataHolder fetch();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // 1. Инициализация View
        profilePhotoIV = view.findViewById(R.id.profile_fragment_photo_IB);
        helloTV = view.findViewById(R.id.profile_fragment_name_TV);
        ageTV = view.findViewById(R.id.profile_fragment_age_TV);
        heightTV = view.findViewById(R.id.profile_fragment_height_TV);
        groupRecyclerView = view.findViewById(R.id.group_RV);

        // 2. Инициализация DAO
        initDAOs();

        // 3. Настройка RecyclerView
        groupRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        groupRecyclerView.setNestedScrollingEnabled(false);
        profileAdapter = new ProfileAdapter(new ArrayList<>(), getResources(), this);
        groupRecyclerView.setAdapter(profileAdapter);

        // 4. Настройка даты
        setupDateHeader(view);

        // 5. Регистрация выбора фото
        setupPhotoPicker();

        // 6. Работа с ViewModel (Асинхронная загрузка)
        viewModel = new ViewModelProvider(requireActivity()).get(ProfileViewModel.class);

        // Подписываемся на изменения данных
        viewModel.getProfileData().observe(getViewLifecycleOwner(), data -> {
            if (data != null) {
                updateUI(data);
            }
        });

        // Запускаем загрузку, если данных еще нет
        viewModel.loadData(this::fetchProfileDataFromServer);

        return view;
    }

    private void initDAOs() {
        userProfileDAO = new UserProfileDao(MainActivity.getAppDataBase());
        weightHistoryDAO = new WeightHistoryDao(MainActivity.getAppDataBase());
        foodTrackingDAO = new DailyFoodTrackingDao(MainActivity.getAppDataBase());
        activityTrackingDAO = new DailyActivityTrackingDao(MainActivity.getAppDataBase());
        generalGoalDao = new GeneralGoalDao(MainActivity.getAppDataBase());
        activityGoalDao = new ActivityGoalDao(MainActivity.getAppDataBase());
        foodGainGoalDao = new FoodGainGoalDao(MainActivity.getAppDataBase());
        workoutDao = new WORKOUT_EXERCISE_TABLE_DAO(MainActivity.getAppDataBase());
    }

    // Этот метод вызывается внутри ViewModel в фоновом потоке
    public ProfileDataHolder fetchProfileDataFromServer() {
        ProfileDataHolder data = new ProfileDataHolder();
        data.user = userProfileDAO.getProfile();
        data.latestWeight = weightHistoryDAO.getLatestWeight();
        data.latestWorkout = workoutDao.getLatestWorkoutDate();
        data.generalGoalModel = generalGoalDao.getLatestGoal();
        data.activityGoal = activityGoalDao.getLatestGoal();
        data.foodGoal = foodGainGoalDao.getLatestGoal();

        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().getTime());
        data.todayFood = foodTrackingDAO.getLatestEntry();
        data.todayActivity = activityTrackingDAO.getActivityByDate(todayDate);
        return data;
    }

    private void updateUI(ProfileDataHolder data) {
        // Обновление текстовых полей заголовка
        if (data.user != null) {
            String userName = data.user.getUserName();
            helloTV.setText((userName != null && !userName.isEmpty()) ? "Приветствуем, " + userName + "!" : "Приветствуем пользователь!");

            int age = data.user.getUserAge();
            ageTV.setText("Возраст:  " + age + " " + getYearDeclension(age));

            double height = data.user.getUserHeight();
            heightTV.setText(String.format(Locale.getDefault(), "Рост: %.0f см", height));

            // Фото
            String imagePath = data.user.getUserImagePath();
            if (imagePath != null && !imagePath.isEmpty()) {
                File imgFile = new File(imagePath);
                if (imgFile.exists()) {
                    profilePhotoIV.setImageURI(null);
                    profilePhotoIV.setImageURI(Uri.fromFile(imgFile));
                } else {
                    profilePhotoIV.setImageResource(R.drawable.ic_user_profile);
                }
            }
        }

        // Обновление списка RecyclerView
        List<ProfileItemModel> items = prepareDataForRecyclerView(data);
        profileAdapter.dataList.clear();
        profileAdapter.dataList.addAll(items);
        profileAdapter.notifyDataSetChanged();
    }

    private void setupDateHeader(View view) {
        TextView dateTextView = view.findViewById(R.id.dateTextProfile);
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, d MMMM", new Locale("ru", "RU"));
        String formattedDate = dateFormat.format(Calendar.getInstance().getTime());
        formattedDate = formattedDate.substring(0, 1).toUpperCase() + formattedDate.substring(1);
        dateTextView.setText(formattedDate);
    }

    private void setupPhotoPicker() {
        profilePhotoIV.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.frameLayout, new ProfileSettingsFragment())
                    .addToBackStack(null)
                    .commit();
        });

        mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                String newPath = saveImageToInternalStorage(uri);
                if (newPath != null) {
                    updatePhotoInDatabase(newPath);
                    // После обновления в БД просим ViewModel перезагрузить данные
                    viewModel.loadData(this::fetchProfileDataFromServer);
                }
            }
            profilePhotoIV.postDelayed(() -> isPhotoPicking = false, 500);
        });
    }

    // --- Логика подготовки списка (ваша без изменений) ---

    private List<ProfileItemModel> prepareDataForRecyclerView(ProfileDataHolder data) {
        Resources res = getResources();
        List<String> groupNames = new ArrayList<>();
        groupNames.add(res.getString(R.string.group_progress));
        groupNames.add(res.getString(R.string.group_profile));
        groupNames.add(res.getString(R.string.group_general_goal));
        groupNames.add(res.getString(R.string.group_activity_goal));
        groupNames.add(res.getString(R.string.group_food_goal));
        groupNames.add(res.getString(R.string.data_management));
        groupNames.add(res.getString(R.string.Permissions));

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
            if (groupName.equals(res.getString(R.string.group_progress))) group.isExpanded = true;
            flatList.add(group);

            Integer childArrayResId = childListResIdMap.get(groupName);
            if (childArrayResId != null) {
                String[] childrenTemplates = res.getStringArray(childArrayResId);
                for (int i = 0; i < childrenTemplates.length; i++) {
                    String formattedTitle = formatChildTitle(groupName, childrenTemplates[i], i, data, res);
                    ProfileItemModel childItem = new ProfileItemModel(formattedTitle, groupIndexCounter);
                    if (groupName.equals(res.getString(R.string.group_progress))) childItem.isVisible = true;
                    flatList.add(childItem);
                }
            }
            groupIndexCounter = flatList.size();
        }
        return flatList;
    }

    private String formatChildTitle(String groupName, String template, int index, ProfileDataHolder data, Resources res) {
        String value = " Н/Д";
        String unit = "";

        if (groupName.equals(res.getString(R.string.group_profile)) && data.user != null) {
            switch (index) {
                case 0: value = " " + data.user.getUserName(); break;
                case 1: value = " " + String.format(Locale.getDefault(), "%.1f", data.user.getUserHeight()); unit = " см"; break;
                case 2: if (data.latestWeight != null) { value = " " + String.format(Locale.getDefault(), "%.1f", data.latestWeight.getWeight_history_value()); unit = " кг"; } break;
                case 3: value = " " + data.user.getUserAge(); break;
            }
        } else if (groupName.equals(res.getString(R.string.group_general_goal)) && data.generalGoalModel != null) {
            switch (index) {
                case 0: value = " " + data.generalGoalModel.getGeneral_goal_Text(); break;
                case 1: int w = data.generalGoalModel.getGeneral_goal_workoutsWeekly(); value = " " + w; unit = " " + getTimesDeclension(w); break;
                case 2: int d = data.generalGoalModel.getGeneral_goal_foodTrackingWeekly(); value = " " + d; unit = " " + getDaysDeclension(d); break;
            }
        } else if (groupName.equals(res.getString(R.string.group_activity_goal)) && data.activityGoal != null) {
            switch (index) {
                case 0: value = " " + data.activityGoal.getActivity_goal_caloriesToBurn(); unit = " ккал"; break;
                case 1: value = " " + data.activityGoal.getActivity_goal_steps(); unit = " шагов"; break;
            }
        } else if (groupName.equals(res.getString(R.string.group_food_goal)) && data.foodGoal != null) {
            switch (index) {
                case 0: value = " " + data.foodGoal.getFood_gain_goal_calories(); unit = " ккал"; break;
                case 1: value = " " + String.format(Locale.getDefault(), "%.0f", data.foodGoal.getFood_gain_goal_protein()); unit = " г"; break;
                case 2: value = " " + String.format(Locale.getDefault(), "%.0f", data.foodGoal.getFood_gain_goal_fat()); unit = " г"; break;
                case 3: value = " " + String.format(Locale.getDefault(), "%.0f", data.foodGoal.getFood_gain_goal_carb()); unit = " г"; break;
            }
        } else if (groupName.equals(res.getString(R.string.group_progress))) {
            switch (index) {
                case 0: if (data.latestWeight != null) { value = " " + String.format(Locale.getDefault(), "%.1f", data.latestWeight.getWeight_history_value()); unit = " кг"; } break;
                case 1: if (data.todayActivity != null) value = " " + getRelativeDate(data.todayActivity.getDaily_activity_tracking_date()); break;
                case 2: if (data.todayFood != null) value = " " + getRelativeDate(data.todayFood.getDaily_food_tracking_date()); break;
                case 3: if (data.latestWorkout != null) value = " " + getRelativeDate(data.latestWorkout); break;
            }
        }
        if (value.equals(" Н/Д")) unit = "";
        return template.trim() + value + unit;
    }

    @Override
    public void onChildItemClick(int flatPosition, String title) {
        ProfileItemModel clickedItem = profileAdapter.dataList.get(flatPosition);
        if (clickedItem.groupIndex == -1) return;

        ProfileItemModel parentGroup = profileAdapter.dataList.get(clickedItem.groupIndex);
        String groupName = parentGroup.title;

        Fragment fragment = null;
        if (groupName.equals(getResources().getString(R.string.group_profile))) fragment = new ProfileSettingsFragment();
        else if (groupName.equals(getResources().getString(R.string.group_general_goal))) fragment = new GeneralGoalFragment();
        else if (groupName.equals(getResources().getString(R.string.group_activity_goal))) fragment = new ActivityGoalFragment();
        else if (groupName.equals(getResources().getString(R.string.group_food_goal))) fragment = new FoodGoalFragment();

        if (fragment != null) {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.frameLayout, fragment)
                    .addToBackStack(null)
                    .commit();
        }

        if (groupName.equals(getResources().getString(R.string.Permissions))) {
            startActivity(new Intent(getContext(), HealthSettingsActivity.class));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (viewModel != null && !isPhotoPicking) {
            viewModel.loadData(this::fetchProfileDataFromServer);
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            if (viewModel != null) {
                viewModel.loadData(this::fetchProfileDataFromServer);
            }
        }
    }

    // --- Утилитные методы (склонения, даты, сохранение фото) ---

    public static String getYearDeclension(int age) {
        if (age % 100 >= 11 && age % 100 <= 14) return "лет";
        int lastDigit = age % 10;
        if (lastDigit == 1) return "год";
        if (lastDigit >= 2 && lastDigit <= 4) return "года";
        return "лет";
    }

    public static String getDaysDeclension(int number) {
        if (number % 100 >= 11 && number % 100 <= 14) return "дней";
        int lastDigit = number % 10;
        if (lastDigit == 1) return "день";
        if (lastDigit >= 2 && lastDigit <= 4) return "дня";
        return "дней";
    }

    public static String getTimesDeclension(int number) {
        if (number % 100 >= 11 && number % 100 <= 14) return "раз";
        int lastDigit = number % 10;
        if (lastDigit >= 2 && lastDigit <= 4) return "раза";
        return "раз";
    }

    private String getRelativeDate(String dateString) {
        if (dateString == null || dateString.isEmpty()) return "Нет записей";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        try {
            Calendar cal = Calendar.getInstance();
            String today = sdf.format(cal.getTime());
            cal.add(Calendar.DAY_OF_YEAR, -1);
            String yesterday = sdf.format(cal.getTime());

            if (dateString.equals(today)) return "Сегодня";
            if (dateString.equals(yesterday)) return "Вчера";
            return new SimpleDateFormat("d MMM", new Locale("ru")).format(sdf.parse(dateString));
        } catch (Exception e) { return dateString; }
    }

    private String saveImageToInternalStorage(Uri uri) {
        try {
            String fileName = "profile_" + System.currentTimeMillis() + ".jpg";
            File file = new File(requireContext().getFilesDir(), fileName);
            File dir = requireContext().getFilesDir();
            if (dir.exists() && dir.listFiles() != null) {
                for (File f : dir.listFiles()) if (f.getName().startsWith("profile_")) f.delete();
            }
            java.io.InputStream is = requireContext().getContentResolver().openInputStream(uri);
            java.io.FileOutputStream os = new java.io.FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int read;
            while ((read = is.read(buffer)) != -1) os.write(buffer, 0, read);
            os.close(); is.close();
            return file.getAbsolutePath();
        } catch (Exception e) { return null; }
    }

    private void updatePhotoInDatabase(String path) {
        UserProfileModel currentProfile = userProfileDAO.getProfile();
        if (currentProfile != null) {
            currentProfile.setUserImagePath(path);
            userProfileDAO.insertOrUpdateProfile(currentProfile);
        }
    }

    @Override
    public void onGroupItemClick(int flatPosition, String title) {
        if (title != null && (title.equals(getString(R.string.Permissions)) || title.equals("Данные"))) {
            startActivity(new Intent(getContext(), HealthSettingsActivity.class));
        }
        if (title != null && title.equals(getString(R.string.data_management))) {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.frameLayout, new DataManagementFragment())
                    .addToBackStack(null)
                    .commit();
            return;
        }

        // Ваша старая логика для HealthSettings
        if (title != null && (title.equals(getString(R.string.Permissions)) || title.equals("Данные"))) {
            startActivity(new Intent(getContext(), HealthSettingsActivity.class));
        }
    }
}