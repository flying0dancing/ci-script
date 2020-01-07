package com.lombardrisk.ignis.client.design.rule;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.lombardrisk.ignis.client.design.schema.field.FieldDto;
import com.lombardrisk.ignis.common.json.LocalDateDeserializer;
import com.lombardrisk.ignis.common.json.LocalDateSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@AllArgsConstructor
@Builder
@Data
public class RuleDto {

    private final Long id;
    private final String name;
    private final String ruleId;
    private final Type validationRuleType;
    private final Severity validationRuleSeverity;
    private final int version;

    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private final LocalDate startDate;

    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private final LocalDate endDate;
    private final String description;
    private final String expression;
    private final List<FieldDto> contextFields;

    public enum Severity {
        CRITICAL,
        WARNING
    }

    public enum Type {
        SYNTAX,
        QUALITY,
        VALIDITY,
        INTRA_SERIES
    }
}
