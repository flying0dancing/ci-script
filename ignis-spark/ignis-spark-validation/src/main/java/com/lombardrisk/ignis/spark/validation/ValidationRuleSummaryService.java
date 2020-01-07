package com.lombardrisk.ignis.spark.validation;

import com.lombardrisk.ignis.client.internal.RuleSummaryRequest;
import com.lombardrisk.ignis.client.internal.InternalDatasetClient;
import com.lombardrisk.ignis.common.lang.ErrorMessage;
import io.vavr.control.Option;
import io.vavr.control.Try;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import retrofit2.Call;
import retrofit2.Response;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class ValidationRuleSummaryService {

    private final InternalDatasetClient datasetClient;

    @Autowired
    public ValidationRuleSummaryService(final InternalDatasetClient datasetClient) {
        this.datasetClient = datasetClient;
    }

    public Option<ErrorMessage> createValidationResultsSummary(
            final long datasetId,
            final List<DatasetRuleSummary> ruleList) {

        Try<Response<Void>> send = Try.of(() -> sendCreateRequest(datasetId, ruleList).execute());
        if (send.isFailure()) {
            return Option.of(send.getCause().getMessage())
                    .map(ErrorMessage::of);
        }

        if (send.get().isSuccessful()) {
            return Option.none();
        }

        Try<String> tryParseBody = Try.of(() ->
                Objects.requireNonNull(send.get().errorBody())
                        .string());

        if (tryParseBody.isSuccess()) {
            return Option.of(ErrorMessage.of(tryParseBody.get()));
        }

        return Option.of(ErrorMessage.of(tryParseBody.getCause()));
    }

    private Call<Void> sendCreateRequest(final long datasetId, final List<DatasetRuleSummary> ruleList) {
        return datasetClient.createRuleSummaries(
                datasetId,
                ruleList.stream()
                        .map(this::convert)
                        .collect(Collectors.toList()));
    }

    private RuleSummaryRequest convert(final DatasetRuleSummary datasetRuleSummary) {
        Option<String> failureMessage = datasetRuleSummary.getFailureMessage();

        RuleSummaryRequest.RuleSummaryRequestBuilder requestBuilder = RuleSummaryRequest.builder()
                .ruleId(datasetRuleSummary.getRuleId())
                .status(datasetRuleSummary.getStatus());

        Option<ValidationRuleStatistics> statistics = datasetRuleSummary.getStatistics();

        if (statistics.isDefined()) {
            ValidationRuleStatistics ruleStatistics = statistics.get();
            requestBuilder.numberOfFailures(ruleStatistics.getNumberOfFailures());
            requestBuilder.numberOfErrors(ruleStatistics.getNumberOfErrors());
        }
        if (failureMessage.isDefined()) {
            requestBuilder.errorMessage(failureMessage.get());
        }

        return requestBuilder.build();
    }
}
