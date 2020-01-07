package com.lombardrisk.ignis.pipeline;

import java.util.Set;

public interface TransformationStep<T> {

    String getName();

    Set<T> getInputs();

    T getOutput();
}
