package com.bit.logbook.core.presentation;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewbinding.ViewBinding;

public abstract class BaseActivity<VB extends ViewBinding> extends AppCompatActivity {

    protected VB binding;

    protected abstract VB inflateBinding();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = inflateBinding();
        setContentView(binding.getRoot());
        setupViews();
        observeViewModel();
    }

    protected abstract void setupViews();

    protected abstract void observeViewModel();

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}

