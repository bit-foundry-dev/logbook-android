package com.bit.logbook;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.bit.logbook.core.presentation.BaseActivity;
import com.bit.logbook.core.utils.Constants;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationView;

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
    }

    @Override
    protected void observeViewModel() {

    }

    private void setupNavigation() {
        setSupportActionBar(toolbar);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.navHostFragment);
        assert navHostFragment != null;
        navController = navHostFragment.getNavController();

        // Setup AppBarConfiguration with top-level destinations
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
}