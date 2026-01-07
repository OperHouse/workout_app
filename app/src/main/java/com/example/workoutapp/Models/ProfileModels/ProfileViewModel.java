package com.example.workoutapp.Models.ProfileModels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.workoutapp.Fragments.ProfileFragments.ProfileFragment;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProfileViewModel extends ViewModel {
    // Используем ваш статический холдер (сделайте его public или вынесите в отдельный файл)
    private final MutableLiveData<ProfileFragment.ProfileDataHolder> profileData = new MutableLiveData<>();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public LiveData<ProfileFragment.ProfileDataHolder> getProfileData() {
        return profileData;
    }

    public void loadData(ProfileFragment.ProfileDataProvider provider) {
        executor.execute(() -> {
            // Вызываем тяжелый метод получения данных
            ProfileFragment.ProfileDataHolder data = provider.fetch();
            // Отправляем результат в основной поток
            profileData.postValue(data);
        });
    }
}