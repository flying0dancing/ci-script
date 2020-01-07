package com.lombardrisk.ignis.server.controller.external.converter;

import com.lombardrisk.ignis.api.rule.SummaryStatus;
import com.lombardrisk.ignis.client.external.fixture.ExternalClient;
import com.lombardrisk.ignis.client.external.productconfig.export.ValidationRuleExport;
import com.lombardrisk.ignis.client.external.rule.ValidationRuleSummaryView;
import com.lombardrisk.ignis.server.dataset.fixture.DatasetPopulated;
import com.lombardrisk.ignis.server.dataset.rule.model.ValidationResultsSummary;
import com.lombardrisk.ignis.server.dataset.rule.view.ValidationResultsSummaryConverter;
import com.lombardrisk.ignis.server.product.fixture.ProductPopulated;
import com.lombardrisk.ignis.server.product.rule.model.ValidationRule;
import com.lombardrisk.ignis.server.product.rule.view.ValidationRuleConverter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static com.lombardrisk.ignis.client.external.rule.ValidationRuleSummaryView.SummaryStatus.ERROR;
import static com.lombardrisk.ignis.client.external.rule.ValidationRuleSummaryView.SummaryStatus.FAIL;
import static com.lombardrisk.ignis.client.external.rule.ValidationRuleSummaryView.SummaryStatus.SUCCESS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ValidationResultsSummaryConverterTest {

    @Mock
    private ValidationRuleConverter validationRuleConverter;

    @InjectMocks
    private ValidationResultsSummaryConverter validationResultsSummaryConveter;

    @Before
    public void setUp() {
        when(validationRuleConverter.apply(any()))
                .thenReturn(ExternalClient.Populated.validationRuleExport().build());
    }

    @Test
    public void apply_CallsRuleConverter() {
        ValidationRule validationRule = ProductPopulated.validationRule()
                .build();

        ValidationResultsSummary resultsSummary = aSummary()
                .validationRule(validationRule)
                .build();
        validationResultsSummaryConveter.apply(resultsSummary);

        verify(validationRuleConverter).apply(validationRule);
    }

    @Test
    public void apply_SetsValidationView() {
        ValidationRuleExport validationRuleView = ExternalClient.Populated.validationRuleExport().build();

        when(validationRuleConverter.apply(any()))
                .thenReturn(validationRuleView);

        ValidationRuleSummaryView ruleSummaryView = validationResultsSummaryConveter.apply(aSummary().build());

        assertThat(ruleSummaryView.getValidationRule())
                .isEqualTo(validationRuleView);
    }

    @Test
    public void apply_SetsTotalNumberOfRecords() {
        ValidationResultsSummary resultsSummary = DatasetPopulated.validationRuleSummary()
                .dataset(DatasetPopulated.dataset()
                        .id(1L)
                        .recordsCount(102L)
                        .build())
                .build();

        ValidationRuleSummaryView ruleSummaryView = validationResultsSummaryConveter.apply(resultsSummary);

        assertThat(ruleSummaryView.getTotalRecords())
                .isEqualTo(102L);
    }

    @Test
    public void apply_SetsDatasetId() {
        ValidationResultsSummary resultsSummary = DatasetPopulated.validationRuleSummary()
                .dataset(DatasetPopulated.dataset().id(232L).build())
                .build();

        ValidationRuleSummaryView ruleSummaryView = validationResultsSummaryConveter.apply(resultsSummary);

        assertThat(ruleSummaryView.getDatasetId())
                .isEqualTo(232L);
    }

    @Test
    public void apply_SetsErrorMessage() {
        ValidationResultsSummary resultsSummary = aSummary()
                .errorMessage("ERROR")
                .build();

        ValidationRuleSummaryView ruleSummaryView = validationResultsSummaryConveter.apply(resultsSummary);

        assertThat(ruleSummaryView.getErrorMessage())
                .isEqualTo("ERROR");
    }

    @Test
    public void apply_SetsId() {
        ValidationResultsSummary resultsSummary = aSummary()
                .id(8092L)
                .build();

        ValidationRuleSummaryView ruleSummaryView = validationResultsSummaryConveter.apply(resultsSummary);

        assertThat(ruleSummaryView.getId())
                .isEqualTo(8092L);
    }

    @Test
    public void apply_SetsNumberOfFailures() {
        ValidationResultsSummary resultsSummary = aSummary()
                .numberOfFailures(4592L)
                .build();

        ValidationRuleSummaryView ruleSummaryView = validationResultsSummaryConveter.apply(resultsSummary);

        assertThat(ruleSummaryView.getNumberOfFailures())
                .isEqualTo(4592L);
    }

    @Test
    public void apply_SetsNumberOfErrors() {
        ValidationResultsSummary resultsSummary = aSummary()
                .numberOfErrors(43192L)
                .build();

        ValidationRuleSummaryView ruleSummaryView = validationResultsSummaryConveter.apply(resultsSummary);

        assertThat(ruleSummaryView.getNumberOfErrors())
                .isEqualTo(43192L);
    }

    @Test
    public void apply_Success_SetsSuccessStatus() {
        ValidationResultsSummary resultsSummary = aSummary()
                .status(SummaryStatus.SUCCESS)
                .build();

        ValidationRuleSummaryView ruleSummaryView = validationResultsSummaryConveter.apply(resultsSummary);

        assertThat(ruleSummaryView.getStatus())
                .isEqualTo(SUCCESS);
    }

    @Test
    public void apply_Fail_SetsFailStatus() {
        ValidationResultsSummary resultsSummary = aSummary()
                .status(SummaryStatus.FAIL)
                .build();

        ValidationRuleSummaryView ruleSummaryView = validationResultsSummaryConveter.apply(resultsSummary);

        assertThat(ruleSummaryView.getStatus())
                .isEqualTo(FAIL);
    }

    @Test
    public void apply_Error_SetsErrorStatus() {
        ValidationResultsSummary resultsSummary = aSummary()
                .status(SummaryStatus.ERROR)
                .build();

        ValidationRuleSummaryView ruleSummaryView = validationResultsSummaryConveter.apply(resultsSummary);

        assertThat(ruleSummaryView.getStatus())
                .isEqualTo(ERROR);
    }

    private ValidationResultsSummary.ValidationResultsSummaryBuilder aSummary() {
        return DatasetPopulated.validationRuleSummary()
                .dataset(DatasetPopulated.dataset().id(1L).build());
    }
}
