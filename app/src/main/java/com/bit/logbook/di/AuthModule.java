package com.bit.logbook.di;

import com.bit.logbook.feature.auth.data.repository.AuthRepositoryImpl;
import com.bit.logbook.feature.auth.data.source.remote.AuthApiService;
import com.bit.logbook.feature.auth.domain.repository.AuthRepository;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import retrofit2.Retrofit;

@Module
@InstallIn(SingletonComponent.class)
public class AuthModule {

    @Provides
    @Singleton
    public AuthApiService provideAuthApiService(Retrofit retrofit) {
        return retrofit.create(AuthApiService.class);
    }

    @Provides
    @Singleton
    public AuthRepository provideAuthRepository(AuthRepositoryImpl repository) {
        return repository;
    }
}
