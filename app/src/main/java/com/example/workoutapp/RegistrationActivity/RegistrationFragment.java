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
import com.example.workoutapp.Tools.MailHelper;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class RegistrationFragment extends Fragment {

    private TextInputLayout nameLayout, emailLayout, passwordLayout;
    private TextInputEditText nameInput, emailInput, passwordInput;
    private Button btnRegister;
    private TextView tvLoginLink, btnSkip;

    private FirebaseAuth mAuth;

    public RegistrationFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_registration, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mAuth = FirebaseAuth.getInstance();

        initViews(view);
        rootViewSetup(view);
        setupListeners();
        setupAgreementText(view);
    }

    private void initViews(View view) {
        nameLayout = view.findViewById(R.id.name_layout);
        emailLayout = view.findViewById(R.id.email_layout);
        passwordLayout = view.findViewById(R.id.password_layout);

        nameInput = (TextInputEditText) nameLayout.getEditText();
        emailInput = (TextInputEditText) emailLayout.getEditText();
        passwordInput = (TextInputEditText) passwordLayout.getEditText();

        btnSkip = view.findViewById(R.id.btn_skip_registration);
        tvLoginLink = view.findViewById(R.id.tv_login_link);
        btnRegister = view.findViewById(R.id.btn_register);

        nameInput.addTextChangedListener(new TextWatcher() {
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                nameLayout.setError(null);
            }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void rootViewSetup(View view) {
        View rootLayout = view.findViewById(R.id.main);
        if (rootLayout != null) {
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
    }

    private void setupListeners() {
        tvLoginLink.setOnClickListener(v -> openLoginFragment());

        btnSkip.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra("open_fragment", "workout");
            startActivity(intent);
            requireActivity().finish();
        });

        TextInputEditText[] inputs = {nameInput, emailInput, passwordInput};
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

        passwordInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                validatePasswordStrength(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        btnRegister.setOnClickListener(v -> performRegistration());
    }

    private void performRegistration() {
        String name = nameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        // 1. Валидация
        boolean isAllValid = true;
        if (name.isEmpty()) { nameLayout.setError("Введите имя"); isAllValid = false; } else nameLayout.setError(null);
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.setError("Некорректная почта"); isAllValid = false;
        } else emailLayout.setError(null);
        if (password.length() < 6) {
            passwordLayout.setError("Минимум 6 символов"); isAllValid = false;
        } else passwordLayout.setError(null);

        if (!isAllValid) return;

        btnRegister.setEnabled(false);
        hideKeyboard(getView());

        // 2. Создание пользователя в Firebase
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(requireActivity(), task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {

                            // 3. Устанавливаем отображаемое имя (DisplayName)
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(name)
                                    .build();

                            user.updateProfile(profileUpdates).addOnCompleteListener(profileTask -> {

                                // 4. Отправляем письмо для подтверждения почты
                                user.sendEmailVerification().addOnCompleteListener(verifyTask -> {
                                    if (verifyTask.isSuccessful()) {

                                        // 5. Отправляем ТВОЁ приветственное письмо (через MailHelper)
                                        MailHelper.sendWelcomeEmail(email, name);

                                        // 6. Переходим на фрагмент ожидания подтверждения
                                        getParentFragmentManager().beginTransaction()
                                                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                                                .replace(R.id.fragment_container, EmailVerificationFragment.newInstance(email))
                                                .addToBackStack(null)
                                                .commit();

                                    } else {
                                        btnRegister.setEnabled(true);
                                        String error = verifyTask.getException() != null ? verifyTask.getException().getLocalizedMessage() : "Ошибка отправки письма";
                                        Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
                                    }
                                });
                            });
                        }
                    } else {
                        btnRegister.setEnabled(true);
                        String error = task.getException() != null ? task.getException().getLocalizedMessage() : "Ошибка регистрации";
                        Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void openLoginFragment() {
        getParentFragmentManager().beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.fragment_container, new AuthorizationFragment())
                .commit();
    }

    private void validatePasswordStrength(String pass) {
        if (pass.isEmpty()) {
            passwordLayout.setHelperText("");
            passwordLayout.setError(null);
        } else if (pass.length() < 6) {
            passwordLayout.setHelperText("Слишком короткий");
            passwordLayout.setHelperTextColor(ColorStateList.valueOf(Color.RED));
        } else if (pass.matches("^[a-zA-Z0-9]*$")) {
            passwordLayout.setHelperText("Средний (добавьте символы)");
            passwordLayout.setHelperTextColor(ColorStateList.valueOf(Color.YELLOW));
        } else {
            passwordLayout.setHelperText("Надежный пароль");
            passwordLayout.setHelperTextColor(ColorStateList.valueOf(Color.GREEN));
        }
    }

    private void clearAllFocus() {
        if (requireActivity().getCurrentFocus() != null) {
            requireActivity().getCurrentFocus().clearFocus();
        }
    }

    private void setupAgreementText(View view) {
        TextView agreementText = view.findViewById(R.id.agreement_text);
        String fullText = "Регистрируясь, вы принимаете условия Пользовательского соглашения и даете согласие на обработку Персональных данных";

        SpannableString ss = new SpannableString(fullText);
        int highlightColor = ContextCompat.getColor(requireContext(), R.color.light_blue_A200);

        ClickableSpan agreementClick = new ClickableSpan() {
            @Override public void onClick(@NonNull View widget) {
                showAgreementDialog("Соглашение", "Текст соглашения...");
            }
            @Override public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(highlightColor);
                ds.setUnderlineText(true);
            }
        };

        int start1 = fullText.indexOf("Пользовательского соглашения");
        if (start1 != -1) ss.setSpan(agreementClick, start1, start1 + 28, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        agreementText.setText(ss);
        agreementText.setMovementMethod(LinkMovementMethod.getInstance());
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

    private void navigateToMain() {
        Intent intent = new Intent(requireActivity(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }
}