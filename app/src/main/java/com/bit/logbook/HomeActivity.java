package com.bit.logbook;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.bit.logbook.core.data.sync.SyncManager;
import com.bit.logbook.core.data.sync.SyncState;
import com.bit.logbook.core.presentation.BaseActivity;
import com.bit.logbook.core.utils.Constants;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class HomeActivity extends BaseActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private NavController navController;
    private MaterialToolbar toolbar;
    private AppBarConfiguration appBarConfiguration;
    private MaterialButton btnSettings;
    private TextView drawerUsername, drawerEmail;
    private SharedPreferences sharedPreferences;
    private ImageView syncIndicator;
    private SyncState currentSyncState;

    @Inject
    SyncManager syncManager;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_home;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    finish();
                }
            }
        });
    }

    @Override
    protected void initViews() {
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        toolbar = findViewById(R.id.toolbar);
        btnSettings = findViewById(R.id.btnSettings);
        syncIndicator = findViewById(R.id.syncIndicator);

        View headerView = navigationView.getHeaderView(0);
        drawerUsername = headerView.findViewById(R.id.drawer_header_username_tv);
        drawerEmail = headerView.findViewById(R.id.drawer_header_email_tv);

        sharedPreferences = getSharedPreferences(Constants.AUTH_PREFS_NAME, Context.MODE_PRIVATE);

        setupNavigation();
    }

    @Override
    protected void initViewModels() {

    }

    @Override
    protected void setupViews() {
        btnSettings.setOnClickListener(v -> drawerLayout.closeDrawer(GravityCompat.START));
        drawerUsername.setText(sharedPreferences.getString(Constants.KEY_USER_USERNAME, getString(R.string.username)));
        drawerEmail.setText(sharedPreferences.getString(Constants.KEY_USER_EMAIL, getString(R.string.email)));

        syncIndicator.setOnClickListener(v -> onSyncIndicatorClicked());
    }

    @Override
    protected void observeViewModel() {
        syncManager.getSyncState().observe(this, this::updateSyncIndicator);
    }

    private void updateSyncIndicator(SyncState state) {
        if (state == null) return;
        currentSyncState = state;

        switch (state.getStatus()) {
            case SYNCED:
                syncIndicator.setVisibility(View.VISIBLE);
                syncIndicator.setImageResource(R.drawable.ic_sync_synced);
                syncIndicator.setContentDescription(getString(R.string.sync_status_synced));
                break;
            case SYNCING:
                syncIndicator.setVisibility(View.VISIBLE);
                syncIndicator.setImageResource(R.drawable.ic_sync_pending);
                syncIndicator.setContentDescription(getString(R.string.sync_status_syncing));
                break;
            case PENDING:
                syncIndicator.setVisibility(View.VISIBLE);
                syncIndicator.setImageResource(R.drawable.ic_sync_pending);
                syncIndicator.setContentDescription(
                        getString(R.string.sync_status_pending_count, state.getPendingCount()));
                break;
            case ERROR:
                syncIndicator.setVisibility(View.VISIBLE);
                syncIndicator.setImageResource(R.drawable.ic_sync_error);
                syncIndicator.setContentDescription(getString(R.string.sync_status_error));
                break;
        }
    }

    private void onSyncIndicatorClicked() {
        if (currentSyncState == null) return;

        switch (currentSyncState.getStatus()) {
            case SYNCED:
                String lastSync = currentSyncState.getLastSyncTime();
                if (lastSync != null && !lastSync.isEmpty()) {
                    Toast.makeText(this,
                            getString(R.string.sync_summary_synced) + " · " +
                                    getString(R.string.sync_last, lastSync),
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, R.string.sync_summary_synced, Toast.LENGTH_SHORT).show();
                }
                break;
            case PENDING:
                Snackbar.make(toolbar,
                        getString(R.string.sync_summary_pending),
                        Snackbar.LENGTH_LONG)
                        .setAction(R.string.sync_now, v -> syncManager.syncNow())
                        .show();
                break;
            case SYNCING:
                Toast.makeText(this, R.string.sync_status_syncing, Toast.LENGTH_SHORT).show();
                break;
            case ERROR:
                Snackbar.make(toolbar,
                        currentSyncState.getMessage() != null
                                ? currentSyncState.getMessage()
                                : getString(R.string.sync_summary_error),
                        Snackbar.LENGTH_LONG)
                        .setAction(R.string.retry, v -> syncManager.syncNow())
                        .show();
                break;
        }
    }

    private void setupNavigation() {
        setSupportActionBar(toolbar);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.navHostFragment);
        assert navHostFragment != null;
        navController = navHostFragment.getNavController();

        appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.todayFragment, R.id.calendarFragment, R.id.trashFragment)
                .setOpenableLayout(drawerLayout)
                .build();

        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        NavigationUI.setupWithNavController(navigationView, navController);

        navigationView.setNavigationItemSelectedListener(item -> {
            boolean handled = NavigationUI.onNavDestinationSelected(item, navController);
            if (handled) {
                drawerLayout.closeDrawer(GravityCompat.START);
            }
            return handled;
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (syncManager != null) {
            syncManager.unregisterNetworkCallback();
        }
    }
}
