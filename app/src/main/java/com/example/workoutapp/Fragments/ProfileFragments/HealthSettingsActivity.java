package com.example.workoutapp.Fragments.ProfileFragments;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.workoutapp.HealthConnectHelper;
import com.example.workoutapp.HealthPermissions;
import com.example.workoutapp.R;

import kotlin.Unit;

public class HealthSettingsActivity extends AppCompatActivity {

    private TextView tvStatus;
    private Button btnManage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_health_settings);

        tvStatus = findViewById(R.id.tv_connection_status);
        btnManage = findViewById(R.id.btn_manage_permissions);

        if (findViewById(R.id.btn_back) != null) {
            findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        }

        btnManage.setOnClickListener(v -> openSystemHealthSettings());

        updateStatus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateStatus();
    }

    private void updateStatus() {
        HealthConnectHelper.checkGrantedPermissions(this, HealthPermissions.INSTANCE.getREQUIRED_PERMISSIONS(), granted -> {
            // Проверяем, получены ли ВСЕ нужные разрешения
            boolean allGranted = granted.containsAll(HealthPermissions.INSTANCE.getREQUIRED_PERMISSIONS());

            runOnUiThread(() -> {
                if (allGranted) {
                    tvStatus.setText("Статус: Синхронизация активна");
                    tvStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                    btnManage.setText("Управление доступом");
                } else {
                    tvStatus.setText("Статус: Не подключено");
                    tvStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                    btnManage.setText("Дать доступ");
                }
            });
            return Unit.INSTANCE;
        });
    }

    private void openSystemHealthSettings() {
        try {
            // Это откроет официальное окно Health Connect конкретно для вашего приложения
            Intent intent = new Intent("androidx.health.ACTION_HEALTH_CONNECT_SETTINGS");
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Не удалось открыть настройки", Toast.LENGTH_SHORT).show();
        }
    }
}