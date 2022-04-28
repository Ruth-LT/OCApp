package com.mds.ocapp.api;

import com.mds.ocapp.models.WResponse;

import retrofit2.Call;
import retrofit2.http.GET;

public interface DiCampoAPI {

    @GET("dicampo")
    Call<WResponse> getConnectionData();

    @GET("versions/dicampooc")
    Call<WResponse> getLastVersion();

}