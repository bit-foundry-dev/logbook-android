package com.bit.logbook.feature.logManagement.data.source.remote;

import com.bit.logbook.core.data.ApiResponse;
import com.bit.logbook.feature.logManagement.data.model.CreateLogRequest;
import com.bit.logbook.feature.logManagement.data.model.LogDto;
import com.bit.logbook.feature.logManagement.data.model.UpdateLogRequest;

import java.time.LocalDate;
import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface LogApiService {

    @GET("events")
    Call<ApiResponse<List<LogDto>>> getLogs(@Query("startDate") LocalDate startDate);

    @POST("events")
    Call<ApiResponse<LogDto>> createLog(@Body CreateLogRequest request);

    @PATCH("events/{id}")
    Call<ApiResponse<LogDto>> updateLog(@Body UpdateLogRequest request, @Path("id") String id);
}