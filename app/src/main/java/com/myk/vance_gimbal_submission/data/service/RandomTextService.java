package com.myk.vance_gimbal_submission.data.service;

import com.myk.vance_gimbal_submission.data.model.GibberishRemoteDataModel;

import retrofit2.Call;
import retrofit2.http.GET;

/**
 * Endpoints
 */
public interface RandomTextService {
    @GET("gibberish/p-2/12-16")
    Call<GibberishRemoteDataModel> getGibberish();
}