package com.example.workoutapp.RegistrationActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.workoutapp.MainActivity;
import com.example.workoutapp.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class RegistrationActivity extends AppCompatActivity {

    private TextInputLayout nameLayout, emailLayout, passwordLayout;
    private TextInputEditText nameInput, emailInput, passwordInput;
    private Button btnRegister;
    TextView btnSkip;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registration);

        // 1. Инициализация View
        rootViewSetup();
        initViews();

        // 2. Настройка слушателей
        setupListeners();

        // 3. Настройка текста соглашений
        setupAgreementText();
    }

    private void initViews() {
        nameLayout = findViewById(R.id.name_layout);
        emailLayout = findViewById(R.id.email_layout);
        passwordLayout = findViewById(R.id.password_layout);

        nameInput = (TextInputEditText) nameLayout.getEditText();
        emailInput = (TextInputEditText) emailLayout.getEditText();
        passwordInput = (TextInputEditText) passwordLayout.getEditText();

        btnSkip = findViewById(R.id.btn_skip_registration);

        btnRegister = findViewById(R.id.card_data).findViewById(R.id.btn_register); // Укажите верный ID вашей кнопки регистрации
    }

    @SuppressLint("ClickableViewAccessibility")
    private void rootViewSetup() {
        View rootLayout = findViewById(R.id.main);
        ViewCompat.setOnApplyWindowInsetsListener(rootLayout, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        rootLayout.setOnTouchListener((v, event) -> {
            clearAllFocus();
            hideKeyboard(v);
            return false;
        });
    }

    private void setupListeners() {


        btnSkip.setOnClickListener(v -> {
            // Создаем намерение для перехода в MainActivity
            Intent intent = new Intent(RegistrationActivity.this, MainActivity.class);

            // Флаги, чтобы нельзя было вернуться назад в регистрацию кнопкой "Back"
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            // Если вам нужно сказать MainActivity, чтобы она открыла именно WorkoutFragment
            intent.putExtra("open_fragment", "workout");

            startActivity(intent);
            finish(); // Закрываем экран регистрации
        });

        // Обработка кнопки Done на клавиатуре
        TextInputEditText[] inputs = {nameInput, emailInput, passwordInput};
        for (TextInputEditText input : inputs) {
            input.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT) {
                    v.clearFocus();
                    hideKeyboard(v);
                    return true;
                }
                return false;
            });
        }

        // Динамическая проверка сложности пароля
        passwordInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                validatePasswordStrength(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // Кнопка регистрации
        findViewById(R.id.btn_register).setOnClickListener(v -> performRegistration());
    }

    private void validatePasswordStrength(String pass) {
        if (pass.isEmpty()) {
            passwordLayout.setHelperText("");
            passwordLayout.setError(null);
        } else if (pass.length() < 6) {
            passwordLayout.setHelperText("Слишком короткий пароль");
            passwordLayout.setHelperTextColor(ColorStateList.valueOf(Color.RED));
        } else if (pass.matches("^[a-zA-Z0-9]*$")) {
            passwordLayout.setHelperText("Средний пароль: добавьте символы (!@#)");
            passwordLayout.setHelperTextColor(ColorStateList.valueOf(Color.YELLOW));
        } else {
            passwordLayout.setHelperText("Надежный пароль");
            passwordLayout.setHelperTextColor(ColorStateList.valueOf(Color.GREEN));
        }
    }

    private void performRegistration() {
        String name = nameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        boolean isAllValid = true;

        // Валидация имени
        if (name.isEmpty()) {
            nameLayout.setError("Введите имя");
            isAllValid = false;
        } else {
            nameLayout.setError(null);
        }

        // Валидация Email
        if (email.isEmpty()) {
            emailLayout.setError("Введите почту");
            isAllValid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.setError("Некорректный формат почты");
            isAllValid = false;
        } else {
            emailLayout.setError(null);
        }

        // Валидация пароля
        if (password.isEmpty()) {
            passwordLayout.setError("Введите пароль");
            isAllValid = false;
        } else if (password.length() < 6) {
            passwordLayout.setError("Минимум 6 символов");
            isAllValid = false;
        } else {
            passwordLayout.setError(null);
        }

        if (isAllValid) {
            Toast.makeText(this, "Регистрация прошла успешно!", Toast.LENGTH_SHORT).show();
            // Тут логика перехода или сохранения в БД
        }
    }

    private void clearAllFocus() {
        if (getCurrentFocus() != null) {
            getCurrentFocus().clearFocus();
        }
    }

    private void setupAgreementText() {
        TextView agreementText = findViewById(R.id.agreement_text);
        String fullText = "Регистрируясь, вы принимаете условия Пользовательского соглашения и даете согласие на обработку Персональных данных";
        SpannableString ss = new SpannableString(fullText);
        int highlightColor = ContextCompat.getColor(this, R.color.light_blue_A200);

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
                new com.google.android.material.bottomsheet.BottomSheetDialog(this);
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
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}