package com.example.workoutapp.RegistrationActivity;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.workoutapp.R;
import com.google.firebase.FirebaseApp;

public class RegistrationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registration);

        FirebaseApp.initializeApp(this);

        // При первом запуске загружаем фрагмент регистрации
        if (savedInstanceState == null) {
            loadFragment(new RegistrationFragment(), false);
        }
    }

    /**
     * Метод для смены фрагментов
     * @param fragment — какой фрагмент открыть
     * @param addToBackStack — нужно ли сохранять предыдущий экран в истории (кнопка "Назад")
     */
    public void loadFragment(Fragment fragment, boolean addToBackStack) { // Изменил тип на Fragment для универсальности
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        transaction.setCustomAnimations(
                android.R.anim.fade_in,
                android.R.anim.fade_out,
                android.R.anim.fade_in,
                android.R.anim.fade_out
        );

        transaction.replace(R.id.fragment_container, fragment);

        if (addToBackStack) {
            // Если это фрагмент регистрации, даем ему имя, чтобы потом к нему вернуться
            if (fragment instanceof RegistrationFragment) {
                transaction.addToBackStack("RegistrationStep");
            } else {
                transaction.addToBackStack(null);
            }
        }

        transaction.commit();
    }
}