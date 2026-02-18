package com.bit.logbook.feature.logManagement.presentation.trash;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bit.logbook.R;
import com.bit.logbook.feature.logManagement.domain.entity.Log;
import com.bit.logbook.feature.logManagement.presentation.LogDeletionState;
import com.bit.logbook.feature.logManagement.presentation.LogState;
import com.bit.logbook.feature.logManagement.presentation.LogUtils.LogDialogsUtils;
import com.bit.logbook.feature.logManagement.presentation.LogsViewModel;
import com.bit.logbook.feature.logManagement.presentation.today.LogCreationState;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;
import java.util.stream.Collectors;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class TrashFragment extends Fragment {
    private LogsViewModel viewModel;
    private TrashLogAdapter adapter;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private LinearLayout errorContainer;
    private TextView errorMessage;
    private Button retryButton;
    private LinearLayout emptyStateContainer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_trash, container, false);

        viewModel = new ViewModelProvider(this).get(LogsViewModel.class);
        recyclerView = rootView.findViewById(R.id.recycler_view_trash);
        progressBar = rootView.findViewById(R.id.progress_bar);
        errorContainer = rootView.findViewById(R.id.error_container);
        errorMessage = rootView.findViewById(R.id.error_message);
        retryButton = rootView.findViewById(R.id.retry_button);
        emptyStateContainer = rootView.findViewById(R.id.empty_state_container);

        setupRecyclerView();

        retryButton.setOnClickListener(v -> viewModel.getLogs(null, true));

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        observeViewModel();

        viewModel.getLogs(null, true);

        // Add menu provider
        requireActivity().addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menu.clear();
                menuInflater.inflate(R.menu.trash_options_menu, menu);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                int id = menuItem.getItemId();

                if (id == R.id.menu_restore_all) {
                    if (adapter.getItemCount() > 0) {
                        showRestoreAllConfirmationDialog();
                    } else {
                        Toast.makeText(requireContext(), R.string.no_logs_found, Toast.LENGTH_SHORT).show();
                    }
                    return true;
                } else if (id == R.id.menu_refresh_trash) {
                    viewModel.getLogs(null, true);
                } else if (id == R.id.menu_empty_trash) {
                    if (adapter.getItemCount() > 0) {
                        showEmptyTrashConfirmationDialog();
                    } else {
                        Toast.makeText(requireContext(), R.string.no_logs_found, Toast.LENGTH_SHORT).show();
                    }
                    return true;
                }

                return false;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
    }

    private void setupRecyclerView() {
        adapter = new TrashLogAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        adapter.setOnLogClickListener((log, position) -> LogDialogsUtils.showLogDetailsBottomSheet(requireContext(), log));

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
            renderLoading();
        } else if (state.getError() != null) {
            // Restore original state if there is an error
            viewModel.getLogs(null, true);
            Toast.makeText(requireContext(), state.getError(), Toast.LENGTH_SHORT).show();
            viewModel.resetLogCreationState();
        } else if (state.getLog() == null) {
            viewModel.getLogs(null, true);
            Toast.makeText(requireContext(), R.string.log_restored, Toast.LENGTH_SHORT).show();
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
            viewModel.getLogs(null, true);
            Toast.makeText(requireContext(), R.string.log_deleted, Toast.LENGTH_SHORT).show();
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
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyStateContainer.setVisibility(View.GONE);
            adapter.setLogs(logs);
        }
    }


    private void showPopupMenu(View view, Log log, int position) {
        PopupMenu popupMenu = new PopupMenu(requireContext(), view);
        popupMenu.getMenuInflater().inflate(R.menu.trash_popup_menu, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.menu_restore) {
                viewModel.restoreLogs(List.of(log.getId()));
                return true;
            } else if (itemId == R.id.menu_delete) {
                showDeleteConfirmationDialog(log);
                return true;
            }
            return false;
        });

        popupMenu.show();
    }

    private void showDeleteConfirmationDialog(Log log) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.delete_log_title)
                .setMessage(R.string.delete_log_confirmation)
                .setPositiveButton(R.string.delete, (dialog, which) -> viewModel.deleteLogs(List.of(log.getId())))
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showEmptyTrashConfirmationDialog() {
        List<String> logIds = adapter.getLogs().stream()
                .map(Log::getId)
                .collect(Collectors.toList());
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.empty_trash)
                .setMessage(R.string.empty_trash_confirmation)
                .setPositiveButton(R.string.empty_trash, (dialog, which) -> {
                    viewModel.deleteLogs(logIds);
                    adapter.clearLogs();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showRestoreAllConfirmationDialog() {
        List<String> logIds = adapter.getLogs().stream()
                .map(Log::getId)
                .collect(Collectors.toList());
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.restore_all)
                .setMessage(R.string.restore_all_confirmation)
                .setPositiveButton(R.string.restore_all, (dialog, which) -> {
                    viewModel.restoreLogs(logIds);
                    adapter.clearLogs();
                })
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
    }

}
