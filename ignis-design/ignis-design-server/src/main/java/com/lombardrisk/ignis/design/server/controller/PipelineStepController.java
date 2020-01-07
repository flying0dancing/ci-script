package com.lombardrisk.ignis.design.server.controller;

import com.lombardrisk.ignis.client.design.path.design;
import com.lombardrisk.ignis.client.design.pipeline.PipelineStepRequest;
import com.lombardrisk.ignis.client.design.pipeline.PipelineStepView;
import com.lombardrisk.ignis.client.design.pipeline.SyntaxCheckRequest;
import com.lombardrisk.ignis.client.design.pipeline.error.SelectResult;
import com.lombardrisk.ignis.client.design.pipeline.error.UpdatePipelineError;
import com.lombardrisk.ignis.data.common.Identifiable;
import com.lombardrisk.ignis.design.server.pipeline.PipelineStepService;
import com.lombardrisk.ignis.web.common.response.EitherResponse;
import com.lombardrisk.ignis.web.common.response.FcrResponse;
import io.vavr.control.Validation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@AllArgsConstructor
@Slf4j
@RestController
public class PipelineStepController {
    private final PipelineStepService pipelineStepService;

    @PostMapping(path = design.api.v1.pipelines.pipelineId.Steps)
    public EitherResponse<UpdatePipelineError, PipelineStepView> saveStep(
            @PathVariable(design.api.Params.PIPELINE_ID) final Long pipelineId,
            @Valid @RequestBody final PipelineStepRequest stepRequest) {
        log.debug("Save step [{}] for pipeline [{}]", stepRequest.getName(), pipelineId);

        Validation<UpdatePipelineError, PipelineStepView> pipelineStepViews =
                pipelineStepService.savePipelineStep(pipelineId, stepRequest);

        return pipelineStepViews
                .fold(EitherResponse::badRequest, EitherResponse::okResponse);
    }

    @PutMapping(path = design.api.v1.pipelines.pipelineId.steps.ById)
    public EitherResponse<UpdatePipelineError, PipelineStepView> saveStep(
            @PathVariable(design.api.Params.PIPELINE_ID) final Long pipelineId,
            @PathVariable(design.api.Params.STEP_ID) final Long stepId,
            @Valid @RequestBody final PipelineStepRequest stepRequest) {
        log.debug("Save step [{}] for pipeline [{}]", stepRequest.getName(), pipelineId);

        return pipelineStepService.updatePipelineStep(pipelineId, stepId, stepRequest)
                .fold(EitherResponse::badRequest, EitherResponse::okResponse);
    }

    @PostMapping(path = design.api.v1.pipelines.SyntaxCheck)
    public FcrResponse<SelectResult> checkPipelineSyntax(
            @Valid @RequestBody final SyntaxCheckRequest syntaxCheckRequest) {
        log.debug("Checking pipeline step syntax for pipeline");

        return pipelineStepService.checkSyntax(syntaxCheckRequest)
                .fold(FcrResponse::badRequest, FcrResponse::okResponse);
    }

    @DeleteMapping(path = design.api.v1.pipelines.pipelineId.steps.ById)
    public FcrResponse<Identifiable> deleteStep(@PathVariable(design.api.Params.STEP_ID) final Long id) {
        log.debug("Delete step with id [{}]", id);

        return pipelineStepService.deletePipelineStep(id)
                .fold(FcrResponse::crudFailure, FcrResponse::okResponse);
    }
}
