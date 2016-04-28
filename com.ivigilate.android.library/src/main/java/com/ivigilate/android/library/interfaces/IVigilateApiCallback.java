package com.ivigilate.android.library.interfaces;

public interface IVigilateApiCallback<T> {

    /** Successful HTTP response. */
    void success(T data);

    /**
     * Unsuccessful HTTP response due to network failure, non-2XX status code, or unexpected
     * exception.
     */
    void failure(String errorMsg);

}
