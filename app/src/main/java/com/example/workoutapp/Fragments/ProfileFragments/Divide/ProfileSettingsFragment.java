package com.example.workoutapp.Fragments.ProfileFragments.Divide;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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
import com.example.workoutapp.Tools.FirestoreSyncManager;
import com.example.workoutapp.Tools.OnNavigationVisibilityListener;
import com.example.workoutapp.Tools.UidGenerator;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ProfileSettingsFragment extends Fragment {

    private OnNavigationVisibilityListener navigationListener;

    private EditText nameEdit, heightEdit, ageEdit, weightEdit;
    private ShapeableImageView profileImageView;

    private ActivityResultLauncher<String> mGetContent;

    private String currentImagePath = null; // Путь к уже сохраненному фото (из БД)
    private Uri tempSelectedUri = null;     // Временная ссылка на новое выбранное фото

    @SuppressLint("ClickableViewAccessibility")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View profileSettingsFragment = inflater.inflate(R.layout.fragment_profile_settings, container, false);

        // Инициализация UI
        ImageButton imageButtonBack = profileSettingsFragment.findViewById(R.id.imageButtonBack);
        MaterialButton saveBtn = profileSettingsFragment.findViewById(R.id.buttonSave);
        profileImageView = profileSettingsFragment.findViewById(R.id.profile_settings_fragment_photo_IB);

        nameEdit = profileSettingsFragment.findViewById(R.id.editTextName);
        heightEdit = profileSettingsFragment.findViewById(R.id.editTextHeight);
        ageEdit = profileSettingsFragment.findViewById(R.id.editTextAge);
        weightEdit = profileSettingsFragment.findViewById(R.id.editTextWeight);

        imageButtonBack.setOnClickListener(view1 -> requireActivity().getOnBackPressedDispatcher().onBackPressed());

        // Скрытие клавиатуры
        ConstraintLayout rootLayout = profileSettingsFragment.findViewById(R.id.rootConstraintLayout);
        rootLayout.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                hideKeyboardAndClearFocus();
            }
            return false;
        });

        setupEditorActionListener(nameEdit);
        setupEditorActionListener(heightEdit);
        setupEditorActionListener(weightEdit);
        setupEditorActionListener(ageEdit);

        saveBtn.setOnClickListener(v -> saveProfile());

        return profileSettingsFragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Выбор фото (только предпросмотр)
        mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        tempSelectedUri = uri; // Запоминаем выбор
                        profileImageView.setImageDrawable(null);
                        profileImageView.setImageURI(uri); // Показываем в интерфейсе
                    }
                });

        profileImageView.setOnClickListener(v -> mGetContent.launch("image/*"));

        loadProfileData();
    }

    // --- ФИЗИЧЕСКОЕ СОХРАНЕНИЕ ФАЙЛА (вызывается только при Save) ---
    private String finalizeImageSaving(Uri uri) {
        try {
            // Генерируем новое имя файла
            String fileName = "profile_" + System.currentTimeMillis() + ".jpg";
            File file = new File(requireContext().getFilesDir(), fileName);

            // Очищаем старые файлы перед сохранением нового
            File dir = requireContext().getFilesDir();
            if (dir.exists() && dir.listFiles() != null) {
                for (File tempFile : dir.listFiles()) {
                    if (tempFile.getName().startsWith("profile_")) {
                        tempFile.delete();
                    }
                }
            }

            InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
            FileOutputStream outputStream = new FileOutputStream(file);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }

            outputStream.flush();
            outputStream.close();
            inputStream.close();

            return file.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return currentImagePath; // Если не удалось сохранить, оставляем старый путь
        }
    }

    private void loadProfileData() {
        UserProfileDao userProfileDao = new UserProfileDao(MainActivity.getAppDataBase());
        UserProfileModel profile = userProfileDao.getProfile();

        if (profile != null) {
            if (profile.getUserName() != null) nameEdit.setText(profile.getUserName());
            if (profile.getUserHeight() > 0) heightEdit.setText(String.valueOf(profile.getUserHeight()));
            if (profile.getUserAge() > 0) ageEdit.setText(String.valueOf(profile.getUserAge()));

            currentImagePath = profile.getUserImagePath();
            if (currentImagePath != null) {
                File imgFile = new File(currentImagePath);
                if (imgFile.exists()) {
                    profileImageView.setImageURI(Uri.fromFile(imgFile));
                }
            }
        }

        WeightHistoryDao weightDao = new WeightHistoryDao(MainActivity.getAppDataBase());
        WeightHistoryModel lastWeight = weightDao.getLatestWeight();
        if (lastWeight != null && lastWeight.getWeight_history_value() > 0) {
            weightEdit.setText(String.valueOf(lastWeight.getWeight_history_value()));
        }
    }

    private void saveProfile() {
        String name = nameEdit.getText().toString().trim();
        float height = 0f;
        int age = 0;
        float weight = 0f;

        try { height = Float.parseFloat(heightEdit.getText().toString().trim()); } catch (Exception ignored) {}
        try { weight = Float.parseFloat(weightEdit.getText().toString().trim()); } catch (Exception ignored) {}
        try { age = Integer.parseInt(ageEdit.getText().toString().trim()); } catch (Exception ignored) {}

        // 1. Обработка фото
        if (tempSelectedUri != null) {
            currentImagePath = finalizeImageSaving(tempSelectedUri);
        }

        // 2. Инициализация инструментов
        UserProfileDao userProfileDao = new UserProfileDao(MainActivity.getAppDataBase());
        WeightHistoryDao weightDao = new WeightHistoryDao(MainActivity.getAppDataBase());
        FirestoreSyncManager syncManager = new FirestoreSyncManager();

        // 3. СОХРАНЕНИЕ ПРОФИЛЯ (Локально + Облако)
        UserProfileModel profile = new UserProfileModel(1, name, height, age);
        profile.setUserImagePath(currentImagePath);

        userProfileDao.insertOrUpdateProfile(profile); // Локально
        syncManager.syncProfileUpdate(profile);


        String uuid = UidGenerator.generateWeightUid();

        // 4. СОХРАНЕНИЕ ВЕСА (Локально + Облако)
        String formattedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        WeightHistoryModel weightEntry = new WeightHistoryModel(0, uuid, formattedDate, weight);

        // Вызываем ОДИН раз. Внутри DAO должна быть проверка на дубликат веса,
        // чтобы не плодить ID при каждом нажатии "Сохранить"
        long generatedId = weightDao.addWeightEntry(weightEntry);

        if (generatedId != -1) {
            syncManager.syncNewWeight(weightEntry);
        }

        requireActivity().getSupportFragmentManager().popBackStack();
    }
    // --- ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ---

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