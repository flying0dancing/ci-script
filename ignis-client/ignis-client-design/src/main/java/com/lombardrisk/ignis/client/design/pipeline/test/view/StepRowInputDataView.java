package com.lombardrisk.ignis.client.design.pipeline.test.view;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
public class StepRowInputDataView {
    private final Long id;
    @JsonIgnore
    private final Long schemaId;
    private final boolean run;
    private final Map<Long, StepTestCellView> cells;
}
