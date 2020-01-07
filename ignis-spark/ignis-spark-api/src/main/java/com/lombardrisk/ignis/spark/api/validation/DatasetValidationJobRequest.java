package com.lombardrisk.ignis.spark.api.validation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.lombardrisk.ignis.spark.api.DatasetTableLookup;
import com.lombardrisk.ignis.spark.api.JobRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Singular;
import lombok.ToString;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class DatasetValidationJobRequest implements JobRequest {

    private String name;
    private String datasetName;
    private long datasetId;
    private DatasetTableLookup datasetTableLookup;
    private long serviceRequestId;

    @Singular
    private List<DatasetValidationRule> datasetValidationRules;
}
