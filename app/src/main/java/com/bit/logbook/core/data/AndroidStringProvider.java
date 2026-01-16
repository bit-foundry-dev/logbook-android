package com.bit.logbook.core.data;

import android.content.Context;

import com.bit.logbook.core.domain.StringProvider;

import javax.inject.Inject;

import dagger.hilt.android.qualifiers.ApplicationContext;

public class AndroidStringProvider implements StringProvider {

    private final Context context;

    @Inject
    public AndroidStringProvider(@ApplicationContext Context context) {
        this.context = context;
    }

    @Override
    public String get(int resId) {
        return context.getString(resId);
    }
}