package com.bit.logbook.feature.logManagement.presentation;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bit.logbook.R;

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

        setupRecyclerView();

        retryButton.setOnClickListener(v -> viewModel.getLogs(null));

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

        // Optional: Set click listener
        adapter.setOnLogClickListener((log, position) -> {
            Toast.makeText(requireContext(),
                    "Clicked: " + log.getTitle(),
                    Toast.LENGTH_SHORT).show();
        });
    }

    private void observeViewModel() {
        viewModel.getLogState().observe(getViewLifecycleOwner(), state -> {
            if (state == null) return;

            if (state.isLoading()) {
                progressBar.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
                errorContainer.setVisibility(View.GONE);
                emptyStateContainer.setVisibility(View.GONE);
            } else if (state.getError() != null) {
                progressBar.setVisibility(View.GONE);
                recyclerView.setVisibility(View.GONE);
                errorContainer.setVisibility(View.VISIBLE);
                emptyStateContainer.setVisibility(View.GONE);
                errorMessage.setText(state.getError());
            } else if (state.getLogs() != null) {
                progressBar.setVisibility(View.GONE);
                errorContainer.setVisibility(View.GONE);

                if (state.getLogs().isEmpty()) {
                    recyclerView.setVisibility(View.GONE);
                    emptyStateContainer.setVisibility(View.VISIBLE);
                } else {
                    recyclerView.setVisibility(View.VISIBLE);
                    emptyStateContainer.setVisibility(View.GONE);
                    adapter.setLogs(state.getLogs());
                }
            }
        });
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