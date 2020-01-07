package com.lombardrisk.ignis.functional.test.dsl;

import com.lombardrisk.ignis.client.external.dataset.model.Dataset;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@SuppressWarnings("ConstantConditions")
@Getter
@Builder
public class PipelineJobContext {

    private final long jobId;
    private final List<Dataset> pipelineDatasets;
}
