package com.lombardrisk.ignis.client.design.pipeline;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor(onConstructor = @__({@JsonCreator}))
public class SyntaxCheckRequest {
    private final String sparkSql;
    private final Long outputFieldId;
    private final PipelineStepRequest pipelineStep;
}
