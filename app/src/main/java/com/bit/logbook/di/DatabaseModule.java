package com.bit.logbook.di;

import android.content.Context;

import com.bit.logbook.core.data.local.AppDatabase;
import com.bit.logbook.core.data.local.dao.LogDao;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class DatabaseModule {

    @Provides
    @Singleton
    public AppDatabase provideAppDatabase(@ApplicationContext Context context) {
        return AppDatabase.getInstance(context);
    }

    @Provides
    @Singleton
    public LogDao provideLogDao(AppDatabase appDatabase) {
        return appDatabase.logDao();
    }
}
