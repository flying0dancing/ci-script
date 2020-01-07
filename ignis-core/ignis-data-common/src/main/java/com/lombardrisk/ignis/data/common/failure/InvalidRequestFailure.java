package com.lombardrisk.ignis.data.common.failure;

import com.google.common.collect.ImmutableMap;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import static java.util.stream.Collectors.joining;

@Data
@Builder(buildMethodName = "asFailure")
public class InvalidRequestFailure implements CRUDFailure {

    /**
     * <p>key - param name</p>
     * <p>value - error message</p>
     */
    @Singular
    private final ImmutableMap<String, String> paramErrors;

    @Override
    public Type getType() {
        return Type.INVALID_REQUEST_PARAMETERS;
    }

    @Override
    public String getErrorMessage() {
        return "Invalid request: " +
                paramErrors.entrySet()
                        .stream()
                        .map(entry -> entry.getKey() + " - " + entry.getValue())
                        .collect(joining(", "));
    }
}
