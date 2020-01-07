package com.lombardrisk.ignis.pipeline.display;

import com.lombardrisk.ignis.pipeline.TransformationStep;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class PipelineEdge<T, STEP extends TransformationStep<T>> {

    private final T source;
    private final T target;
    private final STEP pipelineStep;
}
