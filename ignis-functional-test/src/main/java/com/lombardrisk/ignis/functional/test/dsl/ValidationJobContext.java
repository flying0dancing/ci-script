package com.lombardrisk.ignis.functional.test.dsl;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ValidationJobContext {
    private final long jobId;
    private final long datasetId;
}
