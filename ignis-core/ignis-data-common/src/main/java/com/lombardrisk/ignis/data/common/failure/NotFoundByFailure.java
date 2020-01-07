package com.lombardrisk.ignis.data.common.failure;

import com.google.common.collect.ImmutableMap;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import static java.util.stream.Collectors.joining;

@Data
@Builder(buildMethodName = "asFailure")
public class NotFoundByFailure implements CRUDFailure {

    private String subject;
    @Singular
    private ImmutableMap<String, Object> withs;

    @Override
    public Type getType() {
        return Type.NOT_FOUND;
    }

    @Override
    public String getErrorMessage() {
        return "Cannot find " + subject + " " +
                withs.entrySet()
                        .stream()
                        .map(condition -> "with " + condition.getKey() + " '" + condition.getValue() + "'")
                        .collect(joining(", "));
    }
}
