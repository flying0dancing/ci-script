package com.lombardrisk.ignis.spark.core.mock;

import okhttp3.Request;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.io.IOException;

public class RetrofitCall<T> implements Call<T> {

    private final T value;
    private final boolean success;
    private final ResponseBody responseBody;

    public RetrofitCall(final T value) {
        this.value = value;
        this.success = true;
        this.responseBody = null;
    }

    private RetrofitCall(final T value, final boolean success, final ResponseBody responseBody) {
        this.value = value;
        this.success = success;
        this.responseBody = responseBody;
    }

    public static RetrofitCall<Void> success() {
        return new RetrofitCall<>(null);
    }

    public static RetrofitCall<Void> failure(final ResponseBody responseBody) {
        return new RetrofitCall<>(null, false, responseBody);
    }

    @Override
    public Response<T> execute() {
        return success
                ? Response.success(value)
                : Response.error(500, responseBody);
    }

    @Override
    public void enqueue(final Callback<T> callback) {

    }

    @Override
    public boolean isExecuted() {
        return false;
    }

    @Override
    public void cancel() {

    }

    @Override
    public boolean isCanceled() {
        return false;
    }

    @Override
    public Call<T> clone() {
        return null;
    }

    @Override
    public Request request() {
        return null;
    }
}
