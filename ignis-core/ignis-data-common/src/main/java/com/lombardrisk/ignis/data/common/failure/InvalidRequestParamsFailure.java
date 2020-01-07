package com.lombardrisk.ignis.data.common.failure;

import io.vavr.Tuple2;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@AllArgsConstructor(access = AccessLevel.PACKAGE)
@Data
public class InvalidRequestParamsFailure<T> implements CRUDFailure {

    private final List<Tuple2<String, T>> invalidParameters;

    @Override
    public Type getType() {
        return Type.INVALID_REQUEST_PARAMETERS;
    }

    @Override
    public String getErrorMessage() {
        return "Supplied parameters are invalid " + invalidParameters;
    }
}
