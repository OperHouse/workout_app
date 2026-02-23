package com.example.workoutapp.RegistrationActivity;

import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.workoutapp.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordFragment extends Fragment {

    private TextInputLayout emailLayout;
    private TextInputEditText emailInput;
    private Button btnSend;
    private FirebaseAuth mAuth;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_forgot_password, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mAuth = FirebaseAuth.getInstance();

        emailLayout = view.findViewById(R.id.forgot_email_layout);
        emailInput = view.findViewById(R.id.forgot_email_edit);
        btnSend = view.findViewById(R.id.btn_send_reset_link);
        View btnBack = view.findViewById(R.id.btn_back_forgot);

        btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        btnSend.setOnClickListener(v -> performReset());
    }

    private void performReset() {
        String email = emailInput.getText().toString().trim();

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.setError("Введите корректный Email");
            return;
        }

        emailLayout.setError(null);
        btnSend.setEnabled(false); // Блокируем кнопку

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    btnSend.setEnabled(true);
                    if (task.isSuccessful()) {
                        // Вместо фрагмента с кодом, показываем сообщение об успехе
                        Toast.makeText(getContext(), "Ссылка для сброса пароля отправлена на почту", Toast.LENGTH_LONG).show();

                        // Возвращаемся на экран авторизации через 2 секунды или сразу
                        getParentFragmentManager().popBackStack();
                    } else {
                        String error = task.getException() != null ? task.getException().getLocalizedMessage() : "Ошибка";
                        Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                    }
                });
    }
}