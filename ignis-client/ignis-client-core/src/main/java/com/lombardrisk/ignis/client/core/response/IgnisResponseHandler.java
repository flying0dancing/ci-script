package com.lombardrisk.ignis.client.core.response;

import lombok.extern.slf4j.Slf4j;
import okhttp3.ResponseBody;
import retrofit2.Response;

import java.io.IOException;

@Slf4j
public class IgnisResponseHandler {

    private final ErrorMapper errorMapper;

    public IgnisResponseHandler(final ErrorMapper errorMapper) {
        this.errorMapper = errorMapper;
    }

    public <R> IgnisResponse<R> handleResponse(final Response<R> response) throws IOException {

        boolean successful = response.isSuccessful();
        R body = response.body();

        if (successful) {
            return IgnisResponse.success(body);
        }

        ResponseBody responseBody = response.errorBody();
        if (responseBody == null) {
            handleNullBody(response);
        }

        return IgnisResponse.error(errorMapper.convertToErrorCodes(responseBody));
    }

    private <R> void handleNullBody(final Response<R> response) throws IOException {
        throw new IOException("Response from Ignis server returned null, " + response);
    }
}
