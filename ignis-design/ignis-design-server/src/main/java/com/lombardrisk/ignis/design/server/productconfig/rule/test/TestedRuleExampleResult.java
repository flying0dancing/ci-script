package com.lombardrisk.ignis.design.server.productconfig.rule.test;

import com.lombardrisk.ignis.client.external.rule.ExampleField;
import com.lombardrisk.ignis.client.external.rule.TestResultType;
import com.lombardrisk.ignis.design.field.model.Field;
import lombok.Builder;
import lombok.Value;

import java.util.Map;

@Value
@Builder
public class TestedRuleExampleResult {

    private final TestResultType testResultType;

    private final Map<Field, ExampleField> exampleFieldsByField;

    private final String unexpectedError;
}
