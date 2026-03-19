package com.example.workoutapp.Data.WorkoutDao;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import com.example.workoutapp.Data.Tables.AppDataBase;
import com.example.workoutapp.Models.WorkoutModels.BaseExModel;
import com.example.workoutapp.Models.WorkoutModels.ExerciseModel;

import net.sqlcipher.database.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class WORKOUT_PRESET_NAME_TABLE_DAO {

    private final SQLiteDatabase db;
    private final CONNECTING_WORKOUT_PRESET_TABLE_DAO connectingPresetDao;
    private final BASE_EXERCISE_TABLE_DAO baseExerciseDao;

    public WORKOUT_PRESET_NAME_TABLE_DAO(SQLiteDatabase db) {
        this.db = db;
        this.connectingPresetDao = new CONNECTING_WORKOUT_PRESET_TABLE_DAO(db);
        this.baseExerciseDao = new BASE_EXERCISE_TABLE_DAO(db);
    }

    // Добавление нового пресета (старый метод для совместимости)
    public long addPreset(String presetName) {
        ContentValues values = new ContentValues();
        values.put(AppDataBase.WORKOUT_PRESET_NAME, presetName);
        return db.insert(AppDataBase.WORKOUT_PRESET_NAME_TABLE, null, values);
    }

    // Добавление пресета с UID
    public long addPreset(String presetName, String presetUid) {
        ContentValues values = new ContentValues();
        values.put(AppDataBase.WORKOUT_PRESET_NAME, presetName);
        values.put(AppDataBase.WORKOUT_PRESET_UID, presetUid);
        return db.insert(AppDataBase.WORKOUT_PRESET_NAME_TABLE, null, values);
    }

    public void deletePreset(long presetId) {
        db.delete(AppDataBase.WORKOUT_PRESET_NAME_TABLE, AppDataBase.WORKOUT_PRESET_NAME_ID + " = ?", new String[]{String.valueOf(presetId)});
        connectingPresetDao.deleteExercisesByPresetId(presetId);
    }

    // ИСПРАВЛЕННЫЙ МЕТОД: теперь тянет UID и не падает
    public List<ExerciseModel> getAllPresets() {
        List<ExerciseModel> presets = new ArrayList<>();

        // 1. Добавляем UID в список запрашиваемых колонок
        String[] columns = {
                AppDataBase.WORKOUT_PRESET_NAME_ID,
                AppDataBase.WORKOUT_PRESET_NAME,
                AppDataBase.WORKOUT_PRESET_UID // Добавлено!
        };

        Cursor cursor = null;
        try {
            cursor = db.query(
                    AppDataBase.WORKOUT_PRESET_NAME_TABLE,
                    columns, null, null, null, null, null
            );

            if (cursor != null && cursor.moveToFirst()) {
                Log.d("PresetSync", "DAO: Найдено строк в базе: " + cursor.getCount());

                int idIndex = cursor.getColumnIndexOrThrow(AppDataBase.WORKOUT_PRESET_NAME_ID);
                int nameIndex = cursor.getColumnIndexOrThrow(AppDataBase.WORKOUT_PRESET_NAME);
                int uidIndex = cursor.getColumnIndexOrThrow(AppDataBase.WORKOUT_PRESET_UID);

                do {
                    long presetId = cursor.getLong(idIndex);
                    String presetName = cursor.getString(nameIndex);
                    String presetUid = cursor.getString(uidIndex); // Получаем UID

                    List<Long> baseExIds = connectingPresetDao.getBaseExIdsByPresetId(presetId);
                    List<Object> exercises = new ArrayList<>();
                    for (Long baseExId : baseExIds) {
                        BaseExModel exercise = baseExerciseDao.getExerciseById(baseExId);
                        if (exercise != null) exercises.add(exercise);
                    }

                    // Создаем модель и ОБЯЗАТЕЛЬНО сеттим UID
                    ExerciseModel model = new ExerciseModel(presetId, presetName, exercises);
                    model.setExercise_uid(presetUid);
                    presets.add(model);

                    Log.d("PresetSync", "DAO: Считан пресет: " + presetName + " | UID: " + presetUid);
                } while (cursor.moveToNext());
            } else {
                Log.d("PresetSync", "DAO: Таблица пресетов пуста");
            }
        } catch (Exception e) {
            Log.e("PresetSync", "DAO Error: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
        }

        return presets;
    }

    public void updatePresetName(long presetId, String newName) {
        ContentValues values = new ContentValues();
        values.put(AppDataBase.WORKOUT_PRESET_NAME, newName);
        db.update(AppDataBase.WORKOUT_PRESET_NAME_TABLE, values, AppDataBase.WORKOUT_PRESET_NAME_ID + " = ?", new String[]{String.valueOf(presetId)});
    }

    // Проверка существования по UID
    public boolean isPresetUidExists(String uid) {
        if (uid == null) return false;
        Cursor cursor = db.rawQuery("SELECT 1 FROM " + AppDataBase.WORKOUT_PRESET_NAME_TABLE +
                " WHERE " + AppDataBase.WORKOUT_PRESET_UID + " = ?", new String[]{uid});
        boolean exists = (cursor != null && cursor.getCount() > 0);
        if (cursor != null) cursor.close();
        return exists;
    }

    // Получение локального ID по UID
    public long getPresetIdByUid(String uid) {
        Cursor cursor = db.query(AppDataBase.WORKOUT_PRESET_NAME_TABLE,
                new String[]{AppDataBase.WORKOUT_PRESET_NAME_ID},
                AppDataBase.WORKOUT_PRESET_UID + " = ?", new String[]{uid}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            long id = cursor.getLong(0);
            cursor.close();
            return id;
        }
        return -1;
    }

    public String getPresetUidById(long presetId) {
        Cursor cursor = db.query(AppDataBase.WORKOUT_PRESET_NAME_TABLE,
                new String[]{AppDataBase.WORKOUT_PRESET_UID}, AppDataBase.WORKOUT_PRESET_NAME_ID + " = ?",
                new String[]{String.valueOf(presetId)}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            String uid = cursor.getString(0);
            cursor.close();
            return uid;
        }
        return null;
    }

    public void savePresetFromCloud(String name, String uid, List<ExerciseModel> exercises) {
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(AppDataBase.WORKOUT_PRESET_NAME, name);
            values.put(AppDataBase.WORKOUT_PRESET_UID, uid);

            long presetId;
            if (isPresetUidExists(uid)) {
                presetId = getPresetIdByUid(uid);
                db.update(AppDataBase.WORKOUT_PRESET_NAME_TABLE, values, AppDataBase.WORKOUT_PRESET_UID + " = ?", new String[]{uid});
                connectingPresetDao.deleteExercisesByPresetId(presetId);
            } else {
                presetId = db.insert(AppDataBase.WORKOUT_PRESET_NAME_TABLE, null, values);
            }

            for (ExerciseModel ex : exercises) {
                long baseExId = baseExerciseDao.getExerciseIdByName(ex.getExerciseName());
                if (baseExId != -1) {
                    connectingPresetDao.addPresetExercise(presetId, baseExId);
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    // =========================
// Полная очистка всех пресетов (локально)
// =========================
    public void deleteAllPresets() {
        db.beginTransaction();
        try {
            // Удаляем имена пресетов
            db.delete(AppDataBase.WORKOUT_PRESET_NAME_TABLE, null, null);
            // Удаляем связи упражнений в пресетах
            db.delete(AppDataBase.CONNECTING_WORKOUT_PRESET_TABLE, null, null);

            db.setTransactionSuccessful();
            Log.d("PresetSync", "Все локальные пресеты удалены");
        } catch (Exception e) {
            Log.e("PresetSync", "Ошибка при удалении пресетов: " + e.getMessage());
        } finally {
            db.endTransaction();
        }
    }

    // =========================
// Получение полной модели пресета по UID (для синхронизации)
// =========================
    public ExerciseModel getPresetByUid(String uid) {
        if (uid == null || uid.isEmpty()) return null;

        ExerciseModel model = null;
        Cursor cursor = null;

        try {
            String[] columns = {
                    AppDataBase.WORKOUT_PRESET_NAME_ID,
                    AppDataBase.WORKOUT_PRESET_NAME,
                    AppDataBase.WORKOUT_PRESET_UID
            };

            cursor = db.query(
                    AppDataBase.WORKOUT_PRESET_NAME_TABLE,
                    columns,
                    AppDataBase.WORKOUT_PRESET_UID + " = ?",
                    new String[]{uid},
                    null, null, null
            );

            if (cursor != null && cursor.moveToFirst()) {
                long presetId = cursor.getLong(cursor.getColumnIndexOrThrow(AppDataBase.WORKOUT_PRESET_NAME_ID));
                String presetName = cursor.getString(cursor.getColumnIndexOrThrow(AppDataBase.WORKOUT_PRESET_NAME));
                String presetUid = cursor.getString(cursor.getColumnIndexOrThrow(AppDataBase.WORKOUT_PRESET_UID));

                // Собираем список упражнений, привязанных к этому пресету
                List<Long> baseExIds = connectingPresetDao.getBaseExIdsByPresetId(presetId);
                List<Object> exercises = new ArrayList<>();
                for (Long baseExId : baseExIds) {
                    BaseExModel exercise = baseExerciseDao.getExerciseById(baseExId);
                    if (exercise != null) {
                        exercises.add(exercise);
                    }
                }

                // Создаем модель. В вашей структуре ExerciseModel используется как контейнер для пресета
                model = new ExerciseModel(presetId, presetName, exercises);
                model.setExercise_uid(presetUid);
            }
        } catch (Exception e) {
            Log.e("PresetSync", "Ошибка получения пресета по UID: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
        }

        return model;
    }
}