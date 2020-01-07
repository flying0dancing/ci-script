package com.lombardrisk.ignis.client.design.pipeline.test.view;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class StepTestCellView {
    private final Long id;
    private final String data;
    private final Long fieldId;
    @JsonIgnore
    private final Long schemaId;
}
