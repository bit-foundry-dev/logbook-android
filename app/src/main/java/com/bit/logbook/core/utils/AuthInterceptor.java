package com.bit.logbook.core.utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.bit.logbook.feature.auth.presentation.login.LoginActivity;

import java.io.IOException;

import javax.inject.Inject;

import dagger.hilt.android.qualifiers.ApplicationContext;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {

    private final Context context;
    private final SharedPreferences sharedPreferences;

    @Inject
    public AuthInterceptor(@ApplicationContext Context context) {
        this.context = context;
        this.sharedPreferences = context.getSharedPreferences(
                Constants.AUTH_PREFS_NAME,
                Context.MODE_PRIVATE
        );
    }

    @NonNull
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();
        String url = originalRequest.url().toString();

        if (isAuthEndpoint(url)) {
            return chain.proceed(originalRequest);
        }

        String token = sharedPreferences.getString(Constants.KEY_USER_TOKEN, null);

        assert token != null;
        Request newRequest = originalRequest.newBuilder()
                .header("Authorization", token)
                .build();

        Response response = chain.proceed(newRequest);

        if (response.code() == 401) {
            // Clear user data
            sharedPreferences.edit().clear().apply();

            // Navigate to login screen
            Intent intent = new Intent(context, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(intent);
        }

        return response;
    }

    private boolean isAuthEndpoint(String url) {
        return url.contains("/auth/");
    }
}