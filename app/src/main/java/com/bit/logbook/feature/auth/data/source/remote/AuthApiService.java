package com.bit.logbook.feature.auth.data.source.remote;

import com.bit.logbook.core.data.ApiResponse;
import com.bit.logbook.feature.auth.data.model.ForgotPasswordRequest;
import com.bit.logbook.feature.auth.data.model.LoginRequest;
import com.bit.logbook.feature.auth.data.model.RegisterRequest;
import com.bit.logbook.feature.auth.data.model.UserDto;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface AuthApiService {

    @POST("auth/login")
    Call<ApiResponse<UserDto>> login(@Body LoginRequest request);

    @POST("auth/register")
    Call<Void> register(@Body RegisterRequest request);

    @POST("auth/forgot-password")
    Call<ApiResponse<UserDto>> forgotPassword(@Body ForgotPasswordRequest request);

    @POST("auth/resend-verification")
    Call<ApiResponse<UserDto>> resendVerification(@Query("email") String email);
}