package com.lombardrisk.ignis.design.server.pipeline.api;

import com.lombardrisk.ignis.design.server.pipeline.model.PipelineStep;
import io.vavr.control.Option;

import java.util.List;

public interface PipelineStepRepository {

    Option<PipelineStep> findById(long id);

    List<PipelineStep> findAll();

    List<PipelineStep> findAllById(Iterable<Long> ids);

    void delete(PipelineStep step);

    PipelineStep savePipelineStep(PipelineStep step);
}
