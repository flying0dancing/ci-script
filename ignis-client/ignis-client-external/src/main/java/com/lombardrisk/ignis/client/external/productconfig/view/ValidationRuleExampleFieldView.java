package com.lombardrisk.ignis.client.external.productconfig.view;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@AllArgsConstructor(onConstructor = @__({ @JsonCreator }))
@Builder
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ValidationRuleExampleFieldView {
    private Long id;
    private Long validationRuleExampleId;
    private String name;
    private String value;
}
