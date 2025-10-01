package com.example.workoutapp.Tools;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.Pager;
import androidx.paging.PagingConfig;
import androidx.paging.PagingData;

import com.example.workoutapp.Data.WorkoutDao.WORKOUT_EXERCISE_TABLE_DAO;
import com.example.workoutapp.Models.WorkoutModels.ExerciseModel;

import kotlinx.coroutines.flow.Flow;

public class WorkoutViewModel extends ViewModel {

    private final WORKOUT_EXERCISE_TABLE_DAO workoutExerciseTableDao;
    public final Flow<PagingData<ExerciseModel>> pagingDataFlow;

    public WorkoutViewModel(WORKOUT_EXERCISE_TABLE_DAO dao) {
        this.workoutExerciseTableDao = dao;

        PagingConfig pagingConfig = new PagingConfig(
                /* pageSize */ 20,
                /* prefetchDistance */ 3,
                /* enablePlaceholders */ false,
                /* initialLoadSize */ 40
        );

        this.pagingDataFlow = new Pager<>(pagingConfig, null, () -> workoutExerciseTableDao.getExercisesPagingSource("unfinished")).getFlow();
    }

    // Фабрика для создания ViewModel
    public static class WorkoutViewModelFactory implements ViewModelProvider.Factory {
        private final WORKOUT_EXERCISE_TABLE_DAO dao;

        public WorkoutViewModelFactory(WORKOUT_EXERCISE_TABLE_DAO dao) {
            this.dao = dao;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(WorkoutViewModel.class)) {
                return (T) new WorkoutViewModel(dao);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}