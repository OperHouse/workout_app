package com.example.workoutapp.RegistrationActivity;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.workoutapp.R;

import java.util.Locale;

public class VerificationCodeFragment extends Fragment {

    private EditText[] edits;
    private Button btnContinue;
    private TextView tvResendCode, tvErrorMessage;
    private CountDownTimer countDownTimer;
    private boolean isTimerRunning = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_verification_code, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupInsets(view.findViewById(R.id.main_verification));

        btnContinue = view.findViewById(R.id.btn_verify_continue);
        tvResendCode = view.findViewById(R.id.btn_resend_code);
        TextView tvUserEmail = view.findViewById(R.id.tv_user_email);
        tvErrorMessage = view.findViewById(R.id.tv_error_message);

        if (getArguments() != null) {
            String email = getArguments().getString("user_email");
            if (email != null) {
                tvUserEmail.setText(email);
            }
        }

        edits = new EditText[]{
                view.findViewById(R.id.code_1),
                view.findViewById(R.id.code_2),
                view.findViewById(R.id.code_3),
                view.findViewById(R.id.code_4),
                view.findViewById(R.id.code_5)
        };

        setupCodeInputs();
        setupListeners(view);

        // Запускаем таймер сразу при входе (например, на 60 секунд)
        startResendTimer();

        // Добавляем простую анимацию появления карточки
        view.findViewById(R.id.verify_card).startAnimation(
                AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_in)
        );
    }

    private void showErrorCode() {
        // 1. Показываем текст ошибки
        View view = getView();
        if (view == null) return;
        tvErrorMessage.setVisibility(View.VISIBLE);

        // 2. Меняем фон каждой ячейки на красный
        for (EditText edit : edits) {
            edit.setBackgroundResource(R.drawable.bg_code_field_error);
        }

        // 3. (Опционально) Тряска карточки для акцента
        view.findViewById(R.id.verify_card).startAnimation(
                AnimationUtils.loadAnimation(getContext(), R.anim.shake)
        );
    }
    private void resetErrorState() {
        tvErrorMessage.setVisibility(View.GONE);
        for (EditText edit : edits) {
            edit.setBackgroundResource(R.drawable.bg_code_field); // Возвращаем обычный фон
        }
    }

    private void setupListeners(View view) {
        view.findViewById(R.id.btn_back_verify).setOnClickListener(v -> {
            if (getParentFragmentManager() != null) getParentFragmentManager().popBackStack();
        });

        btnContinue.setOnClickListener(v -> {
            String code = getEnteredCode();
            if (code.equals("12345")) { // Твоя проверка кода

                // 1. Получаем почту, которая пришла в этот фрагмент
                String email = "";
                if (getArguments() != null) {
                    email = getArguments().getString("user_email");
                }

                // 2. Создаем новый Bundle для СЛЕДУЮЩЕГО фрагмента
                Bundle nextArgs = new Bundle();
                nextArgs.putString("user_email", email);

                CreatePasswordFragment nextFragment = new CreatePasswordFragment();
                nextFragment.setArguments(nextArgs); // ПЕРЕДАЕМ ДАЛЬШЕ

                getParentFragmentManager().beginTransaction()
                        .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out,
                                android.R.anim.fade_in, android.R.anim.fade_out)
                        .replace(R.id.fragment_container, nextFragment)
                        .addToBackStack(null)
                        .commit();

            } else {
                showErrorCode();
            }
        });

        tvResendCode.setOnClickListener(v -> {
            if (!isTimerRunning) {
                // Логика повторной отправки кода
                Toast.makeText(requireContext(), "Код отправлен повторно", Toast.LENGTH_SHORT).show();
                startResendTimer();
            }
        });
    }

    private void startResendTimer() {
        isTimerRunning = true;
        tvResendCode.setEnabled(false);
        tvResendCode.setAlpha(0.5f);

        countDownTimer = new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int seconds = (int) (millisUntilFinished / 1000);
                String text = String.format(Locale.getDefault(),
                        "Отправить повторно через %02d сек", seconds);
                tvResendCode.setText(text);
            }

            @Override
            public void onFinish() {
                isTimerRunning = false;
                tvResendCode.setEnabled(true);
                tvResendCode.setAlpha(1.0f);
                tvResendCode.setText("Отправить код повторно");
                tvResendCode.setTextColor(ContextCompat.getColor(requireContext(), R.color.light_blue_A200));
            }
        }.start();
    }

    private void setupCodeInputs() {
        for (int i = 0; i < edits.length; i++) {
            final int index = i;
            edits[i].addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    // Сбрасываем визуальную ошибку, если пользователь начал вводить заново
                    resetErrorState();

                    if (s.length() == 1 && index < edits.length - 1) {
                        edits[index + 1].requestFocus();
                    }
                }

                @Override public void afterTextChanged(Editable s) {}
            });

            edits[i].setOnKeyListener((v, keyCode, event) -> {
                if (keyCode == KeyEvent.KEYCODE_DEL && event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (edits[index].getText().toString().isEmpty() && index > 0) {
                        edits[index - 1].requestFocus();
                        edits[index - 1].setText("");
                        return true;
                    }
                }
                return false;
            });
        }
    }

    private String getEnteredCode() {
        StringBuilder sb = new StringBuilder();
        for (EditText et : edits) sb.append(et.getText().toString());
        return sb.toString();
    }

    private void setupInsets(View view) {
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (countDownTimer != null) countDownTimer.cancel(); // Обязательно останавливаем таймер
    }
}