package com.lombardrisk.ignis.api.rule;

import java.util.Arrays;

public enum TestResult {
    PASS,
    FAIL,
    ERROR;

    public static TestResult toTestResult(final String name) {
        return Arrays.stream(TestResult.values())
                .filter(testResult -> testResult.name().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }
}
