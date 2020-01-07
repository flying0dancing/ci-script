package com.lombardrisk.ignis.server.job.staging.model;

import com.lombardrisk.ignis.server.product.pipeline.model.Pipeline;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
public class DownstreamPipelineInstruction {

    private final Pipeline pipeline;
    private final String entityCode;
    private final LocalDate referenceDate;
}
