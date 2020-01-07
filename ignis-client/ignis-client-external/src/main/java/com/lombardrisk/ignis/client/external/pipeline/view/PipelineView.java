package com.lombardrisk.ignis.client.external.pipeline.view;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class PipelineView {

    private Long id;
    private String name;
    private List<PipelineStepView> steps;
}
