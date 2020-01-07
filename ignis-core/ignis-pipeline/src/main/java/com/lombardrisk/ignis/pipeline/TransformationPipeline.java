package com.lombardrisk.ignis.pipeline;

import java.util.Set;

public interface TransformationPipeline<S extends TransformationStep> {

    Set<S> getSteps();
}
