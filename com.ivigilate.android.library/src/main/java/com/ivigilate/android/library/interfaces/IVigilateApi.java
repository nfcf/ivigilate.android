package com.ivigilate.android.library.interfaces;

import com.ivigilate.android.library.classes.AddSightingResponse;
import com.ivigilate.android.library.classes.ApiResponse;
import com.ivigilate.android.library.classes.RegisteredDevice;
import com.ivigilate.android.library.classes.DeviceProvisioning;
import com.ivigilate.android.library.classes.Sighting;
import com.ivigilate.android.library.classes.User;

import java.util.List;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;

public interface IVigilateApi {
    @POST("/api/v1/login/")
    void login(@Body User user, Callback<ApiResponse<User>> cb);

    @POST("/api/v1/logout/")
    void logout(Callback<ApiResponse<User>> cb);

    @POST("/api/v1/provisiondevice/") // this one requires login
    void provisionDevice(@Body DeviceProvisioning deviceProvisioning, Callback<ApiResponse<String>> cb);

    @POST("/api/v1/addsightings/") // this one does not require login
    void addSightings(@Body List<Sighting> sightings, Callback<ApiResponse<AddSightingResponse>> cb);

    @GET("/api/v1/beacons/")
    void getBeacons(Callback<List<RegisteredDevice>> cb);

    @GET("/api/v1/detectors/")
    void getDetectors(Callback<List<RegisteredDevice>> cb);
}
