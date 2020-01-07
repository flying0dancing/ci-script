package com.lombardrisk.ignis.client.design.fixtures;

import com.lombardrisk.ignis.client.design.ValidationRuleRequest;
import com.lombardrisk.ignis.client.design.ValidationRuleSeverity;
import com.lombardrisk.ignis.client.design.ValidationRuleType;
import com.lombardrisk.ignis.client.design.schema.CopySchemaRequest;
import lombok.experimental.UtilityClass;

import java.time.LocalDate;

@UtilityClass
public class Populated {

    public static ValidationRuleRequest.ValidationRuleRequestBuilder validationRuleRequest() {
        return ValidationRuleRequest.builder()
                .name("Validation rule name")
                .description("Validation rule description")
                .expression("Validation rule expression")
                .ruleId("Validation regulator ID")
                .startDate(LocalDate.of(2000,1,31))
                .endDate(LocalDate.of(9999,12,31))
                .validationRuleSeverity(ValidationRuleSeverity.WARNING)
                .validationRuleType(ValidationRuleType.QUALITY)
                .version(1);
    }

    public static CopySchemaRequest.CopySchemaRequestBuilder copySchemaRequest() {
        return CopySchemaRequest.builder()
                .displayName("Copied A")
                .physicalTableName("COP_A")
                .startDate(LocalDate.of(2019, 1, 1))
                .endDate(LocalDate.of(2019, 9, 1));
    }

}
