package com.lombardrisk.ignis.client.design;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.lombardrisk.ignis.common.json.LocalDateDeserializer;
import com.lombardrisk.ignis.common.json.LocalDateSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.Valid;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.Set;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class ValidationRuleRequest {

    private Long id;

    @NotBlank
    @Length(min = 1, max = 100)
    private String ruleId;

    @NotBlank
    @Length(min = 1, max = 100)
    private String name;

    @NotNull
    @Min(1)
    private Integer version;

    @NotNull
    private ValidationRuleType validationRuleType;

    @NotNull
    private ValidationRuleSeverity validationRuleSeverity;

    @Length(max = 2000)
    private String description;

    @NotNull
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate startDate;

    @NotNull
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate endDate;

    @NotBlank
    @Length(max = 1000)
    private String expression;

    @Valid
    private Set<RuleExample> ruleExamples;

    @SuppressWarnings("ConstantConditions")
    @AssertTrue(message = "Start Date must not be after End Date")
    public boolean hasDatesOrdered() {
        if (startDate != null && endDate != null) {
            return startDate.isBefore(endDate);
        }
        return true;
    }
}
