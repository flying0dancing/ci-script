package com.lombardrisk.ignis.server.dataset.fixture;

import com.lombardrisk.ignis.api.dataset.DatasetType;
import com.lombardrisk.ignis.api.rule.SummaryStatus;
import com.lombardrisk.ignis.client.internal.CreateDatasetCall;
import com.lombardrisk.ignis.client.internal.RuleSummaryRequest;
import com.lombardrisk.ignis.server.dataset.model.Dataset;
import com.lombardrisk.ignis.server.dataset.model.DatasetOnlyBean;
import com.lombardrisk.ignis.server.dataset.model.DatasetQuery;
import com.lombardrisk.ignis.server.dataset.pipeline.invocation.model.PipelineInvocation;
import com.lombardrisk.ignis.server.dataset.pipeline.invocation.model.PipelineStepInvocation;
import com.lombardrisk.ignis.server.dataset.pipeline.invocation.model.PipelineStepInvocationDataset;
import com.lombardrisk.ignis.server.dataset.pipeline.invocation.model.PipelineStepStatus;
import com.lombardrisk.ignis.server.dataset.rule.detail.ValidationDetailRow;
import com.lombardrisk.ignis.server.dataset.rule.model.ValidationResultsSummary;
import com.lombardrisk.ignis.server.product.fixture.ProductPopulated;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

import static com.google.common.collect.Sets.newHashSet;
import static com.google.common.collect.Sets.newLinkedHashSet;
import static java.util.Collections.emptySet;
import static java.util.Collections.singletonList;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;

@UtilityClass
@Slf4j
public class DatasetPopulated {

    public static Dataset.DatasetBuilder dataset() {
        return Dataset.builder()
                .name(ProductPopulated.SCHEMA_NAME)
                .table(ProductPopulated.SCHEMA_NAME)
                .datasetType(DatasetType.STAGING_DATASET)
                .recordsCount(4L)
                .stagingJobs(newHashSet())
                .latestValidationJobId(9010L)
                .createdTime(new Date())
                .schema(ProductPopulated.table().build())
                .predicate("1 = 1")
                .rowKeySeed(234L)
                .entityCode("entityCode")
                .referenceDate(LocalDate.of(2018, 1, 1))
                .runKey(1L);
    }

    public static ValidationResultsSummary.ValidationResultsSummaryBuilder validationRuleSummary() {
        return ValidationResultsSummary.builder()
                .numberOfFailures(1000L)
                .validationRule(ProductPopulated.validationRule().build())
                .status(SummaryStatus.FAIL)
                .dataset(dataset().build());
    }

    public static PipelineInvocation.PipelineInvocationBuilder pipelineInvocation() {
        return PipelineInvocation.builder()
                .entityCode("Ent")
                .referenceDate(LocalDate.of(100, 1, 1))
                .createdTime(LocalDateTime.of(2001, 1, 1, 0, 0))
                .steps(newLinkedHashSet(singletonList(pipelineStepInvocation().build())));
    }

    public static PipelineStepInvocation.PipelineStepInvocationBuilder pipelineStepInvocation() {
        return PipelineStepInvocation.builder()
                .pipelineStepId(2L)
                .pipelineStep(ProductPopulated.mapPipelineStep().build())
                .inputDatasets(newHashSet(pipelineStepInvocationDataset().build()))
                .outputDatasetIds(emptySet())
                .status(PipelineStepStatus.SUCCESS);
    }

    public static PipelineStepInvocationDataset.PipelineStepInvocationDatasetBuilder pipelineStepInvocationDataset() {
        return PipelineStepInvocationDataset.builder()
                .datasetId(101L)
                .datasetRunKey(999L);
    }

    public static DatasetOnlyBean.DatasetOnlyBeanBuilder datasetOnly() {
        Dataset dataset = dataset().build();
        return DatasetOnlyBean.builder()
                .name(dataset.getName())
                .referenceDate(LocalDate.of(1991, 1, 1))
                .schemaDisplayName(dataset.getSchema().getDisplayName())
                .schemaId(1001L)
                .createdTime(dataset.getCreatedTime())
                .predicate(dataset.getPredicate())
                .recordsCount(dataset.getRecordsCount())
                .rulesDefined(isNotEmpty(dataset.getSchema().getValidationRules()))
                .validationJobId(dataset.getLatestValidationJobId())
                .validationStatus(dataset.getValidationStatus().name());
    }

    public static DatasetQuery.DatasetQueryBuilder datasetQuery() {
        return DatasetQuery.builder()
                .entityCode("Entity")
                .referenceDate(LocalDate.of(2000, 1, 1));
    }

    public static ValidationDetailRow.ValidationDetailRowBuilder validationDetailRow() {
        return ValidationDetailRow.builder()
                .value("field1", "hello");
    }

    public static RuleSummaryRequest.RuleSummaryRequestBuilder ruleSummaryRequest() {
        return RuleSummaryRequest.builder()
                .numberOfFailures(1000L)
                .ruleId(5400L)
                .status(SummaryStatus.SUCCESS);
    }

    public static CreateDatasetCall.CreateDatasetCallBuilder createDatasetCall() {
        return CreateDatasetCall.builder()
                .stagingJobId(0L)
                .pipelineJobId(0L)
                .schemaId(0L)
                .entityCode("Entity")
                .referenceDate(LocalDate.of(2000, 1, 1))
                .predicate("1 = 1")
                .recordsCount(1)
                .autoValidate(true);
    }
}
