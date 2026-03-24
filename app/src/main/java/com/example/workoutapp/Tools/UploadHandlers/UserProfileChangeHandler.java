package com.example.workoutapp.Tools.UploadHandlers;

import com.example.workoutapp.Data.ChangeElmDao;
import com.example.workoutapp.Data.ProfileDao.UserProfileDao;
import com.example.workoutapp.Models.ProfileModels.UserProfileModel;
import com.example.workoutapp.Tools.SyncTools.ProfileSync;

public class UserProfileChangeHandler extends BaseSyncHandler<UserProfileModel> {

    private final UserProfileDao dao;
    private final ProfileSync sync;

    public UserProfileChangeHandler(
            UserProfileDao dao,
            ProfileSync sync,
            ChangeElmDao changeDao
    ) {
        super(changeDao);
        this.dao = dao;
        this.sync = sync;
    }

    @Override
    protected UserProfileModel getModel(String uid) {
        // Так как профиль обычно один, UID здесь может быть формальным (например, "profile_uid")
        return dao.getProfile();
    }

    // Внедряем Runnable onComplete для поддержки 15-секундной цепочки
    @Override
    protected void executeSync(UserProfileModel model, String uid, Runnable onComplete) {
        sync.uploadProfile(model, new ProfileSync.SyncCallback() {
            @Override
            public void onSuccess() {
                // Удаляем задачу из очереди и запускаем следующую через onComplete
                UserProfileChangeHandler.this.onSuccess(uid, onComplete);
            }

            @Override
            public void onFailure(String error) {
                // Пропускаем очередь дальше даже при ошибке выгрузки профиля
                UserProfileChangeHandler.this.onFailure(uid, error, onComplete);
            }
        });
    }
}