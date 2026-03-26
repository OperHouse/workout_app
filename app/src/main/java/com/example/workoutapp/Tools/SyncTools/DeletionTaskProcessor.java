package com.example.workoutapp.Tools.SyncTools;

import android.util.Log;

import com.example.workoutapp.Data.ChangeElmDao;
import com.example.workoutapp.Data.DeletionQueueDao;
import com.example.workoutapp.MainActivity;
import com.example.workoutapp.Models.DeletionTask;
import com.example.workoutapp.Models.WorkoutModels.ExerciseModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class DeletionTaskProcessor {
    private static final String TAG = "DeletionTaskProcessor";
    private final FirestoreSyncManager manager;
    private final DeletionQueueDao queueDao;
    private final ChangeElmDao changeDao;
    private final FirebaseFirestore db;

    public DeletionTaskProcessor(FirestoreSyncManager manager) {
        this.manager = manager;
        this.queueDao = new DeletionQueueDao(MainActivity.getAppDataBase());
        this.changeDao = new ChangeElmDao(MainActivity.getAppDataBase());
        this.db = FirebaseFirestore.getInstance();
    }

    public void process(DeletionTask task) {
        final String uid = task.uid;
        final String type = task.type != null ? task.type : "";
        final String data = task.data;

        Log.d(TAG, "Обработка удаления [" + type + "]: " + uid);

        switch (type.toLowerCase()) {
            case "workout_ex_delete":
                handleWorkoutExerciseDelete(uid, data);
                break;

            case "base_ex_delete":
                handleBaseExerciseDelete(uid);
                break;

            case "workout_preset_delete":
                handleWorkoutPresetDelete(uid);
                break;

            case "meal":
                handleMealDelete(uid);
                break;

            default:
                Log.w(TAG, "Неизвестный тип удаления: " + type);
                break;
        }
    }

    // --- Методы-обработчики ---

    private void handleWorkoutExerciseDelete(String uid, String date) {
        if (date == null || date.isEmpty()) {
            Log.e(TAG, "Пропуск удаления: нет даты для упражнения " + uid);
            queueDao.removeFromQueue(uid);
            return;
        }

        ExerciseModel dummyEx = new ExerciseModel();
        dummyEx.setExercise_uid(uid);
        dummyEx.setEx_Data(date);

        manager.deleteExerciseFromCloud(dummyEx);
    }

    private void handleBaseExerciseDelete(String uid) {
        manager.baseExerciseSync.deleteBaseExercise(uid, null);
    }

    private void handleWorkoutPresetDelete(String uid) {
        manager.presetWorkoutSync.deletePresetFromCloud(uid, new PresetWorkoutSync.SyncCallback() {
            @Override
            public void onSuccess() {
                finishDeletion(uid, "Пресет тренировки");
            }

            @Override
            public void onFailure(String error) {
                Log.e(TAG, "Ошибка удаления пресета: " + error);
            }
        });
    }

    private void handleMealDelete(String uid) {
        String userId = FirebaseAuth.getInstance().getUid();
        if(userId != null){
            db.collection("users").document(userId)
                .collection("meal_diary")
                .document(uid)
                .delete()
                .addOnSuccessListener(aVoid -> finishDeletion(uid, "Прием пищи"))
                .addOnFailureListener(e -> Log.e(TAG, "Ошибка удаления meal из Firestore: " + uid, e));}

    }

    // Вспомогательный метод для очистки обеих очередей (удаления и изменений)
    private void finishDeletion(String uid, String label) {
        queueDao.removeFromQueue(uid);
        changeDao.removeFromQueue(uid);
        Log.d(TAG, label + " успешно удалено из облака и очередей: " + uid);
    }


}