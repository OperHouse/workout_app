package com.example.workoutapp.RegistrationActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.workoutapp.MainActivity;
import com.example.workoutapp.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AuthorizationFragment extends Fragment {

    private TextInputLayout emailLayout, passwordLayout;
    private TextInputEditText emailInput, passwordInput;
    private FirebaseAuth mAuth;

    public AuthorizationFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_authorization, container, false);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        View mainView = view.findViewById(R.id.main_auth);

        // 1. Инициализация View
        emailLayout = view.findViewById(R.id.auth_email_layout);
        passwordLayout = view.findViewById(R.id.auth_password_layout);
        emailInput = view.findViewById(R.id.auth_email_edit);
        passwordInput = view.findViewById(R.id.auth_password_edit);

        // Удаление ошибок при начале ввода
        setupTextWatchers();

        // Слушатель результата регистрации (автозаполнение почты)
        getParentFragmentManager().setFragmentResultListener("auth_result", this, (requestKey, bundle) -> {
            String email = bundle.getString("saved_email", "");
            if (!email.isEmpty() && emailInput != null) {
                emailInput.setText(email);
                passwordInput.requestFocus();
            }
        });

        // Системные отступы
        ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 2. Логика кнопок
        view.findViewById(R.id.btn_back_auth).setOnClickListener(v -> requireActivity().onBackPressed());

        Button btnContinue = view.findViewById(R.id.btn_auth_continue);
        btnContinue.setOnClickListener(v -> performLogin());

        // Переход на забытый пароль
        view.findViewById(R.id.btn_forgot_password).setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out)
                    .replace(R.id.fragment_container, new ForgotPasswordFragment())
                    .addToBackStack(null)
                    .commit();
        });

        // Кнопка перехода на регистрацию
        View btnGoRegister = view.findViewById(R.id.tv_login_link);
        if (btnGoRegister != null) {
            btnGoRegister.setOnClickListener(v -> {
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new RegistrationFragment())
                        .commit();
            });
        }

        // Клавиатура Done
        passwordInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                performLogin();
                return true;
            }
            return false;
        });

        // Скрытие клавиатуры при клике на фон
        mainView.setOnTouchListener((v, event) -> {
            hideKeyboard(v);
            if (emailInput != null) emailInput.clearFocus();
            if (passwordInput != null) passwordInput.clearFocus();
            return false;
        });

        setupAgreementText(view);
    }

    private void setupTextWatchers() {
        TextWatcher commonWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                emailLayout.setError(null);
                passwordLayout.setError(null);
            }
            @Override
            public void afterTextChanged(Editable s) {}
        };
        emailInput.addTextChangedListener(commonWatcher);
        passwordInput.addTextChangedListener(commonWatcher);
    }

    private void performLogin() {
        if (emailInput == null || passwordInput == null) return;

        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (!validateInputs(email, password)) return;

        hideKeyboard(getView());

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(requireActivity(), task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            if (user.isEmailVerified()) {
                                navigateToMain();
                            } else {
                                // Почта не подтверждена - отправляем на экран верификации
                                showVerificationFragment(email);
                                mAuth.signOut();
                            }
                        }
                    } else {
                        // ОБРАБОТКА ОШИБОК (Безопасный режим)
                        Exception e = task.getException();

                        if (e instanceof com.google.firebase.auth.FirebaseAuthInvalidUserException ||
                                e instanceof com.google.firebase.auth.FirebaseAuthInvalidCredentialsException) {

                            // Показываем общую ошибку, чтобы скрыть, что именно неверно
                            String commonError = "Неверная почта или пароль";
                            emailLayout.setError(commonError);
                            passwordLayout.setError(commonError);

                            // Фокус лучше оставить на пароле, так как чаще ошибаются в нем
                            passwordInput.requestFocus();

                        } else if (e instanceof com.google.firebase.FirebaseNetworkException) {
                            Toast.makeText(getContext(), "Проблемы с интернетом", Toast.LENGTH_SHORT).show();
                        } else {
                            // Ошибка безопасности (например, слишком много попыток)
                            Toast.makeText(getContext(), "Ошибка доступа. Попробуйте позже.", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void showVerificationFragment(String email) {
        getParentFragmentManager().beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.fragment_container, EmailVerificationFragment.newInstance(email))
                .addToBackStack(null)
                .commit();
    }

    private boolean validateInputs(String email, String password) {
        boolean isValid = true;
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.setError("Некорректная почта");
            isValid = false;
        } else emailLayout.setError(null);

        if (password.isEmpty()) {
            passwordLayout.setError("Введите пароль");
            isValid = false;
        } else passwordLayout.setError(null);

        return isValid;
    }

    private void navigateToMain() {
        Intent intent = new Intent(requireActivity(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    private void hideKeyboard(View view) {
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void setupAgreementText(View fragmentView) {
        TextView agreementText = fragmentView.findViewById(R.id.agreement_text);
        if (agreementText == null) return;

        String fullText = "Авторизируясь, вы принимаете условия Пользовательского соглашения и даете согласие на обработку Персональных данных";
        SpannableString ss = new SpannableString(fullText);
        int highlightColor = ContextCompat.getColor(requireContext(), R.color.light_blue_A200);

        ClickableSpan agreementClick = new ClickableSpan() {
            @Override public void onClick(@NonNull View widget) {
                // Логика показа диалога
            }
            @Override public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(highlightColor);
                ds.setUnderlineText(true);
            }
        };

        int start1 = fullText.indexOf("Пользовательского соглашения");
        int start2 = fullText.indexOf("Персональных данных");

        if (start1 != -1) ss.setSpan(agreementClick, start1, start1 + 28, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        if (start2 != -1) ss.setSpan(agreementClick, start2, start2 + 19, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        agreementText.setText(ss);
        agreementText.setMovementMethod(LinkMovementMethod.getInstance());
        agreementText.setHighlightColor(Color.TRANSPARENT);
    }
}