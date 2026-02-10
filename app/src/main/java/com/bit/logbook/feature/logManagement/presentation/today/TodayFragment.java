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
import com.bit.logbook.core.utils.DateUtils;
import com.bit.logbook.feature.logManagement.data.model.CreateLogRequest;
import com.bit.logbook.feature.logManagement.data.model.UpdateLogRequest;
import com.bit.logbook.feature.logManagement.domain.entity.Log;
import com.bit.logbook.feature.logManagement.presentation.LogAdapter;
import com.bit.logbook.feature.logManagement.presentation.LogState;
import com.bit.logbook.feature.logManagement.presentation.LogsViewModel;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointBackward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
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
    private boolean isEdit = false;

    private LocalDate startDate;

    public TodayFragment(LocalDate startDate) {
        this.startDate = startDate;
        selectedTime = startDate == null ? LocalDateTime.now() : startDate.atTime(LocalTime.now());
    }

    public TodayFragment() {
        startDate = LocalDate.now();
        selectedTime = startDate == null ? LocalDateTime.now() : startDate.atTime(LocalTime.now());
    }

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

        retryButton.setOnClickListener(v -> viewModel.getLogs(startDate));
        addLogBtn.setOnClickListener(v -> showAddLogDialog(false, null));

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        observeViewModel();

        // Load logs for today
        viewModel.getLogs(startDate);
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

        adapter.setOnLogLongClickListener((log, position) -> showAddLogDialog(true, log));
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
            if (dialogContent != null) {
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
            if (dialogContent != null) {
                dialogContent.setVisibility(View.VISIBLE);
            }
            if (addLogDialog != null) {
                addLogDialog.setCancelable(true);
                addLogDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                addLogDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(true);
            }
            Toast.makeText(requireContext(), state.getError(), Toast.LENGTH_SHORT).show();
            viewModel.resetLogCreationState();
        } else if (state.getLog() != null) {
            if (addLogDialog != null && addLogDialog.isShowing()) {
                addLogDialog.dismiss();
            }
            viewModel.getLogs(startDate);
            if (isEdit) {
                Toast.makeText(requireContext(), R.string.update_log_successful, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), R.string.create_log_successful, Toast.LENGTH_SHORT).show();
            }
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

    private void showAddLogDialog(boolean editable, Log log) {
        isEdit = editable;
        selectedTime = startDate == null ? LocalDateTime.now() : startDate.atTime(LocalTime.now());
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        builder.setTitle(editable ? R.string.update_log_title : R.string.add_log_title);

        View view = getLayoutInflater().inflate(R.layout.dialog_add_log, null);
        builder.setView(view);

        final EditText titleEditText = view.findViewById(R.id.edit_text_title);
        final EditText descriptionEditText = view.findViewById(R.id.edit_text_description);
        final EditText tagEditText = view.findViewById(R.id.edit_text_tag);
        final EditText timeEditText = view.findViewById(R.id.edit_text_time);
        final EditText dateEditText = view.findViewById(R.id.edit_text_date);
        dialogProgressBar = view.findViewById(R.id.dialog_progress_bar);
        dialogContent = view.findViewById(R.id.dialog_content);

        if (editable && log != null) {
            titleEditText.setText(log.getTitle());
            descriptionEditText.setText(log.getDescription());
            tagEditText.setText(log.getTag());
            timeEditText.setText(log.getStartDate().format(DateTimeFormatter.ofPattern("HH:mm")));
            dateEditText.setText(DateUtils.formatFullDate(log.getStartDate().toLocalDate()));
            selectedTime = log.getStartDate();
        } else {
            timeEditText.setText(selectedTime.format(DateTimeFormatter.ofPattern("HH:mm")));
            dateEditText.setText(DateUtils.formatFullDate(selectedTime.toLocalDate()));
        }

        timeEditText.setOnClickListener(v -> {
            MaterialTimePicker timePicker = new MaterialTimePicker.Builder()
                    .setTimeFormat(TimeFormat.CLOCK_12H)
                    .setHour(selectedTime.getHour())
                    .setMinute(selectedTime.getMinute())
                    .setTitleText(R.string.select_time)
                    .build();

            timePicker.addOnPositiveButtonClickListener(dialog -> {
                selectedTime = selectedTime.withHour(timePicker.getHour()).withMinute(timePicker.getMinute());

                timeEditText.setText(selectedTime.format(DateTimeFormatter.ofPattern("HH:mm")));
            });

            timePicker.show(getChildFragmentManager(), "TIME_PICKER");
        });

        dateEditText.setOnClickListener(v -> {
            if (editable) {
                // Create a validator that only allows past and present dates
                CalendarConstraints.DateValidator dateValidator = DateValidatorPointBackward.now();

                CalendarConstraints constraints = new CalendarConstraints.Builder()
                        .setValidator(dateValidator)
                        .setEnd(MaterialDatePicker.todayInUtcMilliseconds())
                        .build();

                MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                        .setTitleText(R.string.select_date)
                        .setSelection(selectedTime.toLocalDate().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli())
                        .setCalendarConstraints(constraints)
                        .build();

                datePicker.addOnPositiveButtonClickListener(selection -> {
                    LocalDate newDate = Instant.ofEpochMilli(selection).atZone(ZoneOffset.UTC).toLocalDate();
                    selectedTime = selectedTime.withYear(newDate.getYear())
                            .withMonth(newDate.getMonthValue())
                            .withDayOfMonth(newDate.getDayOfMonth());
                    dateEditText.setText(DateUtils.formatFullDate(newDate));
                });

                datePicker.show(getChildFragmentManager(), "DATE_PICKER");
            }
        });

        builder.setPositiveButton(R.string.save, (dialog, which) -> {
            // This listener is intentionally left empty because we will override it later
            // to prevent the dialog from closing automatically.
        });

        builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.cancel());

        addLogDialog = builder.create();

        addLogDialog.setOnShowListener(dialogInterface -> {
            Button button = addLogDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener(v -> {
                String title = titleEditText.getText().toString();
                String description = descriptionEditText.getText().toString();
                String tag = tagEditText.getText().toString();

                if (!title.isEmpty()) {
                    if (editable && log != null) {
                        UpdateLogRequest request = new UpdateLogRequest();
                        if (!title.equalsIgnoreCase(log.getTitle())) {
                            request.setTitle(title);
                        }
                        if (!description.equalsIgnoreCase(log.getDescription())) {
                            request.setDescription(description);
                        }
                        if (!tag.equalsIgnoreCase(log.getTag())) {
                            request.setTag(tag);
                        }
                        if (selectedTime != log.getStartDate()){
                            request.setStartDate(selectedTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                        }
                        viewModel.updateLog(request, log.getId());
                    } else {
                        CreateLogRequest request = new CreateLogRequest(title, description, tag, selectedTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                        viewModel.createLog(request);
                    }
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
