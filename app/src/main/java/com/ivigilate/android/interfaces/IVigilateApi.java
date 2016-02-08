package com.ivigilate.android.interfaces;

import com.ivigilate.android.classes.Sighting;
import com.ivigilate.android.classes.User;

import java.util.List;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.POST;

public interface IVigilateApi {
    @POST("/api/v1/login/")
    void login(@Body User user, Callback<User> cb);

    @POST("/api/v1/addsightings/")
    void addSightings(@Body List<Sighting> sightings, Callback<String> cb);
}
