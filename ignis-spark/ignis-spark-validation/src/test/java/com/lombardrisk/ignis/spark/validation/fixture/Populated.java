package com.lombardrisk.ignis.spark.validation.fixture;

import com.lombardrisk.ignis.api.rule.SummaryStatus;
import com.lombardrisk.ignis.spark.api.DatasetTableLookup;
import com.lombardrisk.ignis.spark.api.validation.DatasetValidationJobRequest;
import com.lombardrisk.ignis.spark.api.validation.DatasetValidationRule;
import com.lombardrisk.ignis.spark.validation.DatasetRuleSummary;
import com.lombardrisk.ignis.spark.validation.ValidationRuleStatistics;
import com.lombardrisk.ignis.spark.validation.transform.FieldType;
import com.lombardrisk.ignis.spark.validation.transform.JexlTransformation;

public class Populated {


    public static JexlTransformation.JexlTransformationBuilder<String> jexlTransformation() {
        return JexlTransformation.<String>builder()
                .name("DECIMAL")
                .as("another_field")
                .javaParseResultFunction(Object::toString)
                .fieldType(FieldType.STRING);
    }

    public static DatasetRuleSummary.DatasetRuleSummaryBuilder datasetRuleSummary() {
        return DatasetRuleSummary.builder()
                .ruleId(1L)
                .status(SummaryStatus.SUCCESS)
                .validationRuleStatistics(validationRuleStatistics().build());
    }

    public static ValidationRuleStatistics.ValidationRuleStatisticsBuilder validationRuleStatistics() {
        return ValidationRuleStatistics.builder()
                .numberOfErrors(484)
                .numberOfFailures(918);
    }

    public static DatasetValidationJobRequest.DatasetValidationJobRequestBuilder datasetValidationJobRequest() {
        return DatasetValidationJobRequest.builder()
                .name("ValidationJobRequest")
                .datasetName("DatasetName")
                .serviceRequestId(2342)
                .datasetId(5432)
                .datasetValidationRule(datasetValidationRule().build())
                .datasetTableLookup(datasetTableLookup().build());
    }

    public static DatasetValidationRule.DatasetValidationRuleBuilder datasetValidationRule() {
        return com.lombardrisk.ignis.spark.api.fixture.Populated.datasetValidationRule();
    }

    public static DatasetTableLookup.DatasetTableLookupBuilder datasetTableLookup() {
        return DatasetTableLookup.builder()
                .datasetName("dataset")
                .predicate("true");
    }
}
