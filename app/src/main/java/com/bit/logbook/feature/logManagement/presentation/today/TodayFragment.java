package com.bit.logbook.feature.logManagement.presentation.today;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bit.logbook.R;
import com.bit.logbook.feature.logManagement.data.model.CreateLogRequest;
import com.bit.logbook.feature.logManagement.domain.entity.Log;
import com.bit.logbook.feature.logManagement.presentation.LogAdapter;
import com.bit.logbook.feature.logManagement.presentation.LogState;
import com.bit.logbook.feature.logManagement.presentation.LogsViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class TodayFragment extends Fragment {

    private LogsViewModel viewModel;
    private LogAdapter adapter;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private LinearLayout errorContainer;
    private TextView errorMessage;
    private Button retryButton;
    private LinearLayout emptyStateContainer;
    private FloatingActionButton addLogBtn;
    private LocalDateTime selectedTime;
    private AlertDialog addLogDialog;
    private ProgressBar dialogProgressBar;
    private View dialogContent;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_today, container, false);

        viewModel = new ViewModelProvider(this).get(LogsViewModel.class);
        recyclerView = rootView.findViewById(R.id.recycler_view_logs);
        progressBar = rootView.findViewById(R.id.progress_bar);
        errorContainer = rootView.findViewById(R.id.error_container);
        errorMessage = rootView.findViewById(R.id.error_message);
        retryButton = rootView.findViewById(R.id.retry_button);
        emptyStateContainer = rootView.findViewById(R.id.empty_state_container);
        addLogBtn = rootView.findViewById(R.id.fab_add_log);

        setupRecyclerView();

        retryButton.setOnClickListener(v -> viewModel.getLogs(null));
        addLogBtn.setOnClickListener(v -> showAddLogDialog());

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        observeViewModel();

        // Load logs for today
        viewModel.getLogs(null);
    }

    private void setupRecyclerView() {
        adapter = new LogAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        adapter.setOnLogClickListener((log, position) -> {
            Toast.makeText(requireContext(),
                    "Clicked: " + log.getTitle(),
                    Toast.LENGTH_SHORT).show();
        });
    }

    private void observeViewModel() {
        viewModel.getLogState().observe(getViewLifecycleOwner(), this::handleLogListState);
        viewModel.getLogCreationState().observe(getViewLifecycleOwner(), this::handleLogCreationState);
    }

    private void handleLogListState(LogState state) {
        if (state == null) return;

        if (state.isLoading()) {
            renderLoading();
        } else if (state.getError() != null) {
            renderError(state.getError());
        } else if (state.getLogs() != null) {
            renderSuccess(state.getLogs());
        }
    }

    private void handleLogCreationState(LogCreationState state) {
        if (state == null) return;

        if (state.isLoading()) {
            if (dialogProgressBar != null) {
                dialogProgressBar.setVisibility(View.VISIBLE);
            }
            if(dialogContent != null){
                    dialogContent.setVisibility(View.INVISIBLE);
            }
            if (addLogDialog != null) {
                addLogDialog.setCancelable(false);
                addLogDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                addLogDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(false);
            }
        } else if (state.getError() != null) {
            if (dialogProgressBar != null) {
                dialogProgressBar.setVisibility(View.GONE);
            }
            if(dialogContent != null){
                dialogContent.setVisibility(View.VISIBLE);
            }
            if (addLogDialog != null) {
                addLogDialog.setCancelable(true);
                addLogDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                addLogDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(true);
            }
            Toast.makeText(requireContext(), getString(R.string.create_log_failed) + " :" + state.getError(), Toast.LENGTH_SHORT).show();
            viewModel.resetLogCreationState();
        } else if (state.getLog() != null) {
            if (addLogDialog != null && addLogDialog.isShowing()) {
                addLogDialog.dismiss();
            }
            viewModel.getLogs(null);
            Toast.makeText(requireContext(), R.string.create_log_successful, Toast.LENGTH_SHORT).show();
            viewModel.resetLogCreationState();
        }
    }

    private void renderLoading() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        errorContainer.setVisibility(View.GONE);
        emptyStateContainer.setVisibility(View.GONE);
    }

    private void renderError(String error) {
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        errorContainer.setVisibility(View.VISIBLE);
        emptyStateContainer.setVisibility(View.GONE);
        errorMessage.setText(error);
    }

    private void renderSuccess(List<Log> logs) {
        progressBar.setVisibility(View.GONE);
        errorContainer.setVisibility(View.GONE);
        if (logs.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyStateContainer.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyStateContainer.setVisibility(View.GONE);
            adapter.setLogs(logs);
        }
    }

    private void showAddLogDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        builder.setTitle(R.string.add_log_title);

        View view = getLayoutInflater().inflate(R.layout.dialog_add_log, null);
        builder.setView(view);

        final EditText titleEditText = view.findViewById(R.id.edit_text_title);
        final EditText descriptionEditText = view.findViewById(R.id.edit_text_description);
        final EditText tagEditText = view.findViewById(R.id.edit_text_tag);
        final EditText timeEditText = view.findViewById(R.id.edit_text_time);
        dialogProgressBar = view.findViewById(R.id.dialog_progress_bar);
        dialogContent = view.findViewById(R.id.dialog_content);

        // Pre-fill with current time
        selectedTime = LocalDateTime.now();
        timeEditText.setText(selectedTime.format(DateTimeFormatter.ofPattern("HH:mm")));

        timeEditText.setOnClickListener(v -> {
            MaterialTimePicker timePicker = new MaterialTimePicker.Builder()
                    .setTimeFormat(TimeFormat.CLOCK_12H)
                    .setHour(selectedTime.getHour())
                    .setMinute(selectedTime.getMinute())
                    .setTitleText(R.string.select_time)
                    .build();

            timePicker.addOnPositiveButtonClickListener(dialog -> {
                selectedTime = LocalDateTime.now().withHour(timePicker.getHour()).withMinute(timePicker.getMinute());

                timeEditText.setText(selectedTime.format(DateTimeFormatter.ofPattern("HH:mm")));
            });

            timePicker.show(getChildFragmentManager(), "TIME_PICKER");
        });

        builder.setPositiveButton(R.string.save, (dialog, which) -> {
            // This listener is intentionally left empty because we will override it later
            // to prevent the dialog from closing automatically.
        });

        builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.cancel());

        addLogDialog = builder.create();

        addLogDialog.setOnShowListener(dialogInterface -> {
            Button button = addLogDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener(view1 -> {
                String title = titleEditText.getText().toString();
                String description = descriptionEditText.getText().toString();
                String tag = tagEditText.getText().toString();

                if (!title.isEmpty()) {
                    CreateLogRequest request = new CreateLogRequest(title, description, tag, selectedTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                    viewModel.createLog(request);
                } else {
                    titleEditText.setError(getString(R.string.title_required));
                }
            });
        });
        
        addLogDialog.setOnDismissListener(dialog -> {
            viewModel.resetLogCreationState();
            addLogDialog = null;
            dialogProgressBar = null;
            dialogContent = null;
        });

        addLogDialog.show();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        recyclerView = null;
        progressBar = null;
        errorContainer = null;
        errorMessage = null;
        retryButton = null;
        emptyStateContainer = null;
        addLogBtn = null;
        addLogDialog = null;
        dialogProgressBar = null;
        dialogContent = null;
    }
}
