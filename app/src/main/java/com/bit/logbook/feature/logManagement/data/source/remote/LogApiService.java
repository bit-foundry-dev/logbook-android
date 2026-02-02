package com.bit.logbook.feature.logManagement.data.source.remote;

import com.bit.logbook.core.data.ApiResponse;
import com.bit.logbook.feature.logManagement.data.model.LogDto;

import java.time.LocalDate;
import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface LogApiService {

    @GET("events")
    Call<ApiResponse<List<LogDto>>> getLogs(@Query("startDate") LocalDate startDate);
}