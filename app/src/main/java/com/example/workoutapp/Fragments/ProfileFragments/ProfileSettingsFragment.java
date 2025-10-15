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
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.example.workoutapp.R;
import com.example.workoutapp.Tools.OnNavigationVisibilityListener;

public class ProfileSettingsFragment extends Fragment {

    private OnNavigationVisibilityListener navigationListener;

    @SuppressLint("ClickableViewAccessibility")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View profileSettingsFragment = inflater.inflate(R.layout.fragment_profile_settings, container, false);

        ImageButton imageButtonBack = profileSettingsFragment.findViewById(R.id.imageButtonBack);
        imageButtonBack.setOnClickListener(view1 -> requireActivity().getOnBackPressedDispatcher().onBackPressed());

        // Цели для AutoCompleteTextView
        String[] goals = {"Похудение", "Поддержание", "Набор", "Рекомпозиция", "Марафон", "Силовые тренировки", "Гибкость"};
        AutoCompleteTextView goalView = profileSettingsFragment.findViewById(R.id.autoCompleteTextViewGoal);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), R.layout.spinners_style, goals);
        goalView.setAdapter(adapter);
        goalView.setOnClickListener(v -> goalView.showDropDown());

        // Добавляем слушатель для скрытия клавиатуры при нажатии на пустое место
        ConstraintLayout rootLayout = profileSettingsFragment.findViewById(R.id.rootConstraintLayout); // Нужно задать android:id="rootLayout" в корневом ConstraintLayout в xml
        rootLayout.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                hideKeyboardAndClearFocus();
            }
            return false;
        });

        // Для каждого EditText из layout — добавляем обработчик "Done" и "Next"
        int[] editTextIds = {
                R.id.editTextName,
                R.id.editTextHeight,
                R.id.editTextWeight,
                R.id.editTextAge
        };
        for (int id : editTextIds) {
            EditText editText = profileSettingsFragment.findViewById(id);
            setupEditorActionListener(editText);
        }

        return profileSettingsFragment;
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
}
