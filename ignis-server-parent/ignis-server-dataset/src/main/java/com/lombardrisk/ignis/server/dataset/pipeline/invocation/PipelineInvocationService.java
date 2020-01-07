package com.lombardrisk.ignis.server.dataset.pipeline.invocation;

import com.lombardrisk.ignis.client.external.pipeline.view.PipelineInvocationView;
import com.lombardrisk.ignis.common.MapperUtils;
import com.lombardrisk.ignis.common.time.TimeSource;
import com.lombardrisk.ignis.data.common.failure.CRUDFailure;
import com.lombardrisk.ignis.server.dataset.pipeline.invocation.model.PipelineInvocation;
import com.lombardrisk.ignis.server.dataset.pipeline.invocation.model.PipelineStepInvocation;
import com.lombardrisk.ignis.server.dataset.pipeline.invocation.model.PipelineStepStatus;
import com.lombardrisk.ignis.server.dataset.pipeline.invocation.view.PipelineInvocationToPipelineInvocationView;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.control.Option;
import io.vavr.control.Validation;
import org.apache.commons.lang3.EnumUtils;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public class PipelineInvocationService {

    private final PipelineInvocationJpaRepository pipelineInvocationJpaRepository;
    private final PipelineInvocationToPipelineInvocationView pipelineInvocationToPipelineInvocationView;

    public PipelineInvocationService(
            final TimeSource timeSource,
            final PipelineInvocationJpaRepository pipelineInvocationJpaRepository) {
        this.pipelineInvocationJpaRepository = pipelineInvocationJpaRepository;
        this.pipelineInvocationToPipelineInvocationView = new PipelineInvocationToPipelineInvocationView(timeSource);
    }

    public List<PipelineInvocationView> findAllInvocations() {
        return MapperUtils.map(
                pipelineInvocationJpaRepository.findAll(), pipelineInvocationToPipelineInvocationView::apply);
    }

    public Validation<CRUDFailure, PipelineInvocation> findInvocationById(final Long pipelineInvocationId) {
        return Option.ofOptional(pipelineInvocationJpaRepository.findById(pipelineInvocationId))
                .toValid(CRUDFailure.notFoundIds("PipelineInvocation", pipelineInvocationId));
    }

    public Validation<CRUDFailure, PipelineStepInvocation> findByInvocationIdAndStepInvocationId(
            final long pipelineInvocationId,
            final long pipelineStepInvocationId) {

        return findInvocationAndStepInvocation(pipelineInvocationId, pipelineStepInvocationId).map(tuple -> tuple._2);
    }

    public List<PipelineInvocationView> findInvocationsByServiceRequestId(final Long serviceRequestId) {
        return MapperUtils.map(
                pipelineInvocationJpaRepository.findAllByServiceRequestId(serviceRequestId),
                pipelineInvocationToPipelineInvocationView::apply);
    }

    @Transactional
    public PipelineInvocation create(final PipelineInvocation pipelineInvocation) {
        return pipelineInvocationJpaRepository.save(pipelineInvocation);
    }

    @Transactional
    public Validation<CRUDFailure, PipelineInvocation> updateStepInvocationStatus(
            final Long pipelineInvocationId,
            final Long pipelineStepInvocationId,
            final String pipelineStepStatus) {

        if (!EnumUtils.isValidEnum(PipelineStepStatus.class, pipelineStepStatus)) {
            return Validation.invalid(CRUDFailure.invalidRequestParameter("PipelineStepStatus", pipelineStepStatus));
        }

        Validation<CRUDFailure, Tuple2<PipelineInvocation, PipelineStepInvocation>> invocationAndStepInvocation =
                findInvocationAndStepInvocation(pipelineInvocationId, pipelineStepInvocationId);

        if (invocationAndStepInvocation.isInvalid()) {
            return Validation.invalid(invocationAndStepInvocation.getError());
        }

        PipelineInvocation invocation = invocationAndStepInvocation.get()._1;
        PipelineStepInvocation stepInvocation = invocationAndStepInvocation.get()._2;

        stepInvocation.setStatus(PipelineStepStatus.valueOf(pipelineStepStatus));

        return Validation.valid(pipelineInvocationJpaRepository.save(invocation));
    }

    private Validation<CRUDFailure, Tuple2<PipelineInvocation, PipelineStepInvocation>> findInvocationAndStepInvocation(
            final long invocationId,
            final long stepInvocationId) {

        return findInvocationById(invocationId).flatMap(invocation -> findStepInvocation(invocation, stepInvocationId));
    }

    private Validation<CRUDFailure, Tuple2<PipelineInvocation, PipelineStepInvocation>> findStepInvocation(
            final PipelineInvocation invocation, final Long stepInvocationId) {

        Validation<CRUDFailure, PipelineStepInvocation> pipelineStepInvocation =
                Option.ofOptional(
                        invocation.getSteps().stream()
                                .filter(stepInvocation -> stepInvocation.getId().equals(stepInvocationId))
                                .findFirst())
                        .toValid(CRUDFailure.notFoundIds("PipelineStepInvocation", stepInvocationId));

        return pipelineStepInvocation.map(stepInvocation -> Tuple.of(invocation, stepInvocation));
    }
}
