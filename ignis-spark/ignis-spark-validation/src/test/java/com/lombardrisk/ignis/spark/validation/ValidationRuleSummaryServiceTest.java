package com.lombardrisk.ignis.spark.validation;

import com.google.common.collect.ImmutableList;
import com.lombardrisk.ignis.client.internal.RuleSummaryRequest;
import com.lombardrisk.ignis.client.internal.InternalDatasetClient;
import com.lombardrisk.ignis.common.lang.ErrorMessage;
import com.lombardrisk.ignis.spark.core.mock.RetrofitCall;
import com.lombardrisk.ignis.spark.validation.fixture.Populated;
import io.vavr.control.Option;
import okhttp3.MediaType;
import okhttp3.ResponseBody;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;

import static com.lombardrisk.ignis.api.rule.SummaryStatus.ERROR;
import static com.lombardrisk.ignis.api.rule.SummaryStatus.SUCCESS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ValidationRuleSummaryServiceTest {

    @Rule
    public final JUnitSoftAssertions soft = new JUnitSoftAssertions();

    @Mock
    private InternalDatasetClient datasetClient;

    @Captor
    private ArgumentCaptor<List<RuleSummaryRequest>> summariesCaptor;

    @InjectMocks
    private ValidationRuleSummaryService validationRuleSummaryService;

    @Test
    public void save_DatasetRuleSummaries_CallsDatasetClientWithRequests() {
        List<DatasetRuleSummary> ruleSummaryList = ImmutableList.of(
                Populated.datasetRuleSummary().ruleId(1L).build(),
                Populated.datasetRuleSummary().ruleId(2L).build(),
                Populated.datasetRuleSummary().ruleId(3L).build(),
                Populated.datasetRuleSummary().ruleId(4L).build()
        );

        validationRuleSummaryService.createValidationResultsSummary(100L, ruleSummaryList);

        verify(datasetClient).createRuleSummaries(eq(100L), summariesCaptor.capture());

        List<RuleSummaryRequest> requests = summariesCaptor.getValue();
        soft.assertThat(requests)
                .extracting(RuleSummaryRequest::getRuleId)
                .containsExactlyInAnyOrder(1L, 2L, 3L, 4L);
    }

    @Test
    public void save_DatasetRuleSummariesSuccessAndErrors_CallsDatasetClientWithErrorRequests() {
        List<DatasetRuleSummary> ruleSummaryList = ImmutableList.of(
                Populated.datasetRuleSummary().build(),
                Populated.datasetRuleSummary().build(),
                Populated.datasetRuleSummary()
                        .failureMessage("OOPS")
                        .status(ERROR)
                        .build(),
                Populated.datasetRuleSummary().build()
        );

        validationRuleSummaryService.createValidationResultsSummary(100L, ruleSummaryList);

        verify(datasetClient).createRuleSummaries(anyLong(), summariesCaptor.capture());

        List<RuleSummaryRequest> requests = summariesCaptor.getValue();
        soft.assertThat(requests)
                .extracting(RuleSummaryRequest::getStatus)
                .containsExactlyInAnyOrder(SUCCESS, SUCCESS, ERROR, SUCCESS);
        soft.assertThat(requests)
                .extracting(RuleSummaryRequest::getErrorMessage)
                .containsExactlyInAnyOrder(null, null, "OOPS", null);
    }

    @Test
    public void save_DatasetRuleSummariesSuccessAndErrors_CallsDatasetClientWithFailures() {
        List<DatasetRuleSummary> ruleSummaryList = ImmutableList.of(
                Populated.datasetRuleSummary()
                        .validationRuleStatistics(Populated.validationRuleStatistics()
                                .numberOfFailures(230L)
                                .build())
                        .build()
        );

        validationRuleSummaryService.createValidationResultsSummary(100L, ruleSummaryList);

        verify(datasetClient).createRuleSummaries(anyLong(), summariesCaptor.capture());

        List<RuleSummaryRequest> requests = summariesCaptor.getValue();
        soft.assertThat(requests)
                .extracting(RuleSummaryRequest::getNumberOfFailures)
                .containsExactly(230L);
    }

    @Test
    public void save_DatasetRuleSummariesSuccessAndErrors_CallsDatasetClientWithErrors() {
        List<DatasetRuleSummary> ruleSummaryList = ImmutableList.of(
                Populated.datasetRuleSummary()
                        .validationRuleStatistics(Populated.validationRuleStatistics()
                                .numberOfErrors(920L)
                                .build())
                        .build()
        );

        validationRuleSummaryService.createValidationResultsSummary(100L, ruleSummaryList);

        verify(datasetClient).createRuleSummaries(anyLong(), summariesCaptor.capture());

        List<RuleSummaryRequest> requests = summariesCaptor.getValue();
        soft.assertThat(requests)
                .extracting(RuleSummaryRequest::getNumberOfErrors)
                .containsExactly(920L);
    }

    @Test
    public void save_DatasetClientRequestSuccessful_ReturnsEmptyOption() {
        List<DatasetRuleSummary> ruleSummaryList = ImmutableList.of(
                Populated.datasetRuleSummary().build()
        );

        when(datasetClient.createRuleSummaries(anyLong(), any()))
                .thenReturn(RetrofitCall.success());

        Option<ErrorMessage> errorMessage =
                validationRuleSummaryService.createValidationResultsSummary(100L, ruleSummaryList);

        assertThat(errorMessage.isEmpty())
                .isTrue();
    }

    @Test
    public void save_DatasetClientRequestUnsuccessful_ReturnsOptionWithErrorMessage() {
        List<DatasetRuleSummary> ruleSummaryList = ImmutableList.of(
                Populated.datasetRuleSummary().build()
        );

        when(datasetClient.createRuleSummaries(anyLong(), any()))
                .thenThrow(new RuntimeException("WHOOPS"));

        Option<ErrorMessage> errorMessage =
                validationRuleSummaryService.createValidationResultsSummary(100L, ruleSummaryList);

        assertThat(errorMessage.get())
                .isEqualTo(ErrorMessage.of("WHOOPS"));
    }

    @Test
    public void save_DatasetClientRequestUnSuccessful_ReturnsOptionWithHttpMessage() {
        List<DatasetRuleSummary> ruleSummaryList = ImmutableList.of(
                Populated.datasetRuleSummary().build()
        );

        ResponseBody responseBody = ResponseBody.create(MediaType.parse("application/json"), "DANGER".getBytes());

        when(datasetClient.createRuleSummaries(anyLong(), any()))
                .thenReturn(RetrofitCall.failure(responseBody));

        Option<ErrorMessage> errorMessage =
                validationRuleSummaryService.createValidationResultsSummary(100L, ruleSummaryList);

        assertThat(errorMessage.get())
                .isEqualTo(ErrorMessage.of("DANGER"));
    }
}