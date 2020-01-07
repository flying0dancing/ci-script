package com.lombardrisk.ignis.spark.api.staging;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.lombardrisk.ignis.spark.api.JobRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class StagingAppConfig implements JobRequest {

    private String name;
    private long serviceRequestId;

    private Set<StagingDatasetConfig> items;

    private Set<DownstreamPipeline> downstreamPipelines;
}
