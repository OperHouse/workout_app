package com.example.workoutapp.RegistrationActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.workoutapp.MainActivity;
import com.example.workoutapp.R;
import com.example.workoutapp.Tools.MailHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class EmailVerificationFragment extends Fragment {

    private FirebaseAuth mAuth;
    private String userEmail;
    private TextView tvInfo;
    private Button btnResend, btnCheckStatus;
    private android.os.CountDownTimer countDownTimer;

    public static EmailVerificationFragment newInstance(String email) {
        EmailVerificationFragment fragment = new EmailVerificationFragment();
        Bundle args = new Bundle();
        args.putString("email_key", email);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userEmail = getArguments().getString("email_key");
        }
        mAuth = FirebaseAuth.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_email_verification, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvInfo = view.findViewById(R.id.tv_verification_info);
        btnResend = view.findViewById(R.id.btn_resend_email);
        btnCheckStatus = view.findViewById(R.id.btn_check_status);
        View btnBack = view.findViewById(R.id.btn_back_to_reg);

        tvInfo.setText("Мы отправили письмо на адрес:\n" + userEmail + "\nПожалуйста, подтвердите его для продолжения.");
        startResendTimer();

        // Кнопка: Я подтвердил (Проверить статус)
        btnCheckStatus.setOnClickListener(v -> checkVerificationStatus());

        // Кнопка: Отправить повторно
        btnResend.setOnClickListener(v -> resendEmail());

        // Кнопка: Назад (на регистрацию)
        btnBack.setOnClickListener(v -> {
            mAuth.signOut();
            getParentFragmentManager().popBackStack(); // Возврат к RegistrationFragment
        });
    }

    private void startResendTimer() {
        btnResend.setEnabled(false); // Деактивируем кнопку

        countDownTimer = new android.os.CountDownTimer(60000, 1000) { // 60 секунд
            @Override
            public void onTick(long millisUntilFinished) {
                long secondsRemaining = millisUntilFinished / 1000;
                btnResend.setText("Отправить повторно через (" + secondsRemaining + "сек)");
            }

            @Override
            public void onFinish() {
                btnResend.setEnabled(true);
                btnResend.setText("Отправить письмо еще раз");
            }
        }.start();
    }

    private void checkVerificationStatus() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            // Принудительно обновляем данные пользователя из облака
            user.reload().addOnCompleteListener(task -> {
                if (user.isEmailVerified()) {
                    // 1. Почта подтверждена!
                    Toast.makeText(getContext(), "Почта успешно подтверждена!", Toast.LENGTH_SHORT).show();

                    // 2. ОТПРАВЛЯЕМ ПОЗДРАВИТЕЛЬНОЕ ПИСЬМО
                    // Берем имя из профиля (DisplayName), которое мы сохраняли при регистрации
                    String name = user.getDisplayName() != null ? user.getDisplayName() : "Спортсмен";

                    // Вызываем ваш метод отправки (убедитесь, что название метода совпадает с вашим)
                    MailHelper.sendWelcomeEmail(userEmail, name);

                    // 3. Переходим в главное меню
                    navigateToMain();
                } else {
                    Toast.makeText(getContext(), "Почта еще не подтверждена. Проверьте ящик.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void resendEmail() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.sendEmailVerification().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(getContext(), "Письмо отправлено повторно", Toast.LENGTH_SHORT).show();
                    startResendTimer(); // Запускаем таймер снова после успешной отправки
                } else {
                    Toast.makeText(getContext(), "Ошибка: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    btnResend.setEnabled(true);
                }
            });
        }
    }

    private void navigateToMain() {
        Intent intent = new Intent(requireActivity(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}