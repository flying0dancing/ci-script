package com.lombardrisk.ignis.client.core;

import okhttp3.Credentials;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;

public final class BasicAuthInterceptor implements Interceptor {

    private final String credentials;

    public BasicAuthInterceptor(final String username, final String password) {
        credentials = Credentials.basic(username, password, UTF_8);
    }

    @Override
    public Response intercept(final Chain chain) throws IOException {
        Request authenticatedRequest =
                chain.request()
                        .newBuilder()
                        .header("Authorization", credentials)
                        .build();
        return chain.proceed(authenticatedRequest);
    }
}