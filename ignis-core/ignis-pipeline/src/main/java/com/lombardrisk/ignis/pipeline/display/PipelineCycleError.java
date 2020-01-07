package com.lombardrisk.ignis.pipeline.display;

import com.lombardrisk.ignis.pipeline.TransformationStep;
import lombok.Data;

import java.util.Set;

@Data
public class PipelineCycleError<T, STEP extends TransformationStep<T>> {
    private final Set<T> parentChain;
    private final STEP cyclicalStep;
}
