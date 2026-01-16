package com.bit.logbook.di;

import com.bit.logbook.core.data.AndroidStringProvider;
import com.bit.logbook.core.domain.StringProvider;

import dagger.Binds;
import dagger.Module;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public abstract class StringModule {

    @Binds
    public abstract StringProvider bindStringProvider(
            AndroidStringProvider impl
    );
}

