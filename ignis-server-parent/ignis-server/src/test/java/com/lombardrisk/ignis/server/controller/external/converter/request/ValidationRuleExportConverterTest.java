package com.lombardrisk.ignis.server.controller.external.converter.request;

import com.lombardrisk.ignis.api.rule.ValidationRuleSeverity;
import com.lombardrisk.ignis.api.rule.ValidationRuleType;
import com.lombardrisk.ignis.client.external.fixture.ExternalClient;
import com.lombardrisk.ignis.client.external.productconfig.export.ValidationRuleExport;
import com.lombardrisk.ignis.server.product.rule.view.ValidationRuleExportConverter;
import org.junit.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

public class ValidationRuleExportConverterTest {

    private final ValidationRuleExportConverter converter = new ValidationRuleExportConverter();

    @Test
    public void apply_SetsName() {
        ValidationRuleExport ruleView = ExternalClient.Populated.validationRuleExport()
                .name("name")
                .build();

        assertThat(converter.apply(ruleView).getName()).isEqualTo("name");
    }

    @Test
    public void apply_SetsVersion() {
        ValidationRuleExport ruleView = ExternalClient.Populated.validationRuleExport()
                .version(1)
                .build();

        assertThat(converter.apply(ruleView).getVersion()).isEqualTo(1);
    }

    @Test
    public void apply_SetsDescription() {
        ValidationRuleExport ruleView = ExternalClient.Populated.validationRuleExport()
                .description("description")
                .build();

        assertThat(converter.apply(ruleView).getDescription()).isEqualTo("description");
    }

    @Test
    public void apply_SetsRuleId() {
        ValidationRuleExport ruleView = ExternalClient.Populated.validationRuleExport()
                .ruleId("ruleId")
                .build();

        assertThat(converter.apply(ruleView).getRuleId()).isEqualTo("ruleId");
    }

    @Test
    public void apply_SetsExpression() {
        ValidationRuleExport ruleView = ExternalClient.Populated.validationRuleExport()
                .expression("expression")
                .build();

        assertThat(converter.apply(ruleView).getExpression()).isEqualTo("expression");
    }

    @Test
    public void apply_SetsStartDate() {
        LocalDate startDate = LocalDate.of(1991, 1, 1);
        ValidationRuleExport ruleView = ExternalClient.Populated.validationRuleExport()
                .startDate(startDate)
                .build();

        assertThat(converter.apply(ruleView).getStartDate())
                .isEqualTo(startDate);
    }

    @Test
    public void apply_SetsEndDate() {
        LocalDate endDate = LocalDate.of(1992, 1, 1);
        ValidationRuleExport ruleView = ExternalClient.Populated.validationRuleExport()
                .endDate(endDate)
                .build();

        assertThat(converter.apply(ruleView).getEndDate())
                .isEqualTo(endDate);
    }

    @Test
    public void apply_SetsType() {
        ValidationRuleExport ruleView = ExternalClient.Populated.validationRuleExport()
                .validationRuleType(ValidationRuleExport.Type.QUALITY)
                .build();

        assertThat(converter.apply(ruleView).getValidationRuleType()).isEqualTo(ValidationRuleType.QUALITY);
    }

    @Test
    public void apply_SetsSeverity() {
        ValidationRuleExport ruleView = ExternalClient.Populated.validationRuleExport()
                .validationRuleSeverity(ValidationRuleExport.Severity.WARNING)
                .build();

        assertThat(converter.apply(ruleView).getValidationRuleSeverity())
                .isEqualTo(ValidationRuleSeverity.WARNING);
    }

    @Test
    public void apply_DoesNotSetId() {
        ValidationRuleExport ruleView = ExternalClient.Populated.validationRuleExport()
                .id(1001012L)
                .build();

        assertThat(converter.apply(ruleView).getId())
                .isNull();
    }
}
