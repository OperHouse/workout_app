package com.example.workoutapp.Fragments;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.example.workoutapp.Data.ProfileDao.UserProfileDao;
import com.example.workoutapp.MainActivity;
import com.example.workoutapp.Models.ProfileModels.UserProfileModel;
import com.example.workoutapp.R;
import com.example.workoutapp.Tools.OnNavigationVisibilityListener;
import com.google.android.flexbox.FlexboxLayout;
import com.google.android.material.imageview.ShapeableImageView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class FoodEquivalentFragment extends Fragment {

    private ConstraintLayout cardContainer;
    private TextView caloriesTv, summaryStatsTv, messageTv;
    private TextView userIdTv, currentDateTv;
    private ShapeableImageView userAvatarIv;
    private FlexboxLayout iconsContainer;

    private static final String ARG_CAL = "cal";
    private static final String ARG_DIST = "dist";
    private static final String ARG_STEPS = "steps";
    private static final String ARG_ICON = "target_icon";


    private OnNavigationVisibilityListener navigationListener;

    // Метод для создания фрагмента с передачей данных
    public static FoodEquivalentFragment newInstance(float calories, float distance, int steps, int targetIcon) {
        FoodEquivalentFragment fragment = new FoodEquivalentFragment();
        Bundle args = new Bundle();
        args.putFloat(ARG_CAL, calories);
        args.putFloat(ARG_DIST, distance);
        args.putInt(ARG_STEPS, steps);
        args.putInt(ARG_ICON, targetIcon);
        fragment.setArguments(args);
        return fragment;
    }

    private static class FoodItem {
        String nameSingle, namePlural;
        int caloriesPerUnit;
        int iconRes;
        String colorHex;

        FoodItem(String nameSingle, String namePlural, int calories, int iconRes, String colorHex) {
            this.nameSingle = nameSingle;
            this.namePlural = namePlural;
            this.caloriesPerUnit = calories;
            this.iconRes = iconRes;
            this.colorHex = colorHex;
        }
    }

    public FoodEquivalentFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_food_equivalent, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Инициализация View
        cardContainer = view.findViewById(R.id.equivalent_card_container);
        caloriesTv = view.findViewById(R.id.calories_burned_tv);
        summaryStatsTv = view.findViewById(R.id.summary_stats_tv);
        messageTv = view.findViewById(R.id.equivalent_message_tv);
        iconsContainer = view.findViewById(R.id.food_icons_container);
        userIdTv = view.findViewById(R.id.user_id_tv);
        currentDateTv = view.findViewById(R.id.current_date_tv);
        userAvatarIv = view.findViewById(R.id.user_avatar);

        view.findViewById(R.id.btn_back).setOnClickListener(v -> requireActivity().onBackPressed());
        view.findViewById(R.id.statistic_BTN).setOnClickListener(v -> {
            // 1. Создаем экземпляр фрагмента
            ActivityStatisticFragment fragment = new ActivityStatisticFragment();

            // 2. Выполняем транзакцию
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.frameLayout, fragment)
                    .addToBackStack(null) // позволяет вернуться назад по кнопке "Back"
                    .commit();
        });

        // 1. Загрузка профиля (Имя + Фото)
        loadUserProfile();

        // 2. Установка даты
        setCurrentDate();

        // 3. Получение данных из Bundle
        float burnedCal = 0f;
        float distance = 0f;
        int steps = 0;
        int targetIcon = R.drawable.ic_donut;

        if (getArguments() != null) {
            burnedCal = getArguments().getFloat(ARG_CAL);
            distance = getArguments().getFloat(ARG_DIST);
            steps = getArguments().getInt(ARG_STEPS);
            targetIcon = getArguments().getInt(ARG_ICON);
        }

        // 4. Расчет и отображение
        calculateAndDisplay(burnedCal, distance, steps, targetIcon);

        view.findViewById(R.id.share_BTN).setOnClickListener(v -> shareCard());
    }

    private void loadUserProfile() {
        UserProfileDao userProfileDao = new UserProfileDao(MainActivity.getAppDataBase());
        UserProfileModel profile = userProfileDao.getProfile();

        if (profile != null) {
            // Имя пользователя
            if (profile.getUserName() != null && !profile.getUserName().trim().isEmpty()) {
                userIdTv.setText(profile.getUserName());
            } else {
                userIdTv.setText("Пользователь");
            }

            // Круглое фото профиля
            if (profile.getUserImagePath() != null && !profile.getUserImagePath().isEmpty()) {
                File imgFile = new File(profile.getUserImagePath());
                if (imgFile.exists()) {
                    Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                    userAvatarIv.setImageBitmap(myBitmap);
                }
            }
        } else {
            userIdTv.setText("Пользователь");
        }
    }

    private void setCurrentDate() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("d MMM yyyy 'г. Статистика'", new Locale("ru"));
        currentDateTv.setText(dateFormat.format(calendar.getTime()));
    }

    private void calculateAndDisplay(float burnedCal, float distance, int steps, int targetIcon) {
        // Обновляем основные цифры
        caloriesTv.setText(String.format(Locale.US, "Калории %.1f ккал", burnedCal));
        summaryStatsTv.setText(String.format(Locale.US, "%.2f км · %d", distance, steps));

        // Библиотека продуктов (без наггетсов)
        List<FoodItem> foodLibrary = new ArrayList<>();
        foodLibrary.add(new FoodItem("пончик", "пончикам", 250, R.drawable.ic_donut, "#FFD180"));
        foodLibrary.add(new FoodItem("мороженое", "порциям мороженого", 190, R.drawable.ic_ice_cream, "#80D8FF"));
        foodLibrary.add(new FoodItem("куриная ножка", "куриным ножкам", 215, R.drawable.ic_chicken_leg, "#FF8A80"));
        foodLibrary.add(new FoodItem("бургер", "бургерам", 510, R.drawable.ic_burger, "#CCFF90"));
        foodLibrary.add(new FoodItem("кусок пиццы", "кускам пиццы", 285, R.drawable.ic_pizza, "#FF8A65"));
        foodLibrary.add(new FoodItem("порция картошки фри", "порциям картошки фри", 340, R.drawable.ic_fries, "#FFFF8D"));
        foodLibrary.add(new FoodItem("наггетс", "наггетсам", 45, R.drawable.ic_nugget, "#FFCC80"));

        // Ищем выбранный продукт по иконке из карточки
        FoodItem selected = null;
        for (FoodItem item : foodLibrary) {
            if (item.iconRes == targetIcon) {
                selected = item;
                break;
            }
        }
        if (selected == null) selected = foodLibrary.get(0); // Запасной вариант

        int count = (int) (burnedCal / selected.caloriesPerUnit);

        // Если калорий слишком мало
        if (count < 1) {
            messageTv.setText("Сожгите больше калорий, чтобы увидеть результат!");
            iconsContainer.removeAllViews();
            return;
        }

        // Текст сообщения
        String resultMsg = "Вы сожгли количество калорий, эквивалентное " + count + " " +
                (count == 1 ? selected.nameSingle : selected.namePlural);
        messageTv.setText(resultMsg);

        // Динамический градиент фона карточки
        GradientDrawable gd = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{Color.parseColor(selected.colorHex), Color.WHITE}
        );
        gd.setCornerRadius(dpToPx(24));
        cardContainer.setBackground(gd);

        // Отрисовка иконок
        renderIcons(selected.iconRes, count);
    }

    private void renderIcons(int iconRes, int count) {
        iconsContainer.removeAllViews();

        // Ограничим общее число, чтобы не превращать экран в кашу (например, макс 30)
        int toShow = Math.min(count, 30);

        // Адаптивный размер:
        // Если иконок мало (до 5) — крупные (60dp)
        // Если иконок средне (до 12) — средние (45dp)
        // Если иконок много (больше 12) — маленькие (35dp)
        int iconSizeDp;
        if (toShow <= 5) {
            iconSizeDp = 60;
        } else if (toShow <= 12) {
            iconSizeDp = 45;
        } else if (toShow <= 20) {
            iconSizeDp = 35;
        } else {
            iconSizeDp = 28; // Совсем крошки для наггетсов
        }

        int sizePx = dpToPx(iconSizeDp);
        int marginPx = dpToPx(toShow > 15 ? 4 : 8); // Уменьшаем отступы, если иконок много

        for (int i = 0; i < toShow; i++) {
            ImageView iv = new ImageView(getContext());
            FlexboxLayout.LayoutParams lp = new FlexboxLayout.LayoutParams(sizePx, sizePx);
            lp.setMargins(marginPx, marginPx, marginPx, marginPx);
            iv.setLayoutParams(lp);
            iv.setImageResource(iconRes);
            iv.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            iconsContainer.addView(iv);
        }
    }

    private Bitmap createBitmapFromView(View view) {
        // Создаем пустой Bitmap с размерами View
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        // Привязываем холст (Canvas) к этому Bitmap
        android.graphics.Canvas canvas = new android.graphics.Canvas(bitmap);
        // Рисуем View на холсте
        view.draw(canvas);
        return bitmap;
    }

    private void shareCard() {
        // 1. Делаем скриншот карточки
        Bitmap bitmap = createBitmapFromView(cardContainer);

        try {
            // 2. Сохраняем во временный файл
            java.io.File cachePath = new java.io.File(requireContext().getCacheDir(), "images");
            cachePath.mkdirs(); // создаем папку, если нет
            java.io.FileOutputStream stream = new java.io.FileOutputStream(cachePath + "/workout_result.png");
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            stream.close();

            // 3. Получаем URI через FileProvider (нужен, чтобы другие приложения имели доступ к файлу)
            java.io.File imagePath = new java.io.File(requireContext().getCacheDir(), "images");
            java.io.File newFile = new java.io.File(imagePath, "workout_result.png");
            android.net.Uri contentUri = androidx.core.content.FileProvider.getUriForFile(
                    requireContext(),
                    requireContext().getPackageName() + ".fileprovider",
                    newFile
            );

            if (contentUri != null) {
                android.content.Intent shareIntent = new android.content.Intent();
                shareIntent.setAction(android.content.Intent.ACTION_SEND);
                shareIntent.addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION); // даем права на чтение
                shareIntent.setDataAndType(contentUri, requireContext().getContentResolver().getType(contentUri));
                shareIntent.putExtra(android.content.Intent.EXTRA_STREAM, contentUri);
                startActivity(android.content.Intent.createChooser(shareIntent, "Поделиться результатом"));
            }

        } catch (java.io.IOException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Ошибка при создании картинки", Toast.LENGTH_SHORT).show();
        }
    }

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnNavigationVisibilityListener) {
            navigationListener = (OnNavigationVisibilityListener) context;
        }
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