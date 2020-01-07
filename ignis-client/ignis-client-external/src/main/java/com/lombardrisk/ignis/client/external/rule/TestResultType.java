package com.lombardrisk.ignis.client.external.rule;

import java.util.Arrays;

@SuppressWarnings("squid:S00115")
public enum TestResultType {
    Pass,
    Fail,
    Error;

    public static TestResultType toTestResultType(final String name) {
        return Arrays.stream(TestResultType.values())
                .filter(testResultType -> testResultType.name().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }
}
