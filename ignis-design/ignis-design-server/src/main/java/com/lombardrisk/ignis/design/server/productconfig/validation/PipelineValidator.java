package com.lombardrisk.ignis.design.server.productconfig.validation;

import com.google.common.collect.ImmutableList;
import com.lombardrisk.ignis.client.design.productconfig.validation.PipelineCheck;
import com.lombardrisk.ignis.client.design.productconfig.validation.PipelineTask;
import com.lombardrisk.ignis.client.design.productconfig.validation.TaskStatus;
import com.lombardrisk.ignis.client.design.productconfig.validation.ValidationTask;
import com.lombardrisk.ignis.design.server.pipeline.PipelineStepSelectsValidator;
import com.lombardrisk.ignis.design.server.pipeline.model.Pipeline;
import com.lombardrisk.ignis.design.server.pipeline.model.PipelineStep;
import com.lombardrisk.ignis.pipeline.PipelinePlan;
import com.lombardrisk.ignis.pipeline.PipelinePlanError;
import com.lombardrisk.ignis.pipeline.PipelinePlanGenerator;
import io.vavr.control.Validation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import static com.lombardrisk.ignis.client.design.productconfig.validation.TaskStatus.FAILED;
import static com.lombardrisk.ignis.client.design.productconfig.validation.TaskStatus.RUNNING;
import static com.lombardrisk.ignis.client.design.productconfig.validation.TaskStatus.SUCCESS;
import static com.lombardrisk.ignis.client.design.productconfig.validation.TaskType.PIPELINE_GRAPH;
import static com.lombardrisk.ignis.client.design.productconfig.validation.TaskType.PIPELINE_STEP;
import static java.util.stream.Collectors.toList;

@Slf4j
@AllArgsConstructor
public class PipelineValidator {

    private static final String GRAPH_VALIDATION_NAME = "Graph validation";
    private final PipelineStepSelectsValidator validator;
    private final PipelinePlanGenerator<Long, PipelineStep> pipelinePlanGenerator;

    public Flux<? extends ValidationTask> pipelineValidationStream(final Pipeline pipeline) {
        PipelineTask initialPipelineTask = toInitialPipelineTask(pipeline);

        Flux<PipelineCheck> pipelineGraphCheck = Flux.just(validatePipelineGraph(pipeline));
        Flux<PipelineCheck> pipelineStepChecks = stepValidationStream(pipeline.getSteps());

        return pipelineGraphCheck.concatWith(pipelineStepChecks)
                .scan(initialPipelineTask, this::transitionPipelineTask);
    }

    PipelineTask toInitialPipelineTask(final Pipeline pipeline) {
        return PipelineTask.builder()
                .pipelineId(pipeline.getId())
                .name(pipeline.getName())
                .status(TaskStatus.PENDING)
                .tasks(createPipelineChecks(pipeline))
                .build();
    }

    private List<PipelineCheck> createPipelineChecks(final Pipeline pipeline) {
        List<PipelineCheck> pipelineChecks = new ArrayList<>();

        PipelineCheck pipelineGraphCheck = PipelineCheck.builder()
                .pipelineId(pipeline.getId())
                .name(GRAPH_VALIDATION_NAME)
                .status(TaskStatus.PENDING)
                .type(PIPELINE_GRAPH)
                .build();

        List<PipelineCheck> pipelineStepChecks =
                pipeline.getSteps().stream().map(this::toInitialPipelineStepTask).collect(toList());

        pipelineChecks.add(pipelineGraphCheck);
        pipelineChecks.addAll(pipelineStepChecks);

        return pipelineChecks;
    }

    private PipelineCheck toInitialPipelineStepTask(final PipelineStep pipelineStep) {
        return PipelineCheck.builder()
                .pipelineId(pipelineStep.getPipelineId())
                .pipelineStepId(pipelineStep.getId())
                .name(pipelineStep.getName())
                .status(TaskStatus.PENDING)
                .type(PIPELINE_STEP)
                .build();
    }

    private PipelineTask transitionPipelineTask(
            final PipelineTask currentPipelineTask,
            final PipelineCheck newPipelineCheck) {

        PipelineTask newPipelineTask =
                currentPipelineTask.withTasks(updatedPipelineTasks(currentPipelineTask.getTasks(), newPipelineCheck));

        boolean hasRunningTasks = newPipelineTask.getTasks().stream()
                .anyMatch(task -> task.getStatus().equals(RUNNING));

        boolean allTasksSuccessful = newPipelineTask.getTasks().stream()
                .allMatch(task -> task.getStatus().equals(SUCCESS));

        boolean hasFailedTasks = newPipelineTask.getTasks().stream()
                .anyMatch(task -> task.getStatus().equals(FAILED));

        if (allTasksSuccessful) {
            return newPipelineTask.withStatus(SUCCESS);
        }

        if (hasFailedTasks) {
            return newPipelineTask.withStatus(FAILED);
        }

        if (hasRunningTasks) {
            return newPipelineTask.withStatus(RUNNING);
        }

        return newPipelineTask;
    }

    private List<PipelineCheck> updatedPipelineTasks(
            final List<PipelineCheck> currentPipelineChecks,
            final PipelineCheck newPipelineCheck) {

        List<PipelineCheck> graphChecks = currentPipelineChecks.stream()
                .filter(check -> check.getType() == PIPELINE_GRAPH)
                .collect(toList());

        List<PipelineCheck> stepChecks = currentPipelineChecks.stream()
                .filter(check -> check.getType() == PIPELINE_STEP)
                .collect(toList());

        switch (newPipelineCheck.getType()) {
            case PIPELINE_GRAPH:
                return ImmutableList.<PipelineCheck>builder()
                        .add(newPipelineCheck)
                        .addAll(stepChecks)
                        .build();

            case PIPELINE_STEP:
                List<PipelineCheck> updatedStepChecks = stepChecks.stream()
                        .filter(check -> !check.getPipelineStepId().equals(newPipelineCheck.getPipelineStepId()))
                        .collect(toList());

                updatedStepChecks.add(newPipelineCheck);

                updatedStepChecks.sort(Comparator.comparing(PipelineCheck::getPipelineStepId));

                return ImmutableList.<PipelineCheck>builder()
                        .addAll(graphChecks)
                        .addAll(updatedStepChecks)
                        .build();

            default:
                throw new IllegalStateException("Invalid pipeline task type " + newPipelineCheck.getType());
        }
    }

    private PipelineCheck validatePipelineGraph(final Pipeline pipeline) {
        Validation<PipelinePlanError<Long, PipelineStep>, PipelinePlan<Long, PipelineStep>> pipelinePlans =
                pipelinePlanGenerator.generateExecutionPlan(pipeline);

        if (!pipelinePlans.isEmpty() && pipelinePlans.isValid()) {
            return PipelineCheck.builder()
                    .pipelineId(pipeline.getId())
                    .name(GRAPH_VALIDATION_NAME)
                    .status(SUCCESS)
                    .type(PIPELINE_GRAPH)
                    .build();
        }

        return PipelineCheck.builder()
                .pipelineId(pipeline.getId())
                .name(GRAPH_VALIDATION_NAME)
                .message("")
                .status(FAILED)
                .type(PIPELINE_GRAPH)
                .build();
    }

    private Flux<PipelineCheck> stepValidationStream(final Set<PipelineStep> pipelineSteps) {
        return Flux.fromIterable(pipelineSteps)
                .concatMap(this::singleStepStream);
    }

    private Flux<PipelineCheck> singleStepStream(final PipelineStep step) {
        Flux<PipelineCheck> runningStep = Flux.just(toRunningTask(step));
        return runningStep.concatWith(Flux.just(validateStep(step)));
    }

    private PipelineCheck validateStep(final PipelineStep step) {
        return validator.validate(step)
                .fold(errors -> toFailedTask(step), errors -> toSucceededTask(step));
    }

    private PipelineCheck toRunningTask(final PipelineStep step) {
        return PipelineCheck.builder()
                .pipelineId(step.getPipelineId())
                .pipelineStepId(step.getId())
                .name(step.getName())
                .status(RUNNING)
                .type(PIPELINE_STEP)
                .message(String.format("Validating step %s", step.getName()))
                .build();
    }

    private PipelineCheck toSucceededTask(final PipelineStep step) {
        return PipelineCheck.builder()
                .pipelineId(step.getPipelineId())
                .pipelineStepId(step.getId())
                .name(step.getName())
                .status(TaskStatus.SUCCESS)
                .type(PIPELINE_STEP)
                .message(String.format("Validated step %s", step.getName()))
                .build();
    }

    private PipelineCheck toFailedTask(final PipelineStep step) {
        return PipelineCheck.builder()
                .pipelineId(step.getPipelineId())
                .pipelineStepId(step.getId())
                .name(step.getName())
                .status(TaskStatus.FAILED)
                .type(PIPELINE_STEP)
                .message(String.format("Failed to validate step %s", step.getName()))
                .build();
    }
}
