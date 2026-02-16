package com.bit.logbook.feature.logManagement.presentation.today;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bit.logbook.R;
import com.bit.logbook.core.utils.DateUtils;
import com.bit.logbook.feature.logManagement.data.model.CreateLogRequest;
import com.bit.logbook.feature.logManagement.data.model.UpdateLogRequest;
import com.bit.logbook.feature.logManagement.domain.entity.Log;
import com.bit.logbook.feature.logManagement.presentation.LogAdapter;
import com.bit.logbook.feature.logManagement.presentation.LogDeletionState;
import com.bit.logbook.feature.logManagement.presentation.LogState;
import com.bit.logbook.feature.logManagement.presentation.LogUtils.LogDialogsUtils;
import com.bit.logbook.feature.logManagement.presentation.LogsViewModel;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointBackward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class TodayFragment extends Fragment {

    private LogsViewModel viewModel;
    private LogAdapter adapter;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private LinearLayout errorContainer;
    private TextView errorMessage, emptyText;
    private Button retryButton;
    private LinearLayout emptyStateContainer;
    private LocalDateTime selectedTime;
    private AlertDialog addLogDialog;
    private ProgressBar dialogProgressBar;
    private View dialogContent;
    private boolean isEdit = false;

    private final LocalDate startDate;

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
        emptyText = rootView.findViewById(R.id.empty_text);
        retryButton = rootView.findViewById(R.id.retry_button);
        emptyStateContainer = rootView.findViewById(R.id.empty_state_container);

        setupRecyclerView();

        retryButton.setOnClickListener(v -> viewModel.getLogs(startDate, false));

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        observeViewModel();

        // Load logs for today
        viewModel.getLogs(startDate, false);

        // Add menu provider
        requireActivity().addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menu.clear();
                menuInflater.inflate(R.menu.log_options_menu, menu);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                int id = menuItem.getItemId();

                if (id == R.id.menu_add_log) {
                    showAddLogDialog(false, null);
                    return true;
                } else if (id == R.id.menu_refresh_logs) {
                    viewModel.getLogs(startDate, false);
                } else if (id == R.id.menu_move_all_to_trash) {
                    Toast.makeText(requireContext(), "All to trash", Toast.LENGTH_SHORT).show();
                    return true;
                }

                return false;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
    }

    private void setupRecyclerView() {
        adapter = new LogAdapter(requireContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        adapter.setOnLogClickListener((log, position) -> {
            Toast.makeText(requireContext(), "position: " + position, Toast.LENGTH_SHORT).show();
            LogDialogsUtils.showLogDetailsBottomSheet(requireContext(), log);
        });

        adapter.setOnLogLongClickListener(this::showPopupMenu);
    }

    private void observeViewModel() {
        viewModel.getLogState().observe(getViewLifecycleOwner(), this::handleLogListState);
        viewModel.getLogCreationState().observe(getViewLifecycleOwner(), this::handleLogCreationState);
        viewModel.getLogDeletionState().observe(getViewLifecycleOwner(), this::handleLogDeletionState);
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
            viewModel.getLogs(startDate, false);
            if (isEdit) {
                Toast.makeText(requireContext(), R.string.update_log_successful, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), R.string.create_log_successful, Toast.LENGTH_SHORT).show();
            }
            viewModel.resetLogCreationState();
        }
    }

    private void handleLogDeletionState(LogDeletionState state) {
        if (state == null) return;

        if (state.isLoading()) {
            renderLoading();
        } else if (state.getError() != null) {
            Toast.makeText(requireContext(), state.getError(), Toast.LENGTH_SHORT).show();
            viewModel.resetLogDeletionState();
        } else if (state.isDeleted()) {
            viewModel.getLogs(startDate, false);
            Toast.makeText(requireContext(), R.string.log_moved_to_trash, Toast.LENGTH_SHORT).show();
            viewModel.resetLogDeletionState();
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
            emptyText.setText(startDate.isBefore(LocalDate.now()) ? R.string.no_logs_here : R.string.no_logs_today);
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
        builder.setTitle(editable ? R.string.update_log_title : R.string.add_log_title)
                .setCancelable(editable);

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
            timeEditText.setText(DateUtils.formatTime(log.getStartDate()));
            dateEditText.setText(DateUtils.formatFullDate(log.getStartDate().toLocalDate()));
            selectedTime = log.getStartDate();
        } else {
            timeEditText.setText(DateUtils.formatTime(selectedTime));
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

                timeEditText.setText(DateUtils.formatTime(selectedTime));
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
            if (!editable) {
                titleEditText.requestFocus();
                InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(titleEditText, InputMethodManager.SHOW_IMPLICIT);
            }
            Button button = addLogDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener(v -> {
                String title = titleEditText.getText().toString().trim();
                String description = descriptionEditText.getText().toString().trim();
                String tag = tagEditText.getText().toString().trim();

                if (!title.isEmpty()) {
                    if (editable && log != null) {
                        boolean isChanged = false;
                        UpdateLogRequest request = new UpdateLogRequest();
                        if (!title.equalsIgnoreCase(log.getTitle())) {
                            request.setTitle(title);
                            isChanged = true;
                        }
                        if (!description.equalsIgnoreCase(log.getDescription())) {
                            request.setDescription(description);
                            isChanged = true;
                        }
                        if (!tag.equalsIgnoreCase(log.getTag())) {
                            request.setTag(tag);
                            isChanged = true;
                        }
                        if (selectedTime != log.getStartDate()) {
                            request.setStartDate(selectedTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                            isChanged = true;
                        }
                        if (isChanged) {
                            viewModel.updateLog(request, log.getId());
                        } else {
                            addLogDialog.dismiss();
                        }
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
        Objects.requireNonNull(addLogDialog.getWindow()).setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }

    private void showPopupMenu(View view, Log log, int position) {
        PopupMenu popupMenu = new PopupMenu(requireContext(), view);
        popupMenu.getMenuInflater().inflate(R.menu.log_popup_menu, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.menu_edit) {
                showAddLogDialog(true, log);
                return true;
            } else if (itemId == R.id.menu_move_to_trash) {
                showMoveToTrashConfirmationDialog(log);
                return true;
            }
            return false;
        });

        popupMenu.show();
    }

    private void showMoveToTrashConfirmationDialog(Log log) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.move_to_trash_log_title)
                .setMessage(R.string.move_to_rash_log_confirmation)
                .setPositiveButton(R.string.move_to_trash, (dialog, which) -> viewModel.deleteLog(log.getId()))
                .setNegativeButton(R.string.cancel, null)
                .show();
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
        addLogDialog = null;
        dialogProgressBar = null;
        dialogContent = null;
    }
}
