package com.example.workoutapp.Tools.SyncTools;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.workoutapp.Data.ChangeElmDao;
import com.example.workoutapp.Data.DeletionQueueDao;
import com.example.workoutapp.MainActivity;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class SyncWorker extends Worker {

    public SyncWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        // 1. Проверяем наличие данных в БД перед запуском тяжелых процессов
        ChangeElmDao changeDao = new ChangeElmDao(MainActivity.getAppDataBase());
        DeletionQueueDao deletionDao = new DeletionQueueDao(MainActivity.getAppDataBase());

        boolean hasWork = !changeDao.getAllTasks().isEmpty() || !deletionDao.getAllPendingTasks().isEmpty();

        if (!hasWork) {
            return Result.success(); // Данных нет, выходим
        }

        CountDownLatch latch = new CountDownLatch(1);
        final boolean[] isSuccess = {false};

        // 2. Используем существующий менеджер
        MainActivity.getSyncManager().forceSyncWithTimeout(() -> {
            isSuccess[0] = true;
            latch.countDown();
        });

        try {
            // Ждем 30-40 секунд
            boolean finished = latch.await(40, TimeUnit.SECONDS);

            if (finished && isSuccess[0]) {
                // Если данные еще остались (например, много записей),
                // планируем новый запуск через 1 минуту
                if (!changeDao.getAllTasks().isEmpty() || !deletionDao.getAllPendingTasks().isEmpty()) {
                    MainActivity.schedulePeriodicBackupSync(getApplicationContext());
                }
                return Result.success();
            } else {
                // Если таймаут или ошибка - пробуем позже через систему воркера
                return Result.retry();
            }
        } catch (InterruptedException e) {
            return Result.retry();
        }
    }
}