package com.lombardrisk.ignis.server.dataset.rule;

import com.google.common.collect.ImmutableList;
import com.lombardrisk.ignis.api.rule.SummaryStatus;
import com.lombardrisk.ignis.client.internal.RuleSummaryRequest;
import com.lombardrisk.ignis.common.time.TimeSource;
import com.lombardrisk.ignis.data.common.failure.CRUDFailure;
import com.lombardrisk.ignis.server.dataset.fixture.DatasetPopulated;
import com.lombardrisk.ignis.server.dataset.model.Dataset;
import com.lombardrisk.ignis.server.dataset.rule.model.ValidationResultsSummary;
import com.lombardrisk.ignis.server.product.fixture.ProductPopulated;
import com.lombardrisk.ignis.server.product.rule.ValidationRuleRepository;
import com.lombardrisk.ignis.server.product.rule.model.ValidationRule;
import io.vavr.control.Either;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.Instant;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ValidationResultsSummaryServiceTest {

    @Rule
    public JUnitSoftAssertions soft = new JUnitSoftAssertions();

    @Mock
    private ValidationRuleRepository validationRuleRepository;

    @Mock
    private ValidationResultsSummaryRepository validationRuleSummaryRepository;

    @Mock
    private TimeSource timeSource;

    @Captor
    private ArgumentCaptor<Iterable<ValidationResultsSummary>> summaryCaptor;

    @InjectMocks
    private ValidationResultsSummaryService validationRuleSummaryService;

    @Before
    public void setUp() {
        when(timeSource.nowAsDate())
                .thenReturn(Date.from(Instant.now()));
    }

    @Test
    public void createSummaries_ValidationNotFound_ReturnsBadRequestResponse() {
        when(validationRuleRepository.findAllById(any()))
                .thenReturn(Arrays.asList(
                        ProductPopulated.validationRule()
                                .id(60002L)
                                .build(),
                        ProductPopulated.validationRule()
                                .id(60003L)
                                .build()
                ));

        List<RuleSummaryRequest> requests = Arrays.asList(
                DatasetPopulated.ruleSummaryRequest()
                        .ruleId(60002L)
                        .build(),
                DatasetPopulated.ruleSummaryRequest()
                        .ruleId(60003L)
                        .build(),
                DatasetPopulated.ruleSummaryRequest()
                        .ruleId(60004L)
                        .build()
        );

        Either<CRUDFailure, List<ValidationResultsSummary>> result =
                validationRuleSummaryService.create(null, requests);

        soft.assertThat(result.isLeft())
                .isTrue();
        soft.assertThat(result.getLeft().getType())
                .isEqualTo(CRUDFailure.Type.NOT_FOUND);
        soft.assertThat(result.getLeft().getErrorMessage())
                .isEqualTo("Could not find ValidationRule for ids [60004]");
    }

    @Test
    public void createSummaries_ValidationNotFound_CallsRepository() {
        when(validationRuleRepository.findAllById(any()))
                .thenReturn(singletonList(
                        ProductPopulated.validationRule()
                                .id(60002L)
                                .build()
                ));

        List<RuleSummaryRequest> requests = Arrays.asList(
                DatasetPopulated.ruleSummaryRequest()
                        .ruleId(60002L)
                        .build(),
                DatasetPopulated.ruleSummaryRequest()
                        .ruleId(60003L)
                        .build()
        );

        Dataset dataset = Dataset.builder().build();

        validationRuleSummaryService.create(dataset, requests);

        verify(validationRuleSummaryRepository).deleteAllByDataset(dataset);
    }

    @Test
    public void createSummaries_ValidationRules_CreateSummariesWithValidationRules() {
        ValidationRule rule1 = ProductPopulated.validationRule()
                .id(60001L)
                .build();
        ValidationRule rule2 = ProductPopulated.validationRule()
                .id(60002L)
                .build();

        when(validationRuleRepository.findAllById(any()))
                .thenReturn(Arrays.asList(rule1, rule2));

        List<RuleSummaryRequest> requests = Arrays.asList(
                DatasetPopulated.ruleSummaryRequest()
                        .ruleId(60001L)
                        .build(),
                DatasetPopulated.ruleSummaryRequest()
                        .ruleId(60002L)
                        .build()
        );

        validationRuleSummaryService.create(DatasetPopulated.dataset().build(), requests);

        verify(validationRuleSummaryRepository).saveAll(summaryCaptor.capture());

        Iterable<ValidationResultsSummary> summaries = summaryCaptor.getValue();

        assertThat(summaries)
                .extracting(ValidationResultsSummary::getValidationRule)
                .containsExactly(rule1, rule2);
    }

    @Test
    public void createSummaries_ValidationRules_DeletesOldRules() {
        Dataset dataset = DatasetPopulated.dataset().build();

        when(validationRuleRepository.findAllById(any()))
                .thenReturn(
                        singletonList(
                                ProductPopulated.validationRule()
                                        .id(60001L)
                                        .build()));

        List<RuleSummaryRequest> requests = singletonList(
                DatasetPopulated.ruleSummaryRequest()
                        .ruleId(60001L)
                        .build()
        );

        validationRuleSummaryService.create(dataset, requests);

        verify(validationRuleSummaryRepository).deleteAllByDataset(eq(dataset));
    }

    @Test
    public void createSummaries_ValidationRules_CreateSummariesWithDatasets() {
        Dataset dataset = DatasetPopulated.dataset().build();

        ValidationRule rule1 = ProductPopulated.validationRule()
                .id(60001L)
                .build();
        ValidationRule rule2 = ProductPopulated.validationRule()
                .id(60002L)
                .build();

        when(validationRuleRepository.findAllById(any()))
                .thenReturn(Arrays.asList(rule1, rule2));

        List<RuleSummaryRequest> requests = Arrays.asList(
                DatasetPopulated.ruleSummaryRequest()
                        .ruleId(60001L)
                        .build(),
                DatasetPopulated.ruleSummaryRequest()
                        .ruleId(60002L)
                        .build()
        );

        validationRuleSummaryService.create(dataset, requests);

        verify(validationRuleSummaryRepository).saveAll(summaryCaptor.capture());

        Iterable<ValidationResultsSummary> summaries = summaryCaptor.getValue();

        assertThat(summaries)
                .extracting(ValidationResultsSummary::getDataset)
                .containsExactly(dataset, dataset);
    }

    @Test
    public void createSummaries_ValidationRules_CreateSummariesWithCreatedTime() {
        when(validationRuleRepository.findAllById(any()))
                .thenReturn(singletonList(ProductPopulated.validationRule()
                        .id(60001L)
                        .build()));

        Date createdTime = new Calendar.Builder()
                .setDate(1991, 12, 7)
                .build()
                .getTime();

        when(timeSource.nowAsDate())
                .thenReturn(createdTime);

        List<RuleSummaryRequest> requests = singletonList(
                DatasetPopulated.ruleSummaryRequest()
                        .ruleId(60001L)
                        .build());

        validationRuleSummaryService.create(DatasetPopulated.dataset().build(), requests);

        verify(validationRuleSummaryRepository).saveAll(summaryCaptor.capture());

        Iterable<ValidationResultsSummary> summaries = summaryCaptor.getValue();

        assertThat(summaries)
                .extracting(ValidationResultsSummary::getCreatedTime)
                .containsExactly(createdTime);
    }

    @Test
    public void createSummaries_ValidationRulesIsFailure_CreatesSummaryWithNoFailures() {
        when(validationRuleRepository.findAllById(any()))
                .thenReturn(singletonList(ProductPopulated.validationRule()
                        .id(60001L)
                        .build()));

        List<RuleSummaryRequest> requests = singletonList(
                DatasetPopulated.ruleSummaryRequest()
                        .ruleId(60001L)
                        .numberOfFailures(null)
                        .build());

        validationRuleSummaryService.create(DatasetPopulated.dataset().build(), requests);

        verify(validationRuleSummaryRepository).saveAll(summaryCaptor.capture());

        Iterable<ValidationResultsSummary> summaries = summaryCaptor.getValue();

        assertThat(ImmutableList.copyOf(summaries).get(0).getNumberOfFailures())
                .isNull();
    }

    @Test
    public void createSummaries_ValidationRulesAllSuccessful_CreateSummariesWithRecordsAndFailures() {
        ValidationRule rule1 = ProductPopulated.validationRule()
                .id(60001L)
                .build();
        ValidationRule rule2 = ProductPopulated.validationRule()
                .id(60002L)
                .build();

        when(validationRuleRepository.findAllById(any()))
                .thenReturn(Arrays.asList(rule1, rule2));

        List<RuleSummaryRequest> requests = Arrays.asList(
                DatasetPopulated.ruleSummaryRequest()
                        .numberOfFailures(7000L)
                        .ruleId(60001L)
                        .build(),
                DatasetPopulated.ruleSummaryRequest()
                        .numberOfFailures(8000L)
                        .ruleId(60002L)
                        .build()
        );

        validationRuleSummaryService.create(DatasetPopulated.dataset().build(), requests);

        verify(validationRuleSummaryRepository).saveAll(summaryCaptor.capture());

        Iterable<ValidationResultsSummary> summaries = summaryCaptor.getValue();

        assertThat(summaries)
                .extracting(ValidationResultsSummary::getNumberOfFailures)
                .containsExactly(7000L, 8000L);
    }

    @Test
    public void createSummaries_ValidationRulesNumbersOfErrorsIsPresent_CreateSummaryWithNumberOfErrors() {
        ValidationRule rule1 = ProductPopulated.validationRule()
                .id(60001L)
                .build();
        when(validationRuleRepository.findAllById(any()))
                .thenReturn(Collections.singletonList(rule1));

        List<RuleSummaryRequest> requests = Collections.singletonList(
                DatasetPopulated.ruleSummaryRequest()
                        .numberOfErrors(8765L)
                        .ruleId(60001L)
                        .build()
        );

        validationRuleSummaryService.create(DatasetPopulated.dataset().build(), requests);

        verify(validationRuleSummaryRepository).saveAll(summaryCaptor.capture());

        Iterable<ValidationResultsSummary> summaries = summaryCaptor.getValue();

        assertThat(summaries)
                .extracting(ValidationResultsSummary::getNumberOfErrors)
                .containsExactly(8765L);
    }

    @Test
    public void createSummaries_ValidationRulesNumberOfErrorsIsNull_CreateSummaryWithZeroErrors() {
        ValidationRule rule1 = ProductPopulated.validationRule()
                .id(60001L)
                .build();
        when(validationRuleRepository.findAllById(any()))
                .thenReturn(Collections.singletonList(rule1));

        List<RuleSummaryRequest> requests = Collections.singletonList(
                DatasetPopulated.ruleSummaryRequest()
                        .ruleId(60001L)
                        .build()
        );

        validationRuleSummaryService.create(DatasetPopulated.dataset().build(), requests);

        verify(validationRuleSummaryRepository).saveAll(summaryCaptor.capture());

        Iterable<ValidationResultsSummary> summaries = summaryCaptor.getValue();

        assertThat(ImmutableList.copyOf(summaries).get(0).getNumberOfErrors())
                .isNull();
    }

    @Test
    public void createSummaries_ValidationRulesNumbersOfFailuresArePresent_CreateSummaryWithNumberOfFailures() {
        ValidationRule rule1 = ProductPopulated.validationRule()
                .id(60001L)
                .build();
        when(validationRuleRepository.findAllById(any()))
                .thenReturn(Collections.singletonList(rule1));

        List<RuleSummaryRequest> requests = Collections.singletonList(
                DatasetPopulated.ruleSummaryRequest()
                        .numberOfFailures(3465L)
                        .ruleId(60001L)
                        .build()
        );

        validationRuleSummaryService.create(DatasetPopulated.dataset().build(), requests);

        verify(validationRuleSummaryRepository).saveAll(summaryCaptor.capture());

        Iterable<ValidationResultsSummary> summaries = summaryCaptor.getValue();

        assertThat(summaries)
                .extracting(ValidationResultsSummary::getNumberOfFailures)
                .containsExactly(3465L);
    }

    @Test
    public void createSummaries_ValidationRulesNumberOfFailuresIsNull_CreateSummaryWithZeroFailures() {
        ValidationRule rule1 = ProductPopulated.validationRule()
                .id(60001L)
                .build();
        when(validationRuleRepository.findAllById(any()))
                .thenReturn(Collections.singletonList(rule1));

        List<RuleSummaryRequest> requests = Collections.singletonList(
                DatasetPopulated.ruleSummaryRequest()
                        .ruleId(60001L)
                        .numberOfFailures(null)
                        .build()
        );

        validationRuleSummaryService.create(DatasetPopulated.dataset().build(), requests);

        verify(validationRuleSummaryRepository).saveAll(summaryCaptor.capture());

        Iterable<ValidationResultsSummary> summaries = summaryCaptor.getValue();

        assertThat(ImmutableList.copyOf(summaries).get(0).getNumberOfFailures())
                .isNull();
    }

    @Test
    public void createSummaries_OneSuccessfulAndOneFailedValidationRule_CreateSummariesWithSuccessAndFailure() {
        ValidationRule rule1 = ProductPopulated.validationRule()
                .id(60001L)
                .build();
        ValidationRule rule2 = ProductPopulated.validationRule()
                .id(60002L)
                .build();

        when(validationRuleRepository.findAllById(any()))
                .thenReturn(Arrays.asList(rule1, rule2));

        List<RuleSummaryRequest> requests = Arrays.asList(
                DatasetPopulated.ruleSummaryRequest()
                        .numberOfFailures(7000L)
                        .status(SummaryStatus.SUCCESS)
                        .ruleId(60001L)
                        .build(),
                DatasetPopulated.ruleSummaryRequest()
                        .status(SummaryStatus.ERROR)
                        .errorMessage("Rule threw an exception")
                        .ruleId(60002L)
                        .build()
        );

        validationRuleSummaryService.create(DatasetPopulated.dataset().build(), requests);

        verify(validationRuleSummaryRepository).saveAll(summaryCaptor.capture());

        Iterator<ValidationResultsSummary> summaries = summaryCaptor.getValue().iterator();

        ValidationResultsSummary firstSummary = summaries.next();
        ValidationResultsSummary secondSummary = summaries.next();

        soft.assertThat(firstSummary)
                .extracting(ValidationResultsSummary::getStatus, ValidationResultsSummary::getErrorMessage)
                .containsExactly(SummaryStatus.SUCCESS, null);
        soft.assertThat(secondSummary)
                .extracting(ValidationResultsSummary::getStatus, ValidationResultsSummary::getErrorMessage)
                .containsExactly(SummaryStatus.ERROR, "Rule threw an exception");
    }

    @SuppressWarnings("RedundantTypeArguments")
    @Test
    public void createSummaries_ValidationRules_ReturnsSuccess() {
        ValidationRule rule1 = ProductPopulated.validationRule()
                .id(60001L)
                .build();

        when(validationRuleRepository.findAllById(any()))
                .thenReturn(singletonList(rule1));

        ValidationResultsSummary summary = DatasetPopulated.validationRuleSummary().build();

        when(validationRuleSummaryRepository.saveAll(Mockito.<Iterable<ValidationResultsSummary>>any()))
                .thenReturn(singletonList(summary));

        List<RuleSummaryRequest> requests = singletonList(
                DatasetPopulated.ruleSummaryRequest()
                        .numberOfFailures(7000L)
                        .ruleId(60001L)
                        .build());

        Either<CRUDFailure, List<ValidationResultsSummary>> result = validationRuleSummaryService
                .create(DatasetPopulated.dataset().build(), requests);

        soft.assertThat(result.right().get())
                .containsExactly(summary);
    }

    @Test
    public void findByDatasetId_CallsRepository() {
        validationRuleSummaryService.findByDatasetId(1004L);

        verify(validationRuleSummaryRepository).findByDatasetId(1004L);
    }

    @Test
    public void findByDatasetId_ReturnsSummariesFromRepository() {
        ValidationResultsSummary validationResultsSummary = DatasetPopulated.validationRuleSummary().build();

        when(validationRuleSummaryRepository.findByDatasetId(anyLong()))
                .thenReturn(singletonList(validationResultsSummary));

        List<ValidationResultsSummary> summaries = validationRuleSummaryService.findByDatasetId(1004L);

        assertThat(summaries)
                .containsExactly(validationResultsSummary);
    }
}
