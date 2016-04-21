package com.ivigilate.android.core.interfaces;

import com.ivigilate.android.core.classes.ApiResponse;
import com.ivigilate.android.core.classes.Device;
import com.ivigilate.android.core.classes.Sighting;
import com.ivigilate.android.core.classes.User;

import java.util.List;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.POST;

public interface IVigilateApi {
    @POST("/api/v2/login/")
    void login(@Body User user, Callback<ApiResponse<User>> cb);

    @POST("/api/v2/provisiondevice/") // this one requires login
    void provisionDevice(@Body Device device, Callback<ApiResponse<Void>> cb);

    @POST("/api/v2/addsightings/") // this one does not require login
    void addSightings(@Body List<Sighting> sightings, Callback<ApiResponse<Void>> cb);
}
