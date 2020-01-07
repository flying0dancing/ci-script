package com.lombardrisk.ignis.functional.test.assertions.expected;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Builder(builderMethodName = "expected")
@Getter
public class ExpectedResultsDetails {

    private final Map<String, Class<?>> schema;
    private final ExpectedResultDetailsData data;
}
