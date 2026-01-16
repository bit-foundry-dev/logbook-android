package com.bit.logbook.feature.auth.presentation.login;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;

import com.bit.logbook.MainActivity;
import com.bit.logbook.R;
import com.bit.logbook.core.presentation.BaseActivity;
import com.bit.logbook.feature.auth.presentation.register.RegisterActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Objects;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class LoginActivity extends BaseActivity {

    private LoginViewModel viewModel;

    private TextInputLayout tilEmailUsername;
    private TextInputLayout tilPassword;
    private TextInputEditText etEmailUsername;
    private TextInputEditText etPassword;
    private MaterialButton btnLogin;
    private TextView tvForgotPassword;
    private TextView tvRegister;
    private ProgressBar progressBar;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_login;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initViews() {
        tilEmailUsername = findViewById(R.id.tilEmailUsername);
        tilPassword = findViewById(R.id.tilPassword);
        etEmailUsername = findViewById(R.id.etEmailUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvRegister = findViewById(R.id.tvRegister);
        progressBar = findViewById(R.id.progressBar);
    }

    @Override
    protected void initViewModels() {
        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);
    }

    @Override
    protected void setupViews() {
        btnLogin.setOnClickListener(v -> handleLogin());

        tvForgotPassword.setOnClickListener(v -> showForgotPasswordDialog());

        tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
    }

    @Override
    protected void observeViewModel() {
        viewModel.getLoginState().observe(this, state -> {
            if (state.isLoading()) {
                showLoading();
            } else if (state.getUser() != null) {
                hideLoading();
                Toast.makeText(this, getString(R.string.login_welcome) + state.getUser().getUsername(),
                        Toast.LENGTH_SHORT).show();
                // Navigate to main screen
                 startActivity(new Intent(this, MainActivity.class));
                finish();
            } else if (state.isEmailNotVerified()) {
                hideLoading();
                showEmailNotVerifiedDialog(state.getUnverifiedEmail());
            } else if (state.isResendVerificationSuccess()) {
                hideLoading();
                Toast.makeText(this, R.string.verification_sent_check_inbox,
                        Toast.LENGTH_LONG).show();
            } else if (state.getError() != null) {
                hideLoading();
                showError(state.getError());
            } else if (state.isForgotPasswordSuccess()) {
                hideLoading();
                Toast.makeText(this, R.string.password_reset_sent_check_inbox,
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void handleLogin() {
        String emailOrUsername = Objects.requireNonNull(etEmailUsername.getText()).toString().trim();
        String password = Objects.requireNonNull(etPassword.getText()).toString().trim();

        // Clear previous errors
        tilEmailUsername.setError(null);
        tilPassword.setError(null);

        if (emailOrUsername.isEmpty()) {
            tilEmailUsername.setError(getString(R.string.email_username_required));
            return;
        }

        if (password.isEmpty()) {
            tilPassword.setError(getString(R.string.password_required));
            return;
        }

        viewModel.login(emailOrUsername, password);
    }

    private void showForgotPasswordDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_forgot_password, null);

        TextInputLayout tilEmail = dialogView.findViewById(R.id.tilEmail);
        TextInputEditText etEmail = dialogView.findViewById(R.id.etEmail);

        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.forgot_password)
                .setView(dialogView)
                .setPositiveButton(R.string.send_reset_email, null)
                .setNegativeButton(R.string.cancel, (d, which) -> d.dismiss())
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(Dialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String email = Objects.requireNonNull(etEmail.getText()).toString().trim();

            tilEmail.setError(null);

            if (email.isEmpty()) {
                tilEmail.setError(getString(R.string.email_required));
                return;
            }

            viewModel.forgotPassword(email);
            dialog.dismiss();
        }));

        dialog.show();
    }

    private void showEmailNotVerifiedDialog(String emailOrUsername) {
        // Check if the input is an email or username
        boolean isEmail = isValidEmail(emailOrUsername);

        if (isEmail) {
            // User logged in with email - show simple dialog
            showSimpleResendDialog(emailOrUsername);
        } else {
            // User logged in with username - need to ask for email
            showEmailInputResendDialog();
        }
    }

    private void showSimpleResendDialog(String email) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.resend_verification_email_title)
                .setMessage(R.string.resend_verification_email_message)
                .setPositiveButton(R.string.resend_verification_email, (dialog, which) -> viewModel.resendVerificationEmail(email))
                .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss())
                .setCancelable(true)
                .show();
    }

    private void showEmailInputResendDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_resend_verification, null);

        TextInputLayout tilEmail = dialogView.findViewById(R.id.tilEmail);
        TextInputEditText etEmail = dialogView.findViewById(R.id.etEmail);

        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.resend_verification_email_title)
                .setView(dialogView)
                .setPositiveButton(R.string.resend_verification_email, null)
                .setNegativeButton(R.string.cancel, (d, which) -> d.dismiss())
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(Dialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String email = Objects.requireNonNull(etEmail.getText()).toString().trim();

            tilEmail.setError(null);

            if (email.isEmpty()) {
                tilEmail.setError(getString(R.string.email_required));
                return;
            }

            if (!isValidEmail(email)) {
                tilEmail.setError(getString(R.string.invalid_email_format));
                return;
            }

            viewModel.resendVerificationEmail(email);
            dialog.dismiss();
        }));

        dialog.show();
    }

    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        btnLogin.setVisibility(View.INVISIBLE);
        tvForgotPassword.setEnabled(false);
        tvRegister.setEnabled(false);
    }

    private void hideLoading() {
        progressBar.setVisibility(View.GONE);
        btnLogin.setVisibility(View.VISIBLE);
        tvForgotPassword.setEnabled(true);
        tvRegister.setEnabled(true);
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}