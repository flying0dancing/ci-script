package com.lombardrisk.ignis.functional.test.steps;

import com.lombardrisk.ignis.client.core.response.IgnisResponse;
import com.machinezoo.noexception.throwing.ThrowingSupplier;
import io.vavr.control.Try;
import junit.framework.AssertionFailedError;
import okhttp3.Request;
import okhttp3.ResponseBody;
import org.awaitility.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;
import java.util.function.Function;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.awaitility.Awaitility.await;

public final class CallAssertion {

    private static final Logger LOGGER = LoggerFactory.getLogger(CallAssertion.class);

    private CallAssertion() {
    }

    public static <T> T callAndExpectSuccess(final Call<T> call) {
        Try<Response<T>> tryCall = Try.of(call::execute);

        if (tryCall.isFailure()) {
            Request request = call.request();
            fail(String.format("Failed to call %s on %s", request.method(), request.url()), tryCall.getCause());
        }

        Response<T> response = tryCall.get();

        try {
            ResponseBody errorBody = response.errorBody();
            assertThat(response.isSuccessful())
                    .as("%s %n  %s", response, errorBody != null ? errorBody.string() : EMPTY)
                    .isTrue();
        } catch (IOException e) {
            throw new AssertionError(e.getMessage(), e);
        }
        return response.body();
    }

    public static <T> void callAndWaitForResult(
            final Duration duration,
            final Duration pollInterval,
            final Call<T> call,
            final Function<T, Boolean> condition) {

        String alias = call.request().method() + " " + call.request().url();

        await(alias)
                .atMost(duration)
                .pollInterval(pollInterval)
                .until(() -> Try.of(() -> call.clone().execute())
                        .map(response -> response.isSuccessful() && condition.apply(response.body()))
                        .getOrElse(false));
    }

    public static <T> T callAndExpectSuccess(final ThrowingSupplier<IgnisResponse<T>> call) {
        try {
            IgnisResponse<T> ignisResponse = call.get();

            assertThat(ignisResponse.isSuccess())
                    .as("%s %n  %s", ignisResponse, ignisResponse.getErrorCodes())
                    .isTrue();

            return ignisResponse.getValue();
        } catch (Exception e) {
            LOGGER.error("IOException occurred executing call", e);
            throw new AssertionFailedError("IOException occurred executing call");
        }
    }
}
