package com.bit.logbook.core.presentation;

import androidx.lifecycle.ViewModel;

public abstract class BaseViewModel extends ViewModel {

    @Override
    protected void onCleared() {
        super.onCleared();
        // Clean up any resources if needed
    }
}
