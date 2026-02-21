package com.example.workoutapp.RegistrationActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Insets;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
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
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.workoutapp.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class AuthorizationFragment extends Fragment {

    private TextInputLayout emailLayout, passwordLayout;
    private TextInputEditText emailInput, passwordInput;

    public AuthorizationFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {



        return inflater.inflate(R.layout.fragment_authorization, container, false);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        View mainView = view.findViewById(R.id.main_auth);

        TextInputEditText emailInput = view.findViewById(R.id.auth_email_edit); // Проверь свой ID


        if (getArguments() != null) {
            String savedEmail = getArguments().getString("saved_email");
            if (savedEmail != null && !savedEmail.isEmpty()) {
                emailInput.setText(savedEmail);
                passwordInput.requestFocus();
            }
        }

        TextInputEditText finalEmailInput = emailInput;
        getParentFragmentManager().setFragmentResultListener(
                "auth_result",
                this,
                (requestKey, bundle) -> {
                    String email = bundle.getString("saved_email", "");
                    if (!email.isEmpty()) {
                        finalEmailInput.setText(email);
                        passwordInput.requestFocus();
                    }
                }
        );

        ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
            Insets systemBars = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars()).toPlatformInsets();
            }
            // Устанавливаем padding сверху, равный высоте системной полоски
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            }
            return insets;
        });

        // 1. Инициализация View
        emailLayout = view.findViewById(R.id.auth_email_layout);
        passwordLayout = view.findViewById(R.id.auth_password_layout);
        emailInput = view.findViewById(R.id.auth_email_edit);
        passwordInput = view.findViewById(R.id.auth_password_edit);

        view.findViewById(R.id.btn_auth_vk).setOnClickListener(v -> loginWithVK());
        view.findViewById(R.id.btn_auth_google).setOnClickListener(v -> loginWithGoogle());
        view.findViewById(R.id.btn_auth_twitter).setOnClickListener(v -> loginWithTwitter());

        Button btnContinue = view.findViewById(R.id.btn_auth_continue);
        View btnBack = view.findViewById(R.id.btn_back_auth);

        // 2. Логика кнопок
        btnBack.setOnClickListener(v -> requireActivity().onBackPressed());

        btnContinue.setOnClickListener(v -> performLogin());

        // Закрытие клавиатуры при клике на пустую область
        mainView.setOnTouchListener((v, event) -> {
            hideKeyboard(v);
            if (getActivity().getCurrentFocus() != null) {
                getActivity().getCurrentFocus().clearFocus();
            }
            return false;
        });

        TextInputEditText[] inputs = {emailInput, passwordInput};
        for (TextInputEditText input : inputs) {
            input.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    v.clearFocus();
                    hideKeyboard(v);
                    return true;
                }
                return false;
            });
        }

        view.findViewById(R.id.btn_forgot_password).setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out,
                            android.R.anim.fade_in, android.R.anim.fade_out)
                    .replace(R.id.fragment_container, new ForgotPasswordFragment())
                    .addToBackStack(null) // Важно, чтобы вернуться назад по кнопке
                    .commit();
        });

        // 3. Настройка текста соглашений
        setupAgreementText(view);
    }

    private void performLogin() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        boolean isValid = true;

        if (email.isEmpty()) {
            emailLayout.setError("Введите почту");
            isValid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.setError("Некорректный Email");
            isValid = false;
        } else {
            emailLayout.setError(null);
        }

        if (password.isEmpty()) {
            passwordLayout.setError("Введите пароль");
            isValid = false;
        } else {
            passwordLayout.setError(null);
        }

        if (isValid) {
            Toast.makeText(getContext(), "Вход...", Toast.LENGTH_SHORT).show();
            // Здесь ваша логика авторизации
        }
    }

    private void setupAgreementText(View fragmentView) {
        TextView agreementText = fragmentView.findViewById(R.id.agreement_text);
        String fullText = "Авторизируясь, вы принимаете условия Пользовательского соглашения и даете согласие на обработку Персональных данных";
        SpannableString ss = new SpannableString(fullText);
        int highlightColor = ContextCompat.getColor(requireContext(), R.color.light_blue_A200);

        ClickableSpan agreementClick = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                showAgreementDialog("Пользовательское соглашение", getString(R.string.agreement_text_content));
            }
            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(highlightColor);
                ds.setUnderlineText(true);
                ds.setFakeBoldText(true);
            }
        };

        ClickableSpan dataClick = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                showAgreementDialog("Персональные данные", getString(R.string.privacy_policy_content));
            }
            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(highlightColor);
                ds.setUnderlineText(true);
                ds.setFakeBoldText(true);
            }
        };

        int start1 = fullText.indexOf("Пользовательского соглашения");
        int start2 = fullText.indexOf("Персональных данных");

        if (start1 != -1) ss.setSpan(agreementClick, start1, start1 + 28, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        if (start2 != -1) ss.setSpan(dataClick, start2, start2 + 19, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        agreementText.setText(ss);
        agreementText.setMovementMethod(LinkMovementMethod.getInstance());
        agreementText.setHighlightColor(Color.TRANSPARENT);
    }

    private void showAgreementDialog(String title, String content) {
        com.google.android.material.bottomsheet.BottomSheetDialog bottomSheetDialog =
                new com.google.android.material.bottomsheet.BottomSheetDialog(requireContext());
        View view = getLayoutInflater().inflate(R.layout.dialog_agreement, null);

        ((TextView) view.findViewById(R.id.dialog_title)).setText(title);
        ((TextView) view.findViewById(R.id.dialog_text)).setText(content);
        view.findViewById(R.id.btn_close_agreement).setOnClickListener(v -> bottomSheetDialog.dismiss());

        bottomSheetDialog.setContentView(view);
        View bottomSheet = (View) view.getParent();
        if (bottomSheet != null) bottomSheet.setBackgroundColor(Color.TRANSPARENT);

        bottomSheetDialog.show();
    }

    private void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void loginWithVK() {
        Toast.makeText(requireContext(), "Интеграция с VK...", Toast.LENGTH_SHORT).show();
        // Сюда пойдет вызов VKID SDK
    }

    private void loginWithGoogle() {
        Toast.makeText(requireContext(), "Интеграция с Google...", Toast.LENGTH_SHORT).show();
        // Сюда пойдет Google SignIn Client
    }

    private void loginWithTwitter() {
        Toast.makeText(requireContext(), "Интеграция с Twitter...", Toast.LENGTH_SHORT).show();
    }
}