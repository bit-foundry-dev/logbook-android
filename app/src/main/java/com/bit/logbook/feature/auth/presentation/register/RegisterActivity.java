package com.bit.logbook.feature.auth.presentation.register;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import com.bit.logbook.R;
import com.bit.logbook.core.presentation.BaseActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Objects;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class RegisterActivity extends BaseActivity {

    private RegisterViewModel viewModel;

    private Toolbar toolbar;
    private TextInputLayout tilEmail;
    private TextInputLayout tilUsername;
    private TextInputLayout tilPassword;
    private TextInputLayout tilConfirmPassword;
    private TextInputEditText etEmail;
    private TextInputEditText etUsername;
    private TextInputEditText etPassword;
    private TextInputEditText etConfirmPassword;
    private MaterialButton btnRegister;
    private TextView tvLogin;
    private ProgressBar progressBar;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_register;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            }
        });
    }

    @Override
    protected void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tilEmail = findViewById(R.id.tilEmail);
        tilUsername = findViewById(R.id.tilUsername);
        tilPassword = findViewById(R.id.tilPassword);
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);
        etEmail = findViewById(R.id.etEmail);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvLogin = findViewById(R.id.tvLogin);
        progressBar = findViewById(R.id.progressBar);
    }

    @Override
    protected void initViewModels() {
        viewModel = new ViewModelProvider(this).get(RegisterViewModel.class);
    }

    @Override
    protected void setupViews() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        toolbar.setNavigationOnClickListener(v -> {
            finish();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });

        btnRegister.setOnClickListener(v -> handleRegister());

        tvLogin.setOnClickListener(v -> {
            finish();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });
    }

    @Override
    protected void observeViewModel() {
        viewModel.getRegisterState().observe(this, state -> {
            if (state.isLoading()) {
                showLoading();
            } else if (state.isSuccess()) {
                hideLoading();
                showVerificationDialog(state.getEmail());
            } else if (state.getError() != null) {
                hideLoading();
                showError(state.getError());
            }
        });
    }

    private void handleRegister() {
        String email = Objects.requireNonNull(etEmail.getText()).toString().trim();
        String username = Objects.requireNonNull(etUsername.getText()).toString().trim();
        String password = Objects.requireNonNull(etPassword.getText()).toString().trim();
        String confirmPassword = Objects.requireNonNull(etConfirmPassword.getText()).toString().trim();

        // Clear previous errors
        tilEmail.setError(null);
        tilUsername.setError(null);
        tilPassword.setError(null);
        tilConfirmPassword.setError(null);

        viewModel.register(email, username, password, confirmPassword);
    }

    private void showVerificationDialog(String email) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.email_verification_title)
                .setMessage(getString(R.string.verification_email_sent_to) + email + getString(R.string.check_inbox))
                .setPositiveButton(R.string.ok, (dialog, which) -> {
                    dialog.dismiss();
                    finish();
                })
                .setCancelable(false)
                .show();
    }

    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        btnRegister.setVisibility(View.INVISIBLE);
        tvLogin.setEnabled(false);
    }

    private void hideLoading() {
        progressBar.setVisibility(View.GONE);
        btnRegister.setVisibility(View.VISIBLE);
        tvLogin.setEnabled(true);
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}