package com.lombardrisk.ignis.spark.api.pipeline;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.lombardrisk.ignis.spark.api.JobRequest;
import com.lombardrisk.ignis.spark.api.pipeline.spark.SparkFunctionConfig;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PipelineAppConfig implements JobRequest {

    private String name;
    private long serviceRequestId;
    private Long pipelineInvocationId;
    private List<PipelineStepAppConfig> pipelineSteps;

    @NonNull
    private SparkFunctionConfig sparkFunctionConfig;
}
