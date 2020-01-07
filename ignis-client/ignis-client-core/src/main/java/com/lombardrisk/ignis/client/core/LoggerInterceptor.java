package com.lombardrisk.ignis.client.core;

import lombok.extern.slf4j.Slf4j;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

@Slf4j
public class LoggerInterceptor implements Interceptor {

    @Override
    public Response intercept(final Chain chain) throws IOException {
        Request request = chain.request();
        log.debug("{} {}", request.method(), request.url());

        return chain.proceed(request);
    }
}
