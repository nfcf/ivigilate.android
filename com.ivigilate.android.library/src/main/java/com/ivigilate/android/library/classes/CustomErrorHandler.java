package com.ivigilate.android.library.classes;


import android.content.Context;

import com.google.gson.Gson;
import com.ivigilate.android.library.R;

import retrofit.ErrorHandler;
import retrofit.RetrofitError;
import retrofit.mime.TypedByteArray;

public class CustomErrorHandler implements ErrorHandler {
    private final Context ctx;

    public CustomErrorHandler(Context ctx) {
        this.ctx = ctx;
    }

    @Override
    public Throwable handleError(RetrofitError cause) {
        String errorDescription;

        if (cause.isNetworkError()) {
            errorDescription = ctx.getString(R.string.error_network);
        } else {
            if (cause.getResponse() == null) {
                errorDescription = ctx.getString(R.string.error_no_response);
            } else {

                // Error message handling - return a simple error to Retrofit handlers..
                try {
                    errorDescription = new String(((TypedByteArray)cause.getResponse().getBody()).getBytes());
                } catch (Exception ex) {
                    try {
                        errorDescription = ctx.getString(R.string.error_network_http_error, cause.getResponse().getStatus());
                    } catch (Exception ex2) {
                        errorDescription = ctx.getString(R.string.error_unknown);
                    }
                }
            }
        }

        return new Exception(errorDescription);
    }
}