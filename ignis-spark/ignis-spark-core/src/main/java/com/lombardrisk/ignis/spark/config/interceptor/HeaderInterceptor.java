package com.lombardrisk.ignis.spark.config.interceptor;

import com.google.common.collect.ImmutableMap;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.Map;

public class HeaderInterceptor implements Interceptor {

    private final Map<String, String> headers;

    public HeaderInterceptor(final Map<String, String> headers) {
        this.headers = headers;
    }

    public static HeaderInterceptor oneHeader(final String key, final String value) {
        return new HeaderInterceptor(ImmutableMap.of(key, value));
    }

    @Override
    public Response intercept(final Chain chain) throws IOException {
        Request.Builder request = chain.request().newBuilder();

        for (final Map.Entry<String, String> headerEntry : headers.entrySet()) {
            request.addHeader(headerEntry.getKey(), headerEntry.getValue());
        }

        return chain.proceed(request.build());
    }
}
