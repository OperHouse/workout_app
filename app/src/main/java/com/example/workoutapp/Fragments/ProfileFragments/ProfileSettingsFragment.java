package com.example.workoutapp.Fragments.ProfileFragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.example.workoutapp.Data.ProfileDao.UserProfileDao;
import com.example.workoutapp.Data.ProfileDao.WeightHistoryDao;
import com.example.workoutapp.MainActivity;
import com.example.workoutapp.Models.ProfileModels.UserProfileModel;
import com.example.workoutapp.Models.ProfileModels.WeightHistoryModel;
import com.example.workoutapp.R;
import com.example.workoutapp.Tools.OnNavigationVisibilityListener;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ProfileSettingsFragment extends Fragment {

    private OnNavigationVisibilityListener navigationListener;

    private EditText nameEdit, heightEdit, ageEdit, weightEdit;

    @SuppressLint("ClickableViewAccessibility")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View profileSettingsFragment = inflater.inflate(R.layout.fragment_profile_settings, container, false);

        ImageButton imageButtonBack = profileSettingsFragment.findViewById(R.id.imageButtonBack);
        MaterialButton saveBtn = profileSettingsFragment.findViewById(R.id.buttonSave);
        imageButtonBack.setOnClickListener(view1 -> requireActivity().getOnBackPressedDispatcher().onBackPressed());

        // Инициализация полей
        nameEdit = profileSettingsFragment.findViewById(R.id.editTextName);
        heightEdit = profileSettingsFragment.findViewById(R.id.editTextHeight);
        ageEdit = profileSettingsFragment.findViewById(R.id.editTextAge);
        weightEdit = profileSettingsFragment.findViewById(R.id.editTextWeight);


        // Скрытие клавиатуры по клику на пустое место
        ConstraintLayout rootLayout = profileSettingsFragment.findViewById(R.id.rootConstraintLayout);
        rootLayout.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                hideKeyboardAndClearFocus();
            }
            return false;
        });

        // Обработка Enter/Next
        int[] editTextIds = {R.id.editTextName, R.id.editTextHeight, R.id.editTextWeight, R.id.editTextAge};
        for (int id : editTextIds) {
            EditText editText = profileSettingsFragment.findViewById(id);
            setupEditorActionListener(editText);
        }

        // Кнопка сохранения
        saveBtn.setOnClickListener(v -> saveProfile());

        return profileSettingsFragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadProfileData(); // 🔹 Подгружаем данные из БД
    }

    private void setupEditorActionListener(EditText editText) {
        editText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT) {
                hideKeyboardAndClearFocus();
                return true;
            }
            return false;
        });
    }

    private void hideKeyboardAndClearFocus() {
        View focusedView = requireActivity().getCurrentFocus();
        if (focusedView != null) {
            focusedView.clearFocus();
            InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(focusedView.getWindowToken(), 0);
            }
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnNavigationVisibilityListener) {
            navigationListener = (OnNavigationVisibilityListener) context;
        } else {
            throw new RuntimeException(context + " must implement OnNavigationVisibilityListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        navigationListener = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (navigationListener != null) {
            navigationListener.setBottomNavVisibility(false);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (navigationListener != null) {
            navigationListener.setBottomNavVisibility(true);
        }
    }

    // 🔹 Загрузка данных из базы в поля
    private void loadProfileData() {
        UserProfileDao userProfileDao = new UserProfileDao(MainActivity.getAppDataBase());
        UserProfileModel profile = userProfileDao.getProfile(); // ← метод без ID

        if (profile != null) {
            if (profile.getUserName() != null) nameEdit.setText(profile.getUserName());
            if (profile.getUserHeight() > 0) heightEdit.setText(String.valueOf(profile.getUserHeight()));
            if (profile.getUserAge() > 0) ageEdit.setText(String.valueOf(profile.getUserAge()));
        }

        // Загружаем последний вес (если есть)
        WeightHistoryDao weightDao = new WeightHistoryDao(MainActivity.getAppDataBase());
        WeightHistoryModel lastWeight = weightDao.getLatestWeight();
        if (lastWeight != null && lastWeight.getWeightValue() > 0) {
            weightEdit.setText(String.valueOf(lastWeight.getWeightValue()));
        }
    }

    private void saveProfile() {
        String name = nameEdit.getText().toString().trim();
        float height = 0f;
        int age = 0;
        float weight = 0f;
        long currentTimeMillis = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String formattedDate = sdf.format(new Date(currentTimeMillis));

        try { height = Float.parseFloat(heightEdit.getText().toString().trim()); } catch (NumberFormatException ignored) {}
        try { weight = Float.parseFloat(weightEdit.getText().toString().trim()); } catch (NumberFormatException ignored) {}
        try { age = Integer.parseInt(ageEdit.getText().toString().trim()); } catch (NumberFormatException ignored) {}

        // 1 Сохраняем профиль
        UserProfileDao userProfileDao = new UserProfileDao(MainActivity.getAppDataBase());
        UserProfileModel profile = new UserProfileModel(1, name, height, age);
        userProfileDao.insertOrUpdateProfile(profile);

        // 2 Сохраняем вес
        WeightHistoryDao weightDao = new WeightHistoryDao(MainActivity.getAppDataBase());
        weightDao.addWeightEntry(new WeightHistoryModel(0, formattedDate, weight));


        //Возврат на предыдущий экран
        requireActivity()
                .getSupportFragmentManager()
                .popBackStack();
    }

}
