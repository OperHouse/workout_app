package com.example.workoutapp;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class HealthConnectPermissionsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Можно использовать простую верстку программно, чтобы не создавать XML
        TextView textView = new TextView(this);
        textView.setPadding(50, 50, 50, 50);
        textView.setText("Нашему приложению нужен доступ к данным Health Connect, " +
                "чтобы автоматически синхронизировать ваши шаги и калории с браслета Mi Band. " +
                "Это поможет вам точнее отслеживать прогресс тренировок.");

        setContentView(textView);
    }
}