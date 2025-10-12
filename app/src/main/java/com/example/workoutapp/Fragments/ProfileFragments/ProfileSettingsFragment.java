package com.example.workoutapp.Fragments.ProfileFragments;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;

import com.example.workoutapp.R;
import com.example.workoutapp.Tools.OnNavigationVisibilityListener;
import com.example.workoutapp.databinding.FragmentProfileSettingsBinding;


public class ProfileSettingsFragment extends Fragment {

    private OnNavigationVisibilityListener navigationListener;





    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {


        View profileSettingsFragment = inflater.inflate(R.layout.fragment_profile_settings, container, false);

        ImageButton imageButtonBack = profileSettingsFragment.findViewById(R.id.imageButtonBack);
        imageButtonBack.setOnClickListener(view1 -> {
            requireActivity().getOnBackPressedDispatcher().onBackPressed();
        });
        return profileSettingsFragment;

    }



    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        // Проверяем, что Activity реализует интерфейс
        if (context instanceof OnNavigationVisibilityListener) {
            navigationListener = (OnNavigationVisibilityListener) context;
        } else {
            throw new RuntimeException(context + " must implement OnNavigationVisibilityListener");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() != null && getActivity().getWindow() != null) {
            getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }

        // 1. СКРЫВАЕМ BottomNav ПРИ ОТКРЫТИИ
        if (navigationListener != null) {
            navigationListener.setBottomNavVisibility(false);
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        if (getActivity() != null && getActivity().getWindow() != null) {
            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

            // Clear the systemUiVisibility flag
            getActivity().getWindow().getDecorView().setSystemUiVisibility(0);
        }


        // 2. ПОКАЗЫВАЕМ BottomNav ПРИ ЗАКРЫТИИ (уходе)
        if (navigationListener != null) {
            navigationListener.setBottomNavVisibility(true);
        }
    }
    // Очищаем ссылку на слушателя, чтобы избежать утечек памяти
    @Override
    public void onDetach() {
        super.onDetach();
        navigationListener = null;
    }










}