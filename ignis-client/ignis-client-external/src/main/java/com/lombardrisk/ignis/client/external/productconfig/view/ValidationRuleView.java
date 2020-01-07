package com.lombardrisk.ignis.client.external.productconfig.view;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.lombardrisk.ignis.client.external.productconfig.export.ValidationRuleExport;
import com.lombardrisk.ignis.common.json.LocalDateDeserializer;
import com.lombardrisk.ignis.common.json.LocalDateSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.Set;

@AllArgsConstructor(onConstructor = @__({ @JsonCreator }))
@Builder
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ValidationRuleView {
    private final Long id;
    private final String name;
    private final String ruleId;
    private final int version;
    private final String description;
    private final String expression;
    private final ValidationRuleExport.Type validationRuleType;
    private final ValidationRuleExport.Severity validationRuleSeverity;

    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private final LocalDate startDate;

    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private final LocalDate endDate;

    private final Set<FieldView> contextFields;
    private final Set<ValidationRuleExampleView> validationRuleExamples;
}
