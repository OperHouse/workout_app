package com.example.workoutapp.Tools.SyncTools;

import android.util.Log;

import com.example.workoutapp.Models.ProfileModels.ActivityGoalModel;
import com.example.workoutapp.Models.ProfileModels.DailyActivityTrackingModel;
import com.example.workoutapp.Models.ProfileModels.DailyFoodTrackingModel;
import com.example.workoutapp.Models.ProfileModels.FoodGainGoalModel;
import com.example.workoutapp.Models.ProfileModels.GeneralGoalModel;
import com.example.workoutapp.Models.ProfileModels.UserProfileModel;
import com.example.workoutapp.Models.ProfileModels.WeightHistoryModel;

import java.util.List;

public class InitialDataDownloader {
    private static final String TAG = "InitialDataDownloader";
    private final FirestoreSyncManager manager;

    public InitialDataDownloader(FirestoreSyncManager manager) {
        this.manager = manager;
    }

    /**
     * Основной метод, который запускает все загрузки параллельно.
     * Вызывается один раз при успешном входе пользователя.
     */
    public void downloadEverything() {
        Log.d(TAG, "Начата полная загрузка данных из облака...");

        loadProfile();
        loadWeight();
        loadActivityGoals();
        loadGeneralGoals();
        loadFoodGoals();
        loadDailyFood();
        loadDailyActivity();

        // Можно добавить загрузку упражнений и пресетов, если они нужны сразу
        // loadWorkouts();
    }

    private void loadProfile() {
        manager.profileSync.downloadProfile(new ProfileSync.DownloadCallback() {
            @Override
            public void onDownloaded(UserProfileModel profile) {
                Log.d(TAG, "Профиль загружен");
            }
            @Override
            public void onError(String error) {
                Log.e(TAG, "Ошибка загрузки профиля: " + error);
            }
        });
    }

    private void loadWeight() {
        manager.weightSync.downloadWeightHistory(new WeightSync.DownloadCallback() {
            @Override
            public void onDownloaded(List<WeightHistoryModel> weightList) {
                Log.d(TAG, "История веса загружена: " + weightList.size());
            }
            @Override
            public void onError(String error) {
                Log.e(TAG, "Ошибка загрузки веса: " + error);
            }
        });
    }

    private void loadActivityGoals() {
        manager.activityGoalSync.downloadGoals(new ActivityGoalSync.DownloadCallback() {
            @Override
            public void onDownloaded(List<ActivityGoalModel> goals) {
                Log.d(TAG, "Цели активности загружены");
            }
            @Override
            public void onError(String error) {
                Log.e(TAG, "Ошибка целей активности: " + error);
            }
        });
    }

    private void loadGeneralGoals() {
        manager.generalGoalSync.downloadGoals(new GeneralGoalSync.DownloadCallback() {
            @Override
            public void onDownloaded(List<GeneralGoalModel> goals) {
                Log.d(TAG, "Общие цели загружены");
            }
            @Override
            public void onError(String error) {
                Log.e(TAG, "Ошибка общих целей: " + error);
            }
        });
    }

    private void loadFoodGoals() {
        manager.foodGoalSync.downloadGoals(new FoodGoalSync.DownloadCallback() {
            @Override
            public void onDownloaded(List<FoodGainGoalModel> goals) {
                Log.d(TAG, "Цели питания загружены");
            }
            @Override
            public void onError(String error) {
                Log.e(TAG, "Ошибка целей питания: " + error);
            }
        });
    }

    private void loadDailyFood() {
        manager.dailyFoodSync.downloadEntries(new DailyFoodSync.DownloadCallback() {
            @Override
            public void onDownloaded(List<DailyFoodTrackingModel> entries) {
                Log.d(TAG, "Данные КБЖУ загружены");
            }
            @Override
            public void onError(String error) {
                Log.e(TAG, "Ошибка КБЖУ: " + error);
            }
        });
    }

    private void loadDailyActivity() {
        manager.dailyActivitySync.downloadFromCloud(new DailyActivitySync.DownloadCallback() {
            @Override
            public void onDownloaded(List<DailyActivityTrackingModel> entries) {
                Log.d(TAG, "Данные активности загружены");
            }
            @Override
            public void onError(String error) {
                Log.e(TAG, "Ошибка активности: " + error);
            }
        });
    }
}