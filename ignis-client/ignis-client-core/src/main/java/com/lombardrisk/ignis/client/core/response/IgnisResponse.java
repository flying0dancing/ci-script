package com.lombardrisk.ignis.client.core.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

import static java.util.Collections.emptyList;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class IgnisResponse<T> {

    private final T value;
    private final List<ApiErrorCode> errorCodes;

    public static <V> IgnisResponse<V> success(final V value) {
        return new IgnisResponse<>(value, emptyList());
    }

    public static <V> IgnisResponse<V> error(final List<ApiErrorCode> errorCodes) {
        return new IgnisResponse<>(null, errorCodes);
    }

    public boolean isSuccess() {
        return errorCodes.isEmpty();
    }
}
