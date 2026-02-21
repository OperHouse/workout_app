package com.example.workoutapp.RegistrationActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.workoutapp.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class ForgotPasswordFragment extends Fragment {

    private TextInputLayout emailLayout;
    private TextInputEditText emailInput;
    private Button btnContinue;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_forgot_password, container, false);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Инициализация View
        initViews(view);

        // 2. Исправляем отступы EdgeToEdge
        View mainView = view.findViewById(R.id.main_forgot_pass);
        ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 3. Кнопка назад
        view.findViewById(R.id.btn_back_forgot).setOnClickListener(v -> {
            getParentFragmentManager().popBackStack();
        });

        // 4. Кнопка "Продолжить" — переход на ввод кода
        btnContinue.setOnClickListener(v -> {
            if (validateEmail()) {
                navigateToVerification();
            }
        });

        // 5. Скрытие клавиатуры при клике на фон
        mainView.setOnTouchListener((v, event) -> {
            hideKeyboard(v);
            return false;
        });
    }

    private void initViews(View view) {
        emailLayout = view.findViewById(R.id.forgot_email_layout);
        emailInput = view.findViewById(R.id.forgot_email_edit);
        btnContinue = view.findViewById(R.id.btn_forgot_continue);
    }

    private boolean validateEmail() {
        String email = emailInput.getText().toString().trim();
        if (email.isEmpty()) {
            emailLayout.setError("Введите почту");
            return false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.setError("Некорректный формат почты");
            return false;
        }
        emailLayout.setError(null);
        return true;
    }

    private void navigateToVerification() {
        hideKeyboard(getView());

        String email = emailInput.getText().toString().trim();

        // Создаем Bundle и кладем туда почту
        Bundle args = new Bundle();
        args.putString("user_email", email);

        VerificationCodeFragment fragment = new VerificationCodeFragment();
        fragment.setArguments(args); // Привязываем данные к фрагменту

        getParentFragmentManager().beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out,
                        android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void hideKeyboard(View view) {
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}