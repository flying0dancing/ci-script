package com.lombardrisk.ignis.client.external.productconfig.view;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.LinkedHashSet;
import java.util.Set;

@AllArgsConstructor(onConstructor = @__({ @JsonCreator }))
@Builder
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ValidationRuleExampleView {

    private Long id;
    private Long validationRuleId;
    private TestResult expectedResult;
    private Set<ValidationRuleExampleFieldView> validationRuleExampleFields = new LinkedHashSet<>();
}
