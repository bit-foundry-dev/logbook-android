package com.bit.logbook.feature.auth.data.source.remote;

import com.bit.logbook.feature.auth.data.model.AuthResponse;
import com.bit.logbook.feature.auth.data.model.ForgotPasswordRequest;
import com.bit.logbook.feature.auth.data.model.LoginRequest;
import com.bit.logbook.feature.auth.data.model.RegisterRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface AuthApiService {

    @POST("auth/login")
    Call<AuthResponse> login(@Body LoginRequest request);

    @POST("auth/register")
    Call<Void> register(@Body RegisterRequest request);

    @POST("auth/forgot-password")
    Call<AuthResponse> forgotPassword(@Body ForgotPasswordRequest request);

    @POST("auth/resend-verification")
    Call<AuthResponse> resendVerification(@Query("email") String email);
}