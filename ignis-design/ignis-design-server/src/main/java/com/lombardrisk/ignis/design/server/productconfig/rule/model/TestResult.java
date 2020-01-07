package com.lombardrisk.ignis.design.server.productconfig.rule.model;

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
