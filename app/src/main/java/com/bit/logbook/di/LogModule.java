package com.bit.logbook.di;

import com.bit.logbook.feature.logManagement.data.repository.LogRepositoryImpl;
import com.bit.logbook.feature.logManagement.data.source.remote.LogApiService;
import com.bit.logbook.feature.logManagement.domain.repository.LogRepository;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import retrofit2.Retrofit;

@Module
@InstallIn(SingletonComponent.class)
public class LogModule {

    @Provides
    @Singleton
    public LogApiService provideLogApiService(Retrofit retrofit) {
        return retrofit.create(LogApiService.class);
    }

    @Provides
    @Singleton
    public LogRepository provideLogRepository(LogRepositoryImpl repository) {
        return repository;
    }
}
