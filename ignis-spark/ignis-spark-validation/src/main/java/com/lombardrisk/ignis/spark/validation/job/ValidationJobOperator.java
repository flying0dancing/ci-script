package com.lombardrisk.ignis.spark.validation.job;

import com.lombardrisk.ignis.api.rule.SummaryStatus;
import com.lombardrisk.ignis.common.lang.ErrorMessage;
import com.lombardrisk.ignis.spark.api.validation.DatasetValidationJobRequest;
import com.lombardrisk.ignis.spark.api.validation.DatasetValidationRule;
import com.lombardrisk.ignis.spark.core.JobOperator;
import com.lombardrisk.ignis.spark.core.phoenix.PhoenixTableService;
import com.lombardrisk.ignis.spark.core.repository.RowKeyedDatasetRepository;
import com.lombardrisk.ignis.spark.core.schema.DatasetTableSchema;
import com.lombardrisk.ignis.spark.validation.DatasetRuleSummary;
import com.lombardrisk.ignis.spark.validation.ValidationRuleStatistics;
import com.lombardrisk.ignis.spark.validation.ValidationRuleSummaryService;
import com.lombardrisk.ignis.spark.validation.ValidationService;
import com.lombardrisk.ignis.spark.validation.exception.ValidationJobOperatorException;
import io.vavr.control.Either;
import io.vavr.control.Option;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class ValidationJobOperator implements JobOperator {

    private final ValidationService validationService;
    private final RowKeyedDatasetRepository rowKeyedDatasetRepository;
    private final ValidationRuleSummaryService validationRuleSummaryService;
    private final PhoenixTableService phoenixTableService;
    private final DatasetValidationJobRequest jobRequest;

    @Autowired
    public ValidationJobOperator(
            final ValidationService validationService,
            final RowKeyedDatasetRepository rowKeyedDatasetRepository,
            final ValidationRuleSummaryService validationRuleSummaryService,
            final PhoenixTableService phoenixTableService,
            final DatasetValidationJobRequest jobRequest) {
        this.validationService = validationService;
        this.rowKeyedDatasetRepository = rowKeyedDatasetRepository;
        this.validationRuleSummaryService = validationRuleSummaryService;
        this.phoenixTableService = phoenixTableService;
        this.jobRequest = jobRequest;
    }

    @Override
    public void runJob() {
        log.info("Start validation job [{}] for dataset [{}]",
                jobRequest.getServiceRequestId(), jobRequest.getDatasetId());
        log.trace("{}", jobRequest);

        Option<ErrorMessage> createTableError =
                phoenixTableService.createTableIfNotFound(DatasetTableSchema.validationResultsTableSchema());

        if (createTableError.isDefined()) {
            log.error("Failed to create validation rule results table");
            throw new ValidationJobOperatorException(createTableError.get().getMessage());
        }

        Dataset<Row> dataset = rowKeyedDatasetRepository.readDataFrame(jobRequest.getDatasetTableLookup());
        long datasetId = jobRequest.getDatasetId();

        List<DatasetRuleSummary> datasetRuleSummaries = new ArrayList<>();

        for (DatasetValidationRule rule : jobRequest.getDatasetValidationRules()) {
            DatasetRuleSummary datasetRuleSummary = executeRule(dataset, rule, datasetId);
            datasetRuleSummaries.add(datasetRuleSummary);
        }
        log.debug("Create validation rule result summaries {}", datasetRuleSummaries);

        Option<ErrorMessage> errorResult = validationRuleSummaryService.createValidationResultsSummary(
                datasetId, datasetRuleSummaries);

        if (errorResult.isDefined()) {
            throw new ValidationJobOperatorException(errorResult.get().getMessage());
        }
    }

    private DatasetRuleSummary executeRule(
            final Dataset<Row> dataset,
            final DatasetValidationRule rule,
            final long datasetId) {

        Either<ErrorMessage, ValidationRuleStatistics> tryRunRule =
                validationService.executeRule(rule, datasetId, dataset);

        if (tryRunRule.isRight()) {
            log.debug("Validation rule {} executed without exception {}", rule.getId(), tryRunRule.get());
        }

        return tryRunRule.fold(
                errorMessage -> DatasetRuleSummary.builder()
                        .ruleId(rule.getId())
                        .status(SummaryStatus.ERROR)
                        .failureMessage(errorMessage.getMessage())
                        .build(),
                statistics -> DatasetRuleSummary.builder()
                        .status(statistics.toStatus())
                        .validationRuleStatistics(statistics)
                        .ruleId(rule.getId())
                        .build());
    }
}
