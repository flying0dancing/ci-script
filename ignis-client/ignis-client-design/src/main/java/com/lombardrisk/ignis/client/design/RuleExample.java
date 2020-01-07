package com.lombardrisk.ignis.client.design;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.common.collect.ImmutableMap;
import com.lombardrisk.ignis.client.external.rule.ExampleField;
import com.lombardrisk.ignis.client.external.rule.TestResultType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Builder
@ToString
@EqualsAndHashCode
@AllArgsConstructor(onConstructor = @__({ @JsonCreator }))
public class RuleExample {

    public final Long id;

    @NotNull
    public final TestResultType expectedResult;

    public final TestResultType actualResult;

    @Valid
    @NotNull
    public final ImmutableMap<String, ExampleField> exampleFields;

    public final String unexpectedError;
}
