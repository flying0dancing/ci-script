package com.lombardrisk.ignis.design.server.pipeline.test.model;

import com.lombardrisk.ignis.client.design.pipeline.test.StepTestStatus;
import lombok.Builder;
import lombok.Data;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Builder
@Data
public class PipelineStepTestResult {
    private StepTestStatus status;
    private List<Long> inputRows;
    private List<Long> matching;
    private List<Long> notFound;
    private List<Map<String, Object>> unexpected;
    private List<Map<String, Object>> actualResults;

    public static PipelineStepTestResult getPassEmptyPipelineStepTestResult() {
        return PipelineStepTestResult.builder()
                .status(StepTestStatus.PASS)
                .inputRows(Collections.emptyList())
                .matching(Collections.emptyList())
                .notFound(Collections.emptyList())
                .unexpected(Collections.emptyList())
                .actualResults(Collections.emptyList())
                .build();
    }
}
