package com.lombardrisk.ignis.server.product.pipeline.validation;

import com.lombardrisk.ignis.client.external.pipeline.export.PipelineExport;
import com.lombardrisk.ignis.client.external.pipeline.export.step.PipelineStepExport;
import com.lombardrisk.ignis.common.MapperUtils;
import com.lombardrisk.ignis.data.common.error.ErrorResponse;
import lombok.AllArgsConstructor;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
public class PipelineImportBeanValidator {

    private final Validator validator;

    public List<ErrorResponse> validate(final List<PipelineExport> pipelineExport) {
        return pipelineExport.stream()
                .map(this::validatePipeline)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    private List<ErrorResponse> validatePipeline(final PipelineExport pipelineExport) {
        List<ErrorResponse> pipelineErrors = MapperUtils.mapOrEmpty(
                validator.validate(pipelineExport),
                violation -> ErrorResponse.valueOf(violation.getMessage(), "PIPELINE_INVALID"));

        pipelineErrors.addAll(
                pipelineExport.getSteps()
                        .stream()
                        .flatMap(step -> validator.validate(step).stream()
                                .map(violation -> createErrorMessage(pipelineExport, step, violation)))
                        .collect(Collectors.toList()));

        return pipelineErrors;
    }

    private ErrorResponse createErrorMessage(
            final PipelineExport pipelineExport,
            final PipelineStepExport step,
            final ConstraintViolation<PipelineStepExport> violation) {
        return ErrorResponse.valueOf(
                "Pipeline \"" + pipelineExport.getName() + "\", Step \"" + step.getName() + "\" is invalid: " + violation.getMessage(),
                "PIPELINE_INVALID");
    }
}
