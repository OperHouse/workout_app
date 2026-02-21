package com.example.workoutapp.RegistrationActivity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.workoutapp.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class CreatePasswordFragment extends Fragment {

    private TextInputLayout newPassLayout, confirmPassLayout;
    private TextInputEditText newPassEdit, confirmPassEdit;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_create_password, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Инициализация (инсеты добавь по аналогии с прошлыми экранами)
        newPassLayout = view.findViewById(R.id.new_password_layout);
        confirmPassLayout = view.findViewById(R.id.confirm_password_layout);
        newPassEdit = view.findViewById(R.id.new_password_edit);
        confirmPassEdit = view.findViewById(R.id.confirm_password_edit);
        Button btnSave = view.findViewById(R.id.btn_save_password);

        view.findViewById(R.id.btn_back_create).setOnClickListener(v -> getParentFragmentManager().popBackStack());

        btnSave.setOnClickListener(v -> {
            String pass = newPassEdit.getText().toString();
            String confirm = confirmPassEdit.getText().toString();

            if (pass.length() < 6) {
                newPassLayout.setError("Пароль слишком короткий");
            } else if (!pass.equals(confirm)) {
                confirmPassLayout.setError("Пароли не совпадают");
                newPassLayout.setError(null);
            } else {
                // 1. Сбрасываем ошибки
                newPassLayout.setError(null);
                confirmPassLayout.setError(null);

                showSuccessDialog();
            }
        });
    }

    private void showSuccessDialog() {
        hideKeyboard();
        // Создаем view из нашего макета
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_success_password, null);

        com.google.android.material.dialog.MaterialAlertDialogBuilder builder =
                new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext(), R.style.CustomAlertDialog);

        builder.setView(dialogView);
        builder.setCancelable(false);

        final androidx.appcompat.app.AlertDialog dialog = builder.create();

        // Обработка кнопки внутри кастомного макета
        dialogView.findViewById(R.id.btn_dialog_ok).setOnClickListener(v -> {
            dialog.dismiss();

            getParentFragmentManager().popBackStack("RegistrationStep", androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE);


            String userEmail = getArguments() != null ? getArguments().getString("user_email", "") : "";
            Bundle result = new Bundle();
            result.putString("saved_email", userEmail);

            getParentFragmentManager().setFragmentResult("auth_result", result);

            getParentFragmentManager().popBackStack("Authorization", 0);
        });

        dialog.show();
    }

    private void hideKeyboard() {
        View view = this.getView();
        if (view != null) {
            android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager)
                    requireActivity().getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }
}